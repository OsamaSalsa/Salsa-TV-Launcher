package com.example.ui.home

import android.content.Context
import com.example.ui.common.hasHardwareRenderNode
import com.example.ui.common.YOUTUBE_TV_USER_AGENT
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

data class ResolvedYouTubeStream(
    val videoUrl: String,
    val audioUrl: String? = null,
    val isHls: Boolean = false,
    val userAgent: String,
    val sourceLabel: String = "",
    val backendKey: String = ""
)

/**
 * Zero-sign-in YouTube stream resolver with persistent winning-method memory.
 *
 * Remembers the exact playback method, WebView stage, or native stream backend/host
 * that succeeded last time and prioritizes it immediately on subsequent launches for
 * near-instant startup.
 */
object YouTubeStreamResolver {

    private const val PREFS_NAME = "salsa_screensaver_playback_prefs"
    private const val KEY_PREFERRED_MODE = "preferred_mode" // "web" or "exo"
    private const val KEY_PREFERRED_WEB_STAGE = "preferred_web_stage" // 0..3
    private const val KEY_PREFERRED_INVIDIOUS_HOST = "preferred_invidious_host"
    private const val KEY_PREFERRED_BACKEND_KEY = "preferred_backend_key" // e.g. "innertube:ANDROID_VR", "invidious:https://...", "piped:https://..."
    private const val KEY_CACHED_STREAM_VIDEO_ID = "cached_stream_video_id"
    private const val KEY_CACHED_STREAM_VIDEO_URL = "cached_stream_video_url"
    private const val KEY_CACHED_STREAM_AUDIO_URL = "cached_stream_audio_url"
    private const val KEY_CACHED_STREAM_IS_HLS = "cached_stream_is_hls"
    private const val KEY_CACHED_STREAM_UA = "cached_stream_ua"
    private const val KEY_CACHED_STREAM_SOURCE_LABEL = "cached_stream_source_label"
    private const val KEY_CACHED_STREAM_BACKEND_KEY = "cached_stream_backend_key"
    private const val KEY_CACHED_STREAM_SAVED_AT = "cached_stream_saved_at"

    private const val STREAM_CACHE_TTL_MS = 25L * 60L * 1000L // 25 minutes

    private const val INNERTUBE_PLAYER_URL =
        "https://www.youtube.com/youtubei/v1/player?prettyPrint=false"
    private const val INVIDIOUS_DIRECTORY_URL =
        "https://api.invidious.io/instances.json?sort_by=type,health,api"
    private const val PIPED_DIRECTORY_URL =
        "https://piped-instances.kavin.rocks/"

    private const val TOTAL_TIMEOUT_MS = 6_500L
    private const val INSTANCE_CACHE_TTL_MS = 15L * 60L * 1000L

    private const val BROWSER_UA =
        "Mozilla/5.0 (Linux; Android 13; Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    private const val ANDROID_VR_UA =
        "com.google.android.apps.youtube.vr.oculus/1.62.27 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip"
    private const val IOS_UA =
        "com.google.ios.youtube/20.10.4 (iPhone16,2; U; CPU iOS 18_3_2 like Mac OS X;)"

    private val defaultInvidiousInstances = listOf(
        "https://inv.nadeko.net",
        "https://invidious.nerdvpn.de",
        "https://invidious.tiekoetter.com",
        "https://yewtu.be",
        "https://inv.perditum.com"
    )

    private val defaultPipedApiInstances = listOf(
        "https://api.piped.private.coffee",
        "https://pipedapi.kavin.rocks",
        "https://pipedapi.adminforge.de",
        "https://pipedapi.leptons.xyz",
        "https://pipedapi.ducks.party"
    )

    @Volatile
    private var cachedInvidiousInstances: List<String> = emptyList()
    @Volatile
    private var invidiousFetchedAtMs: Long = 0L

    @Volatile
    private var cachedPipedInstances: List<String> = emptyList()
    @Volatile
    private var pipedFetchedAtMs: Long = 0L

    @Volatile
    private var memoryPreferredMode: String = ""
    @Volatile
    private var memoryPreferredWebStage: Int = -1
    @Volatile
    private var memoryPreferredInvidiousHost: String = ""
    @Volatile
    private var memoryPreferredBackendKey: String = ""

    private data class CachedStreamEntry(
        val stream: ResolvedYouTubeStream,
        val savedAtMs: Long
    )

    private val memoryStreamCache = ConcurrentHashMap<String, CachedStreamEntry>()

    private fun prefs(context: Context?) =
        context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPreferredMode(context: Context?): String {
        if (memoryPreferredMode.isNotEmpty()) return memoryPreferredMode
        val saved = prefs(context)?.getString(KEY_PREFERRED_MODE, "") ?: ""
        if (saved.isNotEmpty()) memoryPreferredMode = saved
        return saved
    }

    fun getPreferredWebStage(context: Context?): Int {
        if (memoryPreferredWebStage in 0..3) return memoryPreferredWebStage
        val saved = prefs(context)?.getInt(KEY_PREFERRED_WEB_STAGE, 0)?.coerceIn(0, 3) ?: 0
        memoryPreferredWebStage = saved
        return saved
    }

    fun getPreferredInvidiousHost(context: Context?): String? {
        if (memoryPreferredInvidiousHost.startsWith("https://")) return memoryPreferredInvidiousHost
        val saved = prefs(context)?.getString(KEY_PREFERRED_INVIDIOUS_HOST, "")?.trim() ?: ""
        if (saved.startsWith("https://")) {
            memoryPreferredInvidiousHost = saved
            return saved
        }
        return null
    }

