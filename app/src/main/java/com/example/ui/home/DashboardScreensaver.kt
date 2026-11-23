package com.example.ui.home

import android.view.KeyEvent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.AuraTvApp
import com.example.data.economic.EconomicRepository
import com.example.data.football.FootballRepository
import com.example.data.local.LauncherPreferences
import com.example.data.news.NewsRepository
import com.example.data.news.NewsSectionType
import com.example.data.weather.WeatherRepository
import com.example.ui.economic.EconomicScreen
import com.example.ui.football.FootballScreen
import com.example.ui.news.NewsScreen
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.weather.WeatherScreen
import com.example.util.LocaleHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Slides for the "All Dashboards" rotating screensaver mode.
 * Exact order & timings:
 * 1. News: 60 seconds (1 minute — enters the next news section & fresh headlines every time it returns from Football)
 * 2. Weather: 10 seconds
 * 3. Economy: 10 seconds
 * 4. Football: 10 seconds
 * Then repeats continuously from News with the next news section.
 */
internal enum class DashboardScreensaverSlide(val durationMs: Long) {
    NEWS(60_000L),
    WEATHER(10_000L),
    ECONOMIC(10_000L),
    FOOTBALL(10_000L)
}

internal object DashboardScreensaverCycleTracker {
    @Volatile
    var persistentNewsCycleCount: Int = 0
        private set

    fun resolveNewsSectionForCycle(cycleCount: Int): NewsSectionType {
        val sections = NewsSectionType.entries
        val safeCycle = ((cycleCount % sections.size) + sections.size) % sections.size
        return sections[safeCycle]
    }

    fun resolveStoryOffsetForCycle(cycleCount: Int): Int {
        val sections = NewsSectionType.entries
        return ((cycleCount.coerceAtLeast(0)) / sections.size) * 2
    }

    fun advanceNextNewsCycle(): Int {
        persistentNewsCycleCount += 1
        return persistentNewsCycleCount
    }

    fun resetForTest() {
        persistentNewsCycleCount = 0
    }
}

