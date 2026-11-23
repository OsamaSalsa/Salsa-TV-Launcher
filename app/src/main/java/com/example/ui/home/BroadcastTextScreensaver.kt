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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.ui.common.AuroraBackground
import com.example.ui.common.AuroraColors
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

private val BroadcastYellow = Color(0xFFFFD60A)
private val BroadcastWhite = Color(0xFFFFFFFF)
private val BroadcastExpoOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val BroadcastExpoIn = CubicBezierEasing(0.7f, 0f, 0.84f, 0f)

/**
 * Reusable ExoPlayer background audio controller for screensavers (Photo Slideshow & Broadcast Text).
 * Supports built-in ambient audio streams as well as local device audio files (content://, file://, /path).
 * Automatically pauses/resumes with lifecycle and releases cleanly on dispose.
 */
@OptIn(UnstableApi::class)
@Composable
internal fun ScreensaverAmbientAudioEffect(
    musicMode: String,
    customMusicUri: String,
    volumePercent: Int
) {
    val context = LocalContext.current
    val resolvedMusicUrl = remember(musicMode, customMusicUri) {
        resolveAmbientMusicUrl(musicMode, customMusicUri)
    }

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

    LaunchedEffect(volumePercent) {
        try {
            musicPlayer.volume = (volumePercent.coerceIn(0, 100) / 100f)
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
 * - Uses the app's dynamic Aurora wallpaper with an exact 60% dark dimming layer for crisp studio contrast.
 * - Displays user-defined multi-line text sequentially, one line at a time.
 * - Alternates headline colors strictly between Broadcast White (#FFFFFF) and Broadcast Yellow (#FFD60A).
 * - Uses kinetic broadcast-style entrance & exit motion with studio accent bar, sweeping flare & progress track.
 * - Supports optional background audio (built-in ambient tracks or local audio file) with volume control.
 */
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

    val defaultLinesText = stringResource(R.string.broadcast_text_default_lines)
    val lines = remember(rawBroadcastText, defaultLinesText) {
        parseBroadcastLines(rawBroadcastText, defaultLinesText)
    }

    val resolvedMusicUrl = remember(musicMode, customMusicUri) {
        resolveAmbientMusicUrl(musicMode, customMusicUri)
    }
    val isAudioActive = resolvedMusicUrl.isNotBlank()

    // Background Audio Engine (identical capabilities to Photo Slideshow)
    ScreensaverAmbientAudioEffect(
        musicMode = musicMode,
        customMusicUri = customMusicUri,
        volumePercent = musicVolume
    )

    var currentIndex by remember(lines) { mutableIntStateOf(0) }
    var manualSkipTrigger by remember { mutableIntStateOf(0) }
    val mountedAtMs = remember { System.currentTimeMillis() }

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

    // Broadcast Animation States (re-run cleanly for every line)
    val textAlpha = remember { Animatable(0f) }
    val textSlideX = remember { Animatable(1f) } // 1f = entering from reading edge, 0f = center, -1f = exiting opposite edge
    val textSlideY = remember { Animatable(1f) } // 1f = below center, 0f = center, -1f = slightly above
    val textScale = remember { Animatable(0.94f) }
    val accentBarScale = remember { Animatable(0f) }
    val lightSweep = remember { Animatable(0f) }
    val lineProgress = remember { Animatable(0f) }

    LaunchedEffect(lines, currentIndex, lineDurationSec, manualSkipTrigger) {
        if (lines.isEmpty()) return@LaunchedEffect

        // Reset cleanly before broadcast entrance
        textAlpha.snapTo(0f)
        textSlideX.snapTo(1f)
        textSlideY.snapTo(1f)
        textScale.snapTo(0.93f)
        accentBarScale.snapTo(0f)
        lightSweep.snapTo(0f)
        lineProgress.snapTo(0f)

        val enterDurationMs = 640
        val exitDurationMs = 420
        val totalHoldMs = (lineDurationSec.coerceIn(3, 30) * 1000L)
        val steadyHoldMs = (totalHoldMs - enterDurationMs - exitDurationMs).coerceAtLeast(1400L)

        // Phase 1: Broadcast Studio Entrance (accent bar + sweep flare + kinetic slide-in)
        val barJob = launch {
            accentBarScale.animateTo(1f, animationSpec = tween(420, easing = BroadcastExpoOut))
        }
        val sweepJob = launch {
            lightSweep.animateTo(1f, animationSpec = tween(720, easing = BroadcastExpoOut))
        }
        val enterAlphaJob = launch {
            delay(70)
            textAlpha.animateTo(1f, animationSpec = tween(480, easing = BroadcastExpoOut))
        }
        val enterXJob = launch {
            delay(70)
            textSlideX.animateTo(0f, animationSpec = tween(enterDurationMs, easing = BroadcastExpoOut))
        }
        val enterYJob = launch {
            delay(70)
            textSlideY.animateTo(0f, animationSpec = tween(enterDurationMs, easing = BroadcastExpoOut))
        }
        val enterScaleJob = launch {
            delay(70)
            textScale.animateTo(1f, animationSpec = tween(enterDurationMs, easing = BroadcastExpoOut))
        }

        // Progress bar sweeps smoothly across the entire on-air window
        val progressJob = launch {
            lineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = (enterDurationMs + steadyHoldMs).toInt(),
                    easing = LinearEasing
                )
            )
        }

        barJob.join()
        enterAlphaJob.join()
        enterXJob.join()
        enterYJob.join()
        enterScaleJob.join()
        sweepJob.join()

        // Phase 2: Subtle On-Air Camera Push-In Hold
        textScale.animateTo(
            targetValue = 1.032f,
            animationSpec = tween(durationMillis = steadyHoldMs.toInt(), easing = LinearEasing)
        )
        progressJob.join()

        // Phase 3: Crisp Broadcast Exit (glide out + retract studio bar)
        val exitAlphaJob = launch {
            textAlpha.animateTo(0f, animationSpec = tween(exitDurationMs - 40, easing = BroadcastExpoIn))
        }
        val exitXJob = launch {
            textSlideX.animateTo(-1f, animationSpec = tween(exitDurationMs, easing = BroadcastExpoIn))
        }
        val exitYJob = launch {
            textSlideY.animateTo(-0.75f, animationSpec = tween(exitDurationMs, easing = BroadcastExpoIn))
        }
        val exitScaleJob = launch {
            textScale.animateTo(0.96f, animationSpec = tween(exitDurationMs, easing = BroadcastExpoIn))
        }
        val exitBarJob = launch {
            delay(80)
            accentBarScale.animateTo(0f, animationSpec = tween(exitDurationMs - 80, easing = BroadcastExpoIn))
        }

        exitAlphaJob.join()
        exitXJob.join()
        exitYJob.join()
        exitScaleJob.join()
        exitBarJob.join()

        // Short studio beat before next headline punches in
        delay(110L)

        if (lines.size > 1) {
            currentIndex = (currentIndex + 1) % lines.size
        } else {
            // Re-trigger broadcast cycle cleanly even if only 1 line is entered
            manualSkipTrigger++
        }
    }

    // Subtle slow studio background drift & live-dot pulse
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
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "live_pulse"
    )

    val safeIndex = currentIndex.coerceIn(0, (lines.size - 1).coerceAtLeast(0))
    val activeLine = lines.getOrElse(safeIndex) { "" }
    val isYellowTurn = (safeIndex % 2 == 1)
    val headlineColor = if (isYellowTurn) BroadcastYellow else BroadcastWhite
    val studioAccentColor = if (isYellowTurn) BroadcastWhite else BroadcastYellow

    val isRtlLayout = LocalLayoutDirection.current == LayoutDirection.Rtl
    val hasArabic = LocaleHelper.isArabicOrRtlLanguage(lang) || LocaleHelper.containsArabicScript(activeLine)
    val headlineFont = if (hasArabic) CairoFontFamily else FontFamily.Default
    val textLayoutDir = if (LocaleHelper.containsArabicScript(activeLine)) LayoutDirection.Rtl else LocalLayoutDirection.current

    // Adaptive broadcast TV typography sizing so short headlines hit hard and longer lines stay crisp
    val headlineFontSize = when {
        activeLine.length <= 26 -> 58.sp
        activeLine.length <= 48 -> 48.sp
        activeLine.length <= 82 -> 39.sp
        else -> 32.sp
    }
    val headlineLineHeight = when {
        activeLine.length <= 26 -> 74.sp
        activeLine.length <= 48 -> 64.sp
        activeLine.length <= 82 -> 54.sp
        else -> 46.sp
    }

    val density = LocalDensity.current
    val slideHorizontalPx = with(density) { 96.dp.toPx() }
    val slideVerticalPx = with(density) { 26.dp.toPx() }
    // RTL enters from right (+X) and exits to left (-X); LTR enters from left (-X) and exits to right (+X)
    val horizontalSign = if (textLayoutDir == LayoutDirection.Rtl) 1f else -1f

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
        // 1. Dynamic Aurora Wallpaper Layer
        AuroraBackground(colors = auroraColors)

        // Subtle animated studio radial glows layered onto dynamic aurora
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx1 = w * (0.28f + 0.14f * sin(studioPhase))
            val cy1 = h * (0.34f + 0.10f * cos(studioPhase))
            val cx2 = w * (0.72f + 0.14f * cos(studioPhase * 0.85f))
            val cy2 = h * (0.66f + 0.10f * sin(studioPhase * 0.85f))

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auroraColors.glow.copy(alpha = 0.26f), Color.Transparent),
                    center = Offset(cx1, cy1),
                    radius = w * 0.36f
                ),
                center = Offset(cx1, cy1),
                radius = w * 0.36f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(BroadcastYellow.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(cx2, cy2),
                    radius = w * 0.30f
                ),
                center = Offset(cx2, cy2),
                radius = w * 0.30f
            )
        }

        // 2. Exact 60% Black Dimming Overlay over the Dynamic Wallpaper
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.60f))
        )

        // Subtle broadcast light-sweep flare during headline entrance
        val sweepVal = lightSweep.value
        if (sweepVal in 0.01f..0.99f) {
            val sweepAlpha = (1f - kotlin.math.abs(sweepVal * 2f - 1f)) * 0.18f
            Canvas(modifier = Modifier.fillMaxSize()) {
                val beamX = if (textLayoutDir == LayoutDirection.Rtl) {
                    size.width * (1.15f - sweepVal * 1.3f)
                } else {
                    size.width * (-0.15f + sweepVal * 1.3f)
                }
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            headlineColor.copy(alpha = sweepAlpha),
                            Color.Transparent
                        ),
                        center = Offset(beamX, size.height * 0.5f),
                        radius = size.width * 0.32f
                    ),
                    center = Offset(beamX, size.height * 0.5f),
                    radius = size.width * 0.32f
                )
            }
        }

        // 3. Top Minimal Broadcast Studio Bar (Counter + Audio Pill on Start, Clock on End)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 44.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.38f))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(headlineColor.copy(alpha = livePulse))
                )
                Text(
                    text = stringResource(R.string.broadcast_text_badge_live),
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
                if (isAudioActive) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = BroadcastYellow,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (timeStr.isNotBlank()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.34f))
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
                                color = BroadcastYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Center Broadcast Kinetic Headline Stage (One Line at a Time — Alternating White & Yellow)
        CompositionLocalProvider(LocalLayoutDirection provides textLayoutDir) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 72.dp)
            ) {
                // Sleek top broadcast accent rule sweeping open
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
                                    studioAccentColor.copy(alpha = 0.25f),
                                    studioAccentColor,
                                    studioAccentColor.copy(alpha = 0.25f)
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(26.dp))

                // Main Broadcast Headline with vertical studio bar & kinetic entrance/exit
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
                        text = activeLine,
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

                    // Symmetric subtle studio echo bar on opposite side for balanced TV composition
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

                // Sleek Broadcast On-Air Progress Bar underneath the headline
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
                                    listOf(
                                        studioAccentColor.copy(alpha = 0.7f),
                                        headlineColor
                                    )
                                )
                            )
                    )
                }
            }
        }

        // 5. Bottom Subtle Exit Hint & Step Dots
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        ) {
            if (lines.size in 2..12) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    lines.indices.forEach { idx ->
                        val isCurrent = idx == safeIndex
                        val dotColor = if (idx % 2 == 1) BroadcastYellow else BroadcastWhite
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