    fun getPreferredBackendKey(context: Context?): String {
        if (memoryPreferredBackendKey.isNotEmpty()) return memoryPreferredBackendKey
        val saved = prefs(context)?.getString(KEY_PREFERRED_BACKEND_KEY, "")?.trim() ?: ""
        if (saved.isNotEmpty()) memoryPreferredBackendKey = saved
        return saved
    }

    /**
     * Returns the ordered list of WebView stages to try, starting with the stage that
     * worked successfully last time so the screensaver starts without waiting for timeouts.
     */
    fun getOrderedWebStages(context: Context?): List<Int> {
        val preferred = getPreferredWebStage(context).coerceIn(0, 3)
        return (listOf(preferred) + listOf(0, 1, 2, 3)).distinct()
    }

    /**
     * Records that WebView playback succeeded on [stage] (and optional [invidiousHost] for stage 3).
     */
    fun recordSuccessfulWebStage(
        context: Context?,
        stage: Int,
        invidiousHost: String? = null
    ) {
        val validStage = stage.coerceIn(0, 3)
        memoryPreferredMode = "web"
        memoryPreferredWebStage = validStage
        if (validStage == 3 && !invidiousHost.isNullOrBlank() && invidiousHost.startsWith("https://")) {
            memoryPreferredInvidiousHost = invidiousHost.trim().trimEnd('/')
        }
        prefs(context)?.edit()?.apply {
            putString(KEY_PREFERRED_MODE, "web")
            putInt(KEY_PREFERRED_WEB_STAGE, validStage)
            if (validStage == 3 && !invidiousHost.isNullOrBlank() && invidiousHost.startsWith("https://")) {
                putString(KEY_PREFERRED_INVIDIOUS_HOST, invidiousHost.trim().trimEnd('/'))
            }
            apply()
        }
    }

    /**
     * Records that ExoPlayer direct stream playback actively started playing [stream] for [videoId].
     */
    fun recordSuccessfulExoStream(
        context: Context?,
        videoId: String,
        stream: ResolvedYouTubeStream
    ) {
        val safeId = videoId.replace(Regex("[^A-Za-z0-9_-]"), "")
        val now = System.currentTimeMillis()
        memoryPreferredMode = "exo"
        if (stream.backendKey.isNotEmpty()) {
            memoryPreferredBackendKey = stream.backendKey
        }
        if (stream.backendKey.startsWith("invidious:")) {
            val host = stream.backendKey.removePrefix("invidious:").trim()
            if (host.startsWith("https://")) {
                memoryPreferredInvidiousHost = host
            }
        }
        if (safeId.isNotEmpty()) {
            memoryStreamCache[safeId] = CachedStreamEntry(stream = stream, savedAtMs = now)
        }
        prefs(context)?.edit()?.apply {
            putString(KEY_PREFERRED_MODE, "exo")
            if (stream.backendKey.isNotEmpty()) {
                putString(KEY_PREFERRED_BACKEND_KEY, stream.backendKey)
            }
            if (stream.backendKey.startsWith("invidious:")) {
                val host = stream.backendKey.removePrefix("invidious:").trim()
                if (host.startsWith("https://")) {
                    putString(KEY_PREFERRED_INVIDIOUS_HOST, host)
                }
            }
            if (safeId.isNotEmpty()) {
                putString(KEY_CACHED_STREAM_VIDEO_ID, safeId)
                putString(KEY_CACHED_STREAM_VIDEO_URL, stream.videoUrl)
                putString(KEY_CACHED_STREAM_AUDIO_URL, stream.audioUrl ?: "")
                putBoolean(KEY_CACHED_STREAM_IS_HLS, stream.isHls)
                putString(KEY_CACHED_STREAM_UA, stream.userAgent)
                putString(KEY_CACHED_STREAM_SOURCE_LABEL, stream.sourceLabel)
                putString(KEY_CACHED_STREAM_BACKEND_KEY, stream.backendKey)
                putLong(KEY_CACHED_STREAM_SAVED_AT, now)
            }
            apply()
        }
    }

    /**
     * Retrieves a recently verified working stream for [videoId] (within TTL and not excluded)
     * so ExoPlayer can start immediately on the next screensaver launch.
     */
    fun getFastCachedStream(
        context: Context?,
        videoId: String,
        excludedUrls: Set<String> = emptySet()
    ): ResolvedYouTubeStream? {
        val safeId = videoId.replace(Regex("[^A-Za-z0-9_-]"), "")
        if (safeId.isEmpty()) return null
        val now = System.currentTimeMillis()

        memoryStreamCache[safeId]?.let { entry ->
            if (now - entry.savedAtMs <= STREAM_CACHE_TTL_MS && entry.stream.videoUrl !in excludedUrls) {
                return entry.stream
            } else if (now - entry.savedAtMs > STREAM_CACHE_TTL_MS) {
                memoryStreamCache.remove(safeId)
            }
        }

        val sp = prefs(context) ?: return null
        val cachedId = sp.getString(KEY_CACHED_STREAM_VIDEO_ID, "") ?: ""
        val savedAt = sp.getLong(KEY_CACHED_STREAM_SAVED_AT, 0L)
        val videoUrl = sp.getString(KEY_CACHED_STREAM_VIDEO_URL, "") ?: ""
        if (cachedId == safeId &&
            videoUrl.startsWith("http") &&
            videoUrl !in excludedUrls &&
            now - savedAt in 0..STREAM_CACHE_TTL_MS
        ) {
            val audioUrl = sp.getString(KEY_CACHED_STREAM_AUDIO_URL, "")?.takeIf { it.isNotBlank() }
            val stream = ResolvedYouTubeStream(
                videoUrl = videoUrl,
                audioUrl = audioUrl,
                isHls = sp.getBoolean(KEY_CACHED_STREAM_IS_HLS, false),
                userAgent = sp.getString(KEY_CACHED_STREAM_UA, BROWSER_UA) ?: BROWSER_UA,
                sourceLabel = sp.getString(KEY_CACHED_STREAM_SOURCE_LABEL, "") ?: "",
                backendKey = sp.getString(KEY_CACHED_STREAM_BACKEND_KEY, "") ?: ""
            )
            memoryStreamCache[safeId] = CachedStreamEntry(stream = stream, savedAtMs = savedAt)
            return stream
        }
        return null
    }

