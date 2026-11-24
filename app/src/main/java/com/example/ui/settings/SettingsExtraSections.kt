package com.example.ui.settings

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.IptvPlaylist
import com.example.ui.common.TvActionButton
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.TvTextField
import com.example.ui.home.extractYouTubeVideoId
import com.example.ui.home.screensaverPresetVideos
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvAccentRed
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
internal fun BackupAndPerformanceSettingsCards(
    appLanguage: String,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    // Full Backup & Restore (JSON) Card
    SettingsGlassCard {
        SettingsCardHeader(
            icon = Icons.Default.Save,
            title = stringResource(R.string.settings_backup_restore_title),
            subtitle = stringResource(R.string.settings_backup_restore_desc)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TvActionButton(
                text = stringResource(R.string.settings_backup_export_btn),
                icon = Icons.Default.Save,
                onClick = onExportBackup,
                baseColor = TvAccentGold.copy(alpha = 0.88f),
                focusedColor = TvAccentGold,
                fillWidth = true,
                modifier = Modifier.weight(1f)
            )
            TvActionButton(
                text = stringResource(R.string.settings_backup_restore_btn),
                icon = Icons.Default.Restore,
                onClick = onImportBackup,
                baseColor = TvAccentBlue.copy(alpha = 0.85f),
                focusedColor = TvAccentGold,
                fillWidth = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
internal fun ScreensaverSettingsSection(
    context: Context,
    viewModel: SettingsViewModel,
    screensaverTimeout: String,
    screensaverMode: String,
    newsVoiceEnabled: Boolean,
    dashboardSoundMode: String,
    dashboardCustomSoundUri: String,
    dashboardSoundVolume: Int,
    screensaverVideo: String,
    customScreensaverVideos: Set<String>,
    deletedScreensaverPresets: Set<String>,
    slideshowSource: String,
    slideshowFolderUri: String,
    slideshowCustomPhotos: Set<String>,
    slideshowIntervalSec: Int,
    slideshowMotion: String,
    slideshowTransition: String,
    slideshowEffect: String,
    slideshowClockMode: String,
    slideshowMusicMode: String,
    slideshowCustomMusicUri: String,
    slideshowMusicVolume: Int,
    slideshowShuffle: Boolean,
    slideshowDimLevel: Int,
    broadcastTextLines: String,
    broadcastLineDurationSec: Int,
    broadcastMusicMode: String,
    broadcastCustomMusicUri: String,
    broadcastMusicVolume: Int,
    broadcastTheme: String,
    broadcastVoiceEnabled: Boolean,
    onShowTvFolderPicker: () -> Unit,
    onLaunchMultiPhotosPicker: () -> Unit,
    onLaunchMusicPicker: () -> Unit,
    onLaunchBroadcastMusicPicker: () -> Unit,
    onLaunchDashboardMusicPicker: () -> Unit,
    onPickLocalScreensaverVideo: () -> Unit,
    onPreviewScreensaver: () -> Unit
) {
    var customVideoInput by remember { mutableStateOf("") }
    var broadcastTextDraft by remember(broadcastTextLines) { mutableStateOf(broadcastTextLines) }
    var newBroadcastLineInput by remember { mutableStateOf("") }
    val defaultBroadcastLinesText = stringResource(R.string.broadcast_text_default_lines)
    val activeBroadcastThemeSpec = remember(broadcastTheme) {
        com.example.ui.home.resolveBroadcastThemeSpec(broadcastTheme)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.Timer,
                    title = stringResource(R.string.settings_screensaver_timeout),
                    subtitle = stringResource(R.string.settings_screensaver_timeout_desc)
                )
                Spacer(modifier = Modifier.height(14.dp))
                TvSelectField(
                    label = stringResource(R.string.settings_screensaver_timeout),
                    options = listOf(
                        "2" to stringResource(R.string.timeout_2min),
                        "5" to stringResource(R.string.timeout_5min),
                        "10" to stringResource(R.string.timeout_10min),
                        "15" to stringResource(R.string.timeout_15min),
                        "30" to stringResource(R.string.timeout_30min),
                        "never" to stringResource(R.string.timeout_never)
                    ),
                    selectedOption = screensaverTimeout,
                    onOptionSelected = { viewModel.setScreensaverTimeout(it) }
                )
            }
        }

        item {
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.Bedtime,
                    title = stringResource(R.string.settings_screensaver_mode),
                    subtitle = stringResource(R.string.settings_screensaver_mode_desc)
                )
                Spacer(modifier = Modifier.height(14.dp))
                TvSelectField(
                    label = stringResource(R.string.settings_screensaver_mode),
                    options = listOf(
                        "clock_date" to stringResource(R.string.screensaver_mode_clock),
                        "wallpaper_info" to stringResource(R.string.screensaver_mode_wallpaper),
                        "slideshow" to stringResource(R.string.screensaver_mode_slideshow),
                        "video_stream" to stringResource(R.string.screensaver_mode_video),
                        "weather_dashboard" to stringResource(R.string.screensaver_mode_weather),
                        "news_dashboard" to stringResource(R.string.screensaver_mode_news),
                        "economic_dashboard" to stringResource(R.string.screensaver_mode_economic),
                        "football_dashboard" to stringResource(R.string.screensaver_mode_football),
                        "all_dashboards_cycle" to stringResource(R.string.screensaver_mode_all_cycle),
                        "broadcast_text" to stringResource(R.string.screensaver_mode_broadcast_text)
                    ),
                    selectedOption = screensaverMode,
                    onOptionSelected = { viewModel.setScreensaverMode(it) }
                )
            }
        }

        if (screensaverMode == "news_dashboard" || screensaverMode == "all_dashboards_cycle") {
            item {
                SettingToggleTile(
                    title = stringResource(R.string.settings_news_voice_title),
                    desc = stringResource(R.string.settings_news_voice_desc),
                    isChecked = newsVoiceEnabled,
                    onToggle = { viewModel.setNewsVoiceEnabled(!newsVoiceEnabled) },
                    modifier = Modifier.testTag("screensaver_news_voice_toggle")
                )
            }
        }

        if (screensaverMode in setOf(
                "all_dashboards_cycle",
                "news_dashboard",
                "weather_dashboard",
                "economic_dashboard",
                "football_dashboard"
            )
        ) {
            item {
                DashboardSoftSoundCard(
                    soundMode = dashboardSoundMode,
                    customSoundUri = dashboardCustomSoundUri,
                    soundVolume = dashboardSoundVolume,
                    onSoundModeSelected = { viewModel.setDashboardSoundMode(it) },
                    onPickCustomSound = onLaunchDashboardMusicPicker,
                    onSoundVolumeSelected = { viewModel.setDashboardSoundVolume(it) }
                )
            }
        }

        if (screensaverMode == "wallpaper_info") {
            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.AutoAwesome,
                        title = stringResource(R.string.slideshow_effects_title),
                        subtitle = stringResource(R.string.slideshow_effects_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_effect_label),
                        options = listOf(
                            "snow" to stringResource(R.string.slideshow_effect_snow),
                            "fireflies" to stringResource(R.string.slideshow_effect_fireflies),
                            "rain" to stringResource(R.string.slideshow_effect_rain),
                            "autumn" to stringResource(R.string.slideshow_effect_autumn),
                            "sakura" to stringResource(R.string.slideshow_effect_sakura),
                            "stars" to stringResource(R.string.slideshow_effect_stars),
                            "bokeh" to stringResource(R.string.slideshow_effect_bokeh),
                            "none" to stringResource(R.string.slideshow_effect_none)
                        ),
                        selectedOption = slideshowEffect,
                        onOptionSelected = { viewModel.setSlideshowEffect(it) }
                    )
                }
            }
        }

        if (screensaverMode == "slideshow") {
            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.PhotoLibrary,
                        title = stringResource(R.string.slideshow_source_title),
                        subtitle = stringResource(R.string.slideshow_source_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_source_title),
                        options = listOf(
                            "folder" to stringResource(R.string.slideshow_source_folder),
                            "custom_photos" to stringResource(R.string.slideshow_source_custom_photos),
                            "online_all" to stringResource(R.string.slideshow_source_online_all),
                            "online_nature" to stringResource(R.string.slideshow_source_online_nature),
                            "online_space" to stringResource(R.string.slideshow_source_online_space),
                            "online_cities" to stringResource(R.string.slideshow_source_online_cities)
                        ),
                        selectedOption = slideshowSource,
                        onOptionSelected = { viewModel.setSlideshowSource(it) }
                    )

                    if (slideshowSource == "folder") {
                        Spacer(modifier = Modifier.height(12.dp))
                        val folderDisplay = remember(slideshowFolderUri) {
                            com.example.ui.home.formatSlideshowFolderDisplay(slideshowFolderUri)
                        }
                        val detectedPhotoCount = remember(slideshowFolderUri) {
                            com.example.ui.home.loadSlideshowFolderImages(context, slideshowFolderUri).size
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (folderDisplay.isNotBlank()) {
                                        "$folderDisplay ($detectedPhotoCount)"
                                    } else {
                                        stringResource(R.string.slideshow_no_folder_selected)
                                    },
                                    color = if (folderDisplay.isNotBlank()) TvAccentGoldRadiant else TvTextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            TvActionButton(
                                text = stringResource(R.string.slideshow_select_folder_btn),
                                icon = Icons.Default.FolderOpen,
                                onClick = onShowTvFolderPicker,
                                baseColor = TvAccentGold.copy(alpha = 0.88f),
                                focusedColor = TvAccentGold
                            )
                        }
                    }

                    if (slideshowSource == "custom_photos") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${slideshowCustomPhotos.size} ${stringResource(R.string.slideshow_source_custom_photos)}",
                                color = TvAccentGoldRadiant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TvActionButton(
                                    text = stringResource(R.string.slideshow_pick_photos_btn),
                                    icon = Icons.Default.Add,
                                    onClick = onLaunchMultiPhotosPicker,
                                    baseColor = TvAccentGold.copy(alpha = 0.88f),
                                    focusedColor = TvAccentGold
                                )
                                if (slideshowCustomPhotos.isNotEmpty()) {
                                    TvActionButton(
                                        text = stringResource(R.string.action_delete),
                                        icon = Icons.Default.Delete,
                                        onClick = { viewModel.clearSlideshowCustomPhotos() },
                                        baseColor = TvAccentRed.copy(alpha = 0.3f),
                                        focusedColor = TvAccentRed
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_order_label),
                        options = listOf(
                            true to stringResource(R.string.slideshow_order_shuffle),
                            false to stringResource(R.string.slideshow_order_sequential)
                        ),
                        selectedOption = slideshowShuffle,
                        onOptionSelected = { viewModel.setSlideshowShuffle(it) }
                    )
                }
            }

            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.Timer,
                        title = stringResource(R.string.slideshow_display_title),
                        subtitle = stringResource(R.string.slideshow_display_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_interval_label),
                        options = listOf(
                            5 to stringResource(R.string.slideshow_interval_5s),
                            10 to stringResource(R.string.slideshow_interval_10s),
                            15 to stringResource(R.string.slideshow_interval_15s),
                            30 to stringResource(R.string.slideshow_interval_30s),
                            60 to stringResource(R.string.slideshow_interval_1m),
                            120 to stringResource(R.string.slideshow_interval_2m),
                            300 to stringResource(R.string.slideshow_interval_5m)
                        ),
                        selectedOption = slideshowIntervalSec,
                        onOptionSelected = { viewModel.setSlideshowIntervalSec(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_motion_label),
                        options = listOf(
                            "ken_burns" to stringResource(R.string.slideshow_motion_ken_burns),
                            "gentle_zoom" to stringResource(R.string.slideshow_motion_gentle_zoom),
                            "static" to stringResource(R.string.slideshow_motion_static)
                        ),
                        selectedOption = slideshowMotion,
                        onOptionSelected = { viewModel.setSlideshowMotion(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_transition_label),
                        options = listOf(
                            "crossfade" to stringResource(R.string.slideshow_transition_crossfade),
                            "slide" to stringResource(R.string.slideshow_transition_slide),
                            "zoom_fade" to stringResource(R.string.slideshow_transition_zoom_fade),
                            "blur_reveal" to stringResource(R.string.slideshow_transition_blur_reveal),
                            "flip_3d" to stringResource(R.string.slideshow_transition_flip_3d),
                            "random" to stringResource(R.string.slideshow_transition_random)
                        ),
                        selectedOption = slideshowTransition,
                        onOptionSelected = { viewModel.setSlideshowTransition(it) }
                    )
                }
            }

            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.AutoAwesome,
                        title = stringResource(R.string.slideshow_effects_title),
                        subtitle = stringResource(R.string.slideshow_effects_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_effect_label),
                        options = listOf(
                            "snow" to stringResource(R.string.slideshow_effect_snow),
                            "fireflies" to stringResource(R.string.slideshow_effect_fireflies),
                            "rain" to stringResource(R.string.slideshow_effect_rain),
                            "autumn" to stringResource(R.string.slideshow_effect_autumn),
                            "sakura" to stringResource(R.string.slideshow_effect_sakura),
                            "stars" to stringResource(R.string.slideshow_effect_stars),
                            "bokeh" to stringResource(R.string.slideshow_effect_bokeh),
                            "none" to stringResource(R.string.slideshow_effect_none)
                        ),
                        selectedOption = slideshowEffect,
                        onOptionSelected = { viewModel.setSlideshowEffect(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_clock_label),
                        options = listOf(
                            "corner_full" to stringResource(R.string.slideshow_clock_corner_full),
                            "minimal" to stringResource(R.string.slideshow_clock_minimal),
                            "hidden" to stringResource(R.string.slideshow_clock_hidden)
                        ),
                        selectedOption = slideshowClockMode,
                        onOptionSelected = { viewModel.setSlideshowClockMode(it) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    TvSelectField(
                        label = stringResource(R.string.slideshow_dim_label),
                        options = listOf(
                            0 to "0%",
                            20 to "20%",
                            35 to "35%",
                            50 to "50%"
                        ),
                        selectedOption = slideshowDimLevel,
                        onOptionSelected = { viewModel.setSlideshowDimLevel(it) }
                    )
                }
            }

            item {
                ScreensaverBackgroundAudioCard(
                    musicMode = slideshowMusicMode,
                    customMusicUri = slideshowCustomMusicUri,
                    musicVolume = slideshowMusicVolume,
                    onMusicModeSelected = { viewModel.setSlideshowMusicMode(it) },
                    onPickCustomMusic = onLaunchMusicPicker,
                    onMusicVolumeSelected = { viewModel.setSlideshowMusicVolume(it) }
                )
            }
        }

        if (screensaverMode == "broadcast_text") {
            val activeParsedLines = com.example.ui.home.parseBroadcastLines(
                rawText = broadcastTextDraft,
                fallbackDefaultText = defaultBroadcastLinesText
            )

            item {
                BroadcastThemeCard(
                    selectedThemeId = activeBroadcastThemeSpec.id,
                    onThemeSelected = { viewModel.setBroadcastTheme(it) }
                )
            }

            item {
                SettingToggleTile(
                    title = stringResource(R.string.settings_broadcast_voice_title),
                    desc = stringResource(R.string.settings_broadcast_voice_desc),
                    isChecked = broadcastVoiceEnabled,
                    onToggle = { viewModel.setBroadcastVoiceEnabled(!broadcastVoiceEnabled) },
                    modifier = Modifier.testTag("screensaver_broadcast_voice_toggle")
                )
            }

            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.SmartDisplay,
                        title = stringResource(R.string.broadcast_text_card_title),
                        subtitle = stringResource(R.string.broadcast_text_card_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    TvTextField(
                        value = broadcastTextDraft,
                        onValueChange = { broadcastTextDraft = it },
                        placeholder = stringResource(R.string.broadcast_text_editor_hint),
                        singleLine = false,
                        minLines = 4,
                        maxLines = 7,
                        onEditFinished = { viewModel.setBroadcastTextLines(broadcastTextDraft.trim()) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TvActionButton(
                            text = stringResource(R.string.broadcast_text_save_btn),
                            icon = Icons.Default.Save,
                            onClick = { viewModel.setBroadcastTextLines(broadcastTextDraft.trim()) },
                            baseColor = TvAccentGold.copy(alpha = 0.88f),
                            focusedColor = TvAccentGold,
                            fillWidth = true,
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                        TvActionButton(
                            text = stringResource(R.string.broadcast_text_reset_btn),
                            icon = Icons.Default.Refresh,
                            onClick = {
                                broadcastTextDraft = ""
                                viewModel.setBroadcastTextLines("")
                            },
                            baseColor = Color(0xFF23304A),
                            focusedColor = TvAccentGold,
                            fillWidth = true,
                            modifier = Modifier.weight(1f).height(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.08f))
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TvTextField(
                            value = newBroadcastLineInput,
                            onValueChange = { newBroadcastLineInput = it },
                            placeholder = stringResource(R.string.broadcast_text_add_line_hint),
                            modifier = Modifier.weight(1f)
                        )
                        TvActionButton(
                            text = stringResource(R.string.broadcast_text_add_line_btn),
                            icon = Icons.Default.Add,
                            onClick = {
                                val cleanNewLine = newBroadcastLineInput.trim()
                                if (cleanNewLine.isNotEmpty()) {
                                    val baseLines = if (broadcastTextDraft.isBlank()) {
                                        emptyList()
                                    } else {
                                        broadcastTextDraft.replace("\r\n", "\n")
                                            .split('\n')
                                            .map { it.trim() }
                                            .filter { it.isNotEmpty() }
                                    }
                                    val updated = (baseLines + cleanNewLine).joinToString("\n")
                                    broadcastTextDraft = updated
                                    viewModel.setBroadcastTextLines(updated)
                                    newBroadcastLineInput = ""
                                }
                            },
                            baseColor = TvAccentBlue.copy(alpha = 0.88f),
                            focusedColor = TvAccentGold,
                            modifier = Modifier.height(50.dp)
                        )
                    }

                    if (activeParsedLines.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.broadcast_text_lines_preview_label),
                            color = TvTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            activeParsedLines.forEachIndexed { idx, lineText ->
                                val isSecondary = idx % 2 == 1
                                val lineColor = if (isSecondary) {
                                    activeBroadcastThemeSpec.secondaryColor
                                } else {
                                    activeBroadcastThemeSpec.primaryColor
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0A101F).copy(alpha = 0.80f))
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(lineColor)
                                    )
                                    Text(
                                        text = String.format(java.util.Locale.US, "%02d", idx + 1),
                                        color = lineColor.copy(alpha = 0.78f),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = lineText,
                                        color = lineColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (broadcastTextDraft.isNotBlank()) {
                                        TvFocusableCard(
                                            onClick = {
                                                val remaining = activeParsedLines.toMutableList()
                                                    .apply { removeAt(idx) }
                                                    .joinToString("\n")
                                                broadcastTextDraft = remaining
                                                viewModel.setBroadcastTextLines(remaining)
                                            },
                                            scaleUpRatio = 1.06f,
                                            shape = RoundedCornerShape(8.dp),
                                            useDefaultBorder = false,
                                            modifier = Modifier.size(34.dp)
                                        ) { isDelFocused ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isDelFocused) TvAccentRed
                                                        else TvAccentRed.copy(alpha = 0.2f)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.action_delete),
                                                    tint = if (isDelFocused) Color.White else TvAccentRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                SettingsGlassCard {
                    SettingsCardHeader(
                        icon = Icons.Default.Timer,
                        title = stringResource(R.string.broadcast_text_duration_title),
                        subtitle = stringResource(R.string.broadcast_text_duration_desc)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TvSelectField(
                        label = stringResource(R.string.broadcast_text_duration_label),
                        options = listOf(
                            3 to stringResource(R.string.broadcast_text_interval_3s),
                            4 to stringResource(R.string.broadcast_text_interval_4s),
                            5 to stringResource(R.string.slideshow_interval_5s),
                            7 to stringResource(R.string.broadcast_text_interval_7s),
                            10 to stringResource(R.string.slideshow_interval_10s),
                            15 to stringResource(R.string.slideshow_interval_15s)
                        ),
                        selectedOption = broadcastLineDurationSec,
                        onOptionSelected = { viewModel.setBroadcastLineDurationSec(it) }
                    )
                }
            }

            item {
                ScreensaverBackgroundAudioCard(
                    musicMode = broadcastMusicMode,
                    customMusicUri = broadcastCustomMusicUri,
                    musicVolume = broadcastMusicVolume,
                    onMusicModeSelected = { viewModel.setBroadcastMusicMode(it) },
                    onPickCustomMusic = onLaunchBroadcastMusicPicker,
                    onMusicVolumeSelected = { viewModel.setBroadcastMusicVolume(it) }
                )
            }
        }

        if (screensaverMode == "video_stream") {
            val activeVideoId = extractYouTubeVideoId(screensaverVideo)
            val visiblePresets = screensaverPresetVideos.filterNot {
                deletedScreensaverPresets.contains(it.id)
            }
            val presetIds = screensaverPresetVideos.map { it.id }.toSet()
            val customVideoEntries = buildList {
                customScreensaverVideos.forEach { raw ->
                    val id = raw.substringBefore("|").trim()
                    val customTitle = raw.substringAfter("|", "").trim()
                    if (id.isNotBlank() && !presetIds.contains(id)) {
                        add(id to customTitle)
                    }
                }
                if (
                    activeVideoId.isNotBlank() &&
                    !presetIds.contains(activeVideoId) &&
                    !deletedScreensaverPresets.contains(activeVideoId) &&
                    none { it.first == activeVideoId }
                ) {
                    add(activeVideoId to "")
                }
            }

            item {
                SettingsGlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            SettingsCardHeader(
                                icon = Icons.Default.SmartDisplay,
                                title = stringResource(R.string.screensaver_video_choice),
                                subtitle = stringResource(R.string.screensaver_video_choice_desc)
                            )
                        }
                        if (deletedScreensaverPresets.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            TvActionButton(
                                text = stringResource(R.string.screensaver_video_restore_defaults),
                                icon = Icons.Default.Refresh,
                                onClick = { viewModel.restoreDefaultScreensaverVideos() },
                                baseColor = Color(0xFF23304A),
                                focusedColor = TvAccentGold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (visiblePresets.isEmpty() && customVideoEntries.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0A101F))
                                    .padding(18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.screensaver_no_videos),
                                    color = TvTextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        visiblePresets.forEach { presetVideo ->
                            val isSelected = activeVideoId == presetVideo.id
                            ScreensaverVideoRowItem(
                                videoId = presetVideo.id,
                                title = stringResource(presetVideo.titleRes),
                                subtitle = stringResource(presetVideo.subtitleRes),
                                isSelected = isSelected,
                                onSelect = { viewModel.setScreensaverVideo(presetVideo.id) },
                                onDelete = { viewModel.deleteScreensaverVideo(presetVideo.id) }
                            )
                        }

                        customVideoEntries.forEach { (customId, customTitle) ->
                            val isSelected = activeVideoId == customId
                            val isLocalVideo = customId.startsWith("content://", ignoreCase = true) ||
                                customId.startsWith("file://", ignoreCase = true) ||
                                customId.startsWith("/")
                            val isDirectStream = !isLocalVideo && (
                                customId.startsWith("http://", ignoreCase = true) ||
                                    customId.startsWith("https://", ignoreCase = true)
                                )
                            val displayTitle = when {
                                customTitle.isNotBlank() -> customTitle
                                isLocalVideo -> stringResource(R.string.screensaver_video_local_default_title)
                                else -> stringResource(R.string.screensaver_video_custom_item_title).format(customId)
                            }
                            val displaySub = when {
                                isLocalVideo -> stringResource(R.string.screensaver_video_local_sub)
                                isDirectStream -> stringResource(R.string.screensaver_video_direct_sub)
                                else -> stringResource(R.string.screensaver_video_custom_item_sub).format(customId)
                            }

                            ScreensaverVideoRowItem(
                                videoId = customId,
                                title = displayTitle,
                                subtitle = displaySub,
                                isSelected = isSelected,
                                onSelect = { viewModel.setScreensaverVideo(customId) },
                                onDelete = { viewModel.deleteScreensaverVideo(customId) }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.08f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.screensaver_video_custom_title),
                            color = TvTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.screensaver_video_custom_desc),
                            color = TvTextSecondary,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TvTextField(
                                value = customVideoInput,
                                onValueChange = { customVideoInput = it },
                                placeholder = stringResource(R.string.screensaver_video_custom_hint),
                                modifier = Modifier.weight(1f)
                            )

                            TvActionButton(
                                text = stringResource(R.string.screensaver_video_custom_save),
                                icon = Icons.Default.Add,
                                onClick = {
                                    if (customVideoInput.isNotBlank()) {
                                        viewModel.addCustomScreensaverVideo(customVideoInput.trim())
                                        customVideoInput = ""
                                    }
                                },
                                baseColor = TvAccentGold.copy(alpha = 0.88f),
                                focusedColor = TvAccentGold,
                                modifier = Modifier.height(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        TvActionButton(
                            text = stringResource(R.string.screensaver_video_pick_local_btn),
                            icon = Icons.Default.FolderOpen,
                            onClick = onPickLocalScreensaverVideo,
                            baseColor = TvAccentBlue.copy(alpha = 0.85f),
                            focusedColor = TvAccentGold,
                            fillWidth = true,
                            modifier = Modifier.height(48.dp)
                        )
                    }
                }
            }
        }

        item {
            SettingActionTile(
                icon = Icons.Default.PlayArrow,
                title = stringResource(R.string.settings_screensaver_preview_btn),
                desc = stringResource(R.string.settings_screensaver_preview_desc),
                onClick = onPreviewScreensaver
            )
        }
    }
}

@Composable
private fun ScreensaverBackgroundAudioCard(
    musicMode: String,
    customMusicUri: String,
    musicVolume: Int,
    onMusicModeSelected: (String) -> Unit,
    onPickCustomMusic: () -> Unit,
    onMusicVolumeSelected: (Int) -> Unit
) {
    SettingsGlassCard {
        SettingsCardHeader(
            icon = Icons.Default.MusicNote,
            title = stringResource(R.string.slideshow_music_title),
            subtitle = stringResource(R.string.slideshow_music_desc)
        )
        Spacer(modifier = Modifier.height(14.dp))
        TvSelectField(
            label = stringResource(R.string.slideshow_music_label),
            options = listOf(
                "off" to stringResource(R.string.slideshow_music_off),
                "lofi_chill" to stringResource(R.string.slideshow_music_lofi),
                "nature_rain" to stringResource(R.string.slideshow_music_nature),
                "ambient_space" to stringResource(R.string.slideshow_music_space),
                "oriental_cafe" to stringResource(R.string.slideshow_music_oriental),
                "custom_file" to stringResource(R.string.slideshow_music_custom)
            ),
            selectedOption = musicMode,
            onOptionSelected = onMusicModeSelected
        )

        if (musicMode == "custom_file") {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customMusicUri.ifBlank {
                        stringResource(R.string.slideshow_music_custom)
                    },
                    color = if (customMusicUri.isNotBlank()) TvAccentGoldRadiant else TvTextSecondary,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TvActionButton(
                    text = stringResource(R.string.slideshow_music_pick_btn),
                    icon = Icons.Default.MusicNote,
                    onClick = onPickCustomMusic,
                    baseColor = TvAccentGold.copy(alpha = 0.88f),
                    focusedColor = TvAccentGold
                )
            }
        }

        if (musicMode != "off") {
            Spacer(modifier = Modifier.height(10.dp))
            TvSelectField(
                label = stringResource(R.string.slideshow_music_volume_label),
                options = listOf(
                    25 to "25%",
                    50 to "50%",
                    75 to "75%",
                    100 to "100%"
                ),
                selectedOption = musicVolume,
                onOptionSelected = onMusicVolumeSelected
            )
        }
    }
}

