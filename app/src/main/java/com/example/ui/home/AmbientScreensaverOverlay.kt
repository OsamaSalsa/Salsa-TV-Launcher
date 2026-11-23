package com.example.ui.home

import android.view.LayoutInflater
import androidx.activity.compose.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.LauncherPreferences
import com.example.ui.common.AuroraBackground
import com.example.ui.common.AuroraColors
import com.example.ui.common.BindWebViewLifecycle
import com.example.ui.common.ensureYouTubeConsentCookies
import com.example.ui.common.findActivityContext
import com.example.ui.common.grantSafeWebPermissions
import com.example.ui.common.hardenWebView
import com.example.ui.common.injectWebViewStabilityShim
import com.example.ui.common.isWebViewDestroyed
import com.example.ui.common.postOnMain
import com.example.ui.common.safeDestroyWebView
import com.example.ui.settings.TvActionButton
import com.example.ui.settings.onlineWallpaperCategories
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.GreetingHelper
import com.example.util.LocaleHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class ScreensaverPresetVideo(
    val id: String,
    val titleRes: Int,
    val subtitleRes: Int,
    val defaultTitle: String,
    val defaultSubtitle: String
)

val screensaverPresetVideos = listOf(
    ScreensaverPresetVideo(
        id = "EG_g7BFaL5s",
        titleRes = R.string.screensaver_video_rain_fireplace,
        subtitleRes = R.string.screensaver_video_rain_fireplace_sub,
        defaultTitle = "Cozy Fireplace Ambience 4K",
        defaultSubtitle = "8 hours of relaxing fireplace to sleep & unwind in peace"
    ),
    ScreensaverPresetVideo(
        id = "TtLeec_DR-Y",
        titleRes = R.string.screensaver_video_winter_cabin,
        subtitleRes = R.string.screensaver_video_winter_cabin_sub,
        defaultTitle = "Modern Oriental Café Music",
        defaultSubtitle = "Relaxing instrumentals for work, study & focus"
    ),
    ScreensaverPresetVideo(
        id = "8xXHeRnLwNQ",
        titleRes = R.string.screensaver_video_nature_waterfalls,
        subtitleRes = R.string.screensaver_video_nature_waterfalls_sub,
        defaultTitle = "Fireplace & Crackling Fire Sounds",
        defaultSubtitle = "8 hours relaxing fireplace 4K with natural fire sounds (No Music)"
    )
)

fun extractYouTubeVideoId(input: String): String {
    val clean = input.trim()
    if (clean.isEmpty()) return "EG_g7BFaL5s"
    if (
        clean.startsWith("content://", ignoreCase = true) ||
        clean.startsWith("file://", ignoreCase = true) ||
        clean.startsWith("/")
    ) {
        return clean
    }
    val isHttpUrl = clean.startsWith("http://", ignoreCase = true) ||
        clean.startsWith("https://", ignoreCase = true)
    val isYouTubeHost = clean.contains("youtube.com", ignoreCase = true) ||
        clean.contains("youtu.be", ignoreCase = true) ||
        clean.contains("youtube-nocookie.com", ignoreCase = true)
    if (isHttpUrl && !isYouTubeHost) {
        return clean
    }
    if (clean.length in 8..15 && !clean.contains("/") && !clean.contains(".")) {
        return clean
    }
    val patterns = listOf(
        "(?<=[?&]v=)[^&#?\\s/]+",
        "(?<=youtu\\.be/)[^&#?\\s/]+",
        "(?<=embed/)[^&#?\\s/]+",
        "(?<=shorts/)[^&#?\\s/]+",
        "(?<=live/)[^&#?\\s/]+"
    )
    for (pattern in patterns) {
        val match = Regex(pattern).find(clean)
        if (match != null && match.value.isNotBlank()) {
            return match.value
        }
    }
    return clean
}

const val SCREENSAVER_YOUTUBE_VIDEO_ID = "EG_g7BFaL5s"