@Composable
internal fun DashboardScreensaver(
    screensaverMode: String,
    is24hFormat: Boolean,
    focusRequester: FocusRequester,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        (context.applicationContext as? AuraTvApp)?.preferences ?: LauncherPreferences(context)
    }
    val initialLang = remember { prefs.getCachedLanguage() }
    val appLanguage by prefs.languageFlow.collectAsState(initial = initialLang)
    val effectiveLang = remember(appLanguage) {
        LocaleHelper.getEffectiveLanguage(appLanguage)
    }

    val isCycleMode = screensaverMode == "all_dashboards_cycle"
    val cycleSlides = remember { DashboardScreensaverSlide.entries }
    var currentSlideIndex by remember(screensaverMode) { mutableIntStateOf(0) }
    var newsCycleCount by remember(screensaverMode) {
        mutableIntStateOf(
            if (screensaverMode == "all_dashboards_cycle") DashboardScreensaverCycleTracker.persistentNewsCycleCount
            else 0
        )
    }
    var slideProgress by remember(screensaverMode) { mutableFloatStateOf(0f) }
    val mountedAtMs = remember { System.currentTimeMillis() }

    val activeSlide = remember(screensaverMode, currentSlideIndex) {
        when (screensaverMode) {
            "weather_dashboard" -> DashboardScreensaverSlide.WEATHER
            "news_dashboard" -> DashboardScreensaverSlide.NEWS
            "economic_dashboard" -> DashboardScreensaverSlide.ECONOMIC
            "football_dashboard" -> DashboardScreensaverSlide.FOOTBALL
            else -> cycleSlides[currentSlideIndex % cycleSlides.size]
        }
    }

    val cycleNewsSection = remember(newsCycleCount) {
        DashboardScreensaverCycleTracker.resolveNewsSectionForCycle(newsCycleCount)
    }
    val cycleNewsStoryOffset = remember(newsCycleCount) {
        DashboardScreensaverCycleTracker.resolveStoryOffsetForCycle(newsCycleCount)
    }

    // Pre-warm all dashboard repositories in cycle mode so transitions appear immediately
    LaunchedEffect(isCycleMode, effectiveLang) {
        if (!isCycleMode) return@LaunchedEffect
        launch(Dispatchers.IO) {
            NewsRepository.ensureInitialized(context, effectiveLang, forceRefresh = false)
        }
        launch(Dispatchers.IO) {
            WeatherRepository.ensureInitialized(context, effectiveLang)
        }
        launch(Dispatchers.IO) {
            EconomicRepository.ensureInitialized(context)
        }
        launch(Dispatchers.IO) {
            val leagues = try {
                prefs.footballLeaguesFlow.first()
            } catch (_: Exception) {
                listOf("premier_league", "la_liga", "serie_a", "champions_league")
            }
            val activeLeague = leagues.firstOrNull() ?: "premier_league"
            FootballRepository.ensureInitialized(
                context = context,
                appLanguage = effectiveLang,
                activeLeagueId = activeLeague,
                selectedLeagueIds = leagues,
                forceRefresh = false
            )
        }
    }

    // Auto-cycle timer: News (60s / 1 min, new section after Football -> News) -> Weather (10s) -> Economy (10s) -> Football (10s) -> repeat
    LaunchedEffect(isCycleMode, currentSlideIndex) {
        if (!isCycleMode) return@LaunchedEffect
        val slide = cycleSlides[currentSlideIndex % cycleSlides.size]
        val durationMs = slide.durationMs
        val stepMs = 100L
        var elapsed = 0L
        slideProgress = 0f
        while (elapsed < durationMs) {
            delay(stepMs)
            elapsed += stepMs
            slideProgress = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        }
        val nextSlideIndex = (currentSlideIndex + 1) % cycleSlides.size
        if (nextSlideIndex == 0) {
            newsCycleCount = DashboardScreensaverCycleTracker.advanceNextNewsCycle()
        }
        currentSlideIndex = nextSlideIndex
    }

    // Keep focus on the screensaver overlay so internal dashboard buttons never stay highlighted
    LaunchedEffect(activeSlide) {
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
        delay(180L)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screensaver_root")
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    val keyCode = keyEvent.nativeKeyEvent.keyCode
                    val isBack = keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                    val elapsedSinceMount = System.currentTimeMillis() - mountedAtMs
                    if (isBack || elapsedSinceMount > 250L) {
                        onDismiss()
                        return@onPreviewKeyEvent true
                    }
                }
                false
            }
    ) {
        Crossfade(
            targetState = activeSlide,
            animationSpec = tween(durationMillis = 650),
            label = "dashboard_screensaver_crossfade"
        ) { slide ->
            when (slide) {
                DashboardScreensaverSlide.NEWS -> {
                    androidx.compose.runtime.key(cycleNewsSection, newsCycleCount, isCycleMode) {
                        NewsScreen(
                            is24hFormat = is24hFormat,
                            appLanguage = appLanguage,
                            onBackToHome = onDismiss,
                            // Standalone News auto-advances across sections every time a section completes.
                            // All-Dashboards stays in the designated section for the full 1-minute slot, then
                            // advances to the NEXT section each time it returns from Football -> News.
                            autoAdvanceSections = !isCycleMode,
                            initialSection = if (isCycleMode) cycleNewsSection else NewsSectionType.TOP_STORIES,
                            initialStoryIndex = if (isCycleMode) cycleNewsStoryOffset else 0,
                            cycleRefreshNonce = if (isCycleMode) newsCycleCount else 0,
                            isScreensaverMode = true
                        )
                    }
                }
                DashboardScreensaverSlide.WEATHER -> {
                    WeatherScreen(
                        is24hFormat = is24hFormat,
                        appLanguage = appLanguage,
                        onBackToHome = onDismiss
                    )
                }
                DashboardScreensaverSlide.ECONOMIC -> {
                    EconomicScreen(
                        is24hFormat = is24hFormat,
                        appLanguage = appLanguage,
                        onBackToHome = onDismiss
                    )
                }
                DashboardScreensaverSlide.FOOTBALL -> {
                    FootballScreen(
                        is24hFormat = is24hFormat,
                        appLanguage = appLanguage,
                        onBackToHome = onDismiss
                    )
                }
            }
        }

        // Subtle bottom hairline progress indicator in auto-cycle mode (zero overlap with content)
        if (isCycleMode) {
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
            ) {
                val trackColor = Color.White.copy(alpha = 0.12f)
                drawRect(color = trackColor, size = size)
                val fillW = (size.width * slideProgress).coerceIn(0f, size.width)
                if (fillW > 0f) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                TvAccentGold.copy(alpha = 0.78f),
                                TvAccentBlue.copy(alpha = 0.85f)
                            ),
                            startX = 0f,
                            endX = fillW
                        ),
                        topLeft = Offset.Zero,
                        size = Size(fillW, size.height)
                    )
                }
            }
        }

        // Transparent input-capture layer so any click/tap or remote key dismisses cleanly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (System.currentTimeMillis() - mountedAtMs > 300L) {
                            onDismiss()
                        }
                    }
                )
        )
    }
}