    /**
     * Clears a cached stream URL if it expired or errored during playback so a fresh stream is resolved.
     */
    fun invalidateCachedExoStream(
        context: Context?,
        videoId: String,
        failedVideoUrl: String
    ) {
        val safeId = videoId.replace(Regex("[^A-Za-z0-9_-]"), "")
        memoryStreamCache[safeId]?.let { entry ->
            if (entry.stream.videoUrl == failedVideoUrl) {
                memoryStreamCache.remove(safeId)
            }
        }
        val sp = prefs(context) ?: return
        val cachedUrl = sp.getString(KEY_CACHED_STREAM_VIDEO_URL, "") ?: ""
        if (cachedUrl == failedVideoUrl) {
            sp.edit()
                .remove(KEY_CACHED_STREAM_VIDEO_ID)
                .remove(KEY_CACHED_STREAM_VIDEO_URL)
                .remove(KEY_CACHED_STREAM_AUDIO_URL)
                .remove(KEY_CACHED_STREAM_SAVED_AT)
                .apply()
        }
    }

    private data class ClientProfile(
        val name: String,
        val headerId: String,
        val version: String,
        val userAgent: String,
        val sendWebOriginHeaders: Boolean,
        val bodyBuilder: (videoId: String) -> JSONObject
    )

    private val clientProfiles = listOf(
        ClientProfile(
            name = "WEB_EMBEDDED_PLAYER",
            headerId = "56",
            version = "1.20250218.01.00",
            userAgent = BROWSER_UA,
            sendWebOriginHeaders = true,
            bodyBuilder = { videoId ->
                JSONObject().apply {
                    put("videoId", videoId)
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                    put(
                        "context",
                        JSONObject().apply {
                            put(
                                "client",
                                JSONObject().apply {
                                    put("clientName", "WEB_EMBEDDED_PLAYER")
                                    put("clientVersion", "1.20250218.01.00")
                                    put("hl", "en")
                                    put("gl", "US")
                                }
                            )
                            put(
                                "thirdParty",
                                JSONObject().put("embedUrl", "https://www.youtube.com/")
                            )
                        }
                    )
                }
            }
        ),
        ClientProfile(
            name = "ANDROID_VR",
            headerId = "28",
            version = "1.62.27",
            userAgent = ANDROID_VR_UA,
            sendWebOriginHeaders = false,
            bodyBuilder = { videoId ->
                JSONObject().apply {
                    put("videoId", videoId)
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                    put(
                        "context",
                        JSONObject().put(
                            "client",
                            JSONObject().apply {
                                put("clientName", "ANDROID_VR")
                                put("clientVersion", "1.62.27")
                                put("deviceMake", "Oculus")
                                put("deviceModel", "Quest 3")
                                put("androidSdkVersion", 32)
                                put("osName", "Android")
                                put("osVersion", "12L")
                                put("hl", "en")
                                put("gl", "US")
                            }
                        )
                    )
                }
            }
        ),
        ClientProfile(
            name = "IOS",
            headerId = "5",
            version = "20.10.4",
            userAgent = IOS_UA,
            sendWebOriginHeaders = false,
            bodyBuilder = { videoId ->
                JSONObject().apply {
                    put("videoId", videoId)
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                    put(
                        "context",
                        JSONObject().put(
                            "client",
                            JSONObject().apply {
                                put("clientName", "IOS")
                                put("clientVersion", "20.10.4")
                                put("deviceMake", "Apple")
                                put("deviceModel", "iPhone16,2")
                                put("osName", "iPhone")
                                put("osVersion", "18.3.2.22D82")
                                put("hl", "en")
                                put("gl", "US")
                            }
                        )
                    )
                }
            }
        ),
        ClientProfile(
            name = "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
            headerId = "85",
            version = "2.0",
            userAgent = YOUTUBE_TV_USER_AGENT,
            sendWebOriginHeaders = true,
            bodyBuilder = { videoId ->
                JSONObject().apply {
                    put("videoId", videoId)
                    put("contentCheckOk", true)
                    put("racyCheckOk", true)
                    put(
                        "context",
                        JSONObject().apply {
                            put(
                                "client",
                                JSONObject().apply {
                                    put("clientName", "TVHTML5_SIMPLY_EMBEDDED_PLAYER")
                                    put("clientVersion", "2.0")
                                    put("hl", "en")
                                    put("gl", "US")
                                }
                            )
                            put(
                                "thirdParty",
                                JSONObject().put("embedUrl", "https://www.youtube.com/")
                            )
                        }
                    )
                }
            }
        )
    )