@Composable
fun AmbientScreensaverOverlay(
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    screensaverMode: String = "clock_date", // "clock_date", "wallpaper_info", "video_stream"
    screensaverVideoId: String = "EG_g7BFaL5s",
    wallpaperMode: String = "dynamic",
    customWallpaperUrl: String = "",
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val hostView = LocalView.current

    DisposableEffect(hostView) {
        val prevKeepScreenOn = hostView.keepScreenOn
        hostView.keepScreenOn = true
        onDispose {
            hostView.keepScreenOn = prevKeepScreenOn
        }
    }

    BackHandler {
        onDismiss()
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    when (screensaverMode) {
        "video_stream" -> {
            // Mode 3: Fullscreen Video with Clock screensaver shown initially until video plays (and as fallback if playback fails)
            VideoScreensaver(
                rawVideoInput = screensaverVideoId,
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
        "slideshow" -> {
            // Mode 4: Photo Slideshow with Ken Burns motion, transitions, background music & particle effects
            SlideshowScreensaver(
                is24hFormat = is24hFormat,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
        "weather_dashboard",
        "news_dashboard",
        "economic_dashboard",
        "football_dashboard",
        "all_dashboards_cycle" -> {
            DashboardScreensaver(
                screensaverMode = screensaverMode,
                is24hFormat = is24hFormat,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
        "broadcast_text" -> {
            // Mode: Broadcast / Media Text Showcase with 60%-dimmed dynamic wallpaper, alternating White/Yellow lines & background audio
            BroadcastTextScreensaver(
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
        "wallpaper_info" -> {
            // Mode 2: App Wallpaper with clock and subtle info at bottom-right
            WallpaperInfoScreensaver(
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
                wallpaperMode = wallpaperMode,
                customWallpaperUrl = customWallpaperUrl,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
        else -> {
            // Mode 1 (Default): Clock and Date centered with ambient glow
            ClockDateScreensaver(
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
                focusRequester = focusRequester,
                onDismiss = onDismiss
            )
        }
    }
}

private const val STREAM_USER_AGENT =
    "Mozilla/5.0 (Linux; Android 13; Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

@OptIn(UnstableApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun VideoScreensaver(
    rawVideoInput: String,
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val cleanInput = rawVideoInput.trim()
    val isYouTubeUrl = cleanInput.contains("youtube.com", ignoreCase = true) ||
        cleanInput.contains("youtu.be", ignoreCase = true) ||
        cleanInput.contains("youtube-nocookie.com", ignoreCase = true)
    val isDirectStreamUrl = !isYouTubeUrl && (
        cleanInput.startsWith("http://", ignoreCase = true) ||
            cleanInput.startsWith("https://", ignoreCase = true) ||
            cleanInput.startsWith("content://", ignoreCase = true) ||
            cleanInput.startsWith("file://", ignoreCase = true) ||
            cleanInput.startsWith("/")
        )
    val videoId = remember(cleanInput) { extractYouTubeVideoId(cleanInput) }
    val hostView = LocalView.current
    val mountedAtMs = remember { System.currentTimeMillis() }

    var isVideoReady by remember(cleanInput) { mutableStateOf(false) }
    var playbackFailed by remember(cleanInput) { mutableStateOf(false) }

    // Fast startup using the saved winning playback method:
    // - If ExoPlayer won last time and we have a cached valid stream for this videoId, start ExoPlayer immediately (0ms wait).
    // - Otherwise start YouTubeWebScreensaver directly on the saved winning WebView stage (0..3) while
    //   YouTubeStreamResolver resolves in parallel using the saved winning backend first.
    val initialCachedStream = remember(cleanInput, videoId) {
        if (!isDirectStreamUrl && YouTubeStreamResolver.getPreferredMode(context) == "exo") {
            YouTubeStreamResolver.getFastCachedStream(context, videoId)
        } else {
            null
        }
    }

    var resolvedStream by remember(cleanInput) { mutableStateOf<ResolvedYouTubeStream?>(initialCachedStream) }
    var excludedStreamUrls by remember(cleanInput) { mutableStateOf<Set<String>>(emptySet()) }
    var useExoPlayer by remember(cleanInput) { mutableStateOf(initialCachedStream != null) }
    var webAllStagesFailed by remember(cleanInput) { mutableStateOf(false) }
    var resolverFinished by remember(cleanInput) { mutableStateOf(false) }
    var invidiousEmbedHosts by remember(cleanInput) {
        mutableStateOf(YouTubeStreamResolver.getCachedInvidiousEmbedHosts(context))
    }

    LaunchedEffect(cleanInput, videoId, excludedStreamUrls) {
        if (isDirectStreamUrl) return@LaunchedEffect
        if (useExoPlayer && resolvedStream != null) return@LaunchedEffect
        resolverFinished = false
        val stream = YouTubeStreamResolver.resolve(
            videoId = videoId,
            excludedVideoUrls = excludedStreamUrls,
            context = context
        )
        invidiousEmbedHosts = YouTubeStreamResolver.getCachedInvidiousEmbedHosts(context)
        resolverFinished = true
        if (stream != null && !isVideoReady) {
            resolvedStream = stream
            useExoPlayer = true
            playbackFailed = false
        } else if (webAllStagesFailed && !isVideoReady) {
            playbackFailed = true
        }
    }

    DisposableEffect(hostView) {
        val prevKeepScreenOn = hostView.keepScreenOn
        hostView.keepScreenOn = true
        onDispose {
            hostView.keepScreenOn = prevKeepScreenOn
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!playbackFailed) {
            if (isDirectStreamUrl) {
                DirectStreamExoPlayerScreensaver(
                    streamUrl = cleanInput,
                    stallTimeoutMs = 15_000L,
                    onVideoActivelyPlaying = {
                        playbackFailed = false
                        isVideoReady = true
                    },
                    onStreamError = {
                        isVideoReady = false
                        playbackFailed = true
                    }
                )
            } else if (useExoPlayer && resolvedStream != null) {
                val activeStream = resolvedStream ?: return@Box
                androidx.compose.runtime.key(activeStream.videoUrl, activeStream.audioUrl) {
                    DirectStreamExoPlayerScreensaver(
                        streamUrl = activeStream.videoUrl,
                        audioUrl = activeStream.audioUrl,
                        isHls = activeStream.isHls,
                        userAgent = activeStream.userAgent,
                        stallTimeoutMs = 12_000L,
                        onVideoActivelyPlaying = {
                            playbackFailed = false
                            isVideoReady = true
                            YouTubeStreamResolver.recordSuccessfulExoStream(
                                context = context,
                                videoId = videoId,
                                stream = activeStream
                            )
                        },
                        onStreamError = {
                            YouTubeStreamResolver.invalidateCachedExoStream(
                                context = context,
                                videoId = videoId,
                                failedVideoUrl = activeStream.videoUrl
                            )
                            if (!isVideoReady) {
                                excludedStreamUrls = excludedStreamUrls + activeStream.videoUrl
                                resolvedStream = null
                                useExoPlayer = false
                                if (webAllStagesFailed) {
                                    playbackFailed = true
                                }
                            } else {
                                isVideoReady = false
                                playbackFailed = true
                            }
                        }
                    )
                }
            } else {
                androidx.compose.runtime.key(videoId) {
                    YouTubeWebScreensaver(
                        videoId = videoId,
                        invidiousHosts = invidiousEmbedHosts,
                        onVideoReady = {
                            playbackFailed = false
                            isVideoReady = true
                        },
                        onAllStagesFailed = {
                            webAllStagesFailed = true
                            if (!isVideoReady && resolverFinished && !useExoPlayer) {
                                playbackFailed = true
                            } else if (isVideoReady) {
                                isVideoReady = false
                                playbackFailed = true
                            }
                        }
                    )
                }
            }
        }

        // Display the Clock & Date screensaver immediately from the start while the video buffers,
        // and keep it displayed seamlessly if the video fails to play (never showing an error dialog).
        AnimatedVisibility(
            visible = !isVideoReady,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 350)),
            modifier = Modifier.fillMaxSize()
        ) {
            ClockDateScreensaver(
                is24hFormat = is24hFormat,
                auroraColors = auroraColors,
                focusRequester = null,
                onDismiss = onDismiss
            )
        }

        // Top-level invisible interaction catcher so pressing any key or clicking always dismisses the screensaver
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (System.currentTimeMillis() - mountedAtMs > 450L) {
                            onDismiss()
                        }
                    }
                )
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && System.currentTimeMillis() - mountedAtMs > 500L) {
                        onDismiss()
                        true
                    } else false
                }
        )
    }
}

@androidx.annotation.Keep
class ScreensaverVideoBridge(
    private val mainHandler: android.os.Handler,
    private val onStarted: () -> Unit,
    private val onError: () -> Unit = {}
) {
    @android.webkit.JavascriptInterface
    fun onVideoStarted() {
        mainHandler.post {
            onStarted()
        }
    }

    @android.webkit.JavascriptInterface
    fun onEmbedError() {
        mainHandler.post {
            onError()
        }
    }
}

/**
 * 100% Zero-Sign-In Multi-Stage Fullscreen WebView Fallback with Persistent Winning Stage Memory:
 * Starts directly on whichever stage succeeded last time, and only cycles through the remaining stages if needed:
 * - Stage 0: `youtube.com/embed/<id>` (with Referer + consent cookies).
 * - Stage 1: YouTube TV Leanback guest mode (`youtube.com/tv#/watch?v=<id>`).
 * - Stage 2: Mobile watch page (`m.youtube.com/watch?v=<id>`).
 * - Stage 3: Invidious Proxied Embed (`https://<host>/embed/<id>?autoplay=1&loop=1&local=true`).
 */
@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
private fun YouTubeWebScreensaver(
    videoId: String,
    invidiousHosts: List<String> = emptyList(),
    onVideoReady: () -> Unit,
    onAllStagesFailed: () -> Unit = {}
) {
    val context = LocalContext.current
    val appReferer = remember(context) { "https://${context.packageName}" }
    val nativeAndroidUa = remember(context) {
        try {
            WebSettings.getDefaultUserAgent(context)
                .replace("; wv", "")
                .replace("Version/4.0 ", "")
        } catch (_: Exception) {
            STREAM_USER_AGENT
        }
    }
    val safeVideoId = remember(videoId) {
        videoId.replace(Regex("[^A-Za-z0-9_-]"), "").ifEmpty { SCREENSAVER_YOUTUBE_VIDEO_ID }
    }
    val effectiveInvHost = remember(context, invidiousHosts) {
        YouTubeStreamResolver.getPreferredInvidiousHost(context)
            ?: invidiousHosts.firstOrNull { it.startsWith("https://") }
            ?: "https://inv.nadeko.net"
    }

    // Order the 4 stages starting with the stage that succeeded last time!
    val stageOrder = remember(context, safeVideoId) {
        YouTubeStreamResolver.getOrderedWebStages(context)
    }

    var webViewRef by remember(safeVideoId) { mutableStateOf<WebView?>(null) }
    var crashCount by remember(safeVideoId) { mutableStateOf(0) }
    var stageStepIndex by remember(safeVideoId) { mutableStateOf(0) }
    // 0 = youtube.com/embed, 1 = YouTube TV Leanback, 2 = m.youtube.com/watch, 3 = Invidious proxy embed
    val playerStage = stageOrder[stageStepIndex.coerceIn(0, stageOrder.lastIndex)]
    var lastLoadedStage by remember(safeVideoId, crashCount) { mutableStateOf(-1) }
    var hasVideoStarted by remember(safeVideoId) { mutableStateOf(false) }
    val mainHandler = remember { android.os.Handler(android.os.Looper.getMainLooper()) }
    val latestPlayerStage by rememberUpdatedState(playerStage)
    val latestInvHost by rememberUpdatedState(effectiveInvHost)

    val markVideoStarted = remember(safeVideoId) {
        {
            if (!hasVideoStarted) {
                hasVideoStarted = true
                YouTubeStreamResolver.recordSuccessfulWebStage(
                    context = context,
                    stage = latestPlayerStage,
                    invidiousHost = latestInvHost
                )
                onVideoReady()
            }
        }
    }

    val advanceStageOnError = remember(safeVideoId, stageOrder) {
        {
            if (!hasVideoStarted) {
                if (stageStepIndex < stageOrder.lastIndex) {
                    stageStepIndex++
                } else {
                    onAllStagesFailed()
                }
            }
        }
    }

    // Timeout safety net while video has not started
    LaunchedEffect(safeVideoId, stageStepIndex, playerStage, hasVideoStarted) {
        if (!hasVideoStarted) {
            val waitMs = when (playerStage) {
                0 -> 6_500L
                1 -> 8_500L
                2 -> 8_000L
                else -> 8_500L
            }
            delay(waitMs)
            if (!hasVideoStarted) {
                if (stageStepIndex < stageOrder.lastIndex) {
                    stageStepIndex++
                } else {
                    onAllStagesFailed()
                }
            }
        }
    }

    val embedUrl = remember(safeVideoId) {
        "https://www.youtube.com/embed/$safeVideoId" +
            "?autoplay=1&mute=1&controls=0&loop=1&playlist=$safeVideoId" +
            "&playsinline=1&rel=0&iv_load_policy=3&fs=0&disablekb=1&modestbranding=1&enablejsapi=1"
    }
    val tvWatchUrl = remember(safeVideoId) {
        "https://www.youtube.com/tv#/watch?v=$safeVideoId"
    }
    val mobileWatchUrl = remember(safeVideoId) {
        "https://m.youtube.com/watch?v=$safeVideoId"
    }
    val invidiousEmbedUrl = remember(safeVideoId, effectiveInvHost) {
        "${effectiveInvHost.trimEnd('/')}/embed/$safeVideoId" +
            "?autoplay=1&loop=1&local=true&quality=medium&listen=0"
    }

    fun loadStageInWebView(wv: WebView, stage: Int) {
        if (isWebViewDestroyed(wv)) return
        try {
            ensureYouTubeConsentCookies(wv)
            wv.stopLoading()
            when (stage) {
                0 -> {
                    wv.settings.userAgentString = nativeAndroidUa
                    wv.loadUrl(
                        embedUrl,
                        mapOf("Referer" to appReferer)
                    )
                }
                1 -> {
                    wv.settings.userAgentString = com.example.ui.common.YOUTUBE_TV_USER_AGENT
                    wv.loadUrl(tvWatchUrl)
                }
                2 -> {
                    wv.settings.userAgentString = nativeAndroidUa
                    wv.loadUrl(mobileWatchUrl)
                }
                else -> {
                    wv.settings.userAgentString = nativeAndroidUa
                    wv.loadUrl(invidiousEmbedUrl)
                }
            }
        } catch (_: Exception) {}
    }

    val cleanFullscreenScript = remember(safeVideoId, playerStage) {
        val stageNum = playerStage
        """
        (function() {
            var STAGE = $stageNum;
            var VIDEO_ID = '$safeVideoId';

            function ensureCleanCss(isPlaying) {
                try {
                    var styleId = 'aura-screensaver-css';
                    var st = document.getElementById(styleId);
                    if (!st) {
                        st = document.createElement('style');
                        st.id = styleId;
                        (document.head || document.documentElement).appendChild(st);
                    }
                    var baseCss =
                        'html, body { background: #000000 !important; margin: 0 !important; padding: 0 !important; overflow: hidden !important; width: 100% !important; height: 100% !important; }';
                    if (STAGE === 0) {
                        baseCss += ' .html5-video-player, #movie_player { background: #000000 !important; width: 100vw !important; height: 100vh !important; }';
                        if (isPlaying) {
                            baseCss +=
                                ' .ytp-chrome-top, .ytp-chrome-bottom, .ytp-gradient-top, .ytp-gradient-bottom, ' +
                                ' .ytp-watermark, .ytp-pause-overlay, .ytp-ce-element, .ytp-title, .ytp-impression-link, ' +
                                ' .ytp-bezel, .ytp-show-cards-title, .ytp-cued-thumbnail-overlay, .ytp-large-play-button, ' +
                                ' .ytp-unmute, .ytp-paid-content-overlay { display: none !important; opacity: 0 !important; visibility: hidden !important; }' +
                                ' video { object-fit: cover !important; width: 100vw !important; height: 100vh !important; left: 0 !important; top: 0 !important; }';
                        }
                    } else if (STAGE === 1) {
                        if (isPlaying) {
                            baseCss +=
                                ' .ytlr-watch-metadata, .ytlr-player-overlay, .ytlr-transport-controls, ' +
                                ' ytlr-overlay-renderer { opacity: 0 !important; }' +
                                ' video { object-fit: cover !important; width: 100vw !important; height: 100vh !important; left: 0 !important; top: 0 !important; }';
                        }
                    } else if (STAGE === 2) {
                        baseCss +=
                            ' ytm-mobile-topbar-renderer, .mobile-topbar-header, #header-bar, ytm-pivot-bar-renderer, ' +
                            ' ytm-single-column-watch-next-results-renderer, .related-chips-slot-wrapper, ' +
                            ' ytm-slim-video-metadata-section-renderer, ytm-item-section-renderer { display: none !important; }';
                        if (isPlaying) {
                            baseCss +=
                                ' .player-controls-content, .player-controls-top, .player-controls-bottom, ' +
                                ' .ytp-unmute, .ytp-chrome-top, .ytp-chrome-bottom, .ytp-watermark, .ytp-large-play-button { display: none !important; opacity: 0 !important; }' +
                                ' #player-container-id, .player-container, #player, .html5-video-player { position: fixed !important; inset: 0 !important; width: 100vw !important; height: 100vh !important; max-width: 100vw !important; max-height: 100vh !important; background: #000 !important; z-index: 99999 !important; }' +
                                ' video { position: fixed !important; inset: 0 !important; width: 100vw !important; height: 100vh !important; object-fit: cover !important; background: #000 !important; }';
                        }
                    } else {
                        // Invidious HTML5 / VideoJS clean fullscreen styling
                        baseCss +=
                            ' .vjs-control-bar, .vjs-big-play-button, .vjs-title-bar, .vjs-poster, ' +
                            ' .vjs-error-display, .vjs-loading-spinner, header, footer, nav { display: none !important; opacity: 0 !important; }' +
                            ' #player-container, .video-js, #player, .h-box { position: fixed !important; inset: 0 !important; width: 100vw !important; height: 100vh !important; max-width: 100vw !important; max-height: 100vh !important; margin: 0 !important; padding: 0 !important; background: #000 !important; }' +
                            ' video { position: fixed !important; inset: 0 !important; width: 100vw !important; height: 100vh !important; object-fit: cover !important; background: #000 !important; }';
                    }
                    if (st.textContent !== baseCss) {
                        st.textContent = baseCss;
                    }
                } catch (e) {}
            }

            function isVideoActivelyPlaying(v, mp) {
                var byVideo = Boolean(
                    v && !v.paused && !v.ended && v.readyState >= 2 &&
                    (v.currentTime > 0.05 || (v.readyState >= 3 && v.videoWidth > 0))
                );
                var byPlayer = Boolean(
                    mp && typeof mp.getPlayerState === 'function' && mp.getPlayerState() === 1 && v && v.currentTime > 0.01
                );
                return byVideo || byPlayer;
            }

            function hasBlockingErrorOrBotWall(v) {
                try {
                    if (v && !v.paused && v.currentTime > 0.05) return false;
                    var errEl = document.querySelector(
                        '.ytp-error, .ytp-error-content-wrap, .ytp-error-content-wrap-subreason, ' +
                        'yt-player-error-message-renderer, ytm-player-error-message-renderer'
                    );
                    if (errEl && errEl.offsetWidth > 0 && errEl.offsetHeight > 0) return true;
                    var txt = (document.body && document.body.innerText) ? document.body.innerText.toLowerCase() : '';
                    if (
                        txt.indexOf('not a bot') !== -1 ||
                        txt.indexOf('sign in to confirm') !== -1 ||
                        txt.indexOf('لست روبوت') !== -1 ||
                        txt.indexOf('تسجيل الدخول لتأكيد') !== -1 ||
                        txt.indexOf('error 153') !== -1 ||
                        txt.indexOf('error 152') !== -1 ||
                        txt.indexOf('error 150') !== -1 ||
                        txt.indexOf('error 101') !== -1 ||
                        txt.indexOf('video unavailable') !== -1 ||
                        txt.indexOf('playback on other websites has been disabled') !== -1 ||
                        txt.indexOf('the media could not be loaded') !== -1 ||
                        txt.indexOf('could not extract') !== -1 ||
                        txt.indexOf('no compatible source was found') !== -1
                    ) {
                        return true;
                    }
                } catch (e) {}
                return false;
            }

            function tryDismissOverlays() {
                try {
                    var bodyTxt = (document.body && document.body.innerText) ? document.body.innerText.toLowerCase() : '';
                    if (
                        (location.hostname && location.hostname.indexOf('consent.') !== -1) ||
                        bodyTxt.indexOf('before you continue to youtube') !== -1 ||
                        bodyTxt.indexOf('قبل المتابعة إلى youtube') !== -1
                    ) {
                        var btns = document.querySelectorAll('form button, button, [role="button"]');
                        for (var i = 0; i < btns.length; i++) {
                            var txt = (btns[i].innerText || btns[i].getAttribute('aria-label') || '').toLowerCase();
                            if (
                                txt.indexOf('reject all') !== -1 ||
                                txt.indexOf('accept all') !== -1 ||
                                txt.indexOf('رفض الكل') !== -1 ||
                                txt.indexOf('قبول الكل') !== -1 ||
                                txt.indexOf('reject') !== -1 ||
                                txt.indexOf('agree') !== -1
                            ) {
                                btns[i].click();
                                return true;
                            }
                        }
                    }
                    if (STAGE === 1) {
                        var tvBtns = document.querySelectorAll('ytlr-button, button, [role="button"]');
                        for (var k = 0; k < tvBtns.length; k++) {
                            var label = (tvBtns[k].innerText || tvBtns[k].getAttribute('aria-label') || '').toLowerCase();
                            if (
                                label.indexOf('signed out') !== -1 ||
                                label.indexOf('without signing in') !== -1 ||
                                label.indexOf('بدون تسجيل') !== -1 ||
                                label.indexOf('المتابعة كضيف') !== -1
                            ) {
                                tvBtns[k].click();
                                return true;
                            }
                        }
                        if (
                            location.pathname.indexOf('/tv') !== -1 &&
                            location.hash.indexOf(VIDEO_ID) === -1 &&
                            !document.querySelector('video')
                        ) {
                            if (!window.__auraTvHashFixed) {
                                window.__auraTvHashFixed = true;
                                location.hash = '#/watch?v=' + VIDEO_ID;
                            }
                        }
                    }
                } catch (e) {}
                return false;
            }

            function notifyIfPlaying(v, mp) {
                if (isVideoActivelyPlaying(v, mp)) {
                    ensureCleanCss(true);
                    try { AuraVideoBridge.onVideoStarted(); } catch(e) {}
                    if (!window.__auraConsoleReadySent) {
                        window.__auraConsoleReadySent = true;
                        try { console.log('AURA_VIDEO_STARTED'); } catch(e) {}
                    }
                }
            }

            function applyCleanVideo() {
                try {
                    tryDismissOverlays();
                    var v = document.querySelector('video');
                    if (hasBlockingErrorOrBotWall(v)) {
                        if (!window.__auraEmbedErrSent) {
                            window.__auraEmbedErrSent = true;
                            try { AuraVideoBridge.onEmbedError(); } catch(e) {}
                            try { console.log('AURA_EMBED_ERROR'); } catch(e) {}
                        }
                        return;
                    }
                    var skipBtn = document.querySelector('.ytp-ad-skip-button, .ytp-skip-ad-button, .ytp-ad-skip-button-modern');
                    if (skipBtn) {
                        try { skipBtn.click(); } catch(e) {}
                    }
                    var mp = document.getElementById('movie_player');
                    var playingNow = isVideoActivelyPlaying(v, mp);
                    ensureCleanCss(playingNow);
                    if (v) {
                        v.loop = true;
                        v.controls = false;
                        if (mp && mp.classList && mp.classList.contains('ad-showing') && isFinite(v.duration) && v.duration > 0) {
                            try { v.currentTime = v.duration; } catch(e) {}
                        }
                        if (!v.__auraBound) {
                            v.__auraBound = true;
                            v.addEventListener('loadeddata', function() { notifyIfPlaying(v, mp); });
                            v.addEventListener('playing', function() { notifyIfPlaying(v, mp); });
                            v.addEventListener('timeupdate', function() { notifyIfPlaying(v, mp); });
                            v.addEventListener('ended', function() {
                                try {
                                    v.currentTime = 0;
                                    v.play().catch(function(){});
                                } catch(e) {}
                            });
                            v.addEventListener('error', function() {
                                if (!window.__auraEmbedErrSent && (!v.currentTime || v.currentTime < 0.05)) {
                                    window.__auraEmbedErrSent = true;
                                    try { AuraVideoBridge.onEmbedError(); } catch(e) {}
                                }
                            });
                        }
                        var now = Date.now();
                        if ((v.paused || v.ended) && (now - (window.__auraLastPlayAttempt || 0) > 850)) {
                            window.__auraLastPlayAttempt = now;
                            if (!window.__auraUnmuteDone) {
                                v.muted = true;
                                if (mp && typeof mp.mute === 'function') {
                                    try { mp.mute(); } catch(e) {}
                                }
                            }
                            if (mp && typeof mp.playVideo === 'function') {
                                try { mp.playVideo(); } catch(e) {}
                            }
                            var playBtnPre = document.querySelector('.ytp-large-play-button, .vjs-big-play-button, .ytp-play-button, button[aria-label*="Play" i]');
                            if (playBtnPre && v.readyState < 2) {
                                try { playBtnPre.click(); } catch(e) {}
                            }
                            var playPromise = v.play();
                            if (playPromise && typeof playPromise.then === 'function') {
                                playPromise.then(function() {
                                    notifyIfPlaying(v, mp);
                                    if (!window.__auraUnmuteScheduled) {
                                        window.__auraUnmuteScheduled = true;
                                        setTimeout(function() {
                                            try {
                                                window.__auraUnmuteDone = true;
                                                v.muted = false;
                                                v.volume = 1.0;
                                                if (mp && typeof mp.unMute === 'function') {
                                                    try { mp.unMute(); mp.setVolume(100); } catch(e) {}
                                                }
                                                setTimeout(function() {
                                                    if (v.paused) {
                                                        v.muted = true;
                                                        if (mp && typeof mp.playVideo === 'function') {
                                                            try { mp.playVideo(); } catch(e) {}
                                                        }
                                                        v.play().catch(function(){});
                                                    }
                                                }, 250);
                                            } catch(e) {}
                                        }, 550);
                                    }
                                }).catch(function() {
                                    v.muted = true;
                                    var playBtn = document.querySelector('.ytp-large-play-button, .vjs-big-play-button, .ytp-play-button, button[aria-label*="Play" i]');
                                    if (playBtn) { try { playBtn.click(); } catch(e) {} }
                                });
                            }
                        } else if (!v.paused && !window.__auraUnmuteScheduled && v.currentTime > 0.15) {
                            window.__auraUnmuteScheduled = true;
                            setTimeout(function() {
                                try {
                                    window.__auraUnmuteDone = true;
                                    v.muted = false;
                                    v.volume = 1.0;
                                    if (mp && typeof mp.unMute === 'function') {
                                        try { mp.unMute(); mp.setVolume(100); } catch(e) {}
                                    }
                                    setTimeout(function() {
                                        if (v.paused) {
                                            v.muted = true;
                                            v.play().catch(function(){});
                                        }
                                    }, 250);
                                } catch(e) {}
                            }, 550);
                        }
                        notifyIfPlaying(v, mp);
                    } else {
                        var largePlay = document.querySelector('.ytp-large-play-button, .vjs-big-play-button, .ytp-play-button, button[aria-label*="Play" i]');
                        if (largePlay && (Date.now() - (window.__auraLastBtnClick || 0) > 1000)) {
                            window.__auraLastBtnClick = Date.now();
                            try { largePlay.click(); } catch(e) {}
                        }
                    }
                } catch (e) {}
            }
            window.__auraApplyCleanVideo = applyCleanVideo;
            applyCleanVideo();
            if (!window.__ambientCleanTimer) {
                window.__ambientCleanTimer = setInterval(function() {
                    if (window.__auraApplyCleanVideo) window.__auraApplyCleanVideo();
                }, 300);
            }
        })();
        """.trimIndent()
    }

    val latestCleanScript by rememberUpdatedState(cleanFullscreenScript)
    val latestHasStarted by rememberUpdatedState(hasVideoStarted)

    // Advance the existing WebView instance when playerStage changes
    LaunchedEffect(webViewRef, safeVideoId, playerStage) {
        val wv = webViewRef ?: return@LaunchedEffect
        if (isWebViewDestroyed(wv)) return@LaunchedEffect
        if (lastLoadedStage != playerStage) {
            lastLoadedStage = playerStage
            loadStageInWebView(wv, playerStage)
        }
    }

    // Kotlin-side playback polling across all stages
    LaunchedEffect(webViewRef, safeVideoId, playerStage) {
        val wv = webViewRef ?: return@LaunchedEffect
        while (!isWebViewDestroyed(wv)) {
            try {
                wv.evaluateJavascript(latestCleanScript, null)
                if (!latestHasStarted) {
                    wv.evaluateJavascript(
                        """
                        (function(){
                            try {
                                var v = document.querySelector('video');
                                var mp = document.getElementById('movie_player');
                                var byVideo = Boolean(v && !v.paused && !v.ended && v.readyState >= 2 && (v.currentTime > 0.05 || (v.readyState >= 3 && v.videoWidth > 0)));
                                var byPlayer = Boolean(mp && typeof mp.getPlayerState === 'function' && mp.getPlayerState() === 1 && v && v.currentTime > 0.01);
                                return byVideo || byPlayer;
                            } catch(e) { return false; }
                        })()
                        """.trimIndent()
                    ) { result ->
                        if (result?.trim() == "true") {
                            markVideoStarted()
                        }
                    }
                }
            } catch (_: Exception) {}
            delay(if (latestHasStarted) 1200L else 400L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (crashCount <= 2) {
            androidx.compose.runtime.key(safeVideoId, crashCount) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx.findActivityContext()).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(android.graphics.Color.BLACK)
                            keepScreenOn = true
                            isVerticalScrollBarEnabled = false
                            isHorizontalScrollBarEnabled = false
                            isFocusable = false
                            isFocusableInTouchMode = false

                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                mediaPlaybackRequiresUserGesture = false
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(false)
                                builtInZoomControls = false
                                displayZoomControls = false
                                allowFileAccess = true
                                allowContentAccess = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                cacheMode = WebSettings.LOAD_DEFAULT
                                userAgentString = nativeAndroidUa
                                safeBrowsingEnabled = false
                            }

                            hardenWebView(this)
                            ensureYouTubeConsentCookies(this)

                            addJavascriptInterface(
                                ScreensaverVideoBridge(
                                    mainHandler = mainHandler,
                                    onStarted = markVideoStarted,
                                    onError = advanceStageOnError
                                ),
                                "AuraVideoBridge"
                            )

                            val webViewInstance = this

                            var fullscreenCustomView: android.view.View? = null
                            var fullscreenCallback: WebChromeClient.CustomViewCallback? = null
                            var fullscreenContainer: android.widget.FrameLayout? = null

                            webChromeClient = object : WebChromeClient() {
                                override fun onPermissionRequest(request: PermissionRequest?) {
                                    grantSafeWebPermissions(request)
                                }

                                override fun onShowCustomView(view: android.view.View?, callback: CustomViewCallback?) {
                                    if (view == null) {
                                        callback?.onCustomViewHidden()
                                        return
                                    }
                                    if (fullscreenCustomView != null) {
                                        callback?.onCustomViewHidden()
                                        return
                                    }
                                    fullscreenCustomView = view
                                    fullscreenCallback = callback
                                    fullscreenContainer = android.widget.FrameLayout(context).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        setBackgroundColor(android.graphics.Color.BLACK)
                                        addView(view)
                                    }
                                    (webViewInstance.parent as? ViewGroup)?.addView(fullscreenContainer)
                                    webViewInstance.visibility = android.view.View.INVISIBLE
                                    mainHandler.post { markVideoStarted() }
                                }

                                override fun onHideCustomView() {
                                    fullscreenContainer?.let { container ->
                                        container.removeAllViews()
                                        (container.parent as? ViewGroup)?.removeView(container)
                                    }
                                    fullscreenContainer = null
                                    fullscreenCustomView = null
                                    try {
                                        fullscreenCallback?.onCustomViewHidden()
                                    } catch (_: Exception) {}
                                    fullscreenCallback = null
                                    webViewInstance.visibility = android.view.View.VISIBLE
                                }

                                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                                    val msg = consoleMessage?.message() ?: ""
                                    if (msg.contains("AURA_VIDEO_STARTED")) {
                                        mainHandler.post { markVideoStarted() }
                                    } else if (msg.contains("AURA_EMBED_ERROR")) {
                                        mainHandler.post { advanceStageOnError() }
                                    }
                                    return super.onConsoleMessage(consoleMessage)
                                }
                            }

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    // Never navigate out to Google Sign-In pages
                                    if (url.contains("accounts.google.com", ignoreCase = true)) {
                                        mainHandler.post { advanceStageOnError() }
                                        return true
                                    }
                                    return !url.startsWith("http://") && !url.startsWith("https://")
                                }

                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: android.graphics.Bitmap?
                                ) {
                                    super.onPageStarted(view, url, favicon)
                                    if (!isWebViewDestroyed(view)) {
                                        injectWebViewStabilityShim(view, disableWebGl = false)
                                    }
                                }

                                override fun onPageCommitVisible(view: WebView?, url: String?) {
                                    super.onPageCommitVisible(view, url)
                                    if (!isWebViewDestroyed(view)) {
                                        injectWebViewStabilityShim(view, disableWebGl = false)
                                        view?.evaluateJavascript(latestCleanScript, null)
                                    }
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    if (isWebViewDestroyed(view)) return
                                    injectWebViewStabilityShim(view, disableWebGl = false)
                                    view?.evaluateJavascript(latestCleanScript, null)
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?,
                                    error: android.webkit.WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    if (request?.isForMainFrame == true && !latestHasStarted) {
                                        mainHandler.post { advanceStageOnError() }
                                    }
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?,
                                    errorResponse: android.webkit.WebResourceResponse?
                                ) {
                                    super.onReceivedHttpError(view, request, errorResponse)
                                    if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 200) >= 400 && !latestHasStarted) {
                                        mainHandler.post { advanceStageOnError() }
                                    }
                                }

                                override fun onRenderProcessGone(
                                    view: WebView?,
                                    detail: RenderProcessGoneDetail?
                                ): Boolean {
                                    safeDestroyWebView(view, rendererGone = true)
                                    if (webViewRef === view) webViewRef = null
                                    postOnMain(300L) {
                                        crashCount++
                                        if (crashCount > 2) {
                                            onAllStagesFailed()
                                        }
                                    }
                                    return true
                                }
                            }

                            lastLoadedStage = playerStage
                            loadStageInWebView(this, playerStage)
                            webViewRef = this
                        }
                    },
                    update = { webView ->
                        webViewRef = webView
                    },
                    onRelease = { webView ->
                        if (webViewRef === webView) webViewRef = null
                        safeDestroyWebView(webView)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    BindWebViewLifecycle(webViewRef)

    DisposableEffect(Unit) {
        onDispose {
            safeDestroyWebView(webViewRef)
            webViewRef = null
        }
    }
}

