package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.palette.graphics.Palette
import com.example.data.local.LauncherPreferences
import com.example.data.model.AppItem
import com.example.data.model.WebBookmark
import com.example.ui.common.AuroraColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AppRepository(
    private val context: Context,
    private val preferences: LauncherPreferences
) {
    private val packageManager: PackageManager = context.packageManager
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val reloadTrigger = MutableStateFlow(System.currentTimeMillis())

    // In-memory cache of decoded app metadata, bitmaps, and palette colors keyed by packageName
    private val packageCache = ConcurrentHashMap<String, AppItem>()
    private val packageCacheDirty = java.util.concurrent.atomic.AtomicBoolean(true)
    private val bookmarkIconAttempts = ConcurrentHashMap.newKeySet<String>()

    private val appUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val pkg = intent?.data?.schemeSpecificPart
            if (pkg != null) {
                packageCache.remove(pkg)
            } else {
                packageCache.clear()
            }
            packageCacheDirty.set(true)
            triggerReload()
        }
    }

    init {
        // Seed built-in portals immediately so initial state is never empty
        seedPortalAppsIntoCache()

        // Automatically fetch & cache social meta / browser icons for any active bookmarks missing a local icon
        repoScope.launch(Dispatchers.IO) {
            preferences.activeWebBookmarksFlow.collect { bookmarks ->
                bookmarks.forEach { bm ->
                    val missingLocalFile = bm.iconUrl.isBlank() ||
                        (bm.iconUrl.startsWith("/") && !java.io.File(bm.iconUrl).exists())
                    if (!bm.iconUrl.startsWith("res://") && missingLocalFile && bookmarkIconAttempts.add(bm.id)) {
                        launch {
                            val resolved = com.example.util.WebBookmarkIconResolver
                                .resolveAndCacheIcon(context, bm.id, bm.url)
                            if (!resolved.isNullOrBlank()) {
                                preferences.updateCustomWebBookmarkIcon(bm.id, resolved)
                                triggerReload()
                            }
                        }
                    }
                }
            }
        }

        // Start parallel background preload immediately upon repository creation
        repoScope.launch {
            try {
                warmUpPackageCache()
                reloadTrigger.value = System.currentTimeMillis()
            } catch (_: Exception) {}
        }

        try {
            val filter = android.content.IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
            androidx.core.content.ContextCompat.registerReceiver(
                context,
                appUpdateReceiver,
                filter,
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (_: Exception) {}
    }

    fun triggerReload() {
        reloadTrigger.value = System.currentTimeMillis()
    }

    fun close() {
        try {
            context.unregisterReceiver(appUpdateReceiver)
        } catch (_: Exception) {}
        try {
            repoScope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
        } catch (_: Exception) {}
    }

    fun getInitialFastApps(): List<AppItem> {
        if (packageCache.isEmpty()) {
            seedPortalAppsIntoCache()
        }
        return packageCache.values
            .filter { it.banner != null || it.packageName.endsWith(".portal") }
            .sortedWith(
                compareBy(
                    { app ->
                        when (app.packageName) {
                            "com.minidownloader.portal" -> 0
                            "com.aurastore.portal" -> 1
                            else -> 2
                        }
                    },
                    { it.label.lowercase() }
                )
            )
    }

    private fun webBookmarkToAppItem(
        bookmark: WebBookmark,
        hiddenPackages: Set<String>,
        customGames: Set<String>,
        customNonGames: Set<String>,
        customWebApps: Set<String>,
        customNonWebApps: Set<String>,
        lockedPackages: Set<String>
    ): AppItem {
        val pkg = "com.bookmark.${bookmark.id}.portal"
        val isGame = when {
            customGames.contains(pkg) -> true
            customWebApps.contains(pkg) -> false
            customNonGames.contains(pkg) -> false
            else -> bookmark.defaultIsGame
        }
        val isWeb = !isGame && !customNonWebApps.contains(pkg)
        val dominant = when (bookmark.id) {
            "preset_youtube" -> 0xFFFF0000.toInt()
            "preset_crazygames" -> 0xFF6366F1.toInt()
            "preset_poki" -> 0xFF00D2FF.toInt()
            "preset_playhop" -> 0xFF22C55E.toInt()
            "preset_easygames" -> 0xFFEF4444.toInt()
            else -> 0xFF38BDF8.toInt()
        }
        val aurora = if (bookmark.id == "preset_youtube") {
            AuroraColors(
                glow = Color(0xFFFF3B30),
                mid = Color(0xFFDC2626),
                accent = Color(0xFF991B1B),
                deep = Color(0xFF450A0A),
                base = Color(0xFF020617),
                teal = Color(0xFFEF4444)
            )
        } else {
            AuroraColors(
                glow = Color(0xFF38BDF8),
                mid = Color(0xFF0EA5E9),
                accent = Color(0xFF6366F1),
                deep = Color(0xFF0369A1),
                base = Color(0xFF020617),
                teal = Color(0xFF14B8A6)
            )
        }
        return AppItem(
            packageName = pkg,
            activityName = bookmark.url,
            label = bookmark.title,
            icon = null,
            banner = null,
            iconBitmap = null,
            bannerBitmap = null,
            dominantColor = dominant,
            auroraColors = aurora,
            isHidden = hiddenPackages.contains(pkg),
            isGame = isGame,
            isWeb = isWeb,
            isLocked = lockedPackages.contains(pkg),
            bookmarkUrl = bookmark.url,
            bookmarkIconUrl = bookmark.iconUrl.ifBlank { null }
        )
    }

    fun resolveBookmarkIconAsync(bookmarkId: String, rawUrl: String) {
        if (bookmarkId.isBlank() || rawUrl.isBlank()) return
        bookmarkIconAttempts.add(bookmarkId)
        repoScope.launch(Dispatchers.IO) {
            val resolved = com.example.util.WebBookmarkIconResolver
                .resolveAndCacheIcon(context, bookmarkId, rawUrl)
            if (!resolved.isNullOrBlank()) {
                preferences.updateCustomWebBookmarkIcon(bookmarkId, resolved)
                triggerReload()
            }
        }
    }

    fun resolveMissingBookmarkIconsAsync(bookmarks: List<WebBookmark>) {
        if (bookmarks.isEmpty()) return
        repoScope.launch(Dispatchers.IO) {
            bookmarks.forEach { bm ->
                val missingLocalFile = bm.iconUrl.isBlank() ||
                    (bm.iconUrl.startsWith("/") && !java.io.File(bm.iconUrl).exists())
                if (!bm.iconUrl.startsWith("res://") && missingLocalFile && bookmarkIconAttempts.add(bm.id)) {
                    launch {
                        val resolved = com.example.util.WebBookmarkIconResolver
                            .resolveAndCacheIcon(context, bm.id, bm.url)
                        if (!resolved.isNullOrBlank()) {
                            preferences.updateCustomWebBookmarkIcon(bm.id, resolved)
                            triggerReload()
                        }
                    }
                }
            }
        }
    }

    private val webSectionPrefsFlow: Flow<Pair<Set<String>, Set<String>>> = combine(
        preferences.customWebAppsFlow,
        preferences.customNonWebAppsFlow
    ) { customWeb, customNonWeb ->
        customWeb to customNonWeb
    }

    /**
     * Flow of real installed apps + custom web bookmarks, respecting saved order, hidden apps, and Apps/Web/Games categorization.
     */
    val installedAppsFlow: Flow<List<AppItem>> = combine(
        combine(
            reloadTrigger,
            preferences.appOrderFlow,
            preferences.hiddenAppsFlow,
            preferences.customGamesFlow,
            preferences.customNonGamesFlow
        ) { _, savedOrder, hiddenPackages, customGames, customNonGames ->
            InstalledAppsInput(savedOrder, hiddenPackages, customGames, customNonGames)
        },
        webSectionPrefsFlow,
        combine(
            preferences.showOnlyTvAppsFlow,
            preferences.lockedAppsFlow,
            preferences.activeWebBookmarksFlow
        ) { tvOnly, lockedPackages, bookmarks ->
            Triple(tvOnly, lockedPackages, bookmarks)
        }
    ) { input, (customWebApps, customNonWebApps), (tvOnly, lockedPackages, bookmarks) ->
        val apps = loadInstalledApps(
            savedOrder = input.savedOrder,
            hiddenPackages = input.hiddenPackages,
            customGames = input.customGames,
            customNonGames = input.customNonGames,
            customWebApps = customWebApps,
            customNonWebApps = customNonWebApps
        )
        val bookmarkItems = bookmarks
            .filter { !input.hiddenPackages.contains("com.bookmark.${it.id}.portal") }
            .map {
                webBookmarkToAppItem(
                    bookmark = it,
                    hiddenPackages = input.hiddenPackages,
                    customGames = input.customGames,
                    customNonGames = input.customNonGames,
                    customWebApps = customWebApps,
                    customNonWebApps = customNonWebApps,
                    lockedPackages = lockedPackages
                )
            }
        val combined = if (bookmarkItems.isEmpty()) apps else apps + bookmarkItems
        val filtered = if (tvOnly) {
            combined.filter { it.banner != null || it.packageName.endsWith(".portal") }
        } else {
            combined
        }
        val withLocks = filtered.map { it.copy(isLocked = lockedPackages.contains(it.packageName)) }
        if (bookmarkItems.isEmpty() || input.savedOrder.isEmpty()) {
            withLocks
        } else {
            val orderMap = input.savedOrder.withIndex().associate { it.value to it.index }
            withLocks.sortedWith(
                compareBy(
                    {
                        when (it.packageName) {
                            "com.minidownloader.portal" -> orderMap[it.packageName] ?: -3
                            "com.aurastore.portal" -> orderMap[it.packageName] ?: -2
                            "com.bookmark.preset_youtube.portal" -> orderMap[it.packageName] ?: -1
                            else -> orderMap[it.packageName] ?: Int.MAX_VALUE
                        }
                    },
                    { it.label.lowercase() }
                )
            )
        }
    }.flowOn(Dispatchers.Default)

    private data class InstalledAppsInput(
        val savedOrder: List<String>,
        val hiddenPackages: Set<String>,
        val customGames: Set<String>,
        val customNonGames: Set<String>
    )

    /**
     * Flow of all apps including hidden ones (used in Settings to manage hidden apps).
     */
    val allAppsFlow: Flow<List<AppItem>> = combine(
        combine(
            reloadTrigger,
            preferences.hiddenAppsFlow,
            preferences.customGamesFlow,
            preferences.customNonGamesFlow,
            preferences.lockedAppsFlow
        ) { _, hiddenPackages, customGames, customNonGames, lockedPackages ->
            AllAppsInput(hiddenPackages, customGames, customNonGames, lockedPackages)
        },
        webSectionPrefsFlow,
        preferences.activeWebBookmarksFlow
    ) { input, (customWebApps, customNonWebApps), bookmarks ->
        val installed = withContext(Dispatchers.Default) {
            loadInstalledApps(
                savedOrder = emptyList(),
                hiddenPackages = emptySet(),
                customGames = input.customGames,
                customNonGames = input.customNonGames,
                customWebApps = customWebApps,
                customNonWebApps = customNonWebApps,
                includeHidden = true
            ).map {
                it.copy(
                    isHidden = input.hiddenPackages.contains(it.packageName),
                    isLocked = input.lockedPackages.contains(it.packageName)
                )
            }
        }
        installed + bookmarks.map {
            webBookmarkToAppItem(
                bookmark = it,
                hiddenPackages = input.hiddenPackages,
                customGames = input.customGames,
                customNonGames = input.customNonGames,
                customWebApps = customWebApps,
                customNonWebApps = customNonWebApps,
                lockedPackages = input.lockedPackages
            )
        }
    }.flowOn(Dispatchers.Default)

    private data class AllAppsInput(
        val hiddenPackages: Set<String>,
        val customGames: Set<String>,
        val customNonGames: Set<String>,
        val lockedPackages: Set<String>
    )

    private fun checkIfGame(
        resolveInfo: ResolveInfo?,
        packageName: String,
        label: String,
        customGames: Set<String>,
        customNonGames: Set<String>,
        defaultIsGame: Boolean = false
    ): Boolean {
        if (customGames.contains(packageName)) return true
        if (customNonGames.contains(packageName)) return false
        if (resolveInfo == null) return defaultIsGame

        val appInfo = resolveInfo.activityInfo.applicationInfo
        val isGameCategory = appInfo.category == android.content.pm.ApplicationInfo.CATEGORY_GAME
        @Suppress("DEPRECATION")
        val isGameFlag = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_IS_GAME) != 0
        val isGamePkg = packageName.contains(".game", ignoreCase = true) ||
            packageName.contains(".games", ignoreCase = true) ||
            packageName.contains(".arcade", ignoreCase = true) ||
            packageName.contains(".emulator", ignoreCase = true)
        val isGameLabel = label.contains("Game", ignoreCase = true) ||
            label.contains("لعبة", ignoreCase = true)

        return isGameCategory || isGameFlag || isGamePkg || isGameLabel
    }

    private fun seedPortalAppsIntoCache() {
        packageCache.remove("com.miniyube.portal")
        packageCache.remove("com.youtube.web.portal")
        packageCache.remove("com.crazygames.portal")
        packageCache.remove("com.poki.portal")
        packageCache.remove("com.playhop.portal")
        packageCache.remove("com.easygames.portal")
        val portalPackages = listOf(
            Triple("com.minidownloader.portal", "Mini Downloader", false),
            Triple("com.aurastore.portal", "Salsa Store", false)
        )
        for ((pkg, label, defaultIsGame) in portalPackages) {
            if (!packageCache.containsKey(pkg)) {
                packageCache[pkg] = AppItem(
                    packageName = pkg,
                    activityName = "${label}Screen",
                    label = label,
                    icon = null,
                    banner = null,
                    iconBitmap = null,
                    bannerBitmap = null,
                    dominantColor = when (pkg) {
                        "com.minidownloader.portal" -> 0xFFE91E63.toInt()
                        "com.aurastore.portal" -> 0xFF8B5CF6.toInt()
                        else -> 0xFFFBBF24.toInt()
                    },
                    auroraColors = AuroraColors(),
                    isHidden = false,
                    isGame = defaultIsGame
                )
            }
        }
    }

    private suspend fun warmUpPackageCache() = coroutineScope {
        val ownPackage = context.packageName
        val leanbackIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
        }
        val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val leanbackApps = packageManager.queryIntentActivities(leanbackIntent, 0)
        val launcherApps = packageManager.queryIntentActivities(launcherIntent, 0)

        val uniqueResolves = LinkedHashMap<String, ResolveInfo>()
        for (info in leanbackApps) {
            val pkg = info.activityInfo.packageName
            if (pkg != ownPackage) uniqueResolves[pkg] = info
        }
        for (info in launcherApps) {
            val pkg = info.activityInfo.packageName
            if (pkg != ownPackage && !uniqueResolves.containsKey(pkg)) {
                uniqueResolves[pkg] = info
            }
        }

        // Remove uninstalled packages from cache (keeping portals)
        val installedSet = uniqueResolves.keys
        for (cachedPkg in packageCache.keys().toList()) {
            if (!cachedPkg.endsWith(".portal") && !installedSet.contains(cachedPkg)) {
                packageCache.remove(cachedPkg)
            }
        }

        // Load any uncached packages in parallel across CPU cores
        uniqueResolves.entries
            .filter { !packageCache.containsKey(it.key) }
            .map { (pkg, resolveInfo) ->
                async(Dispatchers.Default) {
                    val item = buildAppItem(pkg, resolveInfo)
                    packageCache[pkg] = item
                }
            }
            .awaitAll()
        packageCacheDirty.set(false)
    }

    private fun buildAppItem(pkg: String, resolveInfo: ResolveInfo): AppItem {
        val label = resolveInfo.loadLabel(packageManager)?.toString() ?: pkg
        val banner = resolveInfo.activityInfo.loadBanner(packageManager)
            ?: resolveInfo.activityInfo.applicationInfo.loadBanner(packageManager)
        val icon = resolveInfo.loadIcon(packageManager)

        val bannerBmp = banner?.let { drawableToBitmap(it, maxWidth = 320, maxHeight = 180) }
        val iconBmp = icon?.let { drawableToBitmap(it, maxWidth = 96, maxHeight = 96) }

        val sampleBmp = bannerBmp ?: iconBmp
        val (dominantColor, aurora) = sampleBmp?.let { extractColorAndAuroraFast(it) }
            ?: (0xFFFBBF24.toInt() to AuroraColors())

        val isGame = checkIfGame(resolveInfo, pkg, label, emptySet(), emptySet())

        return AppItem(
            packageName = pkg,
            activityName = resolveInfo.activityInfo.name,
            label = label,
            icon = icon,
            banner = banner,
            iconBitmap = iconBmp?.asImageBitmap(),
            bannerBitmap = bannerBmp?.asImageBitmap(),
            dominantColor = dominantColor,
            auroraColors = aurora,
            isHidden = false,
            isGame = isGame
        )
    }

    private suspend fun loadInstalledApps(
        savedOrder: List<String>,
        hiddenPackages: Set<String>,
        customGames: Set<String> = emptySet(),
        customNonGames: Set<String> = emptySet(),
        customWebApps: Set<String> = emptySet(),
        customNonWebApps: Set<String> = emptySet(),
        includeHidden: Boolean = false
    ): List<AppItem> {
        seedPortalAppsIntoCache()
        if (packageCacheDirty.get() || packageCache.size <= 2) {
            warmUpPackageCache()
        }

        val portalDefaultGame = mapOf(
            "com.minidownloader.portal" to false,
            "com.aurastore.portal" to false
        )
        val portalDefaultWeb = emptySet<String>()

        val allList = packageCache.values.mapNotNull { cached ->
            val pkg = cached.packageName
            val isHidden = hiddenPackages.contains(pkg)
            if (!includeHidden && isHidden) return@mapNotNull null

            val isGame = when {
                customGames.contains(pkg) -> true
                customWebApps.contains(pkg) -> false
                customNonGames.contains(pkg) -> false
                portalDefaultGame.containsKey(pkg) -> portalDefaultGame[pkg] ?: false
                else -> cached.isGame
            }
            val isWeb = when {
                isGame -> false
                customWebApps.contains(pkg) -> true
                customNonWebApps.contains(pkg) -> false
                portalDefaultWeb.contains(pkg) -> true
                else -> false
            }

            cached.copy(
                isHidden = isHidden,
                isGame = isGame,
                isWeb = isWeb
            )
        }

        // Apply saved order:
        if (savedOrder.isEmpty()) {
            return allList.sortedWith(
                compareBy(
                    { app ->
                        val isTvApp = app.banner != null || app.packageName.endsWith(".portal")
                        when {
                            app.packageName == "com.minidownloader.portal" -> 0
                            app.packageName == "com.aurastore.portal" -> 1
                            isTvApp -> 2
                            else -> 3
                        }
                    },
                    { it.label.lowercase() }
                )
            )
        }

        val orderMap = savedOrder.withIndex().associate { it.value to it.index }
        return allList.sortedWith(
            compareBy(
                {
                    if (it.packageName == "com.minidownloader.portal" || it.packageName == "com.aurastore.portal") {
                        orderMap[it.packageName] ?: (if (it.packageName == "com.minidownloader.portal") -3 else -2)
                    } else {
                        orderMap[it.packageName] ?: Int.MAX_VALUE
                    }
                },
                { it.label.lowercase() }
            )
        )
    }

    /**
     * Single-pass fast Palette extraction (samples 16x16 = 256 pixels max) to compute both
     * dominantColor and AuroraColors in <1ms per icon instead of two full-bitmap scans.
     */
    private fun extractColorAndAuroraFast(bitmap: Bitmap): Pair<Int, AuroraColors> {
        return try {
            val palette = Palette.from(bitmap)
                .resizeBitmapArea(256)
                .maximumColorCount(16)
                .generate()

            val rawColor = palette.getVibrantColor(
                palette.getLightVibrantColor(
                    palette.getDominantColor(
                        palette.getMutedColor(0xFFFBBF24.toInt())
                    )
                )
            )

            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(rawColor, hsv)
            if (hsv[2] < 0.55f) hsv[2] = 0.70f
            if (hsv[1] < 0.40f) hsv[1] = 0.65f
            val dominantColor = android.graphics.Color.HSVToColor(hsv)

            val src = palette.vibrantSwatch?.rgb
                ?: palette.dominantSwatch?.rgb
                ?: palette.lightVibrantSwatch?.rgb
                ?: palette.darkVibrantSwatch?.rgb
                ?: palette.mutedSwatch?.rgb
                ?: return dominantColor to AuroraColors()

            fun shift(h: Float, l: Float? = null, s: Float? = null): Color {
                val hsl = FloatArray(3)
                ColorUtils.colorToHSL(src, hsl)
                hsl[0] = (hsl[0] + h + 360f) % 360f
                l?.let { hsl[2] = it }
                s?.let { hsl[1] = it }
                return Color(ColorUtils.HSLToColor(hsl))
            }

            val aurora = AuroraColors(
                glow = shift(0f, l = 0.58f, s = 0.95f),
                mid = shift(-25f, l = 0.48f, s = 0.85f),
                accent = shift(70f, l = 0.40f, s = 0.75f),
                deep = shift(-10f, l = 0.22f, s = 0.80f),
                base = shift(0f, l = 0.02f, s = 0.40f),
                teal = shift(25f, l = 0.40f, s = 0.75f)
            )

            dominantColor to aurora
        } catch (_: Exception) {
            0xFFFBBF24.toInt() to AuroraColors()
        }
    }

    private fun drawableToBitmap(drawable: Drawable, maxWidth: Int = 128, maxHeight: Int = 128): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            if (bmp.width > maxWidth * 2 || bmp.height > maxHeight * 2) {
                val scale = minOf(maxWidth.toFloat() / bmp.width, maxHeight.toFloat() / bmp.height)
                val targetW = (bmp.width * scale).toInt().coerceAtLeast(1)
                val targetH = (bmp.height * scale).toInt().coerceAtLeast(1)
                return Bitmap.createScaledBitmap(bmp, targetW, targetH, true)
            }
            return bmp
        }
        val w = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth.coerceAtMost(maxWidth) else 96
        val h = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight.coerceAtMost(maxHeight) else 96
        val bitmap = createBitmap(w, h)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun launchApp(app: AppItem) {
        try {
            val intent = packageManager.getLeanbackLaunchIntentForPackage(app.packageName)
                ?: packageManager.getLaunchIntentForPackage(app.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            android.util.Log.w("AppRepository", "Failed to launch app ${app.packageName}", e)
        }
    }

    suspend fun saveNewOrder(apps: List<AppItem>) {
        val packageNames = apps.map { it.packageName }
        preferences.saveAppOrder(packageNames)
        triggerReload()
    }

    suspend fun hideApp(packageName: String) {
        preferences.hideApp(packageName)
        triggerReload()
    }

    suspend fun unhideApp(packageName: String) {
        preferences.unhideApp(packageName)
        triggerReload()
    }

    suspend fun unhideAll() {
        preferences.unhideAllApps()
        triggerReload()
    }

    suspend fun resetOrder() {
        preferences.resetAppOrder()
        triggerReload()
    }
}
