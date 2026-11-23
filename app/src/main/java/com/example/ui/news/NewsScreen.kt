package com.example.ui.news

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.LauncherPreferences
import com.example.data.news.NewsArticleItem
import com.example.data.news.NewsLocalizedStrings
import com.example.data.news.NewsRepository
import com.example.data.news.NewsSectionType
import com.example.ui.theme.CairoFontFamily
import com.example.util.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun NewsScreen(
    is24hFormat: Boolean,
    appLanguage: String,
    onBackToHome: () -> Unit,
    autoAdvanceSections: Boolean = false,
    initialSection: NewsSectionType = NewsSectionType.TOP_STORIES,
    initialStoryIndex: Int = 0,
    cycleRefreshNonce: Int = 0,
    isScreensaverMode: Boolean = false,
    holdAfterCycleStoriesWhenVoiceOn: Boolean = false,
    onCycleStoriesCompleted: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val prefs = remember(context) {
        (context.applicationContext as? com.example.AuraTvApp)?.preferences ?: LauncherPreferences(context)
    }
    val coroutineScope = rememberCoroutineScope()

    val initialLang = remember { prefs.getCachedLanguage() }
    val savedLang by prefs.languageFlow.collectAsState(initial = initialLang)
    val resolvedLang = remember(savedLang, appLanguage) {
        if (savedLang.isNotBlank() && savedLang != "system") savedLang else appLanguage
    }
    val effectiveLang = remember(resolvedLang) { LocaleHelper.getEffectiveLanguage(resolvedLang) }
    val isRtl = remember(resolvedLang) { LocaleHelper.isRtlLanguage(resolvedLang) }
    val isArabicFont = effectiveLang in setOf("ar", "fa", "ur")
    val strings = remember(effectiveLang) { NewsRepository.getLocalizedStrings(effectiveLang) }

    val newsState by NewsRepository.newsState.collectAsState()
    val isVoiceReaderEnabled by prefs.newsVoiceEnabledFlow.collectAsState(initial = false)
    val voiceReader = rememberNewsVoiceReader()

    val allSections = remember { NewsSectionType.entries }
    var activeSection by remember { mutableStateOf(initialSection) }
    var activeStoryIndex by remember { mutableIntStateOf(initialStoryIndex) }
    var storiesViewedInSection by remember { mutableIntStateOf(0) }
    var storyProgressRatio by remember { mutableFloatStateOf(0.45f) }
    var cycleStoriesCompleted by remember(initialSection, cycleRefreshNonce, holdAfterCycleStoriesWhenVoiceOn) {
        mutableStateOf(false)
    }

    LaunchedEffect(initialSection, initialStoryIndex, cycleRefreshNonce) {
        activeSection = initialSection
        activeStoryIndex = initialStoryIndex
        storiesViewedInSection = 0
        storyProgressRatio = 0.12f
        cycleStoriesCompleted = false
    }

    val initialSectionFocusRequester = remember { FocusRequester() }

    LaunchedEffect(effectiveLang, isScreensaverMode) {
        NewsRepository.ensureInitialized(context, effectiveLang, forceRefresh = false)
        if (!isScreensaverMode) {
            delay(80)
            try {
                initialSectionFocusRequester.requestFocus()
            } catch (_: Exception) {}
        }
    }

    // On each new All-Dashboards cycle entry into News, softly refresh RSS in background so headlines stay fresh
    LaunchedEffect(cycleRefreshNonce, effectiveLang) {
        if (cycleRefreshNonce > 0) {
            val ageMs = System.currentTimeMillis() - newsState.fetchedAtMillis
            if (ageMs > 85_000L) {
                NewsRepository.refreshNewsForLanguage(effectiveLang, force = true)
            }
        }
    }

    // Periodic live RSS refresh every 4 minutes
    LaunchedEffect(effectiveLang) {
        while (true) {
            delay(4 * 60 * 1000L)
            NewsRepository.refreshNewsForLanguage(effectiveLang, force = true)
        }
    }

    val currentSectionStories = remember(newsState, activeSection) {
        newsState.articlesBySection[activeSection].orEmpty().ifEmpty {
            newsState.articlesBySection[NewsSectionType.TOP_STORIES].orEmpty()
        }
    }

    val storyCount = currentSectionStories.size.coerceAtLeast(1)
    val safeIndex = ((activeStoryIndex % storyCount) + storyCount) % storyCount
    val currentStory = currentSectionStories.getOrNull(safeIndex)
    val currentStoryKey = remember(activeSection, safeIndex, currentStory?.id, currentStory?.headline) {
        currentStory?.let { "${activeSection.id}_${safeIndex}_${it.id}_${it.headline.hashCode()}" } ?: ""
    }
    val nextSection = remember(activeSection, allSections) {
        val secIdx = allSections.indexOf(activeSection).coerceAtLeast(0)
        allSections[(secIdx + 1) % allSections.size]
    }
    val nextSectionLeadStory = remember(newsState, nextSection) {
        newsState.articlesBySection[nextSection].orEmpty().firstOrNull()
    }
    val isLastStoryInSection = storiesViewedInSection + 1 >= storyCount
    val nextStory = if (autoAdvanceSections && isLastStoryInSection && nextSectionLeadStory != null) {
        nextSectionLeadStory
    } else {
        currentSectionStories.getOrNull((safeIndex + 1) % storyCount)
    }

    // Speak the currently displayed news article (headline + summary, NEVER publication date/time)
    // whenever Voice Reader is enabled and the visible story changes.
    LaunchedEffect(isVoiceReaderEnabled, currentStoryKey, effectiveLang, cycleStoriesCompleted, holdAfterCycleStoriesWhenVoiceOn) {
        if (!isVoiceReaderEnabled || (holdAfterCycleStoriesWhenVoiceOn && cycleStoriesCompleted) || currentStory == null || currentStoryKey.isBlank()) {
            voiceReader.stop()
        } else {
            val spokenText = buildSpokenNewsText(currentStory)
            voiceReader.speakStory(
                storyKey = currentStoryKey,
                text = spokenText,
                effectiveLang = effectiveLang
            )
        }
    }

    // Auto-cycle stories every 6 seconds.
    // If Voice Reader is enabled and still reading after 6 seconds, hold on the current story until reading finishes.
    LaunchedEffect(
        activeSection,
        safeIndex,
        currentStoryKey,
        storyCount,
        autoAdvanceSections,
        cycleRefreshNonce,
        holdAfterCycleStoriesWhenVoiceOn,
        isVoiceReaderEnabled,
        cycleStoriesCompleted
    ) {
        if (storyCount <= 1 && !autoAdvanceSections && !holdAfterCycleStoriesWhenVoiceOn) return@LaunchedEffect
        if (holdAfterCycleStoriesWhenVoiceOn && isVoiceReaderEnabled && cycleStoriesCompleted) {
            storyProgressRatio = 1f
            return@LaunchedEffect
        }

        val minDurationMs = 6_000L
        val stepMs = 100L
        var elapsed = 0L
        while (elapsed < minDurationMs) {
            storyProgressRatio = (elapsed.toFloat() / minDurationMs).coerceIn(0.12f, 1f)
            delay(stepMs)
            elapsed += stepMs
        }
        storyProgressRatio = 1f

        if (isVoiceReaderEnabled && currentStoryKey.isNotBlank()) {
            var extraWaitMs = 0L
            val maxExtraWaitMs = 60_000L
            while (isVoiceReaderEnabled &&
                !voiceReader.hasCompletedStory(currentStoryKey) &&
                extraWaitMs < maxExtraWaitMs
            ) {
                delay(stepMs)
                extraWaitMs += stepMs
            }
        }

        val nextViewedCount = storiesViewedInSection + 1
        val targetCycleStories = storyCount.coerceAtMost(com.example.ui.home.NEWS_VOICE_CYCLE_STORY_TARGET).coerceAtLeast(1)
        if (holdAfterCycleStoriesWhenVoiceOn && isVoiceReaderEnabled) {
            if (nextViewedCount >= targetCycleStories) {
                storiesViewedInSection = nextViewedCount
                cycleStoriesCompleted = true
                onCycleStoriesCompleted?.invoke()
            } else {
                storiesViewedInSection = nextViewedCount
                activeStoryIndex = (safeIndex + 1) % storyCount
            }
        } else if (autoAdvanceSections && nextViewedCount >= storyCount) {
            activeSection = nextSection
            activeStoryIndex = 0
            storiesViewedInSection = 0
        } else {
            activeStoryIndex = (safeIndex + 1) % storyCount
            storiesViewedInSection = if (autoAdvanceSections) nextViewedCount else 0
        }
    }

    var currentTimeStr by remember { mutableStateOf("") }

    LaunchedEffect(is24hFormat) {
        val timePattern = if (is24hFormat) "HH:mm" else "hh:mm"
        while (true) {
            val now = Date()
            currentTimeStr = SimpleDateFormat(timePattern, Locale.US).format(now)
            delay(1000L)
        }
    }

    BackHandler(enabled = true) {
        onBackToHome()
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Newsroom Digital World Map Matrix Background
            NewsroomWorldMapBackground()

            // 2. Main Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Bar
                NewsHeaderBar(
                    strings = strings,
                    isArabicFont = isArabicFont,
                    activeSourceName = newsState.activeSourceName,
                    currentTimeStr = currentTimeStr,
                    isVoiceReaderEnabled = isVoiceReaderEnabled,
                    isVoiceSpeaking = voiceReader.isSpeaking,
                    isScreensaverMode = isScreensaverMode,
                    onToggleVoiceReader = {
                        val next = !isVoiceReaderEnabled
                        coroutineScope.launch {
                            prefs.setNewsVoiceEnabled(next)
                        }
                    },
                    onRefresh = {
                        coroutineScope.launch {
                            NewsRepository.refreshNewsForLanguage(effectiveLang, force = true)
                        }
                    },
                    onBackToHome = onBackToHome
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Center 2-Pane Area: Left Sections Sidebar + Right Featured Story & Up Next
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Left Sidebar: SECTIONS
                    NewsSectionsSidebar(
                        strings = strings,
                        isArabicFont = isArabicFont,
                        activeSection = activeSection,
                        initialSection = initialSection,
                        interactiveSidebar = !isScreensaverMode,
                        activeStoryTimeAgo = currentStory?.timeAgo ?: "12 min ago",
                        storyProgressRatio = storyProgressRatio,
                        initialSectionFocusRequester = initialSectionFocusRequester,
                        onSelectSection = { section ->
                            if (activeSection != section) {
                                activeSection = section
                                activeStoryIndex = 0
                                storiesViewedInSection = 0
                            }
                        },
                        modifier = Modifier
                            .weight(0.265f)
                            .fillMaxHeight()
                    )

                    // Right Area: Main Story Card + Bottom Controls + Overlapping UP NEXT Card
                    if (currentStory != null) {
                        NewsFeaturedStoryPanel(
                            currentStory = currentStory,
                            nextStory = nextStory,
                            currentIndex = safeIndex,
                            totalCount = storyCount,
                            updatedTime = newsState.updatedTime,
                            strings = strings,
                            isArabicFont = isArabicFont,
                            isVoiceSpeaking = isVoiceReaderEnabled && voiceReader.isSpeaking,
                            onNextStory = {
                                activeStoryIndex = (safeIndex + 1) % storyCount
                            },
                            onPrevStory = {
                                activeStoryIndex = if (safeIndex - 1 < 0) storyCount - 1 else safeIndex - 1
                            },
                            onSelectStoryIndex = { idx ->
                                activeStoryIndex = idx.coerceIn(0, storyCount - 1)
                            },
                            modifier = Modifier
                                .weight(0.735f)
                                .fillMaxHeight()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Breaking News Ticker Bar
                // Moves Left-to-Right (→) for Arabic (whether "ar" or "ar_rtl") and Right-to-Left (←) for all other languages
                BreakingNewsTickerBar(
                    breakingBadge = strings.breakingBadge,
                    tickerItems = newsState.breakingTickerItems,
                    isArabicOrRtl = isArabicFont,
                    isArabicFont = isArabicFont
                )
            }
        }
    }
}

@Composable
private fun NewsroomWorldMapBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "newsroom_ambient")
    val scanPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "news_world_scan"
    )
    val studioGlow = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "news_studio_glow"
    )
    val pingPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "news_hub_ping"
    )

    // Precompute normalized world-map dot coordinates (nx, ny) once instead of 1,560 grid iterations per frame
    val mapDotsNorm = remember {
        val cols = 64
        val rows = 30
        val coords = ArrayList<Float>(512)
        for (r in 2 until rows - 2) {
            for (c in 2 until cols - 2) {
                val nx = c.toFloat() / cols
                val ny = r.toFloat() / rows
                val inAmericas = (nx in 0.10f..0.28f && ny in 0.16f..0.75f && (nx + ny * 0.3f) in 0.20f..0.46f)
                val inEurAfrica = (nx in 0.40f..0.58f && ny in 0.15f..0.74f)
                val inAsia = (nx in 0.58f..0.86f && ny in 0.16f..0.62f)
                if ((inAmericas || inEurAfrica || inAsia) && ((r + c) % 2 == 0)) {
                    coords.add(nx)
                    coords.add(ny)
                }
            }
        }
        FloatArray(coords.size) { coords[it] }
    }

    val baseBgColors = remember {
        listOf(
            Color(0xFF070F22),
            Color(0xFF0C1D3E),
            Color(0xFF102A56),
            Color(0xFF0A1833)
        )
    }
    val crimsonColors = remember {
        listOf(
            Color(0xFFDC2626).copy(alpha = 0.28f),
            Color(0xFF991B1B).copy(alpha = 0.12f),
            Color.Transparent
        )
    }
    val sapphireColors = remember {
        listOf(
            Color(0xFF2563EB).copy(alpha = 0.25f),
            Color(0xFF1D4ED8).copy(alpha = 0.10f),
            Color.Transparent
        )
    }
    val scanColors = remember {
        listOf(
            Color.Transparent,
            Color(0xFF60A5FA).copy(alpha = 0.075f),
            Color(0xFF93C5FD).copy(alpha = 0.11f),
            Color(0xFF60A5FA).copy(alpha = 0.075f),
            Color.Transparent
        )
    }

    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val baseBrush = Brush.linearGradient(
                    colors = baseBgColors,
                    start = Offset.Zero,
                    end = Offset(w, h)
                )
                val hubOffsets = arrayOf(
                    Offset(w * 0.21f, h * 0.34f),
                    Offset(w * 0.50f, h * 0.38f),
                    Offset(w * 0.77f, h * 0.36f)
                )
                val hubRingStroke = Stroke(width = 1.3.dp.toPx())
                val minRingPx = 6.dp.toPx()
                val ringSpanPx = 30.dp.toPx()
                val hubCoreRadius = 2.6.dp.toPx()

                val dotCount = mapDotsNorm.size / 2
                val dotScreenX = FloatArray(dotCount)
                val dotScreenY = FloatArray(dotCount)
                val dotNormX = FloatArray(dotCount)
                for (i in 0 until dotCount) {
                    val nx = mapDotsNorm[i * 2]
                    val ny = mapDotsNorm[i * 2 + 1]
                    dotNormX[i] = nx
                    dotScreenX[i] = nx * w
                    dotScreenY[i] = ny * h
                }

                onDrawBehind {
                    val scan = scanPhase.value
                    val glowAngle = studioGlow.value * 2f * PI.toFloat()
                    val ping = pingPhase.value

                    // 1. Deep broadcast navy-blue gradient
                    drawRect(brush = baseBrush)

                    // 2. Breathing Bottom-Left Crimson Broadcast Studio Glow
                    val crimsonCenter = Offset(
                        w * (0.09f + cos(glowAngle.toDouble()).toFloat() * 0.03f),
                        h * (0.89f + sin(glowAngle.toDouble()).toFloat() * 0.03f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = crimsonColors,
                            center = crimsonCenter,
                            radius = w * 0.36f
                        ),
                        radius = w * 0.36f,
                        center = crimsonCenter
                    )

                    // 3. Breathing Center-Right Electric Sapphire Glow
                    val sapphireCenter = Offset(
                        w * (0.78f + sin(glowAngle.toDouble()).toFloat() * 0.04f),
                        h * (0.54f + cos(glowAngle.toDouble()).toFloat() * 0.04f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = sapphireColors,
                            center = sapphireCenter,
                            radius = w * 0.43f
                        ),
                        radius = w * 0.43f,
                        center = sapphireCenter
                    )

                    // 4. Silky Horizontal Broadcast Scan Beam Sweeping Across World Map
                    val scanX = w * scan
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = scanColors,
                            startX = (scanX - w * 0.09f).coerceAtLeast(0f),
                            endX = (scanX + w * 0.09f).coerceAtMost(w)
                        )
                    )

                    // 5. Dotted World Map Matrix Pattern Illuminated Smoothly by the Scan Beam
                    for (i in 0 until dotCount) {
                        val dist = abs(dotNormX[i] - scan)
                        val highlight = 1f - (dist / 0.08f).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color(0xFF60A5FA).copy(alpha = 0.11f + highlight * 0.22f),
                            radius = 2.1f + highlight * 1.1f,
                            center = Offset(dotScreenX[i], dotScreenY[i])
                        )
                    }

                    // 6. Expanding Global News Hub Satellite Signal Rings (Americas, Europe/MENA, Asia)
                    for (idx in hubOffsets.indices) {
                        val center = hubOffsets[idx]
                        val localPing = (ping + idx * 0.33f) % 1f
                        val ringRadius = minRingPx + ringSpanPx * localPing
                        val ringAlpha = (1f - localPing) * 0.28f
                        drawCircle(
                            color = if (idx == 1) Color(0xFFF87171).copy(alpha = ringAlpha) else Color(0xFF60A5FA).copy(alpha = ringAlpha),
                            radius = ringRadius,
                            center = center,
                            style = hubRingStroke
                        )
                        drawCircle(
                            color = if (idx == 1) Color(0xFFEF4444).copy(alpha = 0.55f) else Color(0xFF93C5FD).copy(alpha = 0.55f),
                            radius = hubCoreRadius,
                            center = center
                        )
                    }
                }
            }
    )
}