@Composable
private fun DashboardSoftSoundCard(
    soundMode: String,
    customSoundUri: String,
    soundVolume: Int,
    onSoundModeSelected: (String) -> Unit,
    onPickCustomSound: () -> Unit,
    onSoundVolumeSelected: (Int) -> Unit
) {
    SettingsGlassCard {
        SettingsCardHeader(
            icon = Icons.Default.MusicNote,
            title = stringResource(R.string.dashboard_sound_title),
            subtitle = stringResource(R.string.dashboard_sound_desc)
        )
        Spacer(modifier = Modifier.height(14.dp))
        TvSelectField(
            label = stringResource(R.string.dashboard_sound_label),
            options = listOf(
                "off" to stringResource(R.string.dashboard_sound_off),
                "studio_pulse" to stringResource(R.string.dashboard_sound_studio),
                "warm_piano" to stringResource(R.string.dashboard_sound_piano),
                "soft_rain" to stringResource(R.string.dashboard_sound_rain),
                "night_horizon" to stringResource(R.string.dashboard_sound_horizon),
                "soft_chime" to stringResource(R.string.dashboard_sound_chime),
                "custom_file" to stringResource(R.string.slideshow_music_custom)
            ),
            selectedOption = soundMode,
            onOptionSelected = onSoundModeSelected
        )

        if (soundMode == "custom_file") {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = customSoundUri.ifBlank {
                        stringResource(R.string.slideshow_music_custom)
                    },
                    color = if (customSoundUri.isNotBlank()) TvAccentGoldRadiant else TvTextSecondary,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                TvActionButton(
                    text = stringResource(R.string.slideshow_music_pick_btn),
                    icon = Icons.Default.MusicNote,
                    onClick = onPickCustomSound,
                    baseColor = TvAccentGold.copy(alpha = 0.88f),
                    focusedColor = TvAccentGold
                )
            }
        }

        if (soundMode != "off") {
            Spacer(modifier = Modifier.height(10.dp))
            TvSelectField(
                label = stringResource(R.string.dashboard_sound_volume_label),
                options = listOf(
                    10 to "10%",
                    20 to "20%",
                    25 to "25%",
                    35 to "35%",
                    50 to "50%",
                    75 to "75%"
                ),
                selectedOption = soundVolume,
                onOptionSelected = onSoundVolumeSelected
            )
        }
    }
}

