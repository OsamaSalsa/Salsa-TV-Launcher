package com.example.ui.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.LauncherPreferences
import com.example.ui.settings.WallpaperCategory
import com.example.ui.settings.onlineWallpaperCategories
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.LocaleHelper
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class LiveSecondaryOption(
    val id: String,
    val title: String,
    val valueLabel: String,
    val icon: ImageVector,
    val isActionOnOk: Boolean = false,
    val staysOpenOnOk: Boolean = false,
    val onStep: (delta: Int) -> Unit
)

private data class LiveUiSettingSection(
    val id: String,
    val title: String,
    val currentValueLabel: String,
    val icon: ImageVector,
    val accentColor: Color,
    val currentStepIndex: Int,
    val totalSteps: Int,
    val scrollsHomeToTop: Boolean = false,
    val showImageGalleryPopup: Boolean = false,
    val secondaryOptions: List<LiveSecondaryOption> = emptyList(),
    val onStep: (delta: Int) -> Unit
)

@Composable
fun LiveUiSettingsOverlay(
    onEnsureTopVisible: () -> Unit,
    onDismiss: () -> Unit,
    onOpenWeather: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember(context) { LauncherPreferences(context.applicationContext) }
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val hudFocusRequester = remember { FocusRequester() }
    val customGreetingFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Secondary external window is ONLY opened when the user presses OK!
    var isSecondaryMenuOpen by remember { mutableStateOf(false) }
    var selectedSecondaryIndex by remember { mutableIntStateOf(0) }
    var isEditingCustomGreeting by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (isEditingCustomGreeting) {
            isEditingCustomGreeting = false
            keyboardController?.hide()
            scope.launch {
                delay(40L)
                try {
                    hudFocusRequester.requestFocus()
                } catch (_: Exception) {}
            }
        } else if (isSecondaryMenuOpen) {
            // Back exits the secondary menu back to the main Live menu
            isSecondaryMenuOpen = false
        } else {
            onDismiss()
        }
    }

    val wallpaperMode by prefs.wallpaperModeFlow.collectAsState(initial = "dynamic")
    val customWallpaperUrl by prefs.customWallpaperUrlFlow.collectAsState(initial = "")
    val wallpaperVisibility by prefs.wallpaperVisibilityFlow.collectAsState(initial = 100)
    val appsTopOffset by prefs.appsTopOffsetFlow.collectAsState(initial = 450)
    val appsPerRow by prefs.appsPerRowFlow.collectAsState(initial = 5)
    val glassOpacity by prefs.glassOpacityFlow.collectAsState(initial = 75)
    val clockSize by prefs.clockSizeFlow.collectAsState(initial = "large")
    val isClock24h by prefs.isClock24h.collectAsState(initial = true)
    val isClockSeconds by prefs.isClockSecondsEnabled.collectAsState(initial = false)
    val dateFormatStyle by prefs.dateFormatStyleFlow.collectAsState(initial = "full")
    val greetingMode by prefs.greetingModeFlow.collectAsState(initial = "poetic")
    val customGreetingText by prefs.customGreetingTextFlow.collectAsState(initial = "")
    val showOnlyTvApps by prefs.showOnlyTvAppsFlow.collectAsState(initial = true)
    val showRecentApps by prefs.showRecentAppsFlow.collectAsState(initial = false)
    val headerControlMode by prefs.headerControlModeFlow.collectAsState(initial = "control_center")
    val nightComfortMode by prefs.nightComfortModeFlow.collectAsState(initial = "off")
    val lowRamMode by prefs.lowRamModeFlow.collectAsState(initial = false)
    val useDynamicColor by prefs.useDynamicColorFlow.collectAsState(initial = false)
    val appLanguage by prefs.languageFlow.collectAsState(initial = prefs.getCachedLanguage())
    val isWeatherEnabled by prefs.isWeatherEnabledFlow.collectAsState(initial = true)
    val weatherData by com.example.data.weather.WeatherRepository.weatherState.collectAsState()
    val weatherGpsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        com.example.data.weather.WeatherRepository.refreshWeatherAsync(
            context = context,
            appLanguage = appLanguage,
            forceLocation = true
        )
    }

    LaunchedEffect(appLanguage) {
        com.example.data.weather.WeatherRepository.ensureInitialized(context, appLanguage)
    }

    val deletedWallpaperIds by prefs.deletedOnlineWallpapersFlow.collectAsState(initial = emptySet())
    val customAddedWallpapers by prefs.customOnlineWallpapersFlow.collectAsState(initial = emptyList())
    val failedWallpaperIds = remember { mutableStateMapOf<String, Boolean>() }

    // System image picker for "custom" wallpaper mode
    val customImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            scope.launch {
                val localUrl = withContext(Dispatchers.IO) {
                    try {
                        val destFile = File(context.filesDir, "custom_wallpaper_live.jpg")
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        if (destFile.exists() && destFile.length() > 0L) {
                            "file://${destFile.absolutePath}?t=${System.currentTimeMillis()}"
                        } else {
                            uri.toString()
                        }
                    } catch (_: Exception) {
                        uri.toString()
                    }
                }
                prefs.setCustomWallpaperUrl(localUrl)
                prefs.setWallpaperMode("custom")
                prefs.setDynamicBackgroundEnabled(false)
            }
        }
    }

    val addedCategoryTitle = stringResource(R.string.wallpaper_added_category)
    val allCategoriesLabel = stringResource(R.string.live_ui_wallpaper_all_categories)

    val effectiveWallpaperCategories = remember(
        customAddedWallpapers,
        deletedWallpaperIds,
        failedWallpaperIds.size,
        addedCategoryTitle
    ) {
        buildList {
            val activeCustom = customAddedWallpapers.filter {
                it.id !in deletedWallpaperIds && failedWallpaperIds[it.id] != true
            }
            if (activeCustom.isNotEmpty()) {
                add(
                    WallpaperCategory(
                        id = "custom_added",
                        titleEn = addedCategoryTitle,
                        titleAr = addedCategoryTitle,
                        wallpapers = activeCustom
                    )
                )
            }
            for (cat in onlineWallpaperCategories) {
                val filtered = cat.wallpapers.filter {
                    it.id !in deletedWallpaperIds && failedWallpaperIds[it.id] != true
                }
                if (filtered.isNotEmpty()) {
                    add(cat.copy(wallpapers = filtered))
                }
            }
        }
    }

    var selectedWallpaperCategoryId by remember { mutableStateOf("all") }

    val activeCategoryWallpapers = remember(effectiveWallpaperCategories, selectedWallpaperCategoryId) {
        if (selectedWallpaperCategoryId == "all") {
            effectiveWallpaperCategories.flatMap { it.wallpapers }
        } else {
            effectiveWallpaperCategories.firstOrNull { it.id == selectedWallpaperCategoryId }?.wallpapers
                ?: effectiveWallpaperCategories.flatMap { it.wallpapers }
        }
    }

    val currentPresetIdx = remember(activeCategoryWallpapers, customWallpaperUrl) {
        activeCategoryWallpapers.indexOfFirst { it.fullUrl == customWallpaperUrl }
            .let { if (it == -1) 0 else it }
    }

    // Localized strings
    val strWallpaperStyle = stringResource(R.string.settings_wallpaper_style)
    val strWpDynamic = stringResource(R.string.settings_wallpaper_dynamic)
    val strWpPresets = stringResource(R.string.settings_wallpaper_presets)
    val strWpCustom = stringResource(R.string.settings_wallpaper_pick_device)
    val strWpDefault = stringResource(R.string.settings_wallpaper_default)

    val strWallpaperCategory = stringResource(R.string.live_ui_wallpaper_category).removePrefix("↳ ")
    val strWallpaperPresetImage = stringResource(R.string.live_ui_wallpaper_preset_image)
    val strPickCustomImage = stringResource(R.string.live_ui_pick_custom_image).removePrefix("↳ ")
    val strPickCustomImageAction = stringResource(R.string.live_ui_pick_custom_image_action)

    val strWallpaperVisibility = stringResource(R.string.settings_wallpaper_visibility)
    val strVis100 = stringResource(R.string.settings_wallpaper_visibility_100)
    val strVis50 = stringResource(R.string.settings_wallpaper_visibility_50)

    val strAppsTopPosition = stringResource(R.string.settings_apps_top_position)
    val strPosHigh = stringResource(R.string.settings_apps_position_high)
    val strPosCompact = stringResource(R.string.settings_apps_position_compact)
    val strPosBalanced = stringResource(R.string.settings_apps_position_balanced)
    val strPosMedLow = stringResource(R.string.settings_apps_position_medium_low)
    val strPosLower = stringResource(R.string.settings_apps_position_lower)
    val strPosUltra = stringResource(R.string.settings_apps_position_ultra)

    val strAppsPerRow = stringResource(R.string.settings_apps_per_row)
    val strAppsSuffix = stringResource(R.string.settings_apps_suffix)
    val strGlassOpacity = stringResource(R.string.settings_glass_opacity)

    val strClockSize = stringResource(R.string.settings_clock_size)
    val strClockLarge = stringResource(R.string.settings_clock_size_large)
    val strClockMedium = stringResource(R.string.settings_clock_size_medium)
    val strClockCompact = stringResource(R.string.settings_clock_size_compact)

    val strClockFormat = stringResource(R.string.settings_clock_format)
    val strClockSeconds = stringResource(R.string.settings_clock_seconds)
    val strEnabled = stringResource(R.string.settings_seconds_enabled)
    val strDisabled = stringResource(R.string.settings_seconds_disabled)

    val strDateFormat = stringResource(R.string.settings_date_format)
    val strDateHidden = stringResource(R.string.settings_date_format_hidden)

    val strGreetingMode = stringResource(R.string.settings_greeting_mode)
    val strGreetingPoetic = stringResource(R.string.settings_greeting_mode_poetic)
    val strGreetingClassic = stringResource(R.string.settings_greeting_mode_classic)
    val strGreetingCustom = stringResource(R.string.settings_greeting_mode_custom)
    val strGreetingHidden = stringResource(R.string.settings_greeting_mode_hidden)

    val strShowOnlyTvApps = stringResource(R.string.settings_show_only_tv_apps)
    val strFilterTvOnly = stringResource(R.string.settings_app_filter_tv_only)
    val strFilterAll = stringResource(R.string.settings_app_filter_all)

    val strRecentApps = stringResource(R.string.settings_recent_apps_title)
    val strHeaderControl = stringResource(R.string.settings_header_control_label)
    val strHeaderModeCc = stringResource(R.string.settings_header_mode_control_center)
    val strHeaderModeDirect = stringResource(R.string.settings_header_mode_direct_settings)
    val strHeaderModeBoth = stringResource(R.string.settings_header_mode_both)
    val strDynamicColor = stringResource(R.string.settings_dynamic_color_title)

    val strTypeCustomGreeting = stringResource(R.string.live_ui_type_custom_greeting)
    val strQuickPresetGreetings = stringResource(R.string.live_ui_quick_preset_greetings)
    val strGreetingPreset1 = stringResource(R.string.live_ui_greeting_preset_1)
    val strGreetingPreset2 = stringResource(R.string.live_ui_greeting_preset_2)
    val strGreetingPreset3 = stringResource(R.string.live_ui_greeting_preset_3)
    val strGreetingPreset4 = stringResource(R.string.live_ui_greeting_preset_4)
    val strGreetingPreset5 = stringResource(R.string.live_ui_greeting_preset_5)
    val strGreetingDoneBtn = stringResource(R.string.live_ui_greeting_done_btn)
    val strGreetingOkToType = stringResource(R.string.live_ui_greeting_ok_to_type)
    val strGreetingInputPlaceholder = stringResource(R.string.live_ui_greeting_input_placeholder)
    val strDeviceShort = stringResource(R.string.live_ui_device_short)
    val strNightComfortTitle = stringResource(R.string.live_ui_night_comfort_title)
    val strNightComfortAuto = stringResource(R.string.live_ui_night_comfort_auto)
    val strLowRamMode = stringResource(R.string.live_ui_low_ram_mode)

    val isArabic = isRtl || appLanguage.startsWith("ar")

    val catOptions = remember(effectiveWallpaperCategories, allCategoriesLabel, isArabic) {
        buildList {
            val totalCount = effectiveWallpaperCategories.sumOf { it.wallpapers.size }
            add("all" to "$allCategoriesLabel ($totalCount)")
            for (cat in effectiveWallpaperCategories) {
                val label = if (isArabic) cat.titleAr else cat.titleEn
                add(cat.id to "$label (${cat.wallpapers.size})")
            }
        }
    }

    // Main Primary Settings List
    val sections: List<LiveUiSettingSection> = buildList {
        // 1. Wallpaper Style
        val wpModes = listOf(
            "dynamic" to strWpDynamic,
            "preset" to strWpPresets,
            "custom" to strWpCustom,
            "default" to strWpDefault
        )
        val wpIdx = wpModes.indexOfFirst { it.first == wallpaperMode }.let { if (it == -1) 0 else it }

        // Secondary options for Wallpaper ONLY when wallpaperMode is "preset" (4K Image) or "custom" (Device Image)
        val wallpaperSecondaryOptions = buildList {
            if (wallpaperMode == "preset" && effectiveWallpaperCategories.isNotEmpty()) {
                val catIdx = catOptions.indexOfFirst { it.first == selectedWallpaperCategoryId }.let { if (it == -1) 0 else it }
                add(
                    LiveSecondaryOption(
                        id = "wp_cat",
                        title = strWallpaperCategory,
                        valueLabel = catOptions[catIdx].second,
                        icon = Icons.Default.Category,
                        onStep = { delta ->
                            val nextCatIdx = (catIdx + delta + catOptions.size) % catOptions.size
                            val nextCatId = catOptions[nextCatIdx].first
                            selectedWallpaperCategoryId = nextCatId
                            val nextWallpapers = if (nextCatId == "all") {
                                effectiveWallpaperCategories.flatMap { it.wallpapers }
                            } else {
                                effectiveWallpaperCategories.firstOrNull { it.id == nextCatId }?.wallpapers.orEmpty()
                            }
                            nextWallpapers.firstOrNull()?.fullUrl?.let { firstUrl ->
                                scope.launch {
                                    prefs.setCustomWallpaperUrl(firstUrl)
                                }
                            }
                        }
                    )
                )
                if (activeCategoryWallpapers.isNotEmpty()) {
                    val safeWpIdx = currentPresetIdx.coerceIn(0, activeCategoryWallpapers.size - 1)
                    val activeWp = activeCategoryWallpapers[safeWpIdx]
                    add(
                        LiveSecondaryOption(
                            id = "wp_img",
                            title = strWallpaperPresetImage,
                            valueLabel = "${activeWp.title} (${safeWpIdx + 1}/${activeCategoryWallpapers.size})",
                            icon = Icons.Default.Image,
                            onStep = { delta ->
                                val nextIdx = (safeWpIdx + delta + activeCategoryWallpapers.size) % activeCategoryWallpapers.size
                                val nextUrl = activeCategoryWallpapers[nextIdx].fullUrl
                                scope.launch {
                                    prefs.setCustomWallpaperUrl(nextUrl)
                                }
                            }
                        )
                    )
                }
            } else if (wallpaperMode == "custom") {
                add(
                    LiveSecondaryOption(
                        id = "wp_pick_device",
                        title = strPickCustomImage,
                        valueLabel = strPickCustomImageAction,
                        icon = Icons.Default.FolderOpen,
                        isActionOnOk = true,
                        onStep = { _ ->
                            try {
                                customImagePickerLauncher.launch("image/*")
                            } catch (_: Exception) {}
                        }
                    )
                )
            }
        }

        add(
            LiveUiSettingSection(
                id = "wallpaper_mode",
                title = strWallpaperStyle,
                currentValueLabel = wpModes[wpIdx].second,
                icon = Icons.Default.Wallpaper,
                accentColor = Color(0xFFF5B942),
                currentStepIndex = wpIdx,
                totalSteps = wpModes.size,
                scrollsHomeToTop = true,
                showImageGalleryPopup = (wallpaperMode == "preset" || wallpaperMode == "custom"),
                secondaryOptions = wallpaperSecondaryOptions,
                onStep = { delta ->
                    val nextIdx = (wpIdx + delta + wpModes.size) % wpModes.size
                    val nextMode = wpModes[nextIdx].first
                    if (nextMode != "preset" && nextMode != "custom") {
                        isSecondaryMenuOpen = false
                    }
                    scope.launch {
                        prefs.setWallpaperMode(nextMode)
                        prefs.setDynamicBackgroundEnabled(nextMode == "dynamic")
                        if (nextMode == "preset" && (customWallpaperUrl.isBlank() || !customWallpaperUrl.startsWith("http"))) {
                            activeCategoryWallpapers.firstOrNull()?.fullUrl?.let { firstUrl ->
                                prefs.setCustomWallpaperUrl(firstUrl)
                            }
                        }
                    }
                }
            )
        )

        // 2. Wallpaper Brightness / Dimming (100% down to 10%)
        val visOptions = listOf(
            100 to strVis100,
            90 to "90%",
            80 to "80%",
            70 to "70%",
            60 to "60%",
            50 to strVis50,
            40 to "40%",
            30 to "30%",
            20 to "20%",
            10 to "10%"
        )
        val visIdx = visOptions.indexOfFirst { it.first == wallpaperVisibility }.let { if (it == -1) 0 else it }
        add(
            LiveUiSettingSection(
                id = "wallpaper_visibility",
                title = strWallpaperVisibility,
                currentValueLabel = visOptions[visIdx].second,
                icon = Icons.Default.Brightness6,
                accentColor = Color(0xFFF5B942),
                currentStepIndex = visIdx,
                totalSteps = visOptions.size,
                scrollsHomeToTop = false,
                onStep = { delta ->
                    val nextIdx = (visIdx + delta + visOptions.size) % visOptions.size
                    scope.launch {
                        prefs.setWallpaperVisibility(visOptions[nextIdx].first)
                    }
                }
            )
        )

        // 3. Apps Start Position / Wallpaper Space (250 .. 500)
        val posOptions = listOf(
            250 to strPosHigh,
            300 to strPosCompact,
            350 to strPosBalanced,
            400 to strPosMedLow,
            450 to strPosLower,
            500 to strPosUltra
        )
        val posIdx = posOptions.indexOfFirst { it.first == appsTopOffset }.let { if (it == -1) 2 else it }
        add(
            LiveUiSettingSection(
                id = "apps_top_offset",
                title = strAppsTopPosition,
                currentValueLabel = posOptions[posIdx].second,
                icon = Icons.Default.VerticalAlignBottom,
                accentColor = Color(0xFF34D399),
                currentStepIndex = posIdx,
                totalSteps = posOptions.size,
                scrollsHomeToTop = true,
                onStep = { delta ->
                    val nextIdx = (posIdx + delta + posOptions.size) % posOptions.size
                    scope.launch {
                        prefs.setAppsTopOffset(posOptions[nextIdx].first)
                    }
                }
            )
        )

        // 4. Apps per Row (4, 5, 6, 7) + Secondary options on OK: App Filter & Recent Apps Row
        val rowOptions = listOf(4, 5, 6, 7).map { it to "$it $strAppsSuffix" }
        val rowIdx = rowOptions.indexOfFirst { it.first == appsPerRow }.let { if (it == -1) 1 else it }
        add(
            LiveUiSettingSection(
                id = "apps_per_row",
                title = strAppsPerRow,
                currentValueLabel = rowOptions[rowIdx].second,
                icon = Icons.Default.GridView,
                accentColor = Color(0xFFA78BFA),
                currentStepIndex = rowIdx,
                totalSteps = rowOptions.size,
                scrollsHomeToTop = false,
                secondaryOptions = listOf(
                    LiveSecondaryOption(
                        id = "sub_app_filter",
                        title = strShowOnlyTvApps,
                        valueLabel = if (showOnlyTvApps) strFilterTvOnly else strFilterAll,
                        icon = Icons.Default.Apps,
                        onStep = { _ ->
                            scope.launch { prefs.setShowOnlyTvApps(!showOnlyTvApps) }
                        }
                    ),
                    LiveSecondaryOption(
                        id = "sub_recent_apps",
                        title = strRecentApps,
                        valueLabel = if (showRecentApps) strEnabled else strDisabled,
                        icon = Icons.Default.History,
                        onStep = { _ ->
                            scope.launch { prefs.setShowRecentApps(!showRecentApps) }
                        }
                    )
                ),
                onStep = { delta ->
                    val nextIdx = (rowIdx + delta + rowOptions.size) % rowOptions.size
                    scope.launch {
                        prefs.setAppsPerRow(rowOptions[nextIdx].first)
                    }
                }
            )
        )

        // 5. Cards Glass Opacity (25%, 50%, 75%, 90%, 100%)
        val opacityOptions = listOf(25, 50, 75, 90, 100).map { it to "$it%" }
        val opacityIdx = opacityOptions.indexOfFirst { it.first == glassOpacity }.let { if (it == -1) 2 else it }
        add(
            LiveUiSettingSection(
                id = "glass_opacity",
                title = strGlassOpacity,
                currentValueLabel = opacityOptions[opacityIdx].second,
                icon = Icons.Default.Opacity,
                accentColor = Color(0xFF38BDF8),
                currentStepIndex = opacityIdx,
                totalSteps = opacityOptions.size,
                scrollsHomeToTop = false,
                onStep = { delta ->
                    val nextIdx = (opacityIdx + delta + opacityOptions.size) % opacityOptions.size
                    scope.launch {
                        prefs.setGlassOpacity(opacityOptions[nextIdx].first)
                    }
                }
            )
        )

        // 6. Clock Size (Secondary Window on OK: 12h/24h Format + Show Seconds)
        val clockFormatLabel = if (isClock24h) "24h • 22:00" else "12h • AM/PM"
        val clockSizeOptions = listOf(
            "large" to strClockLarge,
            "medium" to strClockMedium,
            "compact" to strClockCompact
        )
        val clockSizeIdx = clockSizeOptions.indexOfFirst { it.first == clockSize }.let { if (it == -1) 0 else it }
        add(
            LiveUiSettingSection(
                id = "clock_size",
                title = strClockSize,
                currentValueLabel = clockSizeOptions[clockSizeIdx].second,
                icon = Icons.Default.FormatSize,
                accentColor = Color(0xFFF5B942),
                currentStepIndex = clockSizeIdx,
                totalSteps = clockSizeOptions.size,
                scrollsHomeToTop = true,
                secondaryOptions = listOf(
                    LiveSecondaryOption(
                        id = "sub_clock_fmt",
                        title = strClockFormat,
                        valueLabel = clockFormatLabel,
                        icon = Icons.Default.Schedule,
                        onStep = { _ ->
                            scope.launch { prefs.setClock24h(!isClock24h) }
                        }
                    ),
                    LiveSecondaryOption(
                        id = "sub_clock_sec",
                        title = strClockSeconds,
                        valueLabel = if (isClockSeconds) strEnabled else strDisabled,
                        icon = Icons.Default.Timer,
                        onStep = { _ ->
                            scope.launch { prefs.setClockSecondsEnabled(!isClockSeconds) }
                        }
                    )
                ),
                onStep = { delta ->
                    val nextIdx = (clockSizeIdx + delta + clockSizeOptions.size) % clockSizeOptions.size
                    scope.launch {
                        prefs.setClockSize(clockSizeOptions[nextIdx].first)
                    }
                }
            )
        )

        // 7. Clock Format (12h AM/PM vs 24h / 22:00)
        add(
            LiveUiSettingSection(
                id = "clock_format",
                title = strClockFormat,
                currentValueLabel = clockFormatLabel,
                icon = Icons.Default.Schedule,
                accentColor = Color(0xFF38BDF8),
                currentStepIndex = if (isClock24h) 0 else 1,
                totalSteps = 2,
                scrollsHomeToTop = true,
                onStep = { _ ->
                    scope.launch {
                        prefs.setClock24h(!isClock24h)
                    }
                }
            )
        )

        // 8. Date Format
        val now = Date()
        val dateKeys = listOf(
            "full",
            "full_year",
            "us_full",
            "us_written",
            "us_short",
            "medium",
            "numeric_us",
            "numeric",
            "numeric_iso",
            "numeric_dots",
            "hidden"
        )
        val dateIdx = dateKeys.indexOf(dateFormatStyle).let { if (it == -1) 0 else it }
        val currentDateLabel = if (dateKeys[dateIdx] == "hidden") {
            strDateHidden
        } else {
            LocaleHelper.formatDate(now, dateKeys[dateIdx], appLanguage)
        }
        add(
            LiveUiSettingSection(
                id = "date_format",
                title = strDateFormat,
                currentValueLabel = currentDateLabel,
                icon = Icons.Default.CalendarMonth,
                accentColor = Color(0xFFA78BFA),
                currentStepIndex = dateIdx,
                totalSteps = dateKeys.size,
                scrollsHomeToTop = true,
                onStep = { delta ->
                    val nextIdx = (dateIdx + delta + dateKeys.size) % dateKeys.size
                    scope.launch {
                        prefs.setDateFormatStyle(dateKeys[nextIdx])
                    }
                }
            )
        )

        // 8. Greeting Style (Secondary Window on OK when greetingMode == "custom"!)
        val greetingOptions = listOf(
            "poetic" to strGreetingPoetic,
            "classic" to strGreetingClassic,
            "custom" to strGreetingCustom,
            "hidden" to strGreetingHidden
        )
        val greetingIdx = greetingOptions.indexOfFirst { it.first == greetingMode }.let { if (it == -1) 0 else it }
        val greetingSecondaryOptions = if (greetingMode == "custom") {
            val customPresets = listOf(
                strGreetingPreset1,
                strGreetingPreset2,
                strGreetingPreset3,
                strGreetingPreset4,
                strGreetingPreset5
            ).distinct()
            val allCustomGreetings = if (customGreetingText.isNotBlank() && customGreetingText !in customPresets) {
                listOf(customGreetingText) + customPresets
            } else {
                customPresets
            }
            val cIdx = allCustomGreetings.indexOf(customGreetingText).let { if (it == -1) 0 else it }
            listOf(
                LiveSecondaryOption(
                    id = "sub_greeting_manual",
                    title = strTypeCustomGreeting,
                    valueLabel = if (customGreetingText.isNotBlank()) customGreetingText else allCustomGreetings[cIdx],
                    icon = Icons.Default.Edit,
                    isActionOnOk = true,
                    staysOpenOnOk = true,
                    onStep = { _ ->
                        isEditingCustomGreeting = true
                        scope.launch {
                            delay(50L)
                            try {
                                customGreetingFocusRequester.requestFocus()
                                keyboardController?.show()
                            } catch (_: Exception) {}
                        }
                    }
                ),
                LiveSecondaryOption(
                    id = "sub_greeting_text",
                    title = strQuickPresetGreetings,
                    valueLabel = allCustomGreetings[cIdx],
                    icon = Icons.Default.AutoAwesome,
                    onStep = { delta ->
                        val nextIdx = (cIdx + delta + allCustomGreetings.size) % allCustomGreetings.size
                        val nextPhrase = allCustomGreetings[nextIdx]
                        scope.launch {
                            prefs.setCustomGreetingText(nextPhrase)
                        }
                    }
                )
            )
        } else {
            emptyList()
        }
        add(
            LiveUiSettingSection(
                id = "greeting_mode",
                title = strGreetingMode,
                currentValueLabel = greetingOptions[greetingIdx].second,
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFF5B942),
                currentStepIndex = greetingIdx,
                totalSteps = greetingOptions.size,
                scrollsHomeToTop = true,
                secondaryOptions = greetingSecondaryOptions,
                onStep = { delta ->
                    val nextIdx = (greetingIdx + delta + greetingOptions.size) % greetingOptions.size
                    val nextMode = greetingOptions[nextIdx].first
                    if (nextMode != "custom") {
                        isSecondaryMenuOpen = false
                        isEditingCustomGreeting = false
                        keyboardController?.hide()
                    }
                    scope.launch {
                        prefs.setGreetingMode(nextMode)
                    }
                }
            )
        )

        // 9. Top Bar Control Button Mode
        val headerModes = listOf(
            "control_center" to strHeaderModeCc,
            "direct_settings" to strHeaderModeDirect,
            "both" to strHeaderModeBoth
        )
        val headerIdx = headerModes.indexOfFirst { it.first == headerControlMode }.let { if (it == -1) 0 else it }
        add(
            LiveUiSettingSection(
                id = "header_control_mode",
                title = strHeaderControl,
                currentValueLabel = headerModes[headerIdx].second,
                icon = Icons.Default.Settings,
                accentColor = Color(0xFFA78BFA),
                currentStepIndex = headerIdx,
                totalSteps = headerModes.size,
                scrollsHomeToTop = true,
                onStep = { delta ->
                    val nextIdx = (headerIdx + delta + headerModes.size) % headerModes.size
                    scope.launch {
                        prefs.setHeaderControlMode(headerModes[nextIdx].first)
                    }
                }
            )
        )

        // 10. Night Comfort Eye Filter (Secondary Window on OK: Low-RAM Mode & Dynamic Color)
        val nightModes = listOf(
            "off" to strDisabled,
            "auto" to strNightComfortAuto,
            "always" to strEnabled
        )
        val nightIdx = nightModes.indexOfFirst { it.first == nightComfortMode }.let { if (it == -1) 0 else it }
        val comfortSecondary = buildList {
            add(
                LiveSecondaryOption(
                    id = "sub_low_ram",
                    title = strLowRamMode,
                    valueLabel = if (lowRamMode) strEnabled else strDisabled,
                    icon = Icons.Default.Memory,
                    onStep = { _ ->
                        scope.launch { prefs.setLowRamMode(!lowRamMode) }
                    }
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(
                    LiveSecondaryOption(
                        id = "sub_dynamic_color",
                        title = strDynamicColor,
                        valueLabel = if (useDynamicColor) strEnabled else strDisabled,
                        icon = Icons.Default.ColorLens,
                        onStep = { _ ->
                            scope.launch { prefs.setUseDynamicColor(!useDynamicColor) }
                        }
                    )
                )
            }
        }
        add(
            LiveUiSettingSection(
                id = "night_comfort_mode",
                title = strNightComfortTitle,
                currentValueLabel = nightModes[nightIdx].second,
                icon = Icons.Default.Bedtime,
                accentColor = Color(0xFFF5B942),
                currentStepIndex = nightIdx,
                totalSteps = nightModes.size,
                scrollsHomeToTop = false,
                secondaryOptions = comfortSecondary,
                onStep = { delta ->
                    val nextIdx = (nightIdx + delta + nightModes.size) % nightModes.size
                    scope.launch {
                        prefs.setNightComfortMode(nightModes[nextIdx].first)
                    }
                }
            )
        )

        // 11. Temperature / Weather Display between Clock and Date (Show / Hide + Auto City & Full Weather Page)
        val weatherStrings = com.example.data.weather.WeatherRepository.getLocalizedStrings(appLanguage)
        val weatherSecondary = buildList {
            add(
                LiveSecondaryOption(
                    id = "sub_weather_refresh_city",
                    title = weatherStrings.refreshLocationOption,
                    valueLabel = weatherData.cityName,
                    icon = Icons.Default.LocationOn,
                    onStep = { _ ->
                        if (com.example.data.weather.WeatherRepository.hasLocationPermission(context)) {
                            com.example.data.weather.WeatherRepository.refreshWeatherAsync(
                                context = context,
                                appLanguage = appLanguage,
                                forceLocation = true
                            )
                        } else {
                            weatherGpsPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )
            )
            if (onOpenWeather != null) {
                add(
                    LiveSecondaryOption(
                        id = "sub_weather_open_full",
                        title = weatherStrings.openWeatherPageOption,
                        valueLabel = "${weatherData.currentTempC}°C",
                        icon = Icons.Default.WbSunny,
                        onStep = { _ ->
                            onDismiss()
                            onOpenWeather()
                        }
                    )
                )
            }
        }
        add(
            LiveUiSettingSection(
                id = "weather_display",
                title = weatherStrings.liveSettingTitle,
                currentValueLabel = if (isWeatherEnabled) {
                    "${weatherStrings.showOption} (${weatherData.currentTempC}°C)"
                } else {
                    weatherStrings.hideOption
                },
                icon = Icons.Default.WbSunny,
                accentColor = Color(0xFF38BDF8),
                currentStepIndex = if (isWeatherEnabled) 0 else 1,
                totalSteps = 2,
                scrollsHomeToTop = true,
                secondaryOptions = weatherSecondary,
                onStep = { _ ->
                    scope.launch {
                        prefs.setWeatherEnabled(!isWeatherEnabled)
                    }
                }
            )
        )
    }

    var selectedSectionId by remember { mutableStateOf("wallpaper_mode") }
    val safeIndex = sections.indexOfFirst { it.id == selectedSectionId }.let { if (it == -1) 0 else it }
    val currentSection = sections[safeIndex]
    val prevSection = sections[(safeIndex - 1 + sections.size) % sections.size]
    val nextSection = sections[(safeIndex + 1) % sections.size]

    val hasSecondaryOptions =
        currentSection.showImageGalleryPopup || currentSection.secondaryOptions.isNotEmpty()

    var customGreetingDraft by remember(customGreetingText) {
        mutableStateOf(customGreetingText)
    }

    // Ensure secondary menu closes if the current section has no secondary options
    LaunchedEffect(currentSection.id, hasSecondaryOptions) {
        if (!hasSecondaryOptions) {
            isSecondaryMenuOpen = false
            selectedSecondaryIndex = 0
            isEditingCustomGreeting = false
            keyboardController?.hide()
        } else if (currentSection.id != "greeting_mode") {
            isEditingCustomGreeting = false
            keyboardController?.hide()
        }
    }

    LaunchedEffect(isSecondaryMenuOpen) {
        if (!isSecondaryMenuOpen && isEditingCustomGreeting) {
            isEditingCustomGreeting = false
            keyboardController?.hide()
        }
    }

    // Open secondary menu helper: selects the image thumbnails row by default for 4K wallpaper
    fun openSecondaryMenu() {
        if (!hasSecondaryOptions) return
        selectedSecondaryIndex = if (
            currentSection.id == "wallpaper_mode" &&
            wallpaperMode == "preset" &&
            currentSection.secondaryOptions.size > 1
        ) {
            1 // Row 1 = Image Thumbnails row so Left/Right immediately moves across images!
        } else {
            0
        }
        isSecondaryMenuOpen = true
    }

    val thumbnailListState = rememberLazyListState()
    LaunchedEffect(currentPresetIdx, selectedWallpaperCategoryId, isSecondaryMenuOpen) {
        if (isSecondaryMenuOpen && currentSection.showImageGalleryPopup && wallpaperMode == "preset" && activeCategoryWallpapers.isNotEmpty()) {
            val target = currentPresetIdx.coerceIn(0, activeCategoryWallpapers.size - 1)
            try {
                thumbnailListState.animateScrollToItem((target - 1).coerceAtLeast(0))
            } catch (_: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        delay(40L)
        try {
            hudFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    LaunchedEffect(safeIndex) {
        if (currentSection.scrollsHomeToTop) {
            onEnsureTopVisible()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .focusRequester(hudFocusRequester)
            .focusable()
            .testTag("live_ui_settings_hud")
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (isEditingCustomGreeting) {
                    // When the user is actively typing a custom greeting message, let character keys through!
                    if (native.action == KeyEvent.ACTION_DOWN &&
                        (native.keyCode == KeyEvent.KEYCODE_ENTER ||
                         native.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER ||
                         native.keyCode == KeyEvent.KEYCODE_ESCAPE)
                    ) {
                        isEditingCustomGreeting = false
                        keyboardController?.hide()
                        scope.launch {
                            prefs.setCustomGreetingText(customGreetingDraft.trim())
                            delay(40L)
                            try {
                                hudFocusRequester.requestFocus()
                            } catch (_: Exception) {}
                        }
                        true
                    } else {
                        false
                    }
                } else if (native.action == KeyEvent.ACTION_DOWN) {
                    val subOpts = currentSection.secondaryOptions
                    if (!isSecondaryMenuOpen) {
                        // =========================================================
                        // MAIN MENU D-PAD RULES:
                        // - UP / DOWN: Select Main Menu Section
                        // - LEFT / RIGHT: Change Main Menu Option Value
                        // - OK / ENTER: Enter Secondary Menu (if available)
                        // =========================================================
                        when (native.keyCode) {
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                val nextIdx = (safeIndex - 1 + sections.size) % sections.size
                                selectedSectionId = sections[nextIdx].id
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                val nextIdx = (safeIndex + 1) % sections.size
                                selectedSectionId = sections[nextIdx].id
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                val dir = if (isRtl) -1 else 1
                                if (currentSection.scrollsHomeToTop) onEnsureTopVisible()
                                currentSection.onStep(dir)
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                val dir = if (isRtl) 1 else -1
                                if (currentSection.scrollsHomeToTop) onEnsureTopVisible()
                                currentSection.onStep(dir)
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                if (currentSection.scrollsHomeToTop) onEnsureTopVisible()
                                if (hasSecondaryOptions) {
                                    openSecondaryMenu()
                                } else {
                                    currentSection.onStep(1)
                                }
                                true
                            }
                            else -> false
                        }
                    } else {
                        // =========================================================
                        // SECONDARY MENU D-PAD RULES (when opened via OK):
                        // - UP / DOWN: Move between rows in the Secondary Menu
                        // - LEFT / RIGHT: Change option/image in the Secondary Menu
                        // - OK / BACK: Exit Secondary Menu back to Main Menu
                        //   (or activate manual typing when on a text input row!)
                        // =========================================================
                        when (native.keyCode) {
                            KeyEvent.KEYCODE_DPAD_UP -> {
                                if (subOpts.size > 1) {
                                    selectedSecondaryIndex = (selectedSecondaryIndex - 1 + subOpts.size) % subOpts.size
                                }
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_DOWN -> {
                                if (subOpts.size > 1) {
                                    selectedSecondaryIndex = (selectedSecondaryIndex + 1) % subOpts.size
                                }
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                val dir = if (isRtl) -1 else 1
                                if (currentSection.scrollsHomeToTop) onEnsureTopVisible()
                                if (subOpts.isNotEmpty()) {
                                    val safeSub = selectedSecondaryIndex.coerceIn(0, subOpts.size - 1)
                                    subOpts[safeSub].onStep(dir)
                                }
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_LEFT -> {
                                val dir = if (isRtl) 1 else -1
                                if (currentSection.scrollsHomeToTop) onEnsureTopVisible()
                                if (subOpts.isNotEmpty()) {
                                    val safeSub = selectedSecondaryIndex.coerceIn(0, subOpts.size - 1)
                                    subOpts[safeSub].onStep(dir)
                                }
                                true
                            }
                            KeyEvent.KEYCODE_DPAD_CENTER,
                            KeyEvent.KEYCODE_ENTER,
                            KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                                val activeSub = subOpts.getOrNull(
                                    selectedSecondaryIndex.coerceIn(0, (subOpts.size - 1).coerceAtLeast(0))
                                )
                                if (activeSub?.isActionOnOk == true) {
                                    activeSub.onStep(1)
                                    if (!activeSub.staysOpenOnOk) {
                                        isSecondaryMenuOpen = false
                                    }
                                } else {
                                    // OK exits the secondary menu back to the main menu
                                    isSecondaryMenuOpen = false
                                }
                                true
                            }
                            else -> false
                        }
                    }
                } else {
                    false
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        // Two Separate Compact Floating Transparent Windows side-by-side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.Bottom
        ) {
            // =========================================================================
            // WINDOW 1: Main Primary Live Settings Window (High-Contrast Dark Glass)
            // =========================================================================
            Box(
                modifier = Modifier
                    .widthIn(max = 365.dp)
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0A101E).copy(alpha = 0.86f))
                    .border(
                        width = 1.dp,
                        color = if (!isSecondaryMenuOpen) {
                            TvAccentGold.copy(alpha = 0.55f)
                        } else {
                            Color.White.copy(alpha = 0.22f)
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Mini Bar: LIVE Badge + Section Counter + Previous Section (▲) + Close (×)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(TvAccentGold.copy(alpha = 0.18f))
                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(4.5.dp)
                                        .clip(CircleShape)
                                        .background(TvAccentGoldRadiant)
                                )
                                Text(
                                    text = stringResource(R.string.live_ui_settings_badge),
                                    color = TvAccentGoldRadiant,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Text(
                                text = "${safeIndex + 1}/${sections.size}",
                                color = TvTextSecondary.copy(alpha = 0.85f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Previous Section Button (▲)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isSecondaryMenuOpen = false
                                    val nextIdx = (safeIndex - 1 + sections.size) % sections.size
                                    selectedSectionId = sections[nextIdx].id
                                }
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = TvAccentGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = prevSection.title,
                                color = TvTextSecondary.copy(alpha = 0.82f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Compact Close Button
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.10f))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.action_cancel),
                                tint = TvTextPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Active Main Setting Row (Click / OK enters secondary menu if available, or cycles value)
                    AnimatedContent(
                        targetState = safeIndex,
                        transitionSpec = {
                            val forward = targetState > initialState
                            (slideInVertically(tween(140)) { h -> if (forward) h / 2 else -h / 2 } + fadeIn(tween(140)))
                                .togetherWith(
                                    slideOutVertically(tween(120)) { h -> if (forward) -h / 2 else h / 2 } + fadeOut(tween(120))
                                )
                                .using(SizeTransform(clip = false))
                        },
                        label = "live_main_wheel"
                    ) { idx ->
                        val active = sections[idx.coerceIn(0, sections.size - 1)]
                        val activeHasSub = active.showImageGalleryPopup || active.secondaryOptions.isNotEmpty()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.13f))
                                .clickable {
                                    if (active.scrollsHomeToTop) onEnsureTopVisible()
                                    if (activeHasSub) {
                                        if (isSecondaryMenuOpen) {
                                            isSecondaryMenuOpen = false
                                        } else {
                                            openSecondaryMenu()
                                        }
                                    } else {
                                        active.onStep(1)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(active.accentColor.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = active.icon,
                                        contentDescription = null,
                                        tint = active.accentColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(7.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = active.title,
                                        color = TvTextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.5.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val dotCount = active.totalSteps.coerceIn(2, 11)
                                        val activeDot = active.currentStepIndex.coerceIn(0, dotCount - 1)
                                        for (step in 0 until dotCount) {
                                            val isCurrent = step == activeDot
                                            val dotWidth by animateDpAsState(
                                                targetValue = if (isCurrent) 9.dp else 3.5.dp,
                                                animationSpec = tween(140),
                                                label = "main_dot_w"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .height(3.dp)
                                                    .width(dotWidth)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isCurrent) TvAccentGoldRadiant
                                                        else Color.White.copy(alpha = 0.24f)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Value Pill (Left / Right arrows change the main option)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TvAccentGold.copy(alpha = 0.84f))
                                    .padding(horizontal = 6.dp, vertical = 3.5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.14f))
                                        .clickable {
                                            if (active.scrollsHomeToTop) onEnsureTopVisible()
                                            active.onStep(-1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }

                                Text(
                                    text = active.currentValueLabel,
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier
                                        .widthIn(min = 48.dp, max = 128.dp)
                                        .padding(horizontal = 2.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.14f))
                                        .clickable {
                                            if (active.scrollsHomeToTop) onEnsureTopVisible()
                                            active.onStep(1)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.5.dp))

                    // Bottom Mini Bar: Next Section Preview (▼) + OK Sub-Options badge (if available)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    isSecondaryMenuOpen = false
                                    val nextIdx = (safeIndex + 1) % sections.size
                                    selectedSectionId = sections[nextIdx].id
                                }
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = TvAccentGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = nextSection.title,
                                color = TvTextSecondary.copy(alpha = 0.85f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (hasSecondaryOptions) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (isSecondaryMenuOpen) TvAccentBlue.copy(alpha = 0.88f)
                                        else Color.White.copy(alpha = 0.11f)
                                    )
                                    .clickable {
                                        if (isSecondaryMenuOpen) {
                                            isSecondaryMenuOpen = false
                                        } else {
                                            openSecondaryMenu()
                                        }
                                    }
                                    .padding(horizontal = 7.dp, vertical = 1.5.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentSection.showImageGalleryPopup) Icons.Default.Image else Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = if (isSecondaryMenuOpen) Color.Black else TvAccentBlue,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = if (currentSection.showImageGalleryPopup) {
                                        stringResource(R.string.live_ui_enter_sub_images)
                                    } else {
                                        stringResource(R.string.live_ui_enter_sub_options)
                                    },
                                    color = if (isSecondaryMenuOpen) Color.Black else TvAccentBlue,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Text(
                                text = "▲▼ • ◀▶",
                                color = TvTextSecondary.copy(alpha = 0.70f),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // WINDOW 2: Separate External Floating Secondary Options / Images Window
            // Appears ONLY when the user presses OK on a setting with secondary options!
            // =========================================================================
            AnimatedVisibility(
                visible = isSecondaryMenuOpen && hasSecondaryOptions,
                enter = fadeIn(tween(180)) + scaleIn(tween(180), initialScale = 0.92f),
                exit = fadeOut(tween(140)) + scaleOut(tween(140), targetScale = 0.92f)
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 350.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0A101E).copy(alpha = 0.86f))
                        .border(
                            width = 1.dp,
                            color = TvAccentBlue.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .testTag("live_ui_secondary_external_window")
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Case A: Wallpaper Image Mode ("preset" or "custom") -> Compact External Image Picker Window
                        if (currentSection.showImageGalleryPopup) {
                            if (wallpaperMode == "preset") {
                                val catIdx = catOptions.indexOfFirst { it.first == selectedWallpaperCategoryId }
                                    .let { if (it == -1) 0 else it }
                                val isCategoryRowFocused = (selectedSecondaryIndex == 0)
                                val isImagesRowFocused = (selectedSecondaryIndex == 1)

                                // Row 0: Category Switcher Row + Pick Device Image Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (isCategoryRowFocused) TvAccentBlue.copy(alpha = 0.28f)
                                                else Color.White.copy(alpha = 0.08f)
                                            )
                                            .border(
                                                width = if (isCategoryRowFocused) 1.dp else 0.dp,
                                                color = if (isCategoryRowFocused) TvAccentBlue else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedSecondaryIndex = 0
                                                currentSection.secondaryOptions.firstOrNull()?.onStep(1)
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.12f))
                                                .clickable {
                                                    selectedSecondaryIndex = 0
                                                    currentSection.secondaryOptions.firstOrNull()?.onStep(-1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = null,
                                                tint = TvAccentGold,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                        Text(
                                            text = catOptions.getOrNull(catIdx)?.second.orEmpty(),
                                            color = TvTextPrimary,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.12f))
                                                .clickable {
                                                    selectedSecondaryIndex = 0
                                                    currentSection.secondaryOptions.firstOrNull()?.onStep(1)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = TvAccentGold,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }

                                    // Quick Device / USB Image Button
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White.copy(alpha = 0.10f))
                                            .clickable {
                                                try {
                                                    customImagePickerLauncher.launch("image/*")
                                                } catch (_: Exception) {}
                                                isSecondaryMenuOpen = false
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = TvAccentGold,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = strDeviceShort,
                                            color = TvTextPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Row 1: Compact Visual Wallpaper Thumbnails Strip (◀ / ▶ moves across images)
                                LazyRow(
                                    state = thumbnailListState,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = if (isImagesRowFocused) 1.dp else 0.dp,
                                            color = if (isImagesRowFocused) TvAccentBlue.copy(alpha = 0.75f)
                                            else Color.Transparent,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .padding(2.dp)
                                ) {
                                    itemsIndexed(
                                        activeCategoryWallpapers,
                                        key = { _, wp -> wp.id }
                                    ) { index, wp ->
                                        val isImgSelected =
                                            (wp.fullUrl == customWallpaperUrl || index == currentPresetIdx)
                                        Box(
                                            modifier = Modifier
                                                .width(74.dp)
                                                .height(44.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.30f))
                                                .border(
                                                    width = if (isImgSelected) 1.5.dp else 0.5.dp,
                                                    color = if (isImgSelected) TvAccentGoldRadiant
                                                    else Color.White.copy(alpha = 0.16f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    onEnsureTopVisible()
                                                    selectedSecondaryIndex = 1
                                                    scope.launch {
                                                        prefs.setWallpaperMode("preset")
                                                        prefs.setDynamicBackgroundEnabled(false)
                                                        prefs.setCustomWallpaperUrl(wp.fullUrl)
                                                    }
                                                }
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(wp.thumbnailUrl)
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = wp.title,
                                                contentScale = ContentScale.Crop,
                                                onError = {
                                                    failedWallpaperIds[wp.id] = true
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .background(Color.Black.copy(alpha = 0.58f))
                                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = wp.title,
                                                    color = if (isImgSelected) TvAccentGoldRadiant else Color.White,
                                                    fontSize = 7.5.sp,
                                                    fontWeight = if (isImgSelected) FontWeight.Bold else FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (isImgSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = TvAccentGoldRadiant,
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(2.dp)
                                                        .size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else if (wallpaperMode == "custom") {
                                // Custom Device Wallpaper External Window
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .clickable {
                                            try {
                                                customImagePickerLauncher.launch("image/*")
                                            } catch (_: Exception) {}
                                            isSecondaryMenuOpen = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FolderOpen,
                                            contentDescription = null,
                                            tint = TvAccentGold,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = strPickCustomImage,
                                            color = TvTextPrimary,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(7.dp))
                                            .background(TvAccentGold.copy(alpha = 0.85f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = strPickCustomImageAction,
                                            color = Color.Black,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        } else {
                            // Case B: Any Other Setting with Secondary Options -> Compact External Sub-Options Window
                            currentSection.secondaryOptions.forEachIndexed { subIdx, subOpt ->
                                val isSubActive = (selectedSecondaryIndex == subIdx)
                                if (subOpt.id == "sub_greeting_manual") {
                                    // Dedicated Manual Custom Greeting Input Box (Live update as user types!)
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                if (isEditingCustomGreeting) Color.White.copy(alpha = 0.16f)
                                                else if (isSubActive) Color.White.copy(alpha = 0.12f)
                                                else Color.White.copy(alpha = 0.05f)
                                            )
                                            .border(
                                                width = if (isEditingCustomGreeting) 1.5.dp else if (isSubActive) 1.dp else 0.dp,
                                                color = if (isEditingCustomGreeting) TvAccentGoldRadiant
                                                else if (isSubActive) TvAccentBlue.copy(alpha = 0.65f)
                                                else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable {
                                                selectedSecondaryIndex = subIdx
                                                if (currentSection.scrollsHomeToTop) onEnsureTopVisible()
                                                if (!isEditingCustomGreeting) {
                                                    subOpt.onStep(1)
                                                }
                                            }
                                            .testTag("live_ui_custom_greeting_manual_row")
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.5.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = null,
                                                    tint = if (isEditingCustomGreeting) TvAccentGoldRadiant
                                                    else if (isSubActive) TvAccentBlue
                                                    else TvTextSecondary,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = subOpt.title,
                                                    color = if (isEditingCustomGreeting) TvAccentGoldRadiant
                                                    else if (isSubActive) TvTextPrimary
                                                    else TvTextSecondary,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Action pill: Press OK to type / Save
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isEditingCustomGreeting) TvAccentGoldRadiant
                                                        else if (isSubActive) TvAccentBlue.copy(alpha = 0.85f)
                                                        else Color.White.copy(alpha = 0.14f)
                                                    )
                                                    .clickable {
                                                        selectedSecondaryIndex = subIdx
                                                        if (isEditingCustomGreeting) {
                                                            isEditingCustomGreeting = false
                                                            keyboardController?.hide()
                                                            scope.launch {
                                                                prefs.setCustomGreetingText(customGreetingDraft.trim())
                                                                delay(40L)
                                                                try {
                                                                    hudFocusRequester.requestFocus()
                                                                } catch (_: Exception) {}
                                                            }
                                                        } else {
                                                            subOpt.onStep(1)
                                                        }
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                            ) {
                                                Text(
                                                    text = if (isEditingCustomGreeting) {
                                                        strGreetingDoneBtn
                                                    } else {
                                                        strGreetingOkToType
                                                    },
                                                    color = if (isEditingCustomGreeting || isSubActive) Color.Black else TvTextPrimary,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }

                                        // Editable Text Input Box (Live updates home screen greeting immediately)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(Color.Black.copy(alpha = 0.36f))
                                                .border(
                                                    width = 0.8.dp,
                                                    color = if (isEditingCustomGreeting) TvAccentGoldRadiant.copy(alpha = 0.85f)
                                                    else Color.White.copy(alpha = 0.18f),
                                                    shape = RoundedCornerShape(7.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            BasicTextField(
                                                value = customGreetingDraft,
                                                onValueChange = { newText ->
                                                    val capped = newText.take(90)
                                                    customGreetingDraft = capped
                                                    onEnsureTopVisible()
                                                    scope.launch {
                                                        prefs.setCustomGreetingText(capped)
                                                    }
                                                },
                                                singleLine = true,
                                                textStyle = TextStyle(
                                                    color = TvTextPrimary,
                                                    fontSize = 10.5.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                ),
                                                cursorBrush = SolidColor(TvAccentGoldRadiant),
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(
                                                    onDone = {
                                                        isEditingCustomGreeting = false
                                                        keyboardController?.hide()
                                                        scope.launch {
                                                            prefs.setCustomGreetingText(customGreetingDraft.trim())
                                                            delay(40L)
                                                            try {
                                                                hudFocusRequester.requestFocus()
                                                            } catch (_: Exception) {}
                                                        }
                                                    }
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .focusRequester(customGreetingFocusRequester)
                                                    .testTag("live_ui_custom_greeting_input"),
                                                decorationBox = { innerTextField ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Box(modifier = Modifier.weight(1f)) {
                                                            if (customGreetingDraft.isEmpty()) {
                                                                Text(
                                                                    text = strGreetingInputPlaceholder,
                                                                    color = TvTextSecondary.copy(alpha = 0.65f),
                                                                    fontSize = 10.sp
                                                                )
                                                            }
                                                            innerTextField()
                                                        }
                                                        if (customGreetingDraft.isNotEmpty() && isEditingCustomGreeting) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(start = 4.dp)
                                                                    .size(14.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color.White.copy(alpha = 0.16f))
                                                                    .clickable {
                                                                        customGreetingDraft = ""
                                                                        scope.launch {
                                                                            prefs.setCustomGreetingText("")
                                                                        }
                                                                    },
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = null,
                                                                    tint = TvTextPrimary,
                                                                    modifier = Modifier.size(9.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(
                                                if (isSubActive) Color.White.copy(alpha = 0.12f)
                                                else Color.White.copy(alpha = 0.05f)
                                            )
                                            .border(
                                                width = if (isSubActive) 1.dp else 0.dp,
                                                color = if (isSubActive) TvAccentBlue.copy(alpha = 0.65f) else Color.Transparent,
                                                shape = RoundedCornerShape(9.dp)
                                            )
                                            .clickable {
                                                selectedSecondaryIndex = subIdx
                                                if (currentSection.scrollsHomeToTop) onEnsureTopVisible()
                                                subOpt.onStep(1)
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.5.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = subOpt.icon,
                                                contentDescription = null,
                                                tint = if (isSubActive) TvAccentBlue else TvTextSecondary,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = subOpt.title,
                                                color = if (isSubActive) TvTextPrimary else TvTextSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSubActive) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(7.dp))
                                                .background(
                                                    if (isSubActive) TvAccentBlue.copy(alpha = 0.85f)
                                                    else Color.White.copy(alpha = 0.14f)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        selectedSecondaryIndex = subIdx
                                                        subOpt.onStep(-1)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                    contentDescription = null,
                                                    tint = if (isSubActive) Color.Black else TvTextPrimary,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                            Text(
                                                text = subOpt.valueLabel,
                                                color = if (isSubActive) Color.Black else TvTextPrimary,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.widthIn(max = 120.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .clickable {
                                                        selectedSecondaryIndex = subIdx
                                                        subOpt.onStep(1)
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isRtl) Icons.AutoMirrored.Filled.KeyboardArrowLeft else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                    contentDescription = null,
                                                    tint = if (isSubActive) Color.Black else TvTextPrimary,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Micro-footer inside Secondary Window showing "▲▼◀▶ Select • OK / Back to Exit"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.live_ui_secondary_hint),
                                color = TvTextSecondary.copy(alpha = 0.78f),
                                fontSize = 8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .clickable { isSecondaryMenuOpen = false }
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "OK ✕",
                                    color = TvAccentGoldRadiant,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