@Composable
private fun NewsHeaderBar(
    strings: NewsLocalizedStrings,
    isArabicFont: Boolean,
    activeSourceName: String,
    currentTimeStr: String,
    isVoiceReaderEnabled: Boolean,
    isVoiceSpeaking: Boolean,
    isScreensaverMode: Boolean,
    onToggleVoiceReader: () -> Unit,
    onRefresh: () -> Unit,
    onBackToHome: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Back button + NEWS CENTER + Wireframe Globe + Red • LIVE Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            var backFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (backFocused) Color(0xFFEF4444).copy(alpha = 0.28f)
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .border(
                        width = if (backFocused) 1.5.dp else 1.dp,
                        color = if (backFocused) Color(0xFFF87171) else Color.White.copy(alpha = 0.16f),
                        shape = CircleShape
                    )
                    .onFocusChanged { backFocused = it.isFocused }
                    .focusable()
                    .clickable { onBackToHome() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = strings.backToHomeHint,
                    tint = Color.White,
                    modifier = Modifier.size(17.dp)
                )
            }

            Text(
                text = strings.newsCenterTitle,
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = if (isArabicFont) CairoFontFamily else null
            )

            NewsWireframeGlobeIcon(modifier = Modifier.size(25.dp))

            // Red • LIVE Pill Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDC2626))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val liveDotAnim = rememberInfiniteTransition(label = "news_live_dot")
                val liveDotPulse = liveDotAnim.animateFloat(
                    initialValue = 0.35f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1050, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "news_live_alpha"
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .graphicsLayer {
                            val p = liveDotPulse.value
                            alpha = p
                            val s = 0.85f + p * 0.28f
                            scaleX = s
                            scaleY = s
                        }
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Text(
                    text = strings.liveBadge,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (isArabicFont) CairoFontFamily else null
                )
            }
        }

        // Right: Auto-Selected Language Source Pill + Voice Reader Pill + Refresh + • Live + Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Auto-selected language news source badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.72f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "${strings.sourceLabel}$activeSourceName",
                    color = Color(0xFF93C5FD),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = if (isArabicFont) CairoFontFamily else null
                )
            }

            // Voice Reader Toggle / Live Reading Pill
            if (!isScreensaverMode || isVoiceReaderEnabled) {
                var voiceFocused by remember { mutableStateOf(false) }
                val activeBorderColor = when {
                    voiceFocused -> Color(0xFFFBBF24)
                    isVoiceSpeaking -> Color(0xFFF97316)
                    isVoiceReaderEnabled -> Color(0xFF38BDF8).copy(alpha = 0.65f)
                    else -> Color.White.copy(alpha = 0.16f)
                }
                val pillBgColor = when {
                    voiceFocused -> Color(0xFFF97316).copy(alpha = 0.34f)
                    isVoiceSpeaking -> Color(0xFF9A3412).copy(alpha = 0.62f)
                    isVoiceReaderEnabled -> Color(0xFF0C4A6E).copy(alpha = 0.68f)
                    else -> Color(0xFF1E293B).copy(alpha = 0.72f)
                }
                Row(
                    modifier = Modifier
                        .testTag("news_voice_toggle_button")
                        .clip(RoundedCornerShape(8.dp))
                        .background(pillBgColor)
                        .border(
                            width = if (voiceFocused || isVoiceSpeaking) 1.5.dp else 1.dp,
                            color = activeBorderColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .then(
                            if (!isScreensaverMode) {
                                Modifier
                                    .onFocusChanged { voiceFocused = it.isFocused }
                                    .focusable()
                                    .clickable { onToggleVoiceReader() }
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (isVoiceReaderEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = if (isVoiceReaderEnabled) strings.voiceOnLabel else strings.voiceOffLabel,
                        tint = when {
                            isVoiceSpeaking -> Color(0xFFFDBA74)
                            isVoiceReaderEnabled -> Color(0xFF7DD3FC)
                            else -> Color.White.copy(alpha = 0.65f)
                        },
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = when {
                            isVoiceSpeaking -> strings.voiceSpeakingLabel
                            isVoiceReaderEnabled -> strings.voiceOnLabel
                            else -> strings.voiceOffLabel
                        },
                        color = when {
                            isVoiceSpeaking -> Color(0xFFFED7AA)
                            isVoiceReaderEnabled -> Color(0xFFE0F2FE)
                            else -> Color.White.copy(alpha = 0.75f)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                        maxLines = 1
                    )
                }
            }

            // Refresh button
            var refreshFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(31.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (refreshFocused) Color(0xFF3B82F6).copy(alpha = 0.32f)
                        else Color(0xFF1E293B).copy(alpha = 0.72f)
                    )
                    .border(
                        width = if (refreshFocused) 1.5.dp else 1.dp,
                        color = if (refreshFocused) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onFocusChanged { refreshFocused = it.isFocused }
                    .focusable()
                    .clickable { onRefresh() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = strings.refreshAction,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                Text(
                    text = "Live",
                    color = Color(0xFF4ADE80),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = currentTimeStr,
                color = Color.White,
                fontSize = 31.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NewsWireframeGlobeIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val strokeColor = Color(0xFF60A5FA)

        drawCircle(
            color = strokeColor,
            radius = r,
            center = center,
            style = Stroke(width = 1.6f)
        )
        drawOval(
            color = strokeColor,
            topLeft = Offset(center.x - r * 0.52f, center.y - r),
            size = Size(r * 1.04f, r * 2f),
            style = Stroke(width = 1.3f)
        )
        drawLine(
            color = strokeColor,
            start = Offset(center.x, center.y - r),
            end = Offset(center.x, center.y + r),
            strokeWidth = 1.2f
        )
        drawLine(
            color = strokeColor,
            start = Offset(center.x - r, center.y),
            end = Offset(center.x + r, center.y),
            strokeWidth = 1.2f
        )
        drawLine(
            color = strokeColor,
            start = Offset(center.x - r * 0.85f, center.y - r * 0.48f),
            end = Offset(center.x + r * 0.85f, center.y - r * 0.48f),
            strokeWidth = 1.1f
        )
        drawLine(
            color = strokeColor,
            start = Offset(center.x - r * 0.85f, center.y + r * 0.48f),
            end = Offset(center.x + r * 0.85f, center.y + r * 0.48f),
            strokeWidth = 1.1f
        )
    }
}

@Composable
private fun NewsSectionsSidebar(
    strings: NewsLocalizedStrings,
    isArabicFont: Boolean,
    activeSection: NewsSectionType,
    initialSection: NewsSectionType,
    interactiveSidebar: Boolean,
    activeStoryTimeAgo: String,
    storyProgressRatio: Float,
    initialSectionFocusRequester: FocusRequester,
    onSelectSection: (NewsSectionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val sectionItems = remember(strings) {
        listOf(
            Triple(NewsSectionType.TOP_STORIES, strings.sectionTopStories, Icons.AutoMirrored.Filled.Article),
            Triple(NewsSectionType.POLITICS, strings.sectionPolitics, Icons.Default.RecordVoiceOver),
            Triple(NewsSectionType.ECONOMY, strings.sectionEconomy, Icons.Default.BarChart),
            Triple(NewsSectionType.TECHNOLOGY, strings.sectionTechnology, Icons.Default.Devices),
            Triple(NewsSectionType.WORLD, strings.sectionWorld, Icons.Default.Public)
        )
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF15233E).copy(alpha = 0.86f),
                        Color(0xFF101B32).copy(alpha = 0.88f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = strings.sectionsHeader,
            color = Color.White.copy(alpha = 0.68f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isArabicFont) CairoFontFamily else null,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        sectionItems.forEachIndexed { index, (sectionType, title, icon) ->
            val isSelected = activeSection == sectionType
            var isFocused by remember { mutableStateOf(false) }

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (interactiveSidebar && sectionType == initialSection) {
                                Modifier.focusRequester(initialSectionFocusRequester)
                            } else Modifier
                        )
                        .background(
                            when {
                                isFocused -> Color(0xFFF97316).copy(alpha = 0.16f)
                                isSelected -> Color.White.copy(alpha = 0.05f)
                                else -> Color.Transparent
                            }
                        )
                        .then(
                            if (interactiveSidebar) {
                                Modifier
                                    .onFocusChanged { state ->
                                        isFocused = state.isFocused
                                        if (state.isFocused) {
                                            onSelectSection(sectionType)
                                        }
                                    }
                                    .focusable()
                                    .clickable { onSelectSection(sectionType) }
                            } else {
                                Modifier
                            }
                        )
                        .padding(end = 16.dp, top = 10.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glowing coral-orange active indicator bar on start edge
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(42.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isSelected || isFocused) Color(0xFFF97316)
                                else Color.Transparent
                            )
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isSelected || isFocused) Color.White else Color.White.copy(alpha = 0.62f),
                        modifier = Modifier.size(23.dp)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            color = if (isSelected || isFocused) Color.White else Color.White.copy(alpha = 0.85f),
                            fontSize = 17.sp,
                            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = if (isArabicFont) CairoFontFamily else null,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isSelected) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Progress bar under active section title
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White.copy(alpha = 0.16f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(storyProgressRatio.coerceIn(0.15f, 1f))
                                            .clip(RoundedCornerShape(50))
                                            .background(Color(0xFFF97316))
                                    )
                                }

                                Text(
                                    text = activeStoryTimeAgo,
                                    color = Color.White.copy(alpha = 0.52f),
                                    fontSize = 10.sp,
                                    fontFamily = if (isArabicFont) CairoFontFamily else null,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (index < sectionItems.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                            .height(0.8.dp)
                            .background(Color.White.copy(alpha = 0.09f))
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsFeaturedStoryPanel(
    currentStory: NewsArticleItem,
    nextStory: NewsArticleItem?,
    currentIndex: Int,
    totalCount: Int,
    updatedTime: String,
    strings: NewsLocalizedStrings,
    isArabicFont: Boolean,
    isVoiceSpeaking: Boolean,
    onNextStory: () -> Unit,
    onPrevStory: () -> Unit,
    onSelectStoryIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(16.dp)
    var mainCardFocused by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main Hero Story Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(cardShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF14223C).copy(alpha = 0.90f),
                                Color(0xFF0E192E).copy(alpha = 0.94f)
                            )
                        )
                    )
                    .border(
                        width = if (mainCardFocused) 2.dp else 1.dp,
                        color = if (mainCardFocused) Color(0xFFF97316) else Color.White.copy(alpha = 0.20f),
                        shape = cardShape
                    )
                    .onFocusChanged { mainCardFocused = it.isFocused }
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when (event.key) {
                                Key.DirectionRight -> {
                                    onNextStory()
                                    true
                                }
                                Key.DirectionLeft -> {
                                    onPrevStory()
                                    true
                                }
                                else -> false
                            }
                        } else false
                    }
                    .focusable()
                    .clickable { onNextStory() }
                    .padding(12.dp)
            ) {
                Crossfade(
                    targetState = currentStory,
                    animationSpec = tween(durationMillis = 320),
                    label = "featured_news_story"
                ) { story ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top 54%: Real High-Resolution Journalistic Photo
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.54f)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            NewsEditorialPhoto(
                                storyId = story.id,
                                imageUrl = story.imageUrl,
                                section = story.section,
                                fallbackIndex = currentIndex,
                                contentDescription = story.headline,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Smooth bottom vignette blending into text section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFF101C33).copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                            )
                        }

                        // Bottom 46%: Category Tag + Bold Headline + Summary + Source & Time
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.46f)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = story.categoryLabel,
                                    color = Color(0xFFF97316),
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = if (isArabicFont) CairoFontFamily else null
                                )

                                Text(
                                    text = story.headline,
                                    color = Color.White,
                                    fontSize = 23.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = if (isArabicFont) CairoFontFamily else null,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 28.sp
                                )

                                Text(
                                    text = story.summary,
                                    color = Color.White.copy(alpha = 0.80f),
                                    fontSize = 13.5.sp,
                                    fontFamily = if (isArabicFont) CairoFontFamily else null,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 18.sp,
                                    modifier = Modifier.fillMaxWidth(0.74f)
                                )
                            }

                            // Source • Time ago + Clock Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${story.sourceName} • ${story.timeAgo}",
                                    color = Color.White.copy(alpha = 0.62f),
                                    fontSize = 12.sp,
                                    fontFamily = if (isArabicFont) CairoFontFamily else null
                                )
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.55f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Row below Main Card: Updated time + Carousel Dots (2 / 6) + Stopwatch Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${strings.updatedPrefix}$updatedTime",
                    color = Color.White.copy(alpha = 0.58f),
                    fontSize = 12.sp,
                    fontFamily = if (isArabicFont) CairoFontFamily else null
                )

                // Pagination dots + "2 / 6"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    for (i in 0 until totalCount) {
                        val isCurrent = i == currentIndex
                        Box(
                            modifier = Modifier
                                .size(if (isCurrent) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) Color(0xFFF97316)
                                    else Color.White.copy(alpha = 0.30f)
                                )
                                .clickable { onSelectStoryIndex(i) }
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${currentIndex + 1} / $totalCount",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isVoiceSpeaking) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = strings.voiceSpeakingLabel,
                            tint = Color(0xFFFDBA74),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.55f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Overlapping Bottom-End "UP NEXT" Card matching reference image
        if (nextStory != null) {
            var upNextFocused by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .width(235.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B2B4B).copy(alpha = 0.95f),
                                Color(0xFF111C33).copy(alpha = 0.97f)
                            )
                        )
                    )
                    .border(
                        width = if (upNextFocused) 1.8.dp else 1.dp,
                        color = if (upNextFocused) Color(0xFFF97316) else Color.White.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .onFocusChanged { upNextFocused = it.isFocused }
                    .focusable()
                    .clickable { onNextStory() }
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = strings.upNextHeader,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = if (isArabicFont) CairoFontFamily else null
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(54.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(7.dp))
                    ) {
                        NewsEditorialPhoto(
                            storyId = nextStory.id,
                            imageUrl = nextStory.imageUrl,
                            section = nextStory.section,
                            fallbackIndex = (currentIndex + 1) % totalCount.coerceAtLeast(1),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Text(
                        text = "${strings.upNextPrefix}${nextStory.headline}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 13.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun NewsEditorialPhoto(
    storyId: String,
    imageUrl: String,
    section: NewsSectionType,
    fallbackIndex: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var attemptIndex by remember(storyId, imageUrl, section, fallbackIndex) { mutableIntStateOf(0) }

    val primaryFallback = remember(section, fallbackIndex) {
        NewsRepository.getFallbackPhotoForSection(section, fallbackIndex)
    }
    val secondaryFallback = remember(section, fallbackIndex) {
        NewsRepository.getSecondaryFallbackPhoto(section, fallbackIndex)
    }

    val resolvedUrl = when (attemptIndex) {
        0 -> imageUrl.ifBlank { primaryFallback }
        1 -> primaryFallback
        else -> secondaryFallback
    }

    val imageRequest = remember(resolvedUrl) {
        ImageRequest.Builder(context)
            .data(resolvedUrl)
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Linux; Android 13; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
            )
            // Request pure JPEG/PNG only (NO image/avif) so every Android TV Coil decoder decodes reliably
            .addHeader("Accept", "image/jpeg, image/png, */*;q=0.8")
            .crossfade(250)
            .build()
    }

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0C182E),
                    Color(0xFF15294D),
                    Color(0xFF0D1B36)
                )
            )
        )
    ) {
        AsyncImage(
            model = imageRequest,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            onError = {
                if (attemptIndex < 2) {
                    attemptIndex++
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Bottom Breaking News Ticker Bar.
 * CRITICAL USER REQUIREMENT:
 * "و الشريط السفلي بالنسبة للغة العربية يتحرك عكس اللغات الأخرى"
 * - For Arabic / RTL languages (`isArabicOrRtl == true`): Ticker scrolls LEFT-TO-RIGHT (→) (`directionSign = +1f`).
 * - For all other languages (`isArabicOrRtl == false`): Ticker scrolls RIGHT-TO-LEFT (←) (`directionSign = -1f`).
 */
@Composable
private fun BreakingNewsTickerBar(
    breakingBadge: String,
    tickerItems: List<String>,
    isArabicOrRtl: Boolean,
    isArabicFont: Boolean
) {
    val singleCycleText = remember(tickerItems) {
        val cleaned = tickerItems.map { it.trim() }.filter { it.isNotBlank() }
        if (cleaned.isEmpty()) "" else cleaned.joinToString("     •     ") + "     •     "
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val tickerTextStyle = remember(isArabicFont, isArabicOrRtl) {
        TextStyle(
            color = Color.White.copy(alpha = 0.94f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isArabicFont) CairoFontFamily else null,
            textDirection = if (isArabicOrRtl) TextDirection.Rtl else TextDirection.Ltr
        )
    }

    val measuredLayout = remember(singleCycleText, tickerTextStyle, textMeasurer) {
        textMeasurer.measure(
            text = singleCycleText,
            style = tickerTextStyle,
            softWrap = false,
            maxLines = 1,
            constraints = Constraints()
        )
    }

    val cycleWidthPx = remember(measuredLayout) {
        measuredLayout.size.width.toFloat().coerceAtLeast(300f)
    }

    var scrollOffsetPx by remember(isArabicOrRtl, cycleWidthPx) {
        mutableFloatStateOf(if (isArabicOrRtl) -cycleWidthPx else 0f)
    }

    // Continuous smooth 60fps ticker animation in opposite directions for Arabic vs Other languages
    LaunchedEffect(singleCycleText, isArabicOrRtl, cycleWidthPx) {
        if (cycleWidthPx <= 0f) return@LaunchedEffect
        val speedPxPerSec = with(density) { 68.dp.toPx() }
        scrollOffsetPx = if (isArabicOrRtl) -cycleWidthPx else 0f
        var lastFrameNanos = withFrameNanos { it }

        while (true) {
            val frameNanos = withFrameNanos { it }
            val dtSec = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.1f)
            lastFrameNanos = frameNanos

            if (isArabicOrRtl) {
                // Arabic moves Left-to-Right (→) (opposite of other languages)
                scrollOffsetPx += speedPxPerSec * dtSec
                if (scrollOffsetPx >= 0f) {
                    scrollOffsetPx -= cycleWidthPx
                }
            } else {
                // Other languages move Right-to-Left (←)
                scrollOffsetPx -= speedPxPerSec * dtSec
                if (scrollOffsetPx <= -cycleWidthPx) {
                    scrollOffsetPx += cycleWidthPx
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF451018).copy(alpha = 0.92f),
                        Color(0xFF161F35).copy(alpha = 0.94f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // BREAKING ⚡ / عاجل ⚡ Badge
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .background(Color(0xFFDC2626))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = breakingBadge,
                color = Color.White,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = if (isArabicFont) CairoFontFamily else null
            )
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }

        // Unbounded Canvas Ticker Viewport so text is never clipped to container width
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clipToBounds()
        ) {
            val textHeight = measuredLayout.size.height.toFloat()
            val topY = ((size.height - textHeight) / 2f).coerceAtLeast(0f)

            var drawX = scrollOffsetPx
            while (drawX > 0f) {
                drawX -= cycleWidthPx
            }
            while (drawX < size.width) {
                if (drawX + cycleWidthPx > 0f) {
                    drawText(
                        textLayoutResult = measuredLayout,
                        topLeft = Offset(drawX, topY)
                    )
                }
                drawX += cycleWidthPx
            }
        }
    }
}