    fun getCachedInvidiousEmbedHosts(context: Context? = null): List<String> {
        val preferred = getPreferredInvidiousHost(context)
        val baseList = if (preferred != null) listOf(preferred) else emptyList()
        return (baseList + cachedInvidiousInstances + defaultInvidiousInstances).distinct().take(6)
    }

    suspend fun getInvidiousEmbedHosts(context: Context? = null): List<String> = withContext(Dispatchers.IO) {
        val preferred = getPreferredInvidiousHost(context)
        val baseList = if (preferred != null) listOf(preferred) else emptyList()
        val dynamic = fetchDynamicInvidiousInstances()
        (baseList + dynamic + defaultInvidiousInstances).distinct().take(6)
    }

    suspend fun resolve(
        videoId: String,
        excludedVideoUrls: Set<String> = emptySet(),
        context: Context? = null
    ): ResolvedYouTubeStream? = withContext(Dispatchers.IO) {
        val safeId = videoId.replace(Regex("[^A-Za-z0-9_-]"), "")
        if (safeId.length !in 8..15) return@withContext null

        // 0. Fast path: if we have a cached working stream for this videoId, verify it first in ~200ms
        getFastCachedStream(context, safeId, excludedVideoUrls)?.let { cached ->
            val verifiedCached = verifyResolvedStream(cached)
            if (verifiedCached != null) {
                return@withContext verifiedCached
            } else {
                invalidateCachedExoStream(context, safeId, cached.videoUrl)
            }
        }

        val preferredBackend = getPreferredBackendKey(context)

        // 0b. Fast head-start for the exact backend/host that succeeded last time
        if (preferredBackend.isNotEmpty()) {
            val fastWinner = withTimeoutOrNull(1_800L) {
                when {
                    preferredBackend.startsWith("innertube:") -> {
                        val profileName = preferredBackend.removePrefix("innertube:")
                        val profile = clientProfiles.firstOrNull { it.name == profileName }
                        if (profile != null) tryResolveInnerTube(safeId, profile, excludedVideoUrls) else null
                    }
                    preferredBackend.startsWith("invidious:") -> {
                        val host = preferredBackend.removePrefix("invidious:")
                        if (host.startsWith("https://")) tryResolveInvidious(safeId, host, excludedVideoUrls) else null
                    }
                    preferredBackend.startsWith("piped:") -> {
                        val host = preferredBackend.removePrefix("piped:")
                        if (host.startsWith("https://")) tryResolvePiped(safeId, host, excludedVideoUrls) else null
                    }
                    else -> null
                }
            }
            if (fastWinner != null) {
                return@withContext fastWinner
            }
        }

        withTimeoutOrNull(TOTAL_TIMEOUT_MS) {
            coroutineScope {
                val winner = CompletableDeferred<ResolvedYouTubeStream?>()
                val dynamicInvidious = cachedInvidiousInstances
                val dynamicPiped = cachedPipedInstances
                val preferredInvHost = getPreferredInvidiousHost(context)

                val invidiousHosts = (
                    listOfNotNull(preferredInvHost) +
                        dynamicInvidious +
                        defaultInvidiousInstances
                    ).distinct().take(6)
                val pipedHosts = (dynamicPiped + defaultPipedApiInstances).distinct().take(6)

                val orderedProfiles = if (preferredBackend.startsWith("innertube:")) {
                    val prefName = preferredBackend.removePrefix("innertube:")
                    clientProfiles.sortedByDescending { it.name == prefName }
                } else {
                    clientProfiles
                }

                val tasks = mutableListOf<suspend () -> ResolvedYouTubeStream?>()

                // 1. Direct InnerTube (ordered with last winning profile first)
                orderedProfiles.forEach { profile ->
                    tasks.add {
                        tryResolveInnerTube(safeId, profile, excludedVideoUrls)
                    }
                }

                // 2. Invidious proxied APIs (?local=true) — bypasses cloud/VPN bot gates
                invidiousHosts.forEach { host ->
                    tasks.add {
                        tryResolveInvidious(safeId, host, excludedVideoUrls)
                    }
                }

                // 3. Piped proxied APIs (/streams/<id>) — bypasses cloud/VPN bot gates
                pipedHosts.forEach { apiBase ->
                    tasks.add {
                        tryResolvePiped(safeId, apiBase, excludedVideoUrls)
                    }
                }

                // 4. Refresh live directories in background & try any newly discovered healthy hosts
                tasks.add {
                    val freshInv = fetchDynamicInvidiousInstances().filterNot { it in invidiousHosts }.take(3)
                    for (host in freshInv) {
                        val found = tryResolveInvidious(safeId, host, excludedVideoUrls)
                        if (found != null) return@add found
                    }
                    null
                }
                tasks.add {
                    val freshPiped = fetchDynamicPipedInstances().filterNot { it in pipedHosts }.take(3)
                    for (apiBase in freshPiped) {
                        val found = tryResolvePiped(safeId, apiBase, excludedVideoUrls)
                        if (found != null) return@add found
                    }
                    null
                }

                val remaining = AtomicInteger(tasks.size)
                val jobs = tasks.map { block ->
                    launch(Dispatchers.IO + SupervisorJob()) {
                        val result = try {
                            block()
                        } catch (_: Exception) {
                            null
                        }
                        if (result != null) {
                            winner.complete(result)
                        } else if (remaining.decrementAndGet() == 0) {
                            winner.complete(null)
                        }
                    }
                }

                try {
                    winner.await()
                } finally {
                    jobs.forEach { it.cancel() }
                }
            }
        }
    }

