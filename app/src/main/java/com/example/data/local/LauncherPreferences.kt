package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.WebBookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "launcher_prefs")

class LauncherPreferences(private val context: Context) {

    companion object {
        const val DEFAULT_IPTV_URL = "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8"
        val KEY_APP_ORDER = stringPreferencesKey("custom_app_order")
        val KEY_HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val KEY_DYNAMIC_BG = booleanPreferencesKey("dynamic_background_enabled")
        val KEY_CLOCK_24H = booleanPreferencesKey("clock_24h_format")
        val KEY_APPS_PER_ROW = intPreferencesKey("apps_per_row")
        val KEY_IPTV_URL = stringPreferencesKey("iptv_playlist_url")
        val KEY_WALLPAPER_MODE = stringPreferencesKey("wallpaper_mode")
        val KEY_CUSTOM_WALLPAPER_URL = stringPreferencesKey("custom_wallpaper_url")
        val KEY_WALLPAPER_VISIBILITY = intPreferencesKey("wallpaper_visibility_percent")
        val KEY_LANGUAGE = stringPreferencesKey("app_language")
        val KEY_HAS_PROMPTED_LAUNCHER = booleanPreferencesKey("has_prompted_launcher")
        val KEY_HAS_PROMPTED_STORAGE = booleanPreferencesKey("has_prompted_storage")
        val KEY_CUSTOM_GAMES = stringSetPreferencesKey("custom_game_packages")
        val KEY_CUSTOM_NON_GAMES = stringSetPreferencesKey("custom_non_game_packages")
        val KEY_CUSTOM_WEB_APPS = stringSetPreferencesKey("custom_web_packages")
        val KEY_CUSTOM_NON_WEB_APPS = stringSetPreferencesKey("custom_non_web_packages")
        val KEY_ADS_BLOCKER_ENABLED = booleanPreferencesKey("ads_blocker_enabled")
        val KEY_DNS_BLOCKER_MODE = stringPreferencesKey("dns_blocker_mode")
        val KEY_GLASS_OPACITY = intPreferencesKey("glass_opacity_percent")
        val KEY_SHOW_ONLY_TV_APPS = booleanPreferencesKey("show_only_tv_apps")
        val KEY_LOCKED_APPS = stringSetPreferencesKey("locked_apps")
        val KEY_APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
        val KEY_SCREENSAVER_TIMEOUT = stringPreferencesKey("screensaver_timeout")
        val KEY_SCREENSAVER_MODE = stringPreferencesKey("screensaver_mode")
        val KEY_SCREENSAVER_WALLPAPER = stringPreferencesKey("screensaver_wallpaper")
        val KEY_SCREENSAVER_VIDEO = stringPreferencesKey("screensaver_video_id")
        val KEY_APPS_TOP_OFFSET = intPreferencesKey("apps_top_offset_dp")
        val KEY_CLOCK_SHOW_SECONDS = booleanPreferencesKey("clock_show_seconds")
        val KEY_CLOCK_SIZE = stringPreferencesKey("clock_font_size_mode")
        val KEY_DATE_FORMAT_STYLE = stringPreferencesKey("date_format_style")
        val KEY_GREETING_MODE = stringPreferencesKey("greeting_display_mode")
        val KEY_CUSTOM_GREETING_TEXT = stringPreferencesKey("custom_greeting_text")
        val KEY_IPTV_PRESETS_INITIALIZED = booleanPreferencesKey("iptv_presets_initialized")
        val KEY_CUSTOM_SCREENSAVER_VIDEOS = stringSetPreferencesKey("custom_screensaver_videos")
        val KEY_DELETED_SCREENSAVER_PRESETS = stringSetPreferencesKey("deleted_screensaver_presets")
        val KEY_AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        val KEY_LAST_PLAYED_CHANNEL_ID = longPreferencesKey("last_played_channel_id")
        val KEY_AUTO_RESUME_LAST_CHANNEL = booleanPreferencesKey("auto_resume_last_channel")
        val KEY_LOCKED_IPTV_CATEGORIES = stringSetPreferencesKey("locked_iptv_categories")
        val KEY_EPG_URL_OVERRIDE = stringPreferencesKey("epg_url_override")
        val KEY_SHOW_RECENT_APPS = booleanPreferencesKey("show_recent_apps_row")
        val KEY_RECENT_APPS = stringPreferencesKey("recent_app_packages")
        val KEY_CUSTOM_WEB_BOOKMARKS = stringSetPreferencesKey("custom_web_bookmarks")
        val KEY_ENABLED_PRESET_WEB_BOOKMARKS = stringSetPreferencesKey("enabled_preset_web_bookmarks")
        val KEY_DISABLED_PRESET_WEB_BOOKMARKS = stringSetPreferencesKey("disabled_preset_web_bookmarks")
        val KEY_DELETED_PRESET_WEB_BOOKMARKS = stringSetPreferencesKey("deleted_preset_web_bookmarks")
        val KEY_PRESET_WEB_BOOKMARK_ICONS = stringSetPreferencesKey("preset_web_bookmark_icons")
        val KEY_USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_material_color")
        val KEY_SLIDESHOW_SOURCE = stringPreferencesKey("slideshow_source")
        val KEY_SLIDESHOW_FOLDER_URI = stringPreferencesKey("slideshow_folder_uri")
        val KEY_SLIDESHOW_CUSTOM_PHOTOS = stringSetPreferencesKey("slideshow_custom_photos")
        val KEY_SLIDESHOW_INTERVAL_SEC = intPreferencesKey("slideshow_interval_sec")
        val KEY_SLIDESHOW_MOTION = stringPreferencesKey("slideshow_motion")
        val KEY_SLIDESHOW_TRANSITION = stringPreferencesKey("slideshow_transition")
        val KEY_SLIDESHOW_EFFECT = stringPreferencesKey("slideshow_visual_effect")
        val KEY_SLIDESHOW_CLOCK_MODE = stringPreferencesKey("slideshow_clock_mode")
        val KEY_SLIDESHOW_MUSIC_MODE = stringPreferencesKey("slideshow_music_mode")
        val KEY_SLIDESHOW_CUSTOM_MUSIC_URI = stringPreferencesKey("slideshow_custom_music_uri")
        val KEY_SLIDESHOW_MUSIC_VOLUME = intPreferencesKey("slideshow_music_volume")
        val KEY_SLIDESHOW_SHUFFLE = booleanPreferencesKey("slideshow_shuffle")
        val KEY_SLIDESHOW_DIM_LEVEL = intPreferencesKey("slideshow_dim_level")
        val KEY_BROADCAST_TEXT_LINES = stringPreferencesKey("broadcast_text_lines")
        val KEY_BROADCAST_LINE_DURATION_SEC = intPreferencesKey("broadcast_line_duration_sec")
        val KEY_BROADCAST_MUSIC_MODE = stringPreferencesKey("broadcast_music_mode")
        val KEY_BROADCAST_CUSTOM_MUSIC_URI = stringPreferencesKey("broadcast_custom_music_uri")
        val KEY_BROADCAST_MUSIC_VOLUME = intPreferencesKey("broadcast_music_volume")
        val KEY_HEADER_CONTROL_MODE = stringPreferencesKey("header_control_mode")
        val KEY_RECENT_CHANNELS = stringPreferencesKey("recent_iptv_channel_ids")
        val KEY_LOW_RAM_MODE = booleanPreferencesKey("low_ram_lite_mode")
        val KEY_NIGHT_COMFORT_MODE = stringPreferencesKey("night_comfort_mode")
        val KEY_DELETED_ONLINE_WALLPAPERS = stringSetPreferencesKey("deleted_online_wallpapers")
        val KEY_CUSTOM_ONLINE_WALLPAPERS = stringSetPreferencesKey("custom_online_wallpapers")
        val KEY_WEATHER_ENABLED = booleanPreferencesKey("weather_home_enabled")
        val KEY_NEWS_VOICE_ENABLED = booleanPreferencesKey("news_voice_reader_enabled")
        val KEY_ECONOMIC_INDICES = stringPreferencesKey("economic_selected_indices")
        val KEY_ECONOMIC_COMMODITIES = stringPreferencesKey("economic_selected_commodities")
        val KEY_ECONOMIC_FOREX = stringPreferencesKey("economic_selected_forex")
        val KEY_FOOTBALL_LEAGUES = stringPreferencesKey("football_selected_leagues")
        val KEY_FOOTBALL_CLUBS = stringPreferencesKey("football_favorite_clubs")
        val DEFAULT_PRESET_VIDEO_IDS = listOf("EG_g7BFaL5s", "TtLeec_DR-Y", "8xXHeRnLwNQ")
        val PRESET_WEB_BOOKMARKS = listOf(
            WebBookmark(id = "preset_youtube", title = "YouTube", url = "https://www.youtube.com/tv", iconUrl = "res://ic_youtube_web_banner", isEnabled = true, isPreset = true, defaultIsGame = false, defaultEnabled = true),
            WebBookmark(id = "preset_google", title = "Google", url = "https://www.google.com", iconUrl = "res://ic_google_web_banner", isEnabled = true, isPreset = true, defaultIsGame = false, defaultEnabled = true),
            WebBookmark(id = "preset_crazygames", title = "CrazyGames", url = "https://www.crazygames.com", iconUrl = "res://ic_crazygames_banner", isEnabled = true, isPreset = true, defaultIsGame = true, defaultEnabled = true),
            WebBookmark(id = "preset_poki", title = "Poki", url = "https://poki.com/ar", iconUrl = "res://ic_poki_banner", isEnabled = true, isPreset = true, defaultIsGame = true, defaultEnabled = true),
            WebBookmark(id = "preset_playhop", title = "Playhop", url = "https://playhop.com", iconUrl = "res://ic_playhop_banner", isEnabled = true, isPreset = true, defaultIsGame = true, defaultEnabled = true),
            WebBookmark(id = "preset_easygames", title = "EasyGames", url = "https://www.easygames.net", iconUrl = "res://ic_easygames_banner", isEnabled = true, isPreset = true, defaultIsGame = true, defaultEnabled = true)
        )

        fun isPresetWebBookmarkId(id: String): Boolean = PRESET_WEB_BOOKMARKS.any { it.id == id }
    }