@Composable
private fun BroadcastThemeCard(
    selectedThemeId: String,
    onThemeSelected: (String) -> Unit
) {
    val themeOptions = listOf(
        com.example.ui.home.BroadcastThemeIds.STUDIO_CLASSIC to R.string.broadcast_theme_studio_classic,
        com.example.ui.home.BroadcastThemeIds.BREAKING_NEWS to R.string.broadcast_theme_breaking_news,
        com.example.ui.home.BroadcastThemeIds.ROYAL_CINEMA to R.string.broadcast_theme_royal_cinema,
        com.example.ui.home.BroadcastThemeIds.NEON_CYBER to R.string.broadcast_theme_neon_cyber,
        com.example.ui.home.BroadcastThemeIds.EMERALD_SPORT to R.string.broadcast_theme_emerald_sport,
        com.example.ui.home.BroadcastThemeIds.MINIMAL_ZEN to R.string.broadcast_theme_minimal_zen
    )

    SettingsGlassCard {
        SettingsCardHeader(
            icon = Icons.Default.AutoAwesome,
            title = stringResource(R.string.broadcast_theme_title),
            subtitle = stringResource(R.string.broadcast_theme_desc)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            themeOptions.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { (themeId, labelRes) ->
                        val spec = remember(themeId) {
                            com.example.ui.home.resolveBroadcastThemeSpec(themeId)
                        }
                        val isSelected = selectedThemeId == themeId
                        TvFocusableCard(
                            onClick = { onThemeSelected(themeId) },
                            scaleUpRatio = 1.03f,
                            shape = RoundedCornerShape(14.dp),
                            useDefaultBorder = false,
                            modifier = Modifier
                                .weight(1f)
                                .height(74.dp)
                                .testTag("broadcast_theme_$themeId")
                        ) { isFocused ->
                            val borderColor = when {
                                isFocused -> TvAccentGold
                                isSelected -> spec.secondaryColor
                                else -> Color.White.copy(alpha = 0.10f)
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        when {
                                            isFocused -> spec.glowPrimary.copy(alpha = 0.24f)
                                            isSelected -> spec.glowPrimary.copy(alpha = 0.16f)
                                            else -> spec.backdropTint.copy(alpha = 0.85f)
                                        }
                                    )
                                    .border(
                                        width = if (isFocused || isSelected) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Mini wireframe preview showing layout + surrounding broadcast elements
                                BroadcastThemeMiniPreview(
                                    spec = spec,
                                    modifier = Modifier.size(width = 48.dp, height = 34.dp)
                                )

                                Text(
                                    text = stringResource(labelRes),
                                    color = if (isFocused || isSelected) TvTextPrimary else TvTextPrimary.copy(alpha = 0.88f),
                                    fontSize = 12.sp,
                                    lineHeight = 15.sp,
                                    fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(19.dp)
                                            .clip(CircleShape)
                                            .background(spec.secondaryColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BroadcastThemeMiniPreview(
    spec: com.example.ui.home.BroadcastThemeSpec,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(7.dp))
    ) {
        val w = size.width
        val h = size.height
        val palette = spec.backdropPalette
        val p = spec.primaryColor
        val s = spec.secondaryColor

        // Theme-specific dark base gradient + contrasting color shadow glow preview
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(palette.baseTop, palette.baseBottom),
                start = Offset.Zero,
                end = Offset(w, h)
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.shadowColorPrimary.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(w * 0.25f, h * 0.30f),
                radius = w * 0.55f
            ),
            center = Offset(w * 0.25f, h * 0.30f),
            radius = w * 0.55f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(palette.shadowColorContrast.copy(alpha = 0.48f), Color.Transparent),
                center = Offset(w * 0.78f, h * 0.75f),
                radius = w * 0.55f
            ),
            center = Offset(w * 0.78f, h * 0.75f),
            radius = w * 0.55f
        )
        when (spec.layoutMode) {
            com.example.ui.home.BroadcastLayoutMode.CENTER_STAGE -> {
                drawLine(s, Offset(w * 0.18f, h * 0.25f), Offset(w * 0.18f, h * 0.75f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(s.copy(alpha = 0.55f), Offset(w * 0.82f, h * 0.25f), Offset(w * 0.82f, h * 0.75f), strokeWidth = 3f, cap = StrokeCap.Round)
                drawLine(p, Offset(w * 0.30f, h * 0.50f), Offset(w * 0.70f, h * 0.50f), strokeWidth = 4f, cap = StrokeCap.Round)
            }
            com.example.ui.home.BroadcastLayoutMode.LOWER_THIRD_DESK -> {
                drawRoundRect(
                    color = s.copy(alpha = 0.88f),
                    topLeft = Offset(w * 0.10f, h * 0.50f),
                    size = Size(w * 0.80f, h * 0.14f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawRoundRect(
                    color = p.copy(alpha = 0.22f),
                    topLeft = Offset(w * 0.10f, h * 0.64f),
                    size = Size(w * 0.80f, h * 0.24f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawLine(p, Offset(w * 0.20f, h * 0.76f), Offset(w * 0.72f, h * 0.76f), strokeWidth = 3f, cap = StrokeCap.Round)
            }
            com.example.ui.home.BroadcastLayoutMode.CINEMA_LETTERBOX -> {
                drawRect(Color.Black, topLeft = Offset.Zero, size = Size(w, h * 0.20f))
                drawRect(Color.Black, topLeft = Offset(0f, h * 0.80f), size = Size(w, h * 0.20f))
                drawLine(s, Offset(0f, h * 0.20f), Offset(w, h * 0.20f), strokeWidth = 1.5f)
                drawLine(s, Offset(0f, h * 0.80f), Offset(w, h * 0.80f), strokeWidth = 1.5f)
                drawLine(p, Offset(w * 0.24f, h * 0.50f), Offset(w * 0.76f, h * 0.50f), strokeWidth = 3.5f, cap = StrokeCap.Round)
            }
            com.example.ui.home.BroadcastLayoutMode.HUD_VIEWFINDER -> {
                val b = w * 0.18f
                drawLine(p, Offset(w * 0.14f, h * 0.22f), Offset(w * 0.14f + b, h * 0.22f), strokeWidth = 2.5f)
                drawLine(p, Offset(w * 0.14f, h * 0.22f), Offset(w * 0.14f, h * 0.22f + b), strokeWidth = 2.5f)
                drawLine(s, Offset(w * 0.86f - b, h * 0.22f), Offset(w * 0.86f, h * 0.22f), strokeWidth = 2.5f)
                drawLine(s, Offset(w * 0.86f, h * 0.22f), Offset(w * 0.86f, h * 0.22f + b), strokeWidth = 2.5f)
                drawLine(s, Offset(w * 0.14f, h * 0.78f - b), Offset(w * 0.14f, h * 0.78f), strokeWidth = 2.5f)
                drawLine(s, Offset(w * 0.14f, h * 0.78f), Offset(w * 0.14f + b, h * 0.78f), strokeWidth = 2.5f)
                drawLine(p, Offset(w * 0.86f - b, h * 0.78f), Offset(w * 0.86f, h * 0.78f), strokeWidth = 2.5f)
                drawLine(p, Offset(w * 0.86f, h * 0.78f - b), Offset(w * 0.86f, h * 0.78f), strokeWidth = 2.5f)
                drawLine(p, Offset(w * 0.30f, h * 0.50f), Offset(w * 0.70f, h * 0.50f), strokeWidth = 3f, cap = StrokeCap.Round)
            }
            com.example.ui.home.BroadcastLayoutMode.DIAGONAL_ARENA -> {
                drawLine(s, Offset(w * 0.16f, h * 0.72f), Offset(w * 0.26f, h * 0.28f), strokeWidth = 3.5f, cap = StrokeCap.Round)
                drawLine(s, Offset(w * 0.74f, h * 0.72f), Offset(w * 0.84f, h * 0.28f), strokeWidth = 3.5f, cap = StrokeCap.Round)
                drawLine(p, Offset(w * 0.34f, h * 0.50f), Offset(w * 0.66f, h * 0.50f), strokeWidth = 4f, cap = StrokeCap.Round)
            }
            com.example.ui.home.BroadcastLayoutMode.EDITORIAL_MINIMAL -> {
                drawCircle(s.copy(alpha = 0.85f), radius = 3.5f, center = Offset(w * 0.50f, h * 0.32f))
                drawLine(p, Offset(w * 0.28f, h * 0.54f), Offset(w * 0.72f, h * 0.54f), strokeWidth = 3.5f, cap = StrokeCap.Round)
                drawLine(s.copy(alpha = 0.65f), Offset(w * 0.38f, h * 0.72f), Offset(w * 0.62f, h * 0.72f), strokeWidth = 2f, cap = StrokeCap.Round)
            }
        }
    }
}

@Composable
private fun ScreensaverVideoRowItem(
    videoId: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TvFocusableCard(
            onClick = onSelect,
            scaleUpRatio = 1.02f,
            shape = RoundedCornerShape(12.dp),
            useDefaultBorder = false,
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
        ) { isFocused ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            isFocused -> TvAccentGold.copy(alpha = 0.24f)
                            isSelected -> TvAccentGold.copy(alpha = 0.18f)
                            else -> Color(0xFF0A101F).copy(alpha = 0.85f)
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isYouTubeThumb = !videoId.contains("/") && !videoId.contains(":") && videoId.length in 8..15
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isYouTubeThumb) Color.Black else Color(0xFF162238)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isYouTubeThumb) {
                        AsyncImage(
                            model = "https://img.youtube.com/vi/$videoId/hqdefault.jpg",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.Center)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = if (isSelected || isFocused) TvTextPrimary else TvTextPrimary.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        color = TvTextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(TvAccentGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        TvFocusableCard(
            onClick = onDelete,
            scaleUpRatio = 1.06f,
            shape = RoundedCornerShape(12.dp),
            useDefaultBorder = false,
            modifier = Modifier
                .width(56.dp)
                .height(72.dp)
        ) { isDelFocused ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDelFocused) TvAccentRed
                        else TvAccentRed.copy(alpha = 0.18f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = if (isDelFocused) Color.White else TvAccentRed,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
internal fun IptvSettingsSection(
    viewModel: SettingsViewModel,
    iptvPlaylists: List<IptvPlaylist>,
    activeIptvPlaylist: IptvPlaylist?,
    autoResumeLastChannel: Boolean,
    epgUrlOverride: String
) {
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistUrl by remember { mutableStateOf("") }
    var epgUrlInput by remember(epgUrlOverride) { mutableStateOf(epgUrlOverride) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            SettingsGlassCard(
                highlightColor = TvAccentGold,
                highlightActive = activeIptvPlaylist != null
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(TvAccentGold.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = null,
                                tint = TvAccentGold,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = activeIptvPlaylist?.name ?: stringResource(R.string.iptv_title),
                                    color = TvTextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (activeIptvPlaylist != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TvAccentGold)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.settings_iptv_active_tag),
                                            color = Color.Black,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activeIptvPlaylist?.let {
                                    "${pluralStringResource(R.plurals.settings_iptv_channels_count, it.channelCount, it.channelCount)} • ${it.url}"
                                } ?: stringResource(R.string.iptv_no_playlist),
                                color = TvTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TvActionButton(
                            text = stringResource(R.string.settings_iptv_sync_button),
                            icon = Icons.Default.Refresh,
                            onClick = { viewModel.refreshIptvPlaylist() },
                            baseColor = TvAccentGold.copy(alpha = 0.85f),
                            focusedColor = TvAccentGold
                        )
                        TvActionButton(
                            text = stringResource(R.string.settings_iptv_load_presets),
                            onClick = { viewModel.loadPresetPlaylists() },
                            baseColor = Color(0xFF23304A),
                            focusedColor = TvAccentGold
                        )
                    }
                }
            }
        }

        item {
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.Add,
                    title = stringResource(R.string.settings_iptv_add_new)
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TvTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = stringResource(R.string.settings_iptv_name_placeholder),
                        modifier = Modifier.weight(1f)
                    )

                    TvTextField(
                        value = newPlaylistUrl,
                        onValueChange = { newPlaylistUrl = it },
                        placeholder = stringResource(R.string.settings_iptv_url_placeholder),
                        modifier = Modifier.weight(1.8f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("https://", "http://", ".m3u", ".m3u8").forEach { token ->
                            TvFocusableCard(
                                onClick = {
                                    newPlaylistUrl = if (token.startsWith("http")) {
                                        if (newPlaylistUrl.startsWith("http")) newPlaylistUrl else token + newPlaylistUrl
                                    } else {
                                        if (newPlaylistUrl.endsWith(token)) newPlaylistUrl else newPlaylistUrl + token
                                    }
                                },
                                scaleUpRatio = 1.06f,
                                elevation = 0.dp,
                                cardBackgroundColor = Color.Transparent,
                                useDefaultBorder = false,
                                modifier = Modifier.height(36.dp)
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isFocused) TvAccentGold else Color(0xFF1A2540)
                                        )
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = token,
                                        color = if (isFocused) Color.Black else TvAccentGold,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TvActionButton(
                            text = stringResource(R.string.settings_iptv_add_button),
                            icon = Icons.Default.Add,
                            onClick = {
                                if (newPlaylistUrl.isNotBlank()) {
                                    viewModel.addIptvPlaylist(newPlaylistName, newPlaylistUrl)
                                    newPlaylistName = ""
                                    newPlaylistUrl = ""
                                }
                            },
                            baseColor = TvAccentGold.copy(alpha = 0.90f),
                            focusedColor = TvAccentGold
                        )
                    }
                }
            }
        }

        item {
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.PlayArrow,
                    title = stringResource(R.string.settings_iptv_auto_resume_title),
                    subtitle = stringResource(R.string.settings_iptv_auto_resume_desc)
                )
                Spacer(modifier = Modifier.height(14.dp))
                TvSelectField(
                    label = stringResource(R.string.settings_iptv_auto_resume_title),
                    options = listOf(
                        false to stringResource(R.string.settings_seconds_disabled),
                        true to stringResource(R.string.settings_seconds_enabled)
                    ),
                    selectedOption = autoResumeLastChannel,
                    onOptionSelected = { viewModel.setAutoResumeLastChannel(it) }
                )
            }
        }

        item {
            val detectedEpgUrl = activeIptvPlaylist?.epgUrl.orEmpty()
            SettingsGlassCard {
                SettingsCardHeader(
                    icon = Icons.Default.CalendarMonth,
                    title = stringResource(R.string.settings_iptv_epg_title),
                    subtitle = if (detectedEpgUrl.isNotBlank()) {
                        "${stringResource(R.string.settings_iptv_epg_desc)} ($detectedEpgUrl)"
                    } else {
                        stringResource(R.string.settings_iptv_epg_desc)
                    }
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvTextField(
                        value = epgUrlInput,
                        onValueChange = { epgUrlInput = it },
                        placeholder = stringResource(R.string.settings_iptv_epg_hint),
                        modifier = Modifier.weight(1f)
                    )
                    TvActionButton(
                        text = stringResource(R.string.settings_iptv_epg_save),
                        icon = Icons.Default.Check,
                        onClick = { viewModel.setEpgUrlOverride(epgUrlInput) },
                        baseColor = TvAccentGold.copy(alpha = 0.88f),
                        focusedColor = TvAccentGold,
                        modifier = Modifier.height(52.dp)
                    )
                    TvActionButton(
                        text = stringResource(R.string.settings_iptv_epg_reload),
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.refreshEpg() },
                        baseColor = Color(0xFF23304A),
                        focusedColor = TvAccentGold,
                        modifier = Modifier.height(52.dp)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_iptv_saved_header).format(iptvPlaylists.size),
                    color = TvTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (iptvPlaylists.isNotEmpty()) {
                    TvActionButton(
                        text = stringResource(R.string.settings_iptv_delete_all),
                        icon = Icons.Default.Delete,
                        onClick = { viewModel.deleteAllIptvPlaylists() },
                        baseColor = TvAccentRed.copy(alpha = 0.25f),
                        focusedColor = TvAccentRed
                    )
                }
            }
        }

        if (iptvPlaylists.isEmpty()) {
            item {
                SettingsGlassCard {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.settings_iptv_no_saved),
                            color = TvTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            val playlistChunks = iptvPlaylists.chunked(2)
            items(playlistChunks, key = { chunk -> chunk.first().id }) { chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    chunk.forEach { playlist ->
                        val isActive = (playlist.id == activeIptvPlaylist?.id)
                        Box(modifier = Modifier.weight(1f)) {
                            SettingsGlassCard(
                                highlightColor = TvAccentGold,
                                highlightActive = isActive
                            ) {
                                TvFocusableCard(
                                    onClick = {
                                        if (isActive) {
                                            viewModel.refreshIptvPlaylist()
                                        } else {
                                            viewModel.switchIptvPlaylist(playlist.id)
                                        }
                                    },
                                    scaleUpRatio = 1.02f,
                                    elevation = 0.dp,
                                    cardBackgroundColor = Color.Transparent,
                                    useDefaultBorder = false,
                                    modifier = Modifier.fillMaxWidth()
                                ) { cardFocused ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (cardFocused) TvAccentGold.copy(alpha = 0.22f)
                                                else Color.White.copy(alpha = 0.05f)
                                            )
                                            .padding(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isActive || cardFocused) TvAccentGold.copy(alpha = 0.22f)
                                                    else Color.White.copy(alpha = 0.08f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = if (isActive || cardFocused) TvAccentGoldRadiant else TvTextPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = playlist.name,
                                                color = TvTextPrimary,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = pluralStringResource(R.plurals.settings_iptv_channels_count, playlist.channelCount, playlist.channelCount),
                                                color = if (isActive || cardFocused) TvAccentGoldRadiant else TvTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(TvAccentGold)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.settings_iptv_active_tag),
                                                    color = Color.Black,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = playlist.url,
                                    color = TvTextSecondary.copy(alpha = 0.72f),
                                    fontSize = 11.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!isActive) {
                                        TvActionButton(
                                            text = stringResource(R.string.settings_iptv_activate_button),
                                            onClick = { viewModel.switchIptvPlaylist(playlist.id) },
                                            baseColor = TvAccentGold,
                                            focusedColor = TvAccentGold,
                                            fillWidth = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    } else {
                                        TvActionButton(
                                            text = stringResource(R.string.settings_iptv_sync_button),
                                            icon = Icons.Default.Refresh,
                                            onClick = { viewModel.refreshIptvPlaylist() },
                                            baseColor = TvAccentGold.copy(alpha = 0.85f),
                                            focusedColor = TvAccentGold,
                                            fillWidth = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    TvActionButton(
                                        text = stringResource(R.string.action_delete),
                                        icon = Icons.Default.Delete,
                                        onClick = { viewModel.deleteIptvPlaylist(playlist.id) },
                                        baseColor = TvAccentRed.copy(alpha = 0.25f),
                                        focusedColor = TvAccentRed
                                    )
                                }
                            }
                        }
                    }

                    if (chunk.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