    private fun fetchDynamicInvidiousInstances(): List<String> {
        val now = System.currentTimeMillis()
        if (cachedInvidiousInstances.isNotEmpty() && now - invidiousFetchedAtMs < INSTANCE_CACHE_TTL_MS) {
            return cachedInvidiousInstances
        }
        val text = httpGetText(INVIDIOUS_DIRECTORY_URL, timeoutMs = 2_000) ?: return cachedInvidiousInstances
        return try {
            val arr = JSONArray(text)
            val result = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val tuple = arr.optJSONArray(i) ?: continue
                val info = tuple.optJSONObject(1) ?: continue
                val type = info.optString("type", "")
                val apiEnabled = info.optBoolean("api", false)
                val monitor = info.optJSONObject("monitor")
                val isUp = monitor == null || monitor.optBoolean("up", true)
                val uri = info.optString("uri", "").trim().trimEnd('/')
                if (type.equals("https", ignoreCase = true) && apiEnabled && isUp && uri.startsWith("https://")) {
                    result.add(uri)
                }
                if (result.size >= 8) break
            }
            if (result.isNotEmpty()) {
                cachedInvidiousInstances = result
                invidiousFetchedAtMs = now
            }
            result
        } catch (_: Exception) {
            cachedInvidiousInstances
        }
    }

    private fun fetchDynamicPipedInstances(): List<String> {
        val now = System.currentTimeMillis()
        if (cachedPipedInstances.isNotEmpty() && now - pipedFetchedAtMs < INSTANCE_CACHE_TTL_MS) {
            return cachedPipedInstances
        }
        val text = httpGetText(PIPED_DIRECTORY_URL, timeoutMs = 2_000) ?: return cachedPipedInstances
        return try {
            val arr = JSONArray(text)
            val result = mutableListOf<String>()
            for (i in 0 until arr.length()) {
                val item = arr.optJSONObject(i) ?: continue
                val apiUrl = item.optString("api_url", "").trim().trimEnd('/')
                val upToDate = item.optBoolean("up_to_date", true)
                val uptime24h = item.optDouble("uptime_24h", 100.0)
                if (apiUrl.startsWith("https://") && upToDate && uptime24h >= 50.0) {
                    result.add(apiUrl)
                }
                if (result.size >= 8) break
            }
            if (result.isNotEmpty()) {
                cachedPipedInstances = result
                pipedFetchedAtMs = now
            }
            result
        } catch (_: Exception) {
            cachedPipedInstances
        }
    }

    private fun tryResolveInvidious(
        videoId: String,
        baseUrl: String,
        excludedUrls: Set<String>
    ): ResolvedYouTubeStream? {
        val cleanBase = baseUrl.trimEnd('/')
        val jsonText = httpGetText(
            url = "$cleanBase/api/v1/videos/$videoId?local=true",
            timeoutMs = 2_600
        )
        if (!jsonText.isNullOrBlank()) {
            try {
                val root = JSONObject(jsonText)
                val candidates = mutableListOf<ResolvedYouTubeStream>()
                val maxHeight = if (hasHardwareRenderNode) 1080 else 720

                val hlsUrl = normalizeInvidiousStreamUrl(root.optString("hlsUrl", ""), cleanBase)
                if (hlsUrl != null) {
                    candidates.add(
                        ResolvedYouTubeStream(
                            videoUrl = hlsUrl,
                            audioUrl = null,
                            isHls = true,
                            userAgent = BROWSER_UA,
                            sourceLabel = "invidious-hls",
                            backendKey = "invidious:$cleanBase"
                        )
                    )
                }

                // Prefer muxed formatStreams first on proxy servers for instant single-connection start
                val formatStreams = root.optJSONArray("formatStreams")
                if (formatStreams != null && formatStreams.length() > 0) {
                    pickBestInvidiousProgressive(formatStreams, cleanBase, maxHeight)?.let { url ->
                        candidates.add(
                            ResolvedYouTubeStream(
                                videoUrl = url,
                                audioUrl = null,
                                isHls = false,
                                userAgent = BROWSER_UA,
                                sourceLabel = "invidious-muxed",
                                backendKey = "invidious:$cleanBase"
                            )
                        )
                    }
                }

                // Adaptive HD H.264 + AAC pair
                val adaptive = root.optJSONArray("adaptiveFormats")
                if (adaptive != null && adaptive.length() > 0) {
                    val vUrl = pickBestInvidiousAdaptiveVideo(adaptive, cleanBase, maxHeight)
                    val aUrl = pickBestInvidiousAdaptiveAudio(adaptive, cleanBase)
                    if (vUrl != null && aUrl != null) {
                        candidates.add(
                            ResolvedYouTubeStream(
                                videoUrl = vUrl,
                                audioUrl = aUrl,
                                isHls = false,
                                userAgent = BROWSER_UA,
                                sourceLabel = "invidious-adaptive",
                                backendKey = "invidious:$cleanBase"
                            )
                        )
                    }
                }

                for (candidate in candidates) {
                    if (candidate.videoUrl in excludedUrls) continue
                    val verified = verifyResolvedStream(candidate)
                    if (verified != null) return verified
                }
            } catch (_: Exception) {}
        }

        // Direct proxied latest_version endpoint (works even if JSON API is rate-limited)
        val directProxyUrl = "$cleanBase/latest_version?id=$videoId&itag=18&local=true"
        if (directProxyUrl !in excludedUrls) {
            val fallback = ResolvedYouTubeStream(
                videoUrl = directProxyUrl,
                audioUrl = null,
                isHls = false,
                userAgent = BROWSER_UA,
                sourceLabel = "invidious-direct",
                backendKey = "invidious:$cleanBase"
            )
            val verified = verifyResolvedStream(fallback)
            if (verified != null && verified.videoUrl !in excludedUrls) return verified
        }
        return null
    }

    private fun tryResolvePiped(
        videoId: String,
        apiBaseUrl: String,
        excludedUrls: Set<String>
    ): ResolvedYouTubeStream? {
        val cleanBase = apiBaseUrl.trimEnd('/')
        val jsonText = httpGetText(
            url = "$cleanBase/streams/$videoId",
            timeoutMs = 2_600
        ) ?: return null

        return try {
            val root = JSONObject(jsonText)
            val candidates = mutableListOf<ResolvedYouTubeStream>()
            val maxHeight = if (hasHardwareRenderNode) 1080 else 720

            val hls = root.optString("hls", "").trim()
            if (hls.startsWith("http://") || hls.startsWith("https://")) {
                candidates.add(
                    ResolvedYouTubeStream(
                        videoUrl = hls,
                        audioUrl = null,
                        isHls = true,
                        userAgent = BROWSER_UA,
                        sourceLabel = "piped-hls",
                        backendKey = "piped:$cleanBase"
                    )
                )
            }

            val videoStreams = root.optJSONArray("videoStreams")
            val audioStreams = root.optJSONArray("audioStreams")

            if (videoStreams != null && videoStreams.length() > 0) {
                // 1. Try progressive (videoOnly == false) MP4 stream first
                var bestMuxedUrl: String? = null
                var bestMuxedHeight = -1
                // 2. Also collect best adaptive MP4 H.264 stream
                var bestAdaptiveUrl: String? = null
                var bestAdaptiveScore = -1

                for (i in 0 until videoStreams.length()) {
                    val item = videoStreams.optJSONObject(i) ?: continue
                    val url = item.optString("url", "").trim()
                    if (!url.startsWith("http")) continue
                    val mime = item.optString("mimeType", "").lowercase()
                    val format = item.optString("format", "").lowercase()
                    val codec = item.optString("codec", "").lowercase()
                    val videoOnly = item.optBoolean("videoOnly", true)
                    val height = item.optInt("height", parseHeight(item.optString("quality", "")))
                    val fps = item.optInt("fps", 30)

                    val isMp4 = mime.contains("mp4") || format.contains("mpeg_4") || format.contains("mp4")
                    if (!isMp4) continue

                    if (!videoOnly && height in 1..maxHeight && height > bestMuxedHeight) {
                        bestMuxedHeight = height
                        bestMuxedUrl = url
                    }
                    if (videoOnly && (codec.isEmpty() || codec.contains("avc1")) && height in 1..maxHeight) {
                        val score = (height * 10) - if (fps > 35) 350 else 0
                        if (score > bestAdaptiveScore) {
                            bestAdaptiveScore = score
                            bestAdaptiveUrl = url
                        }
                    }
                }

                if (bestAdaptiveUrl != null && audioStreams != null && audioStreams.length() > 0) {
                    val bestAudioUrl = pickBestPipedAudio(audioStreams)
                    if (bestAudioUrl != null) {
                        candidates.add(
                            ResolvedYouTubeStream(
                                videoUrl = bestAdaptiveUrl,
                                audioUrl = bestAudioUrl,
                                isHls = false,
                                userAgent = BROWSER_UA,
                                sourceLabel = "piped-adaptive",
                                backendKey = "piped:$cleanBase"
                            )
                        )
                    }
                }

                if (bestMuxedUrl != null) {
                    candidates.add(
                        ResolvedYouTubeStream(
                            videoUrl = bestMuxedUrl,
                            audioUrl = null,
                            isHls = false,
                            userAgent = BROWSER_UA,
                            sourceLabel = "piped-muxed",
                            backendKey = "piped:$cleanBase"
                        )
                    )
                }
            }

            for (candidate in candidates) {
                if (candidate.videoUrl in excludedUrls) continue
                val verified = verifyResolvedStream(candidate)
                if (verified != null) return verified
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun tryResolveInnerTube(
        videoId: String,
        profile: ClientProfile,
        excludedUrls: Set<String>
    ): ResolvedYouTubeStream? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(INNERTUBE_PLAYER_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 2_000
                readTimeout = 2_200
                doOutput = true
                instanceFollowRedirects = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", profile.userAgent)
                setRequestProperty("X-YouTube-Client-Name", profile.headerId)
                setRequestProperty("X-YouTube-Client-Version", profile.version)
                if (profile.sendWebOriginHeaders) {
                    setRequestProperty("Origin", "https://www.youtube.com")
                    setRequestProperty("Referer", "https://www.youtube.com/")
                }
            }

            OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(profile.bodyBuilder(videoId).toString())
                writer.flush()
            }

            if (conn.responseCode !in 200..299) return null

            val responseText = BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use {
                it.readText()
            }
            if (responseText.isBlank()) return null

            val root = JSONObject(responseText)
            val streamingData = root.optJSONObject("streamingData") ?: return null
            val candidates = extractInnerTubeCandidates(streamingData, profile.userAgent, profile.name)
            for (candidate in candidates) {
                if (candidate.videoUrl in excludedUrls) continue
                val verified = verifyResolvedStream(candidate)
                if (verified != null) return verified
            }
            null
        } catch (_: Exception) {
            null
        } finally {
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    /**
     * Verifies that the stream URL actually responds with 200/206 and playable media bytes
     * (rejecting HTTP 403 bot blocks and HTML error pages before handing to ExoPlayer).
     */
    private fun verifyResolvedStream(candidate: ResolvedYouTubeStream): ResolvedYouTubeStream? {
        val verifiedVideoUrl = probeStreamUrl(
            url = candidate.videoUrl,
            userAgent = candidate.userAgent,
            isHls = candidate.isHls
        ) ?: return null

        val verifiedAudioUrl = if (!candidate.audioUrl.isNullOrBlank()) {
            probeStreamUrl(
                url = candidate.audioUrl,
                userAgent = candidate.userAgent,
                isHls = false
            ) ?: return null
        } else {
            null
        }

        return candidate.copy(
            videoUrl = verifiedVideoUrl,
            audioUrl = verifiedAudioUrl
        )
    }

    private fun probeStreamUrl(url: String, userAgent: String, isHls: Boolean): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 1_800
                readTimeout = 1_800
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", userAgent)
                if (!isHls) {
                    setRequestProperty("Range", "bytes=0-1023")
                }
            }
            val code = conn.responseCode
            if (code !in listOf(200, 206)) return null

            val contentType = (conn.contentType ?: "").lowercase()
            if (contentType.contains("text/html") || contentType.contains("application/json")) {
                return null
            }

            if (isHls) {
                val headBytes = ByteArray(128)
                val read = conn.inputStream.use { it.read(headBytes) }
                if (read <= 0) return null
                val preview = String(headBytes, 0, read, Charsets.UTF_8)
                if (!preview.contains("#EXTM3U")) return null
            } else {
                val isMediaMime = contentType.isEmpty() ||
                    contentType.startsWith("video/") ||
                    contentType.startsWith("audio/") ||
                    contentType.contains("octet-stream") ||
                    contentType.contains("mp4") ||
                    contentType.contains("webm")
                if (!isMediaMime) return null
            }

            conn.url?.toString()?.takeIf { it.startsWith("http") } ?: url
        } catch (_: Exception) {
            null
        } finally {
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    private fun httpGetText(url: String, timeoutMs: Int): String? {
        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", BROWSER_UA)
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode !in 200..299) return null
            BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use {
                it.readText()
            }
        } catch (_: Exception) {
            null
        } finally {
            try { conn?.disconnect() } catch (_: Exception) {}
        }
    }

    private fun normalizeInvidiousStreamUrl(rawUrl: String, baseUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty()) return null
        val resolved = when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("/") -> "${baseUrl.trimEnd('/')}$trimmed"
            else -> "${baseUrl.trimEnd('/')}/$trimmed"
        }
        val baseHost = try { URL(baseUrl).host } catch (_: Exception) { "" }
        val urlHost = try { URL(resolved).host } catch (_: Exception) { "" }
        return if (baseHost.isNotEmpty() && baseHost.equals(urlHost, ignoreCase = true) && !resolved.contains("local=true")) {
            if (resolved.contains("?")) "$resolved&local=true" else "$resolved?local=true"
        } else {
            resolved
        }
    }

    private fun pickBestInvidiousProgressive(
        formats: JSONArray,
        baseUrl: String,
        maxHeight: Int
    ): String? {
        var bestUrl: String? = null
        var bestHeight = -1
        for (i in 0 until formats.length()) {
            val item = formats.optJSONObject(i) ?: continue
            val url = normalizeInvidiousStreamUrl(item.optString("url", ""), baseUrl) ?: continue
            val type = item.optString("type", "").lowercase()
            if (type.isNotEmpty() && !type.contains("video/mp4")) continue
            val height = parseHeight(item.optString("resolution", item.optString("qualityLabel", "")))
                .takeIf { it > 0 } ?: 360
            if (height <= maxHeight && height > bestHeight) {
                bestHeight = height
                bestUrl = url
            }
        }
        return bestUrl
    }

    private fun pickBestInvidiousAdaptiveVideo(
        formats: JSONArray,
        baseUrl: String,
        maxHeight: Int
    ): String? {
        var bestUrl: String? = null
        var bestScore = -1
        for (i in 0 until formats.length()) {
            val item = formats.optJSONObject(i) ?: continue
            val url = normalizeInvidiousStreamUrl(item.optString("url", ""), baseUrl) ?: continue
            val type = item.optString("type", "").lowercase()
            if (!type.contains("video/mp4") || !type.contains("avc1")) continue
            val height = parseHeight(item.optString("qualityLabel", item.optString("resolution", "")))
            val fps = item.optInt("fps", 30)
            if (height !in 1..maxHeight) continue
            val score = (height * 10) - if (fps > 35) 350 else 0
            if (score > bestScore) {
                bestScore = score
                bestUrl = url
            }
        }
        return bestUrl
    }

    private fun pickBestInvidiousAdaptiveAudio(formats: JSONArray, baseUrl: String): String? {
        var bestUrl: String? = null
        var bestBitrate = -1
        for (i in 0 until formats.length()) {
            val item = formats.optJSONObject(i) ?: continue
            val url = normalizeInvidiousStreamUrl(item.optString("url", ""), baseUrl) ?: continue
            val type = item.optString("type", "").lowercase()
            if (!type.contains("audio/mp4")) continue
            val bitrate = item.optString("bitrate", "0").toIntOrNull() ?: item.optInt("bitrate", 0)
            if (bitrate > bestBitrate) {
                bestBitrate = bitrate
                bestUrl = url
            }
        }
        return bestUrl
    }

    private fun pickBestPipedAudio(audioStreams: JSONArray): String? {
        var bestUrl: String? = null
        var bestBitrate = -1
        for (i in 0 until audioStreams.length()) {
            val item = audioStreams.optJSONObject(i) ?: continue
            val url = item.optString("url", "").trim()
            if (!url.startsWith("http")) continue
            val mime = item.optString("mimeType", "").lowercase()
            val format = item.optString("format", "").lowercase()
            if (!mime.contains("mp4") && !format.contains("m4a")) continue
            val bitrate = item.optInt("bitrate", 0)
            if (bitrate > bestBitrate) {
                bestBitrate = bitrate
                bestUrl = url
            }
        }
        return bestUrl
    }

    private fun extractInnerTubeCandidates(
        streamingData: JSONObject,
        userAgent: String,
        clientName: String
    ): List<ResolvedYouTubeStream> {
        val list = mutableListOf<ResolvedYouTubeStream>()
        val backendKey = "innertube:$clientName"
        val hlsManifestUrl = streamingData.optString("hlsManifestUrl", "").trim()
        if (hlsManifestUrl.startsWith("http://") || hlsManifestUrl.startsWith("https://")) {
            list.add(
                ResolvedYouTubeStream(
                    videoUrl = hlsManifestUrl,
                    audioUrl = null,
                    isHls = true,
                    userAgent = userAgent,
                    sourceLabel = "innertube-$clientName-hls",
                    backendKey = backendKey
                )
            )
        }

        val maxHeight = if (hasHardwareRenderNode) 1080 else 720
        val maxFps = if (hasHardwareRenderNode) 60 else 35

        val adaptive = streamingData.optJSONArray("adaptiveFormats")
        if (adaptive != null && adaptive.length() > 0) {
            val bestVideoUrl = pickBestAdaptiveVideo(adaptive, maxHeight, maxFps)
            val bestAudioUrl = pickBestAdaptiveAudio(adaptive)
            if (bestVideoUrl != null && bestAudioUrl != null) {
                list.add(
                    ResolvedYouTubeStream(
                        videoUrl = bestVideoUrl,
                        audioUrl = bestAudioUrl,
                        isHls = false,
                        userAgent = userAgent,
                        sourceLabel = "innertube-$clientName-adaptive",
                        backendKey = backendKey
                    )
                )
            }
        }

        val formats = streamingData.optJSONArray("formats")
        if (formats != null && formats.length() > 0) {
            val bestMuxedUrl = pickBestProgressiveFormat(formats, maxHeight)
            if (bestMuxedUrl != null) {
                list.add(
                    ResolvedYouTubeStream(
                        videoUrl = bestMuxedUrl,
                        audioUrl = null,
                        isHls = false,
                        userAgent = userAgent,
                        sourceLabel = "innertube-$clientName-muxed",
                        backendKey = backendKey
                    )
                )
            }
        }

        return list
    }

    private fun parseHeight(label: String): Int {
        val match = Regex("(\\d{3,4})p").find(label.lowercase())
        if (match != null) return match.groupValues[1].toIntOrNull() ?: 0
        val resMatch = Regex("\\d{3,4}x(\\d{3,4})").find(label.lowercase())
        if (resMatch != null) return resMatch.groupValues[1].toIntOrNull() ?: 0
        return 0
    }

    private fun pickBestAdaptiveVideo(
        formats: JSONArray,
        maxHeight: Int,
        maxFps: Int
    ): String? {
        var bestUrl: String? = null
        var bestScore = -1
        for (i in 0 until formats.length()) {
            val item = formats.optJSONObject(i) ?: continue
            val url = item.optString("url", "").trim()
            if (!url.startsWith("http")) continue
            val mime = item.optString("mimeType", "").lowercase()
            if (!mime.startsWith("video/mp4") || !mime.contains("avc1")) continue
            val height = item.optInt("height", 0)
            val fps = item.optInt("fps", 30)
            if (height <= 0 || height > maxHeight) continue
            val fpsPenalty = if (fps > maxFps) 400 else 0
            val score = (height * 10) - fpsPenalty
            if (score > bestScore) {
                bestScore = score
                bestUrl = url
            }
        }
        return bestUrl
    }

    private fun pickBestAdaptiveAudio(formats: JSONArray): String? {
        var bestUrl: String? = null
        var bestBitrate = -1
        for (i in 0 until formats.length()) {
            val item = formats.optJSONObject(i) ?: continue
            val url = item.optString("url", "").trim()
            if (!url.startsWith("http")) continue
            val mime = item.optString("mimeType", "").lowercase()
            if (!mime.startsWith("audio/mp4")) continue
            val bitrate = item.optInt("averageBitrate", item.optInt("bitrate", 0))
            if (bitrate > bestBitrate) {
                bestBitrate = bitrate
                bestUrl = url
            }
        }
        return bestUrl
    }

    private fun pickBestProgressiveFormat(formats: JSONArray, maxHeight: Int): String? {
        var bestUrl: String? = null
        var bestHeight = -1
        for (i in 0 until formats.length()) {
            val item = formats.optJSONObject(i) ?: continue
            val url = item.optString("url", "").trim()
            if (!url.startsWith("http")) continue
            val mime = item.optString("mimeType", "").lowercase()
            if (!mime.startsWith("video/mp4")) continue
            val height = item.optInt("height", 0)
            if (height in 1..maxHeight && height > bestHeight) {
                bestHeight = height
                bestUrl = url
            }
        }
        return bestUrl
    }
}