    private inline fun <T> prefFlow(crossinline transform: (Preferences) -> T): Flow<T> =
        context.dataStore.data.map { transform(it) }.distinctUntilChanged()

    val showOnlyTvAppsFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_SHOW_ONLY_TV_APPS] ?: true
    }

    suspend fun setShowOnlyTvApps(tvOnly: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_ONLY_TV_APPS] = tvOnly
        }
    }

    val glassOpacityFlow: Flow<Int> = prefFlow { prefs ->
        prefs[KEY_GLASS_OPACITY] ?: 75
    }

    suspend fun setGlassOpacity(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GLASS_OPACITY] = percent.coerceIn(25, 100)
        }
    }

    val hasPromptedLauncherFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_HAS_PROMPTED_LAUNCHER] ?: false
    }

    suspend fun setHasPromptedLauncher(hasPrompted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_PROMPTED_LAUNCHER] = hasPrompted
        }
    }

    val hasPromptedStorageFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_HAS_PROMPTED_STORAGE] ?: false
    }

    suspend fun setHasPromptedStorage(hasPrompted: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HAS_PROMPTED_STORAGE] = hasPrompted
        }
    }

    val appOrderFlow: Flow<List<String>> = prefFlow { prefs ->
        val raw = prefs[KEY_APP_ORDER] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }
    }

    val hiddenAppsFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_HIDDEN_APPS] ?: emptySet()
    }

    val isDynamicBackgroundEnabled: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_DYNAMIC_BG] ?: true
    }

    val wallpaperModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_WALLPAPER_MODE] ?: "dynamic"
    }

    val customWallpaperUrlFlow: Flow<String> = prefFlow { prefs ->
        val url = prefs[KEY_CUSTOM_WALLPAPER_URL] ?: ""
        if (url == "aurora_gradient" || url.contains("1543946207")) "" else url
    }

    val wallpaperVisibilityFlow: Flow<Int> = prefFlow { prefs ->
        val raw = prefs[KEY_WALLPAPER_VISIBILITY] ?: 100
        when {
            raw in setOf(100, 90, 80, 70, 60, 50, 40, 30, 20, 10) -> raw
            else -> ((raw.coerceIn(10, 100) + 5) / 10) * 10
        }
    }

    suspend fun setWallpaperVisibility(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLPAPER_VISIBILITY] = percent.coerceIn(10, 100)
        }
    }

    val isClock24h: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_CLOCK_24H] ?: true
    }

    val appsPerRowFlow: Flow<Int> = prefFlow { prefs ->
        prefs[KEY_APPS_PER_ROW] ?: 5
    }

    val appsTopOffsetFlow: Flow<Int> = prefFlow { prefs ->
        when (val raw = prefs[KEY_APPS_TOP_OFFSET] ?: 450) {
            in setOf(250, 300, 350, 400, 450, 500) -> raw
            100, 220 -> 250
            280 -> 300
            340 -> 400
            700, 900 -> 450
            else -> 450
        }
    }

    suspend fun setAppsTopOffset(offsetDp: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APPS_TOP_OFFSET] = offsetDp
        }
    }

    val isClockSecondsEnabled: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_CLOCK_SHOW_SECONDS] ?: false
    }

    suspend fun setClockSecondsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOCK_SHOW_SECONDS] = enabled
        }
    }

    val clockSizeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_CLOCK_SIZE] ?: "large"
    }

    suspend fun setClockSize(size: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOCK_SIZE] = size
        }
    }

    val dateFormatStyleFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_DATE_FORMAT_STYLE] ?: "full"
    }

    suspend fun setDateFormatStyle(style: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DATE_FORMAT_STYLE] = style
        }
    }

    val greetingModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_GREETING_MODE] ?: "poetic"
    }

    suspend fun setGreetingMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_GREETING_MODE] = mode
        }
    }

    val customGreetingTextFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_CUSTOM_GREETING_TEXT] ?: ""
    }

    suspend fun setCustomGreetingText(text: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_GREETING_TEXT] = text
        }
    }

    val iptvPlaylistUrlFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_IPTV_URL] ?: DEFAULT_IPTV_URL
    }

    private val quickPrefs by lazy {
        context.applicationContext.getSharedPreferences("salsa_quick_prefs", Context.MODE_PRIVATE)
    }

    fun getCachedLanguage(): String {
        return quickPrefs.getString("app_language", null) ?: "system"
    }

    val languageFlow: Flow<String> = prefFlow { prefs ->
        val lang = prefs[KEY_LANGUAGE] ?: "system"
        if (quickPrefs.getString("app_language", null) != lang) {
            quickPrefs.edit().putString("app_language", lang).apply()
        }
        lang
    }

    suspend fun saveAppOrder(orderList: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_ORDER] = orderList.joinToString(",")
        }
    }

    suspend fun hideApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
            current.add(packageName)
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun unhideApp(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
            current.remove(packageName)
            prefs[KEY_HIDDEN_APPS] = current
        }
    }

    suspend fun unhideAllApps() {
        context.dataStore.edit { prefs ->
            prefs[KEY_HIDDEN_APPS] = emptySet()
        }
    }

    suspend fun resetAppOrder() {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_ORDER] = ""
        }
    }

    suspend fun setDynamicBackgroundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_BG] = enabled
        }
    }

    suspend fun setClock24h(is24h: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CLOCK_24H] = is24h
        }
    }

    suspend fun setAppsPerRow(count: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APPS_PER_ROW] = count
        }
    }

    suspend fun setIptvPlaylistUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IPTV_URL] = url
        }
    }

    suspend fun setWallpaperMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WALLPAPER_MODE] = mode
        }
    }

    suspend fun setCustomWallpaperUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_WALLPAPER_URL] = url
        }
    }

    suspend fun setLanguage(lang: String) {
        quickPrefs.edit().putString("app_language", lang).apply()
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = lang
        }
    }

    val customGamesFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_CUSTOM_GAMES] ?: emptySet()
    }

    val customNonGamesFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_CUSTOM_NON_GAMES] ?: emptySet()
    }

    val customWebAppsFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_CUSTOM_WEB_APPS] ?: emptySet()
    }

    val customNonWebAppsFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_CUSTOM_NON_WEB_APPS] ?: emptySet()
    }

    suspend fun setAppSection(packageName: String, section: com.example.data.model.AppSection) {
        setAppSectionsBatch(mapOf(packageName to section))
    }

    suspend fun setAppSectionsBatch(changes: Map<String, com.example.data.model.AppSection>) {
        if (changes.isEmpty()) return
        context.dataStore.edit { prefs ->
            val games = (prefs[KEY_CUSTOM_GAMES] ?: emptySet()).toMutableSet()
            val nonGames = (prefs[KEY_CUSTOM_NON_GAMES] ?: emptySet()).toMutableSet()
            val webApps = (prefs[KEY_CUSTOM_WEB_APPS] ?: emptySet()).toMutableSet()
            val nonWebApps = (prefs[KEY_CUSTOM_NON_WEB_APPS] ?: emptySet()).toMutableSet()
            for ((packageName, section) in changes) {
                when (section) {
                    com.example.data.model.AppSection.GAMES -> {
                        games.add(packageName)
                        nonGames.remove(packageName)
                        webApps.remove(packageName)
                        nonWebApps.add(packageName)
                    }
                    com.example.data.model.AppSection.WEB -> {
                        webApps.add(packageName)
                        nonWebApps.remove(packageName)
                        games.remove(packageName)
                        nonGames.add(packageName)
                    }
                    com.example.data.model.AppSection.APPS -> {
                        nonGames.add(packageName)
                        games.remove(packageName)
                        nonWebApps.add(packageName)
                        webApps.remove(packageName)
                    }
                }
            }
            prefs[KEY_CUSTOM_GAMES] = games
            prefs[KEY_CUSTOM_NON_GAMES] = nonGames
            prefs[KEY_CUSTOM_WEB_APPS] = webApps
            prefs[KEY_CUSTOM_NON_WEB_APPS] = nonWebApps
        }
    }

    suspend fun setAppIsGame(packageName: String, isGame: Boolean) {
        setAppSection(
            packageName,
            if (isGame) com.example.data.model.AppSection.GAMES else com.example.data.model.AppSection.APPS
        )
    }
    val dnsBlockerModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_DNS_BLOCKER_MODE] ?: "off"
    }

    suspend fun setDnsBlockerMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DNS_BLOCKER_MODE] = mode
            // Also sync legacy enabled key for safety
            prefs[KEY_ADS_BLOCKER_ENABLED] = (mode != "off")
        }
    }

    val lockedAppsFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_LOCKED_APPS] ?: emptySet()
    }

    val appLockPinFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_APP_LOCK_PIN] ?: "1234"
    }

    suspend fun setAppLocked(packageName: String, locked: Boolean) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_LOCKED_APPS] ?: emptySet()).toMutableSet()
            if (locked) current.add(packageName) else current.remove(packageName)
            prefs[KEY_LOCKED_APPS] = current
        }
    }

    suspend fun setAppLockPin(pin: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_APP_LOCK_PIN] = pin
        }
    }

    val screensaverTimeoutFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SCREENSAVER_TIMEOUT] ?: "5"
    }

    suspend fun setScreensaverTimeout(timeout: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCREENSAVER_TIMEOUT] = timeout
        }
    }

    val screensaverModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SCREENSAVER_MODE] ?: "clock_date"
    }

    suspend fun setScreensaverMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCREENSAVER_MODE] = mode
        }
    }

    val screensaverWallpaperFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SCREENSAVER_WALLPAPER] ?: "current"
    }

    suspend fun setScreensaverWallpaper(wallpaper: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCREENSAVER_WALLPAPER] = wallpaper
        }
    }

    val screensaverVideoFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SCREENSAVER_VIDEO] ?: "EG_g7BFaL5s"
    }

    suspend fun setScreensaverVideo(videoId: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SCREENSAVER_VIDEO] = videoId
        }
    }

    val iptvPresetsInitializedFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_IPTV_PRESETS_INITIALIZED] ?: false
    }

    suspend fun setIptvPresetsInitialized(initialized: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IPTV_PRESETS_INITIALIZED] = initialized
        }
    }

    val customScreensaverVideosFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] ?: emptySet()
    }

    val deletedScreensaverPresetsFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_DELETED_SCREENSAVER_PRESETS] ?: emptySet()
    }

    suspend fun addCustomScreensaverVideo(videoId: String, customTitle: String = "") {
        val cleanId = videoId.trim()
        if (cleanId.isBlank()) return
        context.dataStore.edit { prefs ->
            // If it was a deleted preset, restore it
            val deletedPresets = (prefs[KEY_DELETED_SCREENSAVER_PRESETS] ?: emptySet()).toMutableSet()
            if (deletedPresets.remove(cleanId)) {
                prefs[KEY_DELETED_SCREENSAVER_PRESETS] = deletedPresets
            }
            // If not one of the built-in presets, add to custom videos set
            if (!DEFAULT_PRESET_VIDEO_IDS.contains(cleanId)) {
                val currentCustom = (prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] ?: emptySet()).toMutableSet()
                currentCustom.removeAll { it.substringBefore("|") == cleanId }
                val entry = if (customTitle.isNotBlank()) "$cleanId|${customTitle.trim()}" else cleanId
                currentCustom.add(entry)
                prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] = currentCustom
            }
            prefs[KEY_SCREENSAVER_VIDEO] = cleanId
        }
    }

    suspend fun deleteScreensaverVideo(videoId: String) {
        val cleanId = videoId.trim()
        context.dataStore.edit { prefs ->
            val currentCustom = (prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] ?: emptySet()).toMutableSet()
            currentCustom.removeAll { it.substringBefore("|") == cleanId }
            prefs[KEY_CUSTOM_SCREENSAVER_VIDEOS] = currentCustom

            val deletedPresets = (prefs[KEY_DELETED_SCREENSAVER_PRESETS] ?: emptySet()).toMutableSet()
            if (DEFAULT_PRESET_VIDEO_IDS.contains(cleanId)) {
                deletedPresets.add(cleanId)
                prefs[KEY_DELETED_SCREENSAVER_PRESETS] = deletedPresets
            }

            val currentSelected = prefs[KEY_SCREENSAVER_VIDEO] ?: "EG_g7BFaL5s"
            if (currentSelected == cleanId) {
                val remainingPresets = DEFAULT_PRESET_VIDEO_IDS.filterNot { deletedPresets.contains(it) }
                val remainingCustom = currentCustom.map { it.substringBefore("|") }
                val nextVideo = (remainingCustom + remainingPresets).firstOrNull() ?: ""
                prefs[KEY_SCREENSAVER_VIDEO] = nextVideo
            }
        }
    }

    suspend fun restoreDefaultScreensaverVideos() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DELETED_SCREENSAVER_PRESETS] = emptySet()
            val currentSelected = prefs[KEY_SCREENSAVER_VIDEO] ?: ""
            if (currentSelected.isBlank()) {
                prefs[KEY_SCREENSAVER_VIDEO] = "EG_g7BFaL5s"
            }
        }
    }

    val autoCheckUpdatesFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_AUTO_CHECK_UPDATES] ?: true
    }

    suspend fun setAutoCheckUpdates(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_CHECK_UPDATES] = enabled
        }
    }

    val lastPlayedChannelIdFlow: Flow<Long> = prefFlow { prefs ->
        prefs[KEY_LAST_PLAYED_CHANNEL_ID] ?: -1L
    }

    val recentChannelIdsFlow: Flow<List<Long>> = prefFlow { prefs ->
        val raw = prefs[KEY_RECENT_CHANNELS] ?: ""
        if (raw.isBlank()) emptyList()
        else raw.split(",").mapNotNull { it.trim().toLongOrNull() }.filter { it > 0L }
    }

    suspend fun setLastPlayedChannelId(channelId: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_PLAYED_CHANNEL_ID] = channelId
            if (channelId > 0L) {
                val existing = (prefs[KEY_RECENT_CHANNELS] ?: "")
                    .split(",")
                    .mapNotNull { it.trim().toLongOrNull() }
                    .filter { it > 0L && it != channelId }
                val updated = (listOf(channelId) + existing).take(12)
                prefs[KEY_RECENT_CHANNELS] = updated.joinToString(",")
            }
        }
    }

    val autoResumeLastChannelFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_AUTO_RESUME_LAST_CHANNEL] ?: false
    }

    suspend fun setAutoResumeLastChannel(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_RESUME_LAST_CHANNEL] = enabled
        }
    }

    val lockedIptvCategoriesFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_LOCKED_IPTV_CATEGORIES] ?: emptySet()
    }

    suspend fun setIptvCategoryLocked(category: String, locked: Boolean) {
        val clean = category.trim()
        if (clean.isBlank() || clean == "Favorites" || clean == "All") return
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_LOCKED_IPTV_CATEGORIES] ?: emptySet()).toMutableSet()
            if (locked) current.add(clean) else current.remove(clean)
            prefs[KEY_LOCKED_IPTV_CATEGORIES] = current
        }
    }

    val epgUrlOverrideFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_EPG_URL_OVERRIDE] ?: ""
    }

    suspend fun setEpgUrlOverride(url: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EPG_URL_OVERRIDE] = url.trim()
        }
    }

    val showRecentAppsFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_SHOW_RECENT_APPS] ?: false
    }

    suspend fun setShowRecentApps(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SHOW_RECENT_APPS] = enabled
        }
    }

    val recentAppsFlow: Flow<List<String>> = prefFlow { prefs ->
        val raw = prefs[KEY_RECENT_APPS] ?: ""
        if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    suspend fun recordRecentApp(packageName: String) {
        val clean = packageName.trim()
        if (clean.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_RECENT_APPS] ?: "")
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() && it != clean }
            val updated = (listOf(clean) + current).take(6)
            prefs[KEY_RECENT_APPS] = updated.joinToString(",")
        }
    }

    val customWebBookmarksFlow: Flow<List<WebBookmark>> = prefFlow { prefs ->
        val rawSet = prefs[KEY_CUSTOM_WEB_BOOKMARKS] ?: emptySet()
        val disabledIds = prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()
        rawSet.mapNotNull { entry ->
            val parts = entry.split("|", limit = 4)
            if (parts.size >= 3 && parts[0].isNotBlank() && parts[1].isNotBlank() && parts[2].isNotBlank()) {
                WebBookmark(
                    id = parts[0],
                    title = parts[1],
                    url = parts[2],
                    iconUrl = parts.getOrElse(3) { "" },
                    isEnabled = !disabledIds.contains(parts[0]),
                    isPreset = false
                )
            } else null
        }.sortedBy { it.title.lowercase() }
    }

    private fun isPresetBookmarkActive(
        preset: WebBookmark,
        enabledIds: Set<String>,
        disabledIds: Set<String>,
        deletedIds: Set<String>
    ): Boolean {
        if (deletedIds.contains(preset.id)) return false
        return if (preset.defaultEnabled) {
            !disabledIds.contains(preset.id)
        } else {
            enabledIds.contains(preset.id)
        }
    }

    val deletedPresetWebBookmarksFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_DELETED_PRESET_WEB_BOOKMARKS] ?: emptySet()
    }

    val presetWebBookmarksFlow: Flow<List<WebBookmark>> = prefFlow { prefs ->
        val enabledIds = prefs[KEY_ENABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()
        val disabledIds = prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()
        val deletedIds = prefs[KEY_DELETED_PRESET_WEB_BOOKMARKS] ?: emptySet()
        val iconMap = (prefs[KEY_PRESET_WEB_BOOKMARK_ICONS] ?: emptySet()).associate { entry ->
            entry.substringBefore("|") to entry.substringAfter("|", "")
        }
        PRESET_WEB_BOOKMARKS
            .filter { !deletedIds.contains(it.id) }
            .map { preset ->
                val resolvedIcon = preset.iconUrl.ifBlank { iconMap[preset.id].orEmpty() }
                preset.copy(
                    iconUrl = resolvedIcon,
                    isEnabled = isPresetBookmarkActive(preset, enabledIds, disabledIds, deletedIds),
                    isPreset = true
                )
            }
    }

    val activeWebBookmarksFlow: Flow<List<WebBookmark>> = prefFlow { prefs ->
        val enabledIds = prefs[KEY_ENABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()
        val disabledIds = prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()
        val deletedIds = prefs[KEY_DELETED_PRESET_WEB_BOOKMARKS] ?: emptySet()

        val rawSet = prefs[KEY_CUSTOM_WEB_BOOKMARKS] ?: emptySet()
        val custom = rawSet.mapNotNull { entry ->
            val parts = entry.split("|", limit = 4)
            if (
                parts.size >= 3 &&
                parts[0].isNotBlank() &&
                parts[1].isNotBlank() &&
                parts[2].isNotBlank() &&
                !disabledIds.contains(parts[0])
            ) {
                WebBookmark(
                    id = parts[0],
                    title = parts[1],
                    url = parts[2],
                    iconUrl = parts.getOrElse(3) { "" },
                    isEnabled = true,
                    isPreset = false
                )
            } else null
        }.sortedBy { it.title.lowercase() }

        val iconMap = (prefs[KEY_PRESET_WEB_BOOKMARK_ICONS] ?: emptySet()).associate { entry ->
            entry.substringBefore("|") to entry.substringAfter("|", "")
        }
        val enabledPresets = PRESET_WEB_BOOKMARKS
            .filter { isPresetBookmarkActive(it, enabledIds, disabledIds, deletedIds) }
            .map { preset ->
                val resolvedIcon = preset.iconUrl.ifBlank { iconMap[preset.id].orEmpty() }
                preset.copy(
                    iconUrl = resolvedIcon,
                    isEnabled = true,
                    isPreset = true
                )
            }

        enabledPresets + custom
    }

    suspend fun setPresetWebBookmarkEnabled(id: String, enabled: Boolean) {
        val cleanId = id.trim()
        if (cleanId.isBlank()) return
        context.dataStore.edit { prefs ->
            val enabledSet = (prefs[KEY_ENABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            val disabledSet = (prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            val deletedSet = (prefs[KEY_DELETED_PRESET_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            if (enabled) {
                enabledSet.add(cleanId)
                disabledSet.remove(cleanId)
                deletedSet.remove(cleanId)
                val pkg = "com.bookmark.$cleanId.portal"
                val hidden = (prefs[KEY_HIDDEN_APPS] ?: emptySet()).toMutableSet()
                if (hidden.remove(pkg)) {
                    prefs[KEY_HIDDEN_APPS] = hidden
                }
            } else {
                enabledSet.remove(cleanId)
                disabledSet.add(cleanId)
            }
            prefs[KEY_ENABLED_PRESET_WEB_BOOKMARKS] = enabledSet
            prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] = disabledSet
            prefs[KEY_DELETED_PRESET_WEB_BOOKMARKS] = deletedSet
        }
    }

    suspend fun deletePresetWebBookmark(id: String) {
        val cleanId = id.trim()
        if (!isPresetWebBookmarkId(cleanId)) return
        context.dataStore.edit { prefs ->
            val enabledSet = (prefs[KEY_ENABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            val disabledSet = (prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            val deletedSet = (prefs[KEY_DELETED_PRESET_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            enabledSet.remove(cleanId)
            disabledSet.add(cleanId)
            deletedSet.add(cleanId)
            prefs[KEY_ENABLED_PRESET_WEB_BOOKMARKS] = enabledSet
            prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] = disabledSet
            prefs[KEY_DELETED_PRESET_WEB_BOOKMARKS] = deletedSet
        }
    }

    suspend fun restoreDeletedPresetWebBookmarks() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DELETED_PRESET_WEB_BOOKMARKS] = emptySet()
        }
    }

    suspend fun addCustomWebBookmark(title: String, url: String, iconUrl: String = ""): String {
        val cleanTitle = title.trim().replace("|", " ")
        var cleanUrl = url.trim()
        if (cleanTitle.isBlank() || cleanUrl.isBlank()) return ""
        if (!cleanUrl.startsWith("http://", ignoreCase = true) &&
            !cleanUrl.startsWith("https://", ignoreCase = true)
        ) {
            cleanUrl = "https://$cleanUrl"
        }
        val cleanIcon = iconUrl.trim().replace("|", "%7C")
        val id = System.currentTimeMillis().toString(36)
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_CUSTOM_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            current.add(if (cleanIcon.isNotBlank()) "$id|$cleanTitle|$cleanUrl|$cleanIcon" else "$id|$cleanTitle|$cleanUrl")
            prefs[KEY_CUSTOM_WEB_BOOKMARKS] = current
        }
        return id
    }

    suspend fun updateCustomWebBookmarkIcon(id: String, iconUrl: String) {
        val cleanId = id.trim()
        val cleanIcon = iconUrl.trim().replace("|", "%7C")
        if (cleanId.isBlank() || cleanIcon.isBlank()) return
        context.dataStore.edit { prefs ->
            if (isPresetWebBookmarkId(cleanId)) {
                val icons = (prefs[KEY_PRESET_WEB_BOOKMARK_ICONS] ?: emptySet()).toMutableSet()
                icons.removeAll { it.substringBefore("|") == cleanId }
                icons.add("$cleanId|$cleanIcon")
                prefs[KEY_PRESET_WEB_BOOKMARK_ICONS] = icons
                return@edit
            }
            val current = (prefs[KEY_CUSTOM_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            val existing = current.firstOrNull { it.substringBefore("|") == cleanId } ?: return@edit
            val parts = existing.split("|", limit = 4)
            if (parts.size >= 3) {
                current.remove(existing)
                current.add("${parts[0]}|${parts[1]}|${parts[2]}|$cleanIcon")
                prefs[KEY_CUSTOM_WEB_BOOKMARKS] = current
            }
        }
    }

    suspend fun removeCustomWebBookmark(id: String) {
        val cleanId = id.trim()
        if (cleanId.isBlank()) return
        if (isPresetWebBookmarkId(cleanId)) {
            deletePresetWebBookmark(cleanId)
            return
        }
        com.example.util.WebBookmarkIconResolver.deleteCachedIcon(context, cleanId)
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_CUSTOM_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            current.removeAll { it.substringBefore("|") == cleanId }
            prefs[KEY_CUSTOM_WEB_BOOKMARKS] = current

            val enabledSet = (prefs[KEY_ENABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            val disabledSet = (prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] ?: emptySet()).toMutableSet()
            if (enabledSet.remove(cleanId)) prefs[KEY_ENABLED_PRESET_WEB_BOOKMARKS] = enabledSet
            if (disabledSet.remove(cleanId)) prefs[KEY_DISABLED_PRESET_WEB_BOOKMARKS] = disabledSet
        }
    }

    val useDynamicColorFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_USE_DYNAMIC_COLOR] ?: false
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USE_DYNAMIC_COLOR] = enabled
        }
    }

    // --- Photo Slideshow Screensaver Preferences ---

    val slideshowSourceFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_SOURCE] ?: "online_all"
    }

    suspend fun setSlideshowSource(source: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_SOURCE] = source
        }
    }

    val slideshowFolderUriFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_FOLDER_URI] ?: ""
    }

    suspend fun setSlideshowFolderUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_FOLDER_URI] = uri.trim()
        }
    }

    val slideshowCustomPhotosFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_CUSTOM_PHOTOS] ?: emptySet()
    }

    suspend fun addSlideshowCustomPhotos(uris: Collection<String>) {
        context.dataStore.edit { prefs ->
            val current = (prefs[KEY_SLIDESHOW_CUSTOM_PHOTOS] ?: emptySet()).toMutableSet()
            uris.map { it.trim() }.filter { it.isNotBlank() }.forEach { current.add(it) }
            prefs[KEY_SLIDESHOW_CUSTOM_PHOTOS] = current
        }
    }

    suspend fun clearSlideshowCustomPhotos() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_CUSTOM_PHOTOS] = emptySet()
        }
    }

    val slideshowIntervalSecFlow: Flow<Int> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_INTERVAL_SEC] ?: 10
    }

    suspend fun setSlideshowIntervalSec(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_INTERVAL_SEC] = seconds.coerceIn(3, 600)
        }
    }

    val slideshowMotionFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_MOTION] ?: "ken_burns"
    }

    suspend fun setSlideshowMotion(motion: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_MOTION] = motion
        }
    }

    val slideshowTransitionFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_TRANSITION] ?: "crossfade"
    }

    suspend fun setSlideshowTransition(transition: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_TRANSITION] = transition
        }
    }

    val slideshowEffectFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_EFFECT] ?: "snow"
    }

    suspend fun setSlideshowEffect(effect: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_EFFECT] = effect
        }
    }

    val slideshowClockModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_CLOCK_MODE] ?: "corner_full"
    }

    suspend fun setSlideshowClockMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_CLOCK_MODE] = mode
        }
    }

    val slideshowMusicModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_MUSIC_MODE] ?: "off"
    }

    suspend fun setSlideshowMusicMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_MUSIC_MODE] = mode
        }
    }

    val slideshowCustomMusicUriFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_CUSTOM_MUSIC_URI] ?: ""
    }

    suspend fun setSlideshowCustomMusicUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_CUSTOM_MUSIC_URI] = uri.trim()
        }
    }

    val slideshowMusicVolumeFlow: Flow<Int> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_MUSIC_VOLUME] ?: 50
    }

    suspend fun setSlideshowMusicVolume(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_MUSIC_VOLUME] = percent.coerceIn(10, 100)
        }
    }

    val slideshowShuffleFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_SHUFFLE] ?: true
    }

    suspend fun setSlideshowShuffle(shuffle: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_SHUFFLE] = shuffle
        }
    }

    val slideshowDimLevelFlow: Flow<Int> = prefFlow { prefs ->
        prefs[KEY_SLIDESHOW_DIM_LEVEL] ?: 20
    }

    suspend fun setSlideshowDimLevel(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SLIDESHOW_DIM_LEVEL] = percent.coerceIn(0, 70)
        }
    }

    val broadcastTextLinesFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_BROADCAST_TEXT_LINES] ?: ""
    }

    suspend fun setBroadcastTextLines(text: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BROADCAST_TEXT_LINES] = text
        }
    }

    val broadcastLineDurationSecFlow: Flow<Int> = prefFlow { prefs ->
        prefs[KEY_BROADCAST_LINE_DURATION_SEC] ?: 5
    }

    suspend fun setBroadcastLineDurationSec(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BROADCAST_LINE_DURATION_SEC] = seconds.coerceIn(3, 30)
        }
    }

    val broadcastMusicModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_BROADCAST_MUSIC_MODE] ?: "off"
    }

    suspend fun setBroadcastMusicMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BROADCAST_MUSIC_MODE] = mode
        }
    }

    val broadcastCustomMusicUriFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_BROADCAST_CUSTOM_MUSIC_URI] ?: ""
    }

    suspend fun setBroadcastCustomMusicUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BROADCAST_CUSTOM_MUSIC_URI] = uri.trim()
        }
    }

    val broadcastMusicVolumeFlow: Flow<Int> = prefFlow { prefs ->
        prefs[KEY_BROADCAST_MUSIC_VOLUME] ?: 50
    }

    suspend fun setBroadcastMusicVolume(percent: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_BROADCAST_MUSIC_VOLUME] = percent.coerceIn(10, 100)
        }
    }

    val headerControlModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_HEADER_CONTROL_MODE] ?: "control_center"
    }

    suspend fun setHeaderControlMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HEADER_CONTROL_MODE] = mode
        }
    }

    val lowRamModeFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_LOW_RAM_MODE] ?: run {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            am?.isLowRamDevice == true
        }
    }

    suspend fun setLowRamMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LOW_RAM_MODE] = enabled
        }
    }

    val nightComfortModeFlow: Flow<String> = prefFlow { prefs ->
        prefs[KEY_NIGHT_COMFORT_MODE] ?: "off"
    }

    suspend fun setNightComfortMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NIGHT_COMFORT_MODE] = mode
        }
    }

    val isWeatherEnabledFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_WEATHER_ENABLED] ?: true
    }

    suspend fun setWeatherEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WEATHER_ENABLED] = enabled
        }
    }

    val newsVoiceEnabledFlow: Flow<Boolean> = prefFlow { prefs ->
        prefs[KEY_NEWS_VOICE_ENABLED] ?: false
    }

    suspend fun setNewsVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_NEWS_VOICE_ENABLED] = enabled
        }
    }

    val economicIndicesFlow: Flow<List<String>> = prefFlow { prefs ->
        val raw = prefs[KEY_ECONOMIC_INDICES] ?: "sp500,nasdaq,dow,dxy"
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty {
            listOf("sp500", "nasdaq", "dow", "dxy")
        }
    }

    suspend fun setEconomicIndices(ids: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ECONOMIC_INDICES] = ids.joinToString(",")
        }
    }

    val economicCommoditiesFlow: Flow<List<String>> = prefFlow { prefs ->
        val raw = prefs[KEY_ECONOMIC_COMMODITIES] ?: "gold,brent,btc,eth"
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty {
            listOf("gold", "brent", "btc", "eth")
        }
    }

    suspend fun setEconomicCommodities(ids: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ECONOMIC_COMMODITIES] = ids.joinToString(",")
        }
    }

    val economicForexFlow: Flow<List<String>> = prefFlow { prefs ->
        val raw = prefs[KEY_ECONOMIC_FOREX] ?: "eur_usd,gbp_usd,usd_jpy,usd_sar"
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty {
            listOf("eur_usd", "gbp_usd", "usd_jpy", "usd_sar")
        }
    }

    suspend fun setEconomicForex(ids: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ECONOMIC_FOREX] = ids.joinToString(",")
        }
    }

    val footballLeaguesFlow: Flow<List<String>> = prefFlow { prefs ->
        val raw = prefs[KEY_FOOTBALL_LEAGUES] ?: "premier_league,la_liga,serie_a,champions_league"
        raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.ifEmpty {
            listOf("premier_league", "la_liga", "serie_a", "champions_league")
        }
    }

    suspend fun setFootballLeagues(ids: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FOOTBALL_LEAGUES] = ids.joinToString(",")
        }
    }

    val footballClubsFlow: Flow<Set<String>> = prefFlow { prefs ->
        val raw = prefs[KEY_FOOTBALL_CLUBS] ?: ""
        if (raw.isBlank()) emptySet()
        else raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    suspend fun setFootballClubs(ids: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FOOTBALL_CLUBS] = ids.joinToString(",")
        }
    }

    val deletedOnlineWallpapersFlow: Flow<Set<String>> = prefFlow { prefs ->
        prefs[KEY_DELETED_ONLINE_WALLPAPERS] ?: emptySet()
    }

    suspend fun deleteOnlineWallpaper(wallpaperId: String) {
        context.dataStore.edit { prefs ->
            val currentDeleted = prefs[KEY_DELETED_ONLINE_WALLPAPERS] ?: emptySet()
            prefs[KEY_DELETED_ONLINE_WALLPAPERS] = currentDeleted + wallpaperId
            val currentCustom = prefs[KEY_CUSTOM_ONLINE_WALLPAPERS] ?: emptySet()
            val updatedCustom = currentCustom.filterNot { it.startsWith("$wallpaperId|||") }.toSet()
            if (updatedCustom.size != currentCustom.size) {
                prefs[KEY_CUSTOM_ONLINE_WALLPAPERS] = updatedCustom
            }
        }
    }

    suspend fun restoreDeletedOnlineWallpapers() {
        context.dataStore.edit { prefs ->
            prefs[KEY_DELETED_ONLINE_WALLPAPERS] = emptySet()
        }
    }

    val customOnlineWallpapersFlow: Flow<List<com.example.ui.settings.UnsplashWallpaper>> = prefFlow { prefs ->
        val rawSet = prefs[KEY_CUSTOM_ONLINE_WALLPAPERS] ?: emptySet()
        rawSet.mapNotNull { entry ->
            val parts = entry.split("|||")
            if (parts.size >= 4) {
                com.example.ui.settings.UnsplashWallpaper(
                    id = parts[0],
                    title = parts[1],
                    photoId = parts[3],
                    customThumbUrl = parts[2],
                    customFullUrl = parts[3]
                )
            } else {
                null
            }
        }
    }

    suspend fun addCustomOnlineWallpaper(wallpaper: com.example.ui.settings.UnsplashWallpaper) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_CUSTOM_ONLINE_WALLPAPERS] ?: emptySet()
            val cleanTitle = wallpaper.title.replace("|||", " ").trim().ifBlank { "4K Wallpaper" }
            val serialized = "${wallpaper.id}|||$cleanTitle|||${wallpaper.thumbnailUrl}|||${wallpaper.fullUrl}"
            val filtered = current.filterNot { it.startsWith("${wallpaper.id}|||") }.toSet()
            prefs[KEY_CUSTOM_ONLINE_WALLPAPERS] = filtered + serialized
            val deleted = prefs[KEY_DELETED_ONLINE_WALLPAPERS] ?: emptySet()
            if (wallpaper.id in deleted) {
                prefs[KEY_DELETED_ONLINE_WALLPAPERS] = deleted - wallpaper.id
            }
        }
    }

    suspend fun exportPreferencesSnapshot(): Map<String, Any> {
        val result = linkedMapOf<String, Any>()
        context.dataStore.edit { prefs ->
            for ((key, value) in prefs.asMap()) {
                when (value) {
                    is Boolean, is Int, is Long, is Float, is Double, is String -> result[key.name] = value
                    is Set<*> -> result[key.name] = value.filterIsInstance<String>()
                }
            }
        }
        return result
    }

    suspend fun importPreferencesSnapshot(entries: Map<String, Any?>) {
        context.dataStore.edit { prefs ->
            val stringKeys = listOf(
                KEY_APP_ORDER, KEY_IPTV_URL, KEY_WALLPAPER_MODE, KEY_CUSTOM_WALLPAPER_URL,
                KEY_LANGUAGE, KEY_DNS_BLOCKER_MODE, KEY_APP_LOCK_PIN,
                KEY_SCREENSAVER_TIMEOUT, KEY_SCREENSAVER_MODE, KEY_SCREENSAVER_WALLPAPER,
                KEY_SCREENSAVER_VIDEO, KEY_CLOCK_SIZE, KEY_DATE_FORMAT_STYLE, KEY_GREETING_MODE,
                KEY_CUSTOM_GREETING_TEXT, KEY_EPG_URL_OVERRIDE, KEY_RECENT_APPS,
                KEY_SLIDESHOW_SOURCE, KEY_SLIDESHOW_FOLDER_URI, KEY_SLIDESHOW_MOTION,
                KEY_SLIDESHOW_TRANSITION, KEY_SLIDESHOW_EFFECT, KEY_SLIDESHOW_CLOCK_MODE,
                KEY_SLIDESHOW_MUSIC_MODE, KEY_SLIDESHOW_CUSTOM_MUSIC_URI, KEY_HEADER_CONTROL_MODE,
                KEY_RECENT_CHANNELS, KEY_NIGHT_COMFORT_MODE, KEY_FOOTBALL_LEAGUES, KEY_FOOTBALL_CLUBS,
                KEY_ECONOMIC_INDICES, KEY_ECONOMIC_COMMODITIES, KEY_ECONOMIC_FOREX,
                KEY_BROADCAST_TEXT_LINES, KEY_BROADCAST_MUSIC_MODE, KEY_BROADCAST_CUSTOM_MUSIC_URI
            )
            val boolKeys = listOf(
                KEY_DYNAMIC_BG, KEY_CLOCK_24H, KEY_HAS_PROMPTED_LAUNCHER, KEY_HAS_PROMPTED_STORAGE,
                KEY_ADS_BLOCKER_ENABLED, KEY_SHOW_ONLY_TV_APPS, KEY_CLOCK_SHOW_SECONDS,
                KEY_IPTV_PRESETS_INITIALIZED, KEY_AUTO_CHECK_UPDATES, KEY_AUTO_RESUME_LAST_CHANNEL,
                KEY_SHOW_RECENT_APPS, KEY_USE_DYNAMIC_COLOR, KEY_SLIDESHOW_SHUFFLE,
                KEY_LOW_RAM_MODE, KEY_WEATHER_ENABLED, KEY_NEWS_VOICE_ENABLED
            )
            val intKeys = listOf(
                KEY_APPS_PER_ROW, KEY_GLASS_OPACITY, KEY_APPS_TOP_OFFSET, KEY_WALLPAPER_VISIBILITY,
                KEY_SLIDESHOW_INTERVAL_SEC, KEY_SLIDESHOW_MUSIC_VOLUME, KEY_SLIDESHOW_DIM_LEVEL,
                KEY_BROADCAST_LINE_DURATION_SEC, KEY_BROADCAST_MUSIC_VOLUME
            )
            val longKeys = listOf(KEY_LAST_PLAYED_CHANNEL_ID)
            val stringSetKeys = listOf(
                KEY_HIDDEN_APPS, KEY_CUSTOM_GAMES, KEY_CUSTOM_NON_GAMES,
                KEY_CUSTOM_WEB_APPS, KEY_CUSTOM_NON_WEB_APPS, KEY_LOCKED_APPS,
                KEY_CUSTOM_SCREENSAVER_VIDEOS, KEY_DELETED_SCREENSAVER_PRESETS,
                KEY_LOCKED_IPTV_CATEGORIES, KEY_CUSTOM_WEB_BOOKMARKS,
                KEY_ENABLED_PRESET_WEB_BOOKMARKS, KEY_DISABLED_PRESET_WEB_BOOKMARKS,
                KEY_DELETED_PRESET_WEB_BOOKMARKS, KEY_PRESET_WEB_BOOKMARK_ICONS,
                KEY_SLIDESHOW_CUSTOM_PHOTOS, KEY_DELETED_ONLINE_WALLPAPERS,
                KEY_CUSTOM_ONLINE_WALLPAPERS
            )

            for (k in stringKeys) {
                (entries[k.name] as? String)?.let { prefs[k] = it }
            }
            for (k in boolKeys) {
                (entries[k.name] as? Boolean)?.let { prefs[k] = it }
            }
            for (k in intKeys) {
                (entries[k.name] as? Number)?.toInt()?.let { prefs[k] = it }
            }
            for (k in longKeys) {
                (entries[k.name] as? Number)?.toLong()?.let { prefs[k] = it }
            }
            for (k in stringSetKeys) {
                @Suppress("UNCHECKED_CAST")
                (entries[k.name] as? Collection<*>)?.mapNotNull { it as? String }?.toSet()?.let {
                    prefs[k] = it
                }
            }
        }
    }
}


