package com.example.ui.home

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.R
import com.example.data.local.LauncherPreferences
import com.example.ui.common.AuroraColors
import com.example.ui.news.NewsVoicePlaybackTracker
import com.example.ui.news.rememberNewsVoiceReader
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.util.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

internal val SHARED_AMBIENT_MUSIC_STREAMS = mapOf(
    "lofi_chill" to "https://cdn.pixabay.com/download/audio/2022/05/27/audio_1808fbf07a.mp3",
    "nature_rain" to "https://cdn.pixabay.com/download/audio/2022/03/10/audio_5b3650f688.mp3",
    "ambient_space" to "https://cdn.pixabay.com/download/audio/2022/01/18/audio_d0a13f69d2.mp3",
    "oriental_cafe" to "https://cdn.pixabay.com/download/audio/2022/10/25/audio_946bc6eb4c.mp3"
)

internal fun resolveAmbientMusicUrl(musicMode: String, customMusicUri: String): String {
    return when (musicMode) {
        "off" -> ""
        "custom_file" -> customMusicUri.trim()
        else -> SHARED_AMBIENT_MUSIC_STREAMS[musicMode] ?: ""
    }
}

internal fun parseBroadcastLines(rawText: String, fallbackDefaultText: String): List<String> {
    val parsed = rawText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (parsed.isNotEmpty()) return parsed
    return fallbackDefaultText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .split('\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}

internal fun shouldWaitForBroadcastLineSpeech(
    isVoiceEnabled: Boolean,
    speechCompleted: Boolean
): Boolean = isVoiceEnabled && !speechCompleted

internal fun buildBroadcastSpeechKey(lineIndex: Int, lineText: String): String =
    "broadcast_${lineIndex}_${lineText.hashCode()}"

internal fun takeBroadcastCodePoints(text: String, fraction: Float): String {
    if (text.isEmpty()) return ""
    val clamped = fraction.coerceIn(0f, 1f)
    if (clamped >= 0.999f) return text
    val totalCodePoints = text.codePointCount(0, text.length)
    if (totalCodePoints <= 0) return ""
    val targetCodePoints = (totalCodePoints * clamped).roundToInt().coerceIn(0, totalCodePoints)
    if (targetCodePoints == 0) return ""
    if (targetCodePoints >= totalCodePoints) return text
    val endCharIndex = text.offsetByCodePoints(0, targetCodePoints)
    return text.substring(0, endCharIndex)
}

internal object BroadcastThemeIds {
    const val STUDIO_CLASSIC = "studio_classic"
    const val BREAKING_NEWS = "breaking_news"
    const val ROYAL_CINEMA = "royal_cinema"
    const val NEON_CYBER = "neon_cyber"
    const val EMERALD_SPORT = "emerald_sport"
    const val MINIMAL_ZEN = "minimal_zen"
}

internal enum class BroadcastMotionStyle {
    HORIZONTAL_GLIDE,
    LOWER_THIRD_RISE,
    CINEMA_ZOOM_DISSOLVE,
    CYBER_TYPEWRITER,
    DIAGONAL_SLATE,
    GENTLE_FLOAT
}

internal enum class BroadcastLayoutMode {
    CENTER_STAGE,
    LOWER_THIRD_DESK,
    CINEMA_LETTERBOX,
    HUD_VIEWFINDER,
    DIAGONAL_ARENA,
    EDITORIAL_MINIMAL
}

internal enum class BroadcastFrameStyle {
    SIDE_BARS,
    BREAKING_TICKER,
    CINEMA_RULES,
    CYBER_BRACKETS,
    ARENA_BEAMS,
    MINIMAL_UNDERLINE
}

internal data class BroadcastBackdropPalette(
    val baseTop: Color,
    val baseBottom: Color,
    val shiftTop: Color,
    val shiftBottom: Color,
    val shadowColorPrimary: Color,
    val shadowColorContrast: Color,
    val vignetteTint: Color
)

internal data class BroadcastThemeSpec(
    val id: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val badgeAccentColor: Color,
    val backdropTint: Color,
    val dimAlpha: Float,
    val glowPrimary: Color,
    val glowSecondary: Color,
    val motionStyle: BroadcastMotionStyle,
    val layoutMode: BroadcastLayoutMode,
    val frameStyle: BroadcastFrameStyle,
    val backdropPalette: BroadcastBackdropPalette,
    val showCyberGrid: Boolean = false
)

internal fun resolveBroadcastThemeSpec(themeId: String): BroadcastThemeSpec {
    return when (themeId) {
        BroadcastThemeIds.BREAKING_NEWS -> BroadcastThemeSpec(
            id = BroadcastThemeIds.BREAKING_NEWS,
            primaryColor = Color(0xFFFFFFFF),
            secondaryColor = Color(0xFFFF453A),
            badgeAccentColor = Color(0xFFFF3B30),
            backdropTint = Color(0xFF140408),
            dimAlpha = 0.28f,
            glowPrimary = Color(0xFFDC2626),
            glowSecondary = Color(0xFFFF9F0A),
            motionStyle = BroadcastMotionStyle.LOWER_THIRD_RISE,
            layoutMode = BroadcastLayoutMode.LOWER_THIRD_DESK,
            frameStyle = BroadcastFrameStyle.BREAKING_TICKER,
            backdropPalette = BroadcastBackdropPalette(
                baseTop = Color(0xFF150409),
                baseBottom = Color(0xFF280711),
                shiftTop = Color(0xFF0A0C20),
                shiftBottom = Color(0xFF1E0806),
                shadowColorPrimary = Color(0xFFB91C1C),
                shadowColorContrast = Color(0xFF1D4ED8),
                vignetteTint = Color(0xFF080204)
            )
        )
        BroadcastThemeIds.ROYAL_CINEMA -> BroadcastThemeSpec(
            id = BroadcastThemeIds.ROYAL_CINEMA,
            primaryColor = Color(0xFFFFF8E7),
            secondaryColor = Color(0xFFF5C451),
            badgeAccentColor = Color(0xFFD4AF37),
            backdropTint = Color(0xFF0D0904),
            dimAlpha = 0.28f,
            glowPrimary = Color(0xFFD4AF37),
            glowSecondary = Color(0xFFFDE68A),
            motionStyle = BroadcastMotionStyle.CINEMA_ZOOM_DISSOLVE,
            layoutMode = BroadcastLayoutMode.CINEMA_LETTERBOX,
            frameStyle = BroadcastFrameStyle.CINEMA_RULES,
            backdropPalette = BroadcastBackdropPalette(
                baseTop = Color(0xFF0E0A04),
                baseBottom = Color(0xFF201609),
                shiftTop = Color(0xFF190816),
                shiftBottom = Color(0xFF130D06),
                shadowColorPrimary = Color(0xFF9A5B13),
                shadowColorContrast = Color(0xFF6B21A8),
                vignetteTint = Color(0xFF050301)
            )
        )
        BroadcastThemeIds.NEON_CYBER -> BroadcastThemeSpec(
            id = BroadcastThemeIds.NEON_CYBER,
            primaryColor = Color(0xFF22D3EE),
            secondaryColor = Color(0xFFFF4D8D),
            badgeAccentColor = Color(0xFF06B6D4),
            backdropTint = Color(0xFF030816),
            dimAlpha = 0.26f,
            glowPrimary = Color(0xFF06B6D4),
            glowSecondary = Color(0xFFEC4899),
            motionStyle = BroadcastMotionStyle.CYBER_TYPEWRITER,
            layoutMode = BroadcastLayoutMode.HUD_VIEWFINDER,
            frameStyle = BroadcastFrameStyle.CYBER_BRACKETS,
            backdropPalette = BroadcastBackdropPalette(
                baseTop = Color(0xFF030A18),
                baseBottom = Color(0xFF081B2E),
                shiftTop = Color(0xFF170626),
                shiftBottom = Color(0xFF051828),
                shadowColorPrimary = Color(0xFF0E7490),
                shadowColorContrast = Color(0xFFBE185D),
                vignetteTint = Color(0xFF02040C)
            ),
            showCyberGrid = true
        )
        BroadcastThemeIds.EMERALD_SPORT -> BroadcastThemeSpec(
            id = BroadcastThemeIds.EMERALD_SPORT,
            primaryColor = Color(0xFFFFFFFF),
            secondaryColor = Color(0xFF34D399),
            badgeAccentColor = Color(0xFF10B981),
            backdropTint = Color(0xFF03120D),
            dimAlpha = 0.28f,
            glowPrimary = Color(0xFF10B981),
            glowSecondary = Color(0xFF38BDF8),
            motionStyle = BroadcastMotionStyle.DIAGONAL_SLATE,
            layoutMode = BroadcastLayoutMode.DIAGONAL_ARENA,
            frameStyle = BroadcastFrameStyle.ARENA_BEAMS,
            backdropPalette = BroadcastBackdropPalette(
                baseTop = Color(0xFF03120D),
                baseBottom = Color(0xFF08261C),
                shiftTop = Color(0xFF041425),
                shiftBottom = Color(0xFF051D1A),
                shadowColorPrimary = Color(0xFF047857),
                shadowColorContrast = Color(0xFF0369A1),
                vignetteTint = Color(0xFF010906)
            )
        )
        BroadcastThemeIds.MINIMAL_ZEN -> BroadcastThemeSpec(
            id = BroadcastThemeIds.MINIMAL_ZEN,
            primaryColor = Color(0xFFF8FAFC),
            secondaryColor = Color(0xFF93C5FD),
            badgeAccentColor = Color(0xFF60A5FA),
            backdropTint = Color(0xFF060A13),
            dimAlpha = 0.30f,
            glowPrimary = Color(0xFF60A5FA),
            glowSecondary = Color(0xFFCBD5E1),
            motionStyle = BroadcastMotionStyle.GENTLE_FLOAT,
            layoutMode = BroadcastLayoutMode.EDITORIAL_MINIMAL,
            frameStyle = BroadcastFrameStyle.MINIMAL_UNDERLINE,
            backdropPalette = BroadcastBackdropPalette(
                baseTop = Color(0xFF060A14),
                baseBottom = Color(0xFF101A2C),
                shiftTop = Color(0xFF140D21),
                shiftBottom = Color(0xFF091B23),
                shadowColorPrimary = Color(0xFF1E3A5F),
                shadowColorContrast = Color(0xFF5B21B6),
                vignetteTint = Color(0xFF03050B)
            )
        )
        else -> BroadcastThemeSpec(
            id = BroadcastThemeIds.STUDIO_CLASSIC,
            primaryColor = Color(0xFFFFFFFF),
            secondaryColor = Color(0xFFFFD60A),
            badgeAccentColor = Color(0xFFFFD60A),
            backdropTint = Color(0xFF040814),
            dimAlpha = 0.26f,
            glowPrimary = Color(0xFF38BDF8),
            glowSecondary = Color(0xFFFFD60A),
            motionStyle = BroadcastMotionStyle.HORIZONTAL_GLIDE,
            layoutMode = BroadcastLayoutMode.CENTER_STAGE,
            frameStyle = BroadcastFrameStyle.SIDE_BARS,
            backdropPalette = BroadcastBackdropPalette(
                baseTop = Color(0xFF050A18),
                baseBottom = Color(0xFF0B1830),
                shiftTop = Color(0xFF110B24),
                shiftBottom = Color(0xFF071A24),
                shadowColorPrimary = Color(0xFF1D4ED8),
                shadowColorContrast = Color(0xFFB45309),
                vignetteTint = Color(0xFF02040B)
            )
        )
    }
}

private val BroadcastWhite = Color(0xFFFFFFFF)
private val BroadcastExpoOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val BroadcastExpoIn = CubicBezierEasing(0.7f, 0f, 0.84f, 0f)
private val BroadcastSilkEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * Reusable ExoPlayer background audio controller for screensavers (Photo Slideshow, Dashboard & Broadcast Text).
 * Supports built-in ambient audio streams as well as local device audio files (content://, file://, /path).
 * Automatically ducks volume while TTS speech is active when [duckDuringSpeech] is enabled.
 */
@OptIn(UnstableApi::class)
@Composable
internal fun ScreensaverAmbientAudioEffect(
    musicMode: String,
    customMusicUri: String,
    volumePercent: Int,
    duckDuringSpeech: Boolean = false
) {
    val context = LocalContext.current
    val resolvedMusicUrl = remember(musicMode, customMusicUri) {
        resolveAmbientMusicUrl(musicMode, customMusicUri)
    }
    val isSpeechActive by NewsVoicePlaybackTracker.isSpeakingFlow.collectAsState()

    if (resolvedMusicUrl.isBlank()) return

    val musicPlayer = remember(context) {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(
                "Mozilla/5.0 (Linux; Android 13; Android TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            )
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(12_000)
            .setReadTimeoutMs(15_000)
        val dataSourceFactory = DefaultDataSource.Factory(context, httpFactory)
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                false
            )
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
            }
    }

    LaunchedEffect(resolvedMusicUrl) {
        try {
            val mediaItem = if (resolvedMusicUrl.startsWith("/")) {
                MediaItem.fromUri(Uri.fromFile(File(resolvedMusicUrl)))
            } else {
                MediaItem.fromUri(resolvedMusicUrl)
            }
            musicPlayer.setMediaItem(mediaItem)
            musicPlayer.prepare()
            musicPlayer.playWhenReady = true
        } catch (_: Exception) {}
    }

    LaunchedEffect(volumePercent, duckDuringSpeech, isSpeechActive) {
        try {
            val baseVol = volumePercent.coerceIn(0, 100) / 100f
            val duckFactor = if (duckDuringSpeech && isSpeechActive) 0.24f else 1f
            musicPlayer.volume = (baseVol * duckFactor).coerceIn(0f, 1f)
        } catch (_: Exception) {}
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, musicPlayer) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    try {
                        musicPlayer.playWhenReady = false
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_START -> {
                    try {
                        musicPlayer.playWhenReady = true
                    } catch (_: Exception) {}
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                musicPlayer.stop()
                musicPlayer.release()
            } catch (_: Exception) {}
        }
    }
}

/**
 * Broadcast / Media Text Screensaver:
 * - Six distinct layout, entrance/exit choreography & broadcast-graphic themes.
 * - Optional Text-To-Speech Voice Reader that finishes reading the active line before advancing.
 * - Optional ambient background audio with automatic voice ducking.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun BroadcastTextScreensaver(
    is24hFormat: Boolean,
    auroraColors: AuroraColors,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        (context.applicationContext as? com.example.AuraTvApp)?.preferences ?: LauncherPreferences(context)
    }

    val lang by prefs.languageFlow.collectAsState(initial = "system")
    val rawBroadcastText by prefs.broadcastTextLinesFlow.collectAsState(initial = "")
    val lineDurationSec by prefs.broadcastLineDurationSecFlow.collectAsState(initial = 5)
    val musicMode by prefs.broadcastMusicModeFlow.collectAsState(initial = "off")
    val customMusicUri by prefs.broadcastCustomMusicUriFlow.collectAsState(initial = "")
    val musicVolume by prefs.broadcastMusicVolumeFlow.collectAsState(initial = 50)
    val broadcastThemeId by prefs.broadcastThemeFlow.collectAsState(initial = BroadcastThemeIds.STUDIO_CLASSIC)
    val isVoiceEnabled by prefs.broadcastVoiceEnabledFlow.collectAsState(initial = false)
    val themeSpec = remember(broadcastThemeId) { resolveBroadcastThemeSpec(broadcastThemeId) }

    val defaultLinesText = stringResource(R.string.broadcast_text_default_lines)
    val lines = remember(rawBroadcastText, defaultLinesText) {
        parseBroadcastLines(rawBroadcastText, defaultLinesText)
    }

    val resolvedMusicUrl = remember(musicMode, customMusicUri) {
        resolveAmbientMusicUrl(musicMode, customMusicUri)
    }
    val isAudioActive = resolvedMusicUrl.isNotBlank()

    // Background Audio Engine (auto-ducks softly while Broadcast Voice Reader speaks)
    ScreensaverAmbientAudioEffect(
        musicMode = musicMode,
        customMusicUri = customMusicUri,
        volumePercent = musicVolume,
        duckDuringSpeech = isVoiceEnabled
    )

    // Broadcast TTS Voice Reader (instantiated when enabled; released cleanly on disable/exit)
    val voiceReader = if (isVoiceEnabled) rememberNewsVoiceReader() else null
    val latestVoiceEnabled by rememberUpdatedState(isVoiceEnabled)
    val latestVoiceReader by rememberUpdatedState(voiceReader)

    var currentIndex by remember(lines) { mutableIntStateOf(0) }
    var manualSkipTrigger by remember { mutableIntStateOf(0) }
    val mountedAtMs = remember { System.currentTimeMillis() }

    val safeIndex = currentIndex.coerceIn(0, (lines.size - 1).coerceAtLeast(0))
    val activeLine = lines.getOrElse(safeIndex) { "" }
    val currentSpeechKey = remember(safeIndex, activeLine, manualSkipTrigger) {
        buildBroadcastSpeechKey(safeIndex, activeLine)
    }

    // Trigger spoken audio for the active line when Voice Reader is active
    LaunchedEffect(isVoiceEnabled, currentSpeechKey, lang) {
        if (isVoiceEnabled && activeLine.isNotBlank()) {
            val spokenLang = if (LocaleHelper.containsArabicScript(activeLine)) {
                "ar"
            } else {
                LocaleHelper.getEffectiveLanguage(lang)
            }
            voiceReader?.speakStory(
                storyKey = currentSpeechKey,
                text = activeLine,
                effectiveLang = spokenLang
            )
        } else {
            voiceReader?.stop()
        }
    }

    // Clock HUD state
    var timeStr by remember { mutableStateOf("") }
    var amPmStr by remember { mutableStateOf("") }
    LaunchedEffect(is24hFormat, lang) {
        val pattern = if (is24hFormat) "HH:mm" else "hh:mm"
        val timeFmt = SimpleDateFormat(pattern, Locale.US)
        while (true) {
            val cal = Calendar.getInstance()
            timeStr = timeFmt.format(cal.time)
            amPmStr = if (!is24hFormat) LocaleHelper.getAmPmString(cal.get(Calendar.HOUR_OF_DAY), lang) else ""
            delay(1000L)
        }
    }

    // Broadcast Animation States (tailored per BroadcastMotionStyle)
    val textAlpha = remember { Animatable(0f) }
    val textSlideX = remember { Animatable(1f) }
    val textSlideY = remember { Animatable(1f) }
    val textScale = remember { Animatable(1f) }
    val textScaleY = remember { Animatable(1f) }
    val textTiltDeg = remember { Animatable(0f) }
    val accentBarScale = remember { Animatable(0f) }
    val chromeReveal = remember { Animatable(0f) }
    val typewriterProgress = remember { Animatable(1f) }
    val lightSweep = remember { Animatable(0f) }
    val lineProgress = remember { Animatable(0f) }

    LaunchedEffect(lines, currentIndex, lineDurationSec, manualSkipTrigger, themeSpec.motionStyle) {
        if (lines.isEmpty()) return@LaunchedEffect

        val motion = themeSpec.motionStyle
        val totalHoldMs = (lineDurationSec.coerceIn(3, 30) * 1000L)

        // Snap initial transforms specifically for the selected BroadcastMotionStyle
        accentBarScale.snapTo(0f)
        chromeReveal.snapTo(0f)
        lightSweep.snapTo(0f)
        lineProgress.snapTo(0f)
        textScaleY.snapTo(1f)
        textTiltDeg.snapTo(0f)
        typewriterProgress.snapTo(1f)

        when (motion) {
            BroadcastMotionStyle.HORIZONTAL_GLIDE -> {
                textAlpha.snapTo(0f)
                textSlideX.snapTo(1f)
                textSlideY.snapTo(0.25f)
                textScale.snapTo(0.94f)
            }
            BroadcastMotionStyle.LOWER_THIRD_RISE -> {
                textAlpha.snapTo(0f)
                textSlideX.snapTo(0f)
                textSlideY.snapTo(1.25f)
                textScale.snapTo(0.98f)
            }
            BroadcastMotionStyle.CINEMA_ZOOM_DISSOLVE -> {
                textAlpha.snapTo(0f)
                textSlideX.snapTo(0f)
                textSlideY.snapTo(0f)
                textScale.snapTo(1.16f)
            }
            BroadcastMotionStyle.CYBER_TYPEWRITER -> {
                textAlpha.snapTo(0f)
                textSlideX.snapTo(0f)
                textSlideY.snapTo(0f)
                textScale.snapTo(0.98f)
                typewriterProgress.snapTo(0f)
            }
            BroadcastMotionStyle.DIAGONAL_SLATE -> {
                textAlpha.snapTo(0f)
                textSlideX.snapTo(1f)
                textSlideY.snapTo(1f)
                textScale.snapTo(0.90f)
                textTiltDeg.snapTo(-2.6f)
            }
            BroadcastMotionStyle.GENTLE_FLOAT -> {
                textAlpha.snapTo(0f)
                textSlideX.snapTo(0f)
                textSlideY.snapTo(1f)
                textScale.snapTo(0.99f)
            }
        }

        val enterDurationMs = when (motion) {
            BroadcastMotionStyle.CINEMA_ZOOM_DISSOLVE -> 920
            BroadcastMotionStyle.CYBER_TYPEWRITER -> 1050
            BroadcastMotionStyle.GENTLE_FLOAT -> 850
            BroadcastMotionStyle.LOWER_THIRD_RISE -> 520
            BroadcastMotionStyle.DIAGONAL_SLATE -> 580
            BroadcastMotionStyle.HORIZONTAL_GLIDE -> 640
        }
        val exitDurationMs = when (motion) {
            BroadcastMotionStyle.CINEMA_ZOOM_DISSOLVE -> 700
            BroadcastMotionStyle.CYBER_TYPEWRITER -> 360
            BroadcastMotionStyle.GENTLE_FLOAT -> 640
            else -> 440
        }
        val steadyHoldMs = (totalHoldMs - enterDurationMs - exitDurationMs).coerceAtLeast(1400L)

        // Progress sweeps smoothly across scheduled enter + steady hold
        val progressJob = launch {
            lineProgress.animateTo(
                targetValue = 0.96f,
                animationSpec = tween(
                    durationMillis = (enterDurationMs + steadyHoldMs).toInt(),
                    easing = LinearEasing
                )
            )
        }

        // Phase 1: Theme-Distinct Entrance Choreography
        val chromeJob = launch {
            chromeReveal.animateTo(1f, animationSpec = tween(460, easing = BroadcastExpoOut))
        }
        val barJob = launch {
            accentBarScale.animateTo(1f, animationSpec = tween(460, easing = BroadcastExpoOut))
        }
        val sweepJob = launch {
            lightSweep.animateTo(1f, animationSpec = tween(760, easing = BroadcastExpoOut))
        }

        val enterJobs = when (motion) {
            BroadcastMotionStyle.HORIZONTAL_GLIDE -> listOf(
                launch { delay(60); textAlpha.animateTo(1f, tween(480, easing = BroadcastExpoOut)) },
                launch { delay(60); textSlideX.animateTo(0f, tween(enterDurationMs, easing = BroadcastExpoOut)) },
                launch { delay(60); textSlideY.animateTo(0f, tween(enterDurationMs, easing = BroadcastExpoOut)) },
                launch { delay(60); textScale.animateTo(1f, tween(enterDurationMs, easing = BroadcastExpoOut)) }
            )
            BroadcastMotionStyle.LOWER_THIRD_RISE -> listOf(
                launch { textAlpha.animateTo(1f, tween(340, easing = BroadcastExpoOut)) },
                launch { textSlideY.animateTo(0f, tween(enterDurationMs, easing = BroadcastExpoOut)) },
                launch { textScale.animateTo(1f, tween(enterDurationMs, easing = BroadcastExpoOut)) }
            )
            BroadcastMotionStyle.CINEMA_ZOOM_DISSOLVE -> listOf(
                launch { delay(90); textAlpha.animateTo(1f, tween(760, easing = BroadcastSilkEasing)) },
                launch { textScale.animateTo(1f, tween(enterDurationMs, easing = BroadcastSilkEasing)) }
            )
            BroadcastMotionStyle.CYBER_TYPEWRITER -> listOf(
                launch { textAlpha.animateTo(1f, tween(220, easing = LinearEasing)) },
                launch { textScale.animateTo(1f, tween(320, easing = BroadcastExpoOut)) },
                launch {
                    delay(120)
                    typewriterProgress.animateTo(1f, tween(enterDurationMs - 120, easing = LinearEasing))
                }
            )
            BroadcastMotionStyle.DIAGONAL_SLATE -> listOf(
                launch { delay(40); textAlpha.animateTo(1f, tween(420, easing = BroadcastExpoOut)) },
                launch { delay(40); textSlideX.animateTo(0f, tween(enterDurationMs, easing = BroadcastExpoOut)) },
                launch { delay(40); textSlideY.animateTo(0f, tween(enterDurationMs, easing = BroadcastExpoOut)) },
                launch { delay(40); textScale.animateTo(1f, tween(enterDurationMs, easing = BroadcastExpoOut)) },
                launch { delay(40); textTiltDeg.animateTo(0f, tween(enterDurationMs, easing = BroadcastExpoOut)) }
            )
            BroadcastMotionStyle.GENTLE_FLOAT -> listOf(
                launch { delay(80); textAlpha.animateTo(1f, tween(700, easing = BroadcastSilkEasing)) },
                launch { delay(80); textSlideY.animateTo(0f, tween(enterDurationMs, easing = BroadcastSilkEasing)) },
                launch { delay(80); textScale.animateTo(1f, tween(enterDurationMs, easing = BroadcastSilkEasing)) }
            )
        }

        chromeJob.join()
        barJob.join()
        sweepJob.join()
        enterJobs.forEach { it.join() }

        // Phase 2: Theme-Specific On-Air Hold
        when (motion) {
            BroadcastMotionStyle.HORIZONTAL_GLIDE -> {
                textScale.animateTo(1.032f, tween(steadyHoldMs.toInt(), easing = LinearEasing))
            }
            BroadcastMotionStyle.CINEMA_ZOOM_DISSOLVE -> {
                textScale.animateTo(1.022f, tween(steadyHoldMs.toInt(), easing = LinearEasing))
            }
            BroadcastMotionStyle.DIAGONAL_SLATE -> {
                textScale.animateTo(1.035f, tween(steadyHoldMs.toInt(), easing = LinearEasing))
            }
            BroadcastMotionStyle.LOWER_THIRD_RISE,
            BroadcastMotionStyle.CYBER_TYPEWRITER,
            BroadcastMotionStyle.GENTLE_FLOAT -> {
                delay(steadyHoldMs)
            }
        }
        progressJob.join()

        // Voice Reader Guard: If reading aloud is enabled and not finished yet, wait before exit
        var extraWaitMs = 0L
        while (
            shouldWaitForBroadcastLineSpeech(
                isVoiceEnabled = latestVoiceEnabled,
                speechCompleted = latestVoiceReader?.hasCompletedStory(currentSpeechKey) ?: true
            ) && extraWaitMs < 120_000L
        ) {
            delay(100L)
            extraWaitMs += 100L
        }
        lineProgress.animateTo(1f, tween(160, easing = LinearEasing))

        // Phase 3: Theme-Distinct Exit Choreography
        val exitJobs = when (motion) {
            BroadcastMotionStyle.HORIZONTAL_GLIDE -> listOf(
                launch { textAlpha.animateTo(0f, tween(exitDurationMs - 40, easing = BroadcastExpoIn)) },
                launch { textSlideX.animateTo(-1f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { textSlideY.animateTo(-0.35f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { textScale.animateTo(0.95f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { delay(80); accentBarScale.animateTo(0f, tween(exitDurationMs - 80, easing = BroadcastExpoIn)) },
                launch { delay(80); chromeReveal.animateTo(0f, tween(exitDurationMs - 80, easing = BroadcastExpoIn)) }
            )
            BroadcastMotionStyle.LOWER_THIRD_RISE -> listOf(
                launch { textAlpha.animateTo(0f, tween(exitDurationMs - 60, easing = BroadcastExpoIn)) },
                launch { textSlideY.animateTo(1.3f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { delay(60); chromeReveal.animateTo(0f, tween(exitDurationMs - 60, easing = BroadcastExpoIn)) },
                launch { delay(60); accentBarScale.animateTo(0f, tween(exitDurationMs - 60, easing = BroadcastExpoIn)) }
            )
            BroadcastMotionStyle.CINEMA_ZOOM_DISSOLVE -> listOf(
                launch { textAlpha.animateTo(0f, tween(exitDurationMs, easing = BroadcastSilkEasing)) },
                launch { textScale.animateTo(1.07f, tween(exitDurationMs, easing = BroadcastSilkEasing)) },
                launch { delay(100); accentBarScale.animateTo(0f, tween(exitDurationMs - 100, easing = BroadcastSilkEasing)) },
                launch { delay(100); chromeReveal.animateTo(0f, tween(exitDurationMs - 100, easing = BroadcastSilkEasing)) }
            )
            BroadcastMotionStyle.CYBER_TYPEWRITER -> listOf(
                launch { textAlpha.animateTo(0f, tween(exitDurationMs - 40, easing = BroadcastExpoIn)) },
                launch { textScaleY.animateTo(0.12f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { textScale.animateTo(1.06f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { chromeReveal.animateTo(0f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { accentBarScale.animateTo(0f, tween(exitDurationMs, easing = BroadcastExpoIn)) }
            )
            BroadcastMotionStyle.DIAGONAL_SLATE -> listOf(
                launch { textAlpha.animateTo(0f, tween(exitDurationMs - 40, easing = BroadcastExpoIn)) },
                launch { textSlideX.animateTo(-1f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { textSlideY.animateTo(-1f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { textTiltDeg.animateTo(2.4f, tween(exitDurationMs, easing = BroadcastExpoIn)) },
                launch { delay(60); chromeReveal.animateTo(0f, tween(exitDurationMs - 60, easing = BroadcastExpoIn)) },
                launch { delay(60); accentBarScale.animateTo(0f, tween(exitDurationMs - 60, easing = BroadcastExpoIn)) }
            )
            BroadcastMotionStyle.GENTLE_FLOAT -> listOf(
                launch { textAlpha.animateTo(0f, tween(exitDurationMs, easing = BroadcastSilkEasing)) },
                launch { textSlideY.animateTo(-1f, tween(exitDurationMs, easing = BroadcastSilkEasing)) },
                launch { accentBarScale.animateTo(0f, tween(exitDurationMs, easing = BroadcastSilkEasing)) },
                launch { chromeReveal.animateTo(0f, tween(exitDurationMs, easing = BroadcastSilkEasing)) }
            )
        }
        exitJobs.forEach { it.join() }

        delay(110L)

        if (lines.size > 1) {
            currentIndex = (currentIndex + 1) % lines.size
        } else {
            manualSkipTrigger++
        }
    }

    // Ambient theme loops (studio drift, live dot pulse, scanner beam, cursor blink)
    val infiniteTransition = rememberInfiniteTransition(label = "broadcast_studio_ambient")
    val studioPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "studio_drift"
    )
    val livePulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse"
    )
    val scanLineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cyber_scan"
    )
    val backdropMoodShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backdrop_mood_shift"
    )

    val isSecondaryTurn = (safeIndex % 2 == 1)
    val headlineColor = if (isSecondaryTurn) themeSpec.secondaryColor else themeSpec.primaryColor
    val studioAccentColor = if (isSecondaryTurn) themeSpec.primaryColor else themeSpec.secondaryColor

    val isRtlLayout = LocalLayoutDirection.current == LayoutDirection.Rtl
    val hasArabic = LocaleHelper.isArabicOrRtlLanguage(lang) || LocaleHelper.containsArabicScript(activeLine)
    val headlineFont = if (hasArabic) CairoFontFamily else FontFamily.Default
    val textLayoutDir = if (LocaleHelper.containsArabicScript(activeLine)) LayoutDirection.Rtl else LocalLayoutDirection.current

    val isDeskCompact = themeSpec.layoutMode == BroadcastLayoutMode.LOWER_THIRD_DESK
    val headlineFontSize = when {
        isDeskCompact && activeLine.length <= 36 -> 40.sp
        isDeskCompact && activeLine.length <= 72 -> 33.sp
        isDeskCompact -> 27.sp
        activeLine.length <= 26 -> 58.sp
        activeLine.length <= 48 -> 48.sp
        activeLine.length <= 82 -> 39.sp
        else -> 32.sp
    }
    val headlineLineHeight = when {
        isDeskCompact && activeLine.length <= 36 -> 52.sp
        isDeskCompact && activeLine.length <= 72 -> 44.sp
        isDeskCompact -> 37.sp
        activeLine.length <= 26 -> 74.sp
        activeLine.length <= 48 -> 64.sp
        activeLine.length <= 82 -> 54.sp
        else -> 46.sp
    }

    val density = LocalDensity.current
    val slideHorizontalPx = with(density) {
        when (themeSpec.motionStyle) {
            BroadcastMotionStyle.DIAGONAL_SLATE -> 108.dp.toPx()
            else -> 96.dp.toPx()
        }
    }
    val slideVerticalPx = with(density) {
        when (themeSpec.motionStyle) {
            BroadcastMotionStyle.LOWER_THIRD_RISE -> 140.dp.toPx()
            BroadcastMotionStyle.DIAGONAL_SLATE -> 48.dp.toPx()
            BroadcastMotionStyle.GENTLE_FLOAT -> 40.dp.toPx()
            else -> 26.dp.toPx()
        }
    }
    val horizontalSign = if (textLayoutDir == LayoutDirection.Rtl) 1f else -1f

    val displayedText = if (themeSpec.motionStyle == BroadcastMotionStyle.CYBER_TYPEWRITER) {
        val revealed = takeBroadcastCodePoints(activeLine, typewriterProgress.value)
        val showCursor = typewriterProgress.value < 0.99f && livePulse > 0.65f
        if (showCursor) "$revealed▌" else revealed
    } else {
        activeLine
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("broadcast_text_screensaver")
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
                if (event.type == KeyEventType.KeyDown && System.currentTimeMillis() - mountedAtMs > 450L) {
                    when (event.key) {
                        Key.DirectionRight -> {
                            if (lines.size > 1) {
                                currentIndex = if (isRtlLayout) {
                                    (safeIndex - 1 + lines.size) % lines.size
                                } else {
                                    (safeIndex + 1) % lines.size
                                }
                                manualSkipTrigger++
                            }
                            true
                        }
                        Key.DirectionLeft -> {
                            if (lines.size > 1) {
                                currentIndex = if (isRtlLayout) {
                                    (safeIndex + 1) % lines.size
                                } else {
                                    (safeIndex - 1 + lines.size) % lines.size
                                }
                                manualSkipTrigger++
                            }
                            true
                        }
                        else -> {
                            onDismiss()
                            true
                        }
                    }
                } else false
            }
    ) {
        // 1 & 2. Dedicated Per-Theme Dark Dynamic Background + Shifting Contrasting Colored Shadows
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val palette = themeSpec.backdropPalette

            // Smoothly morphing dark base gradient whose angle and tones evolve over time
            val topTone = lerp(palette.baseTop, palette.shiftTop, backdropMoodShift)
            val bottomTone = lerp(palette.baseBottom, palette.shiftBottom, 1f - backdropMoodShift)
            val midTone = lerp(
                lerp(topTone, bottomTone, 0.5f),
                palette.shadowColorContrast,
                0.14f + 0.10f * backdropMoodShift
            )
            val gradStartX = w * (0.18f + 0.22f * sin(studioPhase * 0.5f))
            val gradEndX = w * (0.82f - 0.22f * cos(studioPhase * 0.5f))

            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(topTone, midTone, bottomTone),
                    start = Offset(gradStartX, 0f),
                    end = Offset(gradEndX, h)
                )
            )

            // Drifting Orb 1: Primary Theme Atmospheric Shadow-Glow
            val cx1 = w * (0.26f + 0.18f * sin(studioPhase))
            val cy1 = h * (0.30f + 0.14f * cos(studioPhase))
            val orb1Color = lerp(palette.shadowColorPrimary, themeSpec.glowPrimary, 0.35f * backdropMoodShift)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        orb1Color.copy(alpha = 0.36f),
                        orb1Color.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(cx1, cy1),
                    radius = w * 0.44f
                ),
                center = Offset(cx1, cy1),
                radius = w * 0.44f
            )

            // Drifting Orb 2: Contrasting Different-Hue Shadow Pool that transforms the background
            val cx2 = w * (0.74f + 0.18f * cos(studioPhase * 0.85f))
            val cy2 = h * (0.68f + 0.15f * sin(studioPhase * 0.85f))
            val contrastColor = lerp(palette.shadowColorContrast, themeSpec.glowSecondary, 0.24f * (1f - backdropMoodShift))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        contrastColor.copy(alpha = 0.38f),
                        contrastColor.copy(alpha = 0.14f),
                        Color.Transparent
                    ),
                    center = Offset(cx2, cy2),
                    radius = w * 0.42f
                ),
                center = Offset(cx2, cy2),
                radius = w * 0.42f
            )

            // Drifting Orb 3: Deep Shadow Veil (dark complementary shadow gliding across the canvas)
            val cx3 = w * (0.50f + 0.26f * cos(studioPhase * 0.65f + 1.4f))
            val cy3 = h * (0.48f + 0.20f * sin(studioPhase * 0.65f + 1.4f))
            val deepShadowVeil = lerp(palette.vignetteTint, palette.shadowColorContrast, 0.26f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        deepShadowVeil.copy(alpha = 0.54f),
                        deepShadowVeil.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(cx3, cy3),
                    radius = w * 0.48f
                ),
                center = Offset(cx3, cy3),
                radius = w * 0.48f
            )

            // Cyber HUD grid + sweeping horizontal laser scan line
            if (themeSpec.showCyberGrid) {
                val gridColor = themeSpec.glowPrimary.copy(alpha = 0.085f)
                val step = w / 16f
                var gx = step
                while (gx < w) {
                    drawLine(gridColor, Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                    gx += step
                }
                var gy = step
                while (gy < h) {
                    drawLine(gridColor, Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                    gy += step
                }
                val scanY = h * scanLineProgress
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            themeSpec.glowPrimary.copy(alpha = 0.48f),
                            themeSpec.glowSecondary.copy(alpha = 0.56f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(0f, scanY),
                    end = Offset(w, scanY),
                    strokeWidth = 3f
                )
            }

            // Diagonal stadium spotlight beams for Emerald Sport Arena
            if (themeSpec.frameStyle == BroadcastFrameStyle.ARENA_BEAMS) {
                val sway = w * 0.03f * sin(studioPhase)
                val beamLeft = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(w * 0.24f, 0f)
                    lineTo(w * 0.62f + sway, h)
                    lineTo(w * 0.36f + sway, h)
                    close()
                }
                val beamRight = Path().apply {
                    moveTo(w, 0f)
                    lineTo(w * 0.76f, 0f)
                    lineTo(w * 0.38f - sway, h)
                    lineTo(w * 0.64f - sway, h)
                    close()
                }
                drawPath(
                    path = beamLeft,
                    brush = Brush.verticalGradient(
                        listOf(themeSpec.glowPrimary.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
                drawPath(
                    path = beamRight,
                    brush = Brush.verticalGradient(
                        listOf(palette.shadowColorContrast.copy(alpha = 0.20f), Color.Transparent)
                    )
                )
            }

            // 3. Soft Dynamic Vignette Shadow around perimeter for moody depth & crisp text readability
            val vignetteColor = lerp(palette.vignetteTint, themeSpec.backdropTint, 0.35f)
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        vignetteColor.copy(alpha = themeSpec.dimAlpha),
                        vignetteColor.copy(alpha = 0.68f)
                    ),
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = w * 0.78f
                )
            )
        }

        // Subtle broadcast light-sweep flare during entrance (skipped on Minimal Zen & Cyber HUD)
        val sweepVal = lightSweep.value
        if (sweepVal in 0.01f..0.99f &&
            themeSpec.motionStyle != BroadcastMotionStyle.GENTLE_FLOAT &&
            themeSpec.motionStyle != BroadcastMotionStyle.CYBER_TYPEWRITER
        ) {
            val sweepAlpha = (1f - kotlin.math.abs(sweepVal * 2f - 1f)) * 0.18f
            Canvas(modifier = Modifier.fillMaxSize()) {
                val beamX = if (textLayoutDir == LayoutDirection.Rtl) {
                    size.width * (1.15f - sweepVal * 1.3f)
                } else {
                    size.width * (-0.15f + sweepVal * 1.3f)
                }
                val beamY = if (themeSpec.layoutMode == BroadcastLayoutMode.LOWER_THIRD_DESK) {
                    size.height * 0.78f
                } else {
                    size.height * 0.5f
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(headlineColor.copy(alpha = sweepAlpha), Color.Transparent),
                        center = Offset(beamX, beamY),
                        radius = size.width * 0.32f
                    ),
                    center = Offset(beamX, beamY),
                    radius = size.width * 0.32f
                )
            }
        }

        // 4. Cinema Letterbox Matte Bars (Royal Cinema theme)
        if (themeSpec.layoutMode == BroadcastLayoutMode.CINEMA_LETTERBOX) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.88f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .align(Alignment.BottomCenter)
                        .background(themeSpec.secondaryColor.copy(alpha = 0.42f))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.88f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .align(Alignment.TopCenter)
                        .background(themeSpec.secondaryColor.copy(alpha = 0.42f))
                )
            }
        }

        // 5. Top Studio Bar (Counter + Voice/Audio Indicators + Clock)
        val topBarVerticalPad = if (themeSpec.layoutMode == BroadcastLayoutMode.CINEMA_LETTERBOX) 14.dp else 30.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 44.dp, vertical = topBarVerticalPad),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(if (themeSpec.layoutMode == BroadcastLayoutMode.HUD_VIEWFINDER) 8.dp else 50.dp))
                    .background(
                        when (themeSpec.layoutMode) {
                            BroadcastLayoutMode.LOWER_THIRD_DESK -> themeSpec.badgeAccentColor.copy(alpha = 0.24f)
                            BroadcastLayoutMode.EDITORIAL_MINIMAL -> Color.Black.copy(alpha = 0.22f)
                            else -> Color.Black.copy(alpha = 0.40f)
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(themeSpec.badgeAccentColor.copy(alpha = livePulse))
                )
                Text(
                    text = if (themeSpec.layoutMode == BroadcastLayoutMode.HUD_VIEWFINDER) {
                        "HUD // ${stringResource(R.string.broadcast_text_badge_live)}"
                    } else {
                        stringResource(R.string.broadcast_text_badge_live)
                    },
                    color = BroadcastWhite.copy(alpha = 0.92f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (LocaleHelper.isArabicOrRtlLanguage(lang)) CairoFontFamily else FontFamily.Default
                )
                Text(
                    text = "•",
                    color = TvTextSecondary.copy(alpha = 0.65f),
                    fontSize = 12.sp
                )
                Text(
                    text = String.format(Locale.US, "%02d / %02d", safeIndex + 1, lines.size.coerceAtLeast(1)),
                    color = headlineColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                if (isVoiceEnabled) {
                    val isSpeakingNow = voiceReader?.isSpeaking == true
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (isSpeakingNow) themeSpec.secondaryColor else TvTextSecondary.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(15.dp)
                            .graphicsLayer {
                                alpha = if (isSpeakingNow) (0.55f + 0.45f * livePulse) else 0.7f
                            }
                    )
                }
                if (isAudioActive) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = themeSpec.secondaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (timeStr.isNotBlank()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .clip(RoundedCornerShape(if (themeSpec.layoutMode == BroadcastLayoutMode.HUD_VIEWFINDER) 8.dp else 50.dp))
                            .background(
                                if (themeSpec.layoutMode == BroadcastLayoutMode.EDITORIAL_MINIMAL) {
                                    Color.Black.copy(alpha = 0.22f)
                                } else {
                                    Color.Black.copy(alpha = 0.36f)
                                }
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = timeStr,
                            color = BroadcastWhite.copy(alpha = 0.92f),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            style = TextStyle(fontFeatureSettings = "tnum")
                        )
                        if (!is24hFormat && amPmStr.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = amPmStr,
                                color = themeSpec.secondaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Theme-Specific Headline Stage & Surrounding Elements
        CompositionLocalProvider(LocalLayoutDirection provides textLayoutDir) {
            when (themeSpec.layoutMode) {
                BroadcastLayoutMode.LOWER_THIRD_DESK -> {
                    // Lower-Third Broadcast Desk anchored near bottom
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(start = 52.dp, end = 52.dp, bottom = 84.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 1160.dp)
                                .align(Alignment.Center)
                                .graphicsLayer {
                                    alpha = textAlpha.value
                                    translationY = textSlideY.value * slideVerticalPx
                                }
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF0A0F1A).copy(alpha = 0.88f))
                                .border(
                                    width = 1.dp,
                                    color = themeSpec.secondaryColor.copy(alpha = 0.38f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            // Attached Breaking/Live Desk Header Strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                themeSpec.badgeAccentColor.copy(alpha = 0.92f),
                                                themeSpec.glowSecondary.copy(alpha = 0.72f),
                                                Color(0xFF1A070C)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 20.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = livePulse))
                                    )
                                    Text(
                                        text = stringResource(R.string.broadcast_text_badge_live),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = if (hasArabic) CairoFontFamily else FontFamily.Default
                                    )
                                }
                                Text(
                                    text = String.format(Locale.US, "%02d / %02d", safeIndex + 1, lines.size.coerceAtLeast(1)),
                                    color = Color.White.copy(alpha = 0.92f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(fontFeatureSettings = "tnum")
                                )
                            }

                            // Headline Row with vertical Broadcast Desk anchor pillar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 26.dp, vertical = 20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(7.dp)
                                        .height(if (activeLine.length > 48) 68.dp else 52.dp)
                                        .graphicsLayer { scaleY = accentBarScale.value }
                                        .clip(RoundedCornerShape(50))
                                        .background(themeSpec.badgeAccentColor)
                                )
                                Spacer(modifier = Modifier.width(20.dp))
                                Text(
                                    text = displayedText,
                                    color = headlineColor,
                                    fontSize = headlineFontSize,
                                    lineHeight = headlineLineHeight,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = headlineFont,
                                    textAlign = TextAlign.Start,
                                    style = TextStyle(
                                        fontFamily = headlineFont,
                                        shadow = Shadow(
                                            color = Color.Black.copy(alpha = 0.7f),
                                            blurRadius = 14f,
                                            offset = Offset(0f, 3f)
                                        )
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Full-width bottom progress ribbon across the desk card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(Color.White.copy(alpha = 0.12f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = lineProgress.value.coerceIn(0f, 1f))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(themeSpec.badgeAccentColor, themeSpec.glowSecondary)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }

                BroadcastLayoutMode.HUD_VIEWFINDER -> {
                    // Cyber HUD Viewfinder Box with 4 L-shaped corner brackets & typewriter cursor
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BoxWithConstraints(
                            modifier = Modifier
                                .widthIn(min = 520.dp, max = 1060.dp)
                                .graphicsLayer {
                                    alpha = chromeReveal.value.coerceAtLeast(textAlpha.value)
                                    scaleX = textScale.value
                                    scaleY = textScaleY.value
                                    transformOrigin = TransformOrigin.Center
                                }
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF030A18).copy(alpha = 0.74f))
                        ) {
                            // 4 Glowing L-Shaped Corner Brackets
                            Canvas(modifier = Modifier.matchParentSize()) {
                                val w = size.width
                                val h = size.height
                                val bracketLen = 34.dp.toPx()
                                val stroke = 3.dp.toPx()
                                val cornerCol = themeSpec.glowPrimary
                                val altCol = themeSpec.glowSecondary
                                // Top-left
                                drawLine(cornerCol, Offset(0f, 0f), Offset(bracketLen, 0f), stroke, cap = StrokeCap.Round)
                                drawLine(cornerCol, Offset(0f, 0f), Offset(0f, bracketLen), stroke, cap = StrokeCap.Round)
                                // Top-right
                                drawLine(altCol, Offset(w - bracketLen, 0f), Offset(w, 0f), stroke, cap = StrokeCap.Round)
                                drawLine(altCol, Offset(w, 0f), Offset(w, bracketLen), stroke, cap = StrokeCap.Round)
                                // Bottom-left
                                drawLine(altCol, Offset(0f, h - bracketLen), Offset(0f, h), stroke, cap = StrokeCap.Round)
                                drawLine(altCol, Offset(0f, h), Offset(bracketLen, h), stroke, cap = StrokeCap.Round)
                                // Bottom-right
                                drawLine(cornerCol, Offset(w - bracketLen, h), Offset(w, h), stroke, cap = StrokeCap.Round)
                                drawLine(cornerCol, Offset(w, h - bracketLen), Offset(w, h), stroke, cap = StrokeCap.Round)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 42.dp, vertical = 32.dp)
                            ) {
                                // HUD Telemetry Tag Bar
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(themeSpec.glowPrimary.copy(alpha = 0.14f))
                                        .padding(horizontal = 14.dp, vertical = 5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(themeSpec.glowSecondary.copy(alpha = livePulse))
                                    )
                                    Text(
                                        text = String.format(Locale.US, "SIGNAL // CH-%02d", safeIndex + 1),
                                        color = themeSpec.glowPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Spacer(modifier = Modifier.height(22.dp))

                                Text(
                                    text = displayedText,
                                    color = headlineColor,
                                    fontSize = headlineFontSize,
                                    lineHeight = headlineLineHeight,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = headlineFont,
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        fontFamily = headlineFont,
                                        shadow = Shadow(
                                            color = themeSpec.glowPrimary.copy(alpha = 0.45f),
                                            blurRadius = 22f,
                                            offset = Offset(0f, 0f)
                                        )
                                    ),
                                    modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
                                )

                                Spacer(modifier = Modifier.height(28.dp))

                                // Segmented HUD Progress Bar
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.width(280.dp)
                                ) {
                                    val segmentCount = 12
                                    val filledSegments = (lineProgress.value * segmentCount).roundToInt()
                                    for (seg in 0 until segmentCount) {
                                        val isLit = seg < filledSegments
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(
                                                    if (isLit) {
                                                        if (seg % 2 == 0) themeSpec.glowPrimary else themeSpec.glowSecondary
                                                    } else {
                                                        Color.White.copy(alpha = 0.12f)
                                                    }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                BroadcastLayoutMode.CINEMA_LETTERBOX -> {
                    // Royal Cinema Theater Stage between 2.39:1 matte bars with golden diamond ornaments
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 80.dp)
                    ) {
                        // Top wide golden ornament rule with center diamond
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .width(340.dp)
                                .graphicsLayer {
                                    scaleX = accentBarScale.value
                                    alpha = accentBarScale.value
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.5.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color.Transparent, studioAccentColor)
                                        )
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .graphicsLayer { rotationZ = 45f }
                                    .background(studioAccentColor)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(1.5.dp)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(studioAccentColor, Color.Transparent)
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Text(
                            text = displayedText,
                            color = headlineColor,
                            fontSize = headlineFontSize,
                            lineHeight = headlineLineHeight,
                            fontWeight = FontWeight.Bold,
                            fontFamily = headlineFont,
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontFamily = headlineFont,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.90f),
                                    blurRadius = 30f,
                                    offset = Offset(0f, 6f)
                                )
                            ),
                            modifier = Modifier
                                .widthIn(max = 1040.dp)
                                .graphicsLayer {
                                    alpha = textAlpha.value
                                    scaleX = textScale.value
                                    scaleY = textScale.value
                                }
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Bottom wide golden cinema hairline & slim progress bar
                        Box(
                            modifier = Modifier
                                .width(320.dp)
                                .height(2.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.14f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = lineProgress.value.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(studioAccentColor.copy(alpha = 0.7f), headlineColor)
                                        )
                                    )
                            )
                        }
                    }
                }

                BroadcastLayoutMode.DIAGONAL_ARENA -> {
                    // Emerald Sport Diagonal Slate with scoreboard pill & skewed parallelogram stripes
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 68.dp)
                            .graphicsLayer {
                                alpha = textAlpha.value
                                translationX = textSlideX.value * slideHorizontalPx * horizontalSign
                                translationY = textSlideY.value * slideVerticalPx
                                rotationZ = textTiltDeg.value
                                scaleX = textScale.value
                                scaleY = textScale.value
                            }
                    ) {
                        // Floating Arena Scoreboard Pill above headline
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(themeSpec.badgeAccentColor.copy(alpha = 0.24f))
                                .border(
                                    width = 1.dp,
                                    color = themeSpec.secondaryColor.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(themeSpec.secondaryColor.copy(alpha = livePulse))
                            )
                            Text(
                                text = String.format(Locale.US, "STADIUM // %02d - %02d", safeIndex + 1, lines.size.coerceAtLeast(1)),
                                color = themeSpec.secondaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .widthIn(max = 1080.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFF042219).copy(alpha = 0.65f))
                                .border(
                                    width = 1.dp,
                                    color = themeSpec.secondaryColor.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(horizontal = 36.dp, vertical = 26.dp)
                        ) {
                            // Skewed twin arena speed stripes on start
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .height(54.dp)
                                        .graphicsLayer { rotationZ = 14f }
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(themeSpec.secondaryColor)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height(54.dp)
                                        .graphicsLayer { rotationZ = 14f }
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(themeSpec.glowSecondary.copy(alpha = 0.7f))
                                )
                            }

                            Spacer(modifier = Modifier.width(24.dp))

                            Text(
                                text = displayedText,
                                color = headlineColor,
                                fontSize = headlineFontSize,
                                lineHeight = headlineLineHeight,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = headlineFont,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontFamily = headlineFont,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.85f),
                                        blurRadius = 24f,
                                        offset = Offset(0f, 5f)
                                    )
                                ),
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(5.dp)
                                        .height(54.dp)
                                        .graphicsLayer { rotationZ = 14f }
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(themeSpec.glowSecondary.copy(alpha = 0.7f))
                                )
                                Box(
                                    modifier = Modifier
                                        .width(8.dp)
                                        .height(54.dp)
                                        .graphicsLayer { rotationZ = 14f }
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(themeSpec.secondaryColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(26.dp))

                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.14f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = lineProgress.value.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(themeSpec.secondaryColor, themeSpec.glowSecondary)
                                        )
                                    )
                            )
                        }
                    }
                }

                BroadcastLayoutMode.EDITORIAL_MINIMAL -> {
                    // Minimal Zen Float with large translucent architectural step watermark
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 88.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Subtle large background index watermark
                        Text(
                            text = String.format(Locale.US, "%02d", safeIndex + 1),
                            color = themeSpec.secondaryColor.copy(alpha = 0.08f * textAlpha.value),
                            fontSize = 190.sp,
                            fontWeight = FontWeight.Black,
                            style = TextStyle(fontFeatureSettings = "tnum"),
                            modifier = Modifier.graphicsLayer {
                                translationY = textSlideY.value * slideVerticalPx * 0.45f
                            }
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.widthIn(max = 980.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(studioAccentColor.copy(alpha = livePulse * textAlpha.value))
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            Text(
                                text = displayedText,
                                color = headlineColor,
                                fontSize = headlineFontSize,
                                lineHeight = headlineLineHeight,
                                fontWeight = FontWeight.Bold,
                                fontFamily = headlineFont,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontFamily = headlineFont,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.72f),
                                        blurRadius = 20f,
                                        offset = Offset(0f, 4f)
                                    )
                                ),
                                modifier = Modifier.graphicsLayer {
                                    alpha = textAlpha.value
                                    translationY = textSlideY.value * slideVerticalPx
                                }
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.White.copy(alpha = 0.14f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = lineProgress.value.coerceIn(0f, 1f))
                                        .clip(RoundedCornerShape(50))
                                        .background(studioAccentColor)
                                )
                            }
                        }
                    }
                }

                BroadcastLayoutMode.CENTER_STAGE -> {
                    // Classic Studio Glide: Centered stage with twin vertical glowing bars
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth()
                            .padding(horizontal = 72.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(88.dp)
                                .height(4.dp)
                                .graphicsLayer {
                                    scaleX = accentBarScale.value
                                    alpha = accentBarScale.value
                                }
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            studioAccentColor.copy(alpha = 0.18f),
                                            studioAccentColor,
                                            studioAccentColor.copy(alpha = 0.18f)
                                        )
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(26.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.widthIn(max = 1040.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(if (activeLine.length > 48) 84.dp else 64.dp)
                                    .graphicsLayer {
                                        scaleY = accentBarScale.value
                                        alpha = accentBarScale.value
                                    }
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                headlineColor.copy(alpha = 0.35f),
                                                headlineColor,
                                                studioAccentColor
                                            )
                                        )
                                    )
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            Text(
                                text = displayedText,
                                color = headlineColor,
                                fontSize = headlineFontSize,
                                lineHeight = headlineLineHeight,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = headlineFont,
                                textAlign = TextAlign.Center,
                                style = TextStyle(
                                    fontFamily = headlineFont,
                                    shadow = Shadow(
                                        color = Color.Black.copy(alpha = 0.85f),
                                        blurRadius = 26f,
                                        offset = Offset(0f, 6f)
                                    )
                                ),
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .graphicsLayer {
                                        alpha = textAlpha.value
                                        translationX = textSlideX.value * slideHorizontalPx * horizontalSign
                                        translationY = textSlideY.value * slideVerticalPx
                                        scaleX = textScale.value
                                        scaleY = textScale.value
                                    }
                            )

                            Spacer(modifier = Modifier.width(24.dp))

                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height(if (activeLine.length > 48) 84.dp else 64.dp)
                                    .graphicsLayer {
                                        scaleY = accentBarScale.value
                                        alpha = accentBarScale.value * 0.45f
                                    }
                                    .clip(RoundedCornerShape(50))
                                    .background(studioAccentColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(34.dp))

                        Box(
                            modifier = Modifier
                                .width(260.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.14f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = lineProgress.value.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(studioAccentColor.copy(alpha = 0.7f), headlineColor)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // 7. Bottom Subtle Exit Hint & Step Dots
        val bottomHintPadding = if (themeSpec.layoutMode == BroadcastLayoutMode.CINEMA_LETTERBOX) 14.dp else 26.dp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomHintPadding)
        ) {
            if (lines.size in 2..12) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    lines.indices.forEach { idx ->
                        val isCurrent = idx == safeIndex
                        val dotColor = if (idx % 2 == 1) themeSpec.secondaryColor else themeSpec.primaryColor
                        Box(
                            modifier = Modifier
                                .width(if (isCurrent) 22.dp else 6.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isCurrent) dotColor else dotColor.copy(alpha = 0.28f)
                                )
                        )
                    }
                }
            }
            Text(
                text = stringResource(R.string.ambient_press_any_key),
                color = TvTextPrimary.copy(alpha = 0.65f),
                fontSize = 12.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.8f),
                        blurRadius = 8f,
                        offset = Offset(0f, 2f)
                    )
                )
            )
        }
    }
}