@OptIn(UnstableApi::class)
private class ChunkedHttpDataSource(
    private val upstream: HttpDataSource,
    private val chunkSizeBytes: Long = 2L * 1024L * 1024L
) : DataSource {
    private var currentDataSpec: DataSpec? = null
    private var currentPosition: Long = 0L
    private var bytesRemainingTotal: Long = C.LENGTH_UNSET.toLong()
    private var bytesRemainingInChunk: Long = 0L
    private var openedChunk: Boolean = false

    override fun addTransferListener(transferListener: TransferListener) {
        upstream.addTransferListener(transferListener)
    }

    override fun open(dataSpec: DataSpec): Long {
        currentDataSpec = dataSpec
        currentPosition = dataSpec.position
        bytesRemainingTotal = if (dataSpec.length != C.LENGTH_UNSET.toLong()) {
            dataSpec.length
        } else {
            C.LENGTH_UNSET.toLong()
        }
        openNextChunk(initial = true)
        return bytesRemainingTotal
    }

    private fun openNextChunk(initial: Boolean): Boolean {
        val base = currentDataSpec ?: return false
        if (bytesRemainingTotal == 0L) return false

        val toRequest = if (bytesRemainingTotal != C.LENGTH_UNSET.toLong()) {
            minOf(chunkSizeBytes, bytesRemainingTotal)
        } else {
            chunkSizeBytes
        }

        val chunkSpec = base.buildUpon()
            .setPosition(currentPosition)
            .setLength(toRequest)
            .build()

        val openedLen = upstream.open(chunkSpec)
        openedChunk = true

        if (initial && bytesRemainingTotal == C.LENGTH_UNSET.toLong()) {
            val contentRange = upstream.responseHeaders["Content-Range"]?.firstOrNull()
                ?: upstream.responseHeaders["content-range"]?.firstOrNull()
            val totalSize = contentRange?.substringAfterLast("/")?.toLongOrNull()
            if (totalSize != null && totalSize > currentPosition) {
                bytesRemainingTotal = totalSize - currentPosition
            }
        }

        bytesRemainingInChunk = if (openedLen != C.LENGTH_UNSET.toLong()) openedLen else toRequest
        return bytesRemainingInChunk != 0L
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemainingTotal == 0L) return C.RESULT_END_OF_INPUT

        if (bytesRemainingInChunk == 0L) {
            closeUpstreamQuietly()
            if (!openNextChunk(initial = false)) {
                return C.RESULT_END_OF_INPUT
            }
        }

        val maxToRead = if (bytesRemainingInChunk > 0L) {
            minOf(length.toLong(), bytesRemainingInChunk).toInt()
        } else {
            length
        }

        val read = upstream.read(buffer, offset, maxToRead)
        if (read == C.RESULT_END_OF_INPUT) {
            if (bytesRemainingTotal == C.LENGTH_UNSET.toLong() || bytesRemainingTotal > 0L) {
                if (bytesRemainingInChunk > 0L && bytesRemainingTotal == C.LENGTH_UNSET.toLong()) {
                    bytesRemainingTotal = 0L
                    return C.RESULT_END_OF_INPUT
                }
                closeUpstreamQuietly()
                if (!openNextChunk(initial = false)) {
                    return C.RESULT_END_OF_INPUT
                }
                return read(buffer, offset, length)
            }
            return C.RESULT_END_OF_INPUT
        }

        if (read > 0) {
            currentPosition += read
            if (bytesRemainingTotal != C.LENGTH_UNSET.toLong()) {
                bytesRemainingTotal = (bytesRemainingTotal - read).coerceAtLeast(0L)
            }
            if (bytesRemainingInChunk > 0L) {
                bytesRemainingInChunk = (bytesRemainingInChunk - read).coerceAtLeast(0L)
            }
        }
        return read
    }

    override fun getUri(): Uri? = upstream.uri ?: currentDataSpec?.uri

    override fun getResponseHeaders(): Map<String, List<String>> = upstream.responseHeaders

    override fun close() {
        currentDataSpec = null
        closeUpstreamQuietly()
    }

    private fun closeUpstreamQuietly() {
        if (openedChunk) {
            openedChunk = false
            try { upstream.close() } catch (_: Exception) {}
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun DirectStreamExoPlayerScreensaver(
    streamUrl: String,
    audioUrl: String? = null,
    isHls: Boolean = false,
    userAgent: String = STREAM_USER_AGENT,
    stallTimeoutMs: Long = 15_000L,
    onVideoActivelyPlaying: () -> Unit = {},
    onStreamError: () -> Unit
) {
    val context = LocalContext.current
    var stoppedByLifecycle by remember { mutableStateOf(false) }
    var retryCount by remember(streamUrl, audioUrl) { mutableStateOf(0) }
    var playerViewRef by remember { mutableStateOf<PlayerView?>(null) }
    var firstFrameRendered by remember(streamUrl, audioUrl) { mutableStateOf(false) }
    var activePlaybackConfirmed by remember(streamUrl, audioUrl) { mutableStateOf(false) }
    var stallReported by remember(streamUrl, audioUrl) { mutableStateOf(false) }

    val exoPlayer: ExoPlayer = remember(streamUrl, audioUrl, isHls, userAgent) {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(12000)
            .setReadTimeoutMs(15000)

        val effectiveUpstreamFactory = if (!isHls && streamUrl.contains("googlevideo.com", ignoreCase = true)) {
            DataSource.Factory { ChunkedHttpDataSource(httpDataSourceFactory.createDataSource()) }
        } else {
            httpDataSourceFactory
        }

        val dataSourceFactory = DefaultDataSource.Factory(context, effectiveUpstreamFactory)

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
            .setDrmSessionManagerProvider { DrmSessionManager.DRM_UNSUPPORTED }

        val renderersFactory = DefaultRenderersFactory(context)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
            .setEnableDecoderFallback(true)

        val trackSelector = DefaultTrackSelector(context).apply {
            parameters = buildUponParameters()
                .setMaxVideoSize(1920, 1080)
                .setPreferredVideoMimeTypes("video/avc", "video/hevc", "video/x-vnd.on2.vp9")
                .setForceHighestSupportedBitrate(true)
                .build()
        }

        val bandwidthMeter = DefaultBandwidthMeter.Builder(context)
            .setInitialBitrateEstimate(10_000_000L)
            .build()

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                2000,  // minBufferMs
                25000, // maxBufferMs
                250,   // bufferForPlaybackMs (near-instant start)
                800    // bufferForPlaybackAfterRebufferMs
            )
            .build()

        val primaryMediaItem = if (streamUrl.startsWith("/")) {
            MediaItem.fromUri(Uri.fromFile(java.io.File(streamUrl)))
        } else {
            MediaItem.fromUri(streamUrl)
        }

        val mediaSource = when {
            isHls || streamUrl.contains(".m3u8", ignoreCase = true) -> {
                HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(primaryMediaItem)
            }
            !audioUrl.isNullOrBlank() -> {
                val videoSource = mediaSourceFactory.createMediaSource(primaryMediaItem)
                val audioSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(audioUrl))
                MergingMediaSource(true, true, videoSource, audioSource)
            }
            else -> {
                mediaSourceFactory.createMediaSource(primaryMediaItem)
            }
        }

        ExoPlayer.Builder(context, renderersFactory)
            .setTrackSelector(trackSelector)
            .setBandwidthMeter(bandwidthMeter)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                repeatMode = Player.REPEAT_MODE_ONE
                setMediaSource(mediaSource)
                prepare()
                playWhenReady = true
                volume = 1f
            }
    }

    // Confirm real frame advancement before hiding clock overlay, and allow generous buffering window
    LaunchedEffect(exoPlayer) {
        var lastPos = exoPlayer.currentPosition
        var stalledMs = 0L
        while (!activePlaybackConfirmed) {
            delay(300L)
            if (stoppedByLifecycle) continue
            val currentPos = exoPlayer.currentPosition
            val isAdvancing = currentPos > lastPos || (exoPlayer.isPlaying && exoPlayer.playbackState == Player.STATE_READY && currentPos > 50L)
            if (isAdvancing) {
                stalledMs = 0L
                val hasVideoSize = exoPlayer.videoSize.width > 0 && exoPlayer.videoSize.height > 0
                if ((firstFrameRendered || hasVideoSize) && !activePlaybackConfirmed) {
                    activePlaybackConfirmed = true
                    onVideoActivelyPlaying()
                    break
                }
            } else {
                stalledMs += 300L
                if (stalledMs >= stallTimeoutMs && !stallReported && !activePlaybackConfirmed) {
                    stallReported = true
                    onStreamError()
                }
            }
            lastPos = currentPos
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    stoppedByLifecycle = true
                    try {
                        exoPlayer.playWhenReady = false
                        exoPlayer.stop()
                        exoPlayer.clearVideoSurface()
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_START -> {
                    if (stoppedByLifecycle) {
                        stoppedByLifecycle = false
                        try {
                            playerViewRef?.let { pv -> exoPlayer.setVideoTextureView(pv.videoSurfaceView as? android.view.TextureView) }
                        } catch (_: Exception) {}
                        try {
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        } catch (_: Exception) {}
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    retryCount = 0
                }
            }

            override fun onRenderedFirstFrame() {
                firstFrameRendered = true
                if (!activePlaybackConfirmed) {
                    activePlaybackConfirmed = true
                    onVideoActivelyPlaying()
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                val isUnrecoverableHttp =
                    error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ||
                        error.errorCode == androidx.media3.common.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
                if (!stoppedByLifecycle && !isUnrecoverableHttp && retryCount < 1) {
                    retryCount++
                    postOnMain(300L) {
                        if (!stoppedByLifecycle) {
                            try {
                                exoPlayer.stop()
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            } catch (_: Exception) {}
                        }
                    }
                } else if (!stallReported) {
                    stallReported = true
                    onStreamError()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            playerViewRef?.player = null
            playerViewRef = null
            try { exoPlayer.stop() } catch (_: Exception) {}
            try { exoPlayer.clearVideoSurface() } catch (_: Exception) {}
            try { exoPlayer.release() } catch (_: Exception) {}
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val view = LayoutInflater.from(ctx)
                .inflate(R.layout.layout_screensaver_player, android.widget.FrameLayout(ctx), false) as PlayerView
            view.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                keepScreenOn = true
                useController = false
                player = exoPlayer
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setEnableComposeSurfaceSyncWorkaround(true)
                playerViewRef = this
            }
        },
        update = { view ->
            view.keepScreenOn = true
            view.useController = false
            view.player = exoPlayer
            view.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            playerViewRef = view
        },
        onRelease = { view ->
            view.player = null
            if (playerViewRef === view) playerViewRef = null
        }
    )
}