package com.example.ui.economic

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.economic.EconomicCalendarEvent
import com.example.data.economic.EconomicDashboardData
import com.example.data.economic.EconomicLocalizedStrings
import com.example.data.economic.EconomicRepository
import com.example.data.economic.MarketQuoteItem
import com.example.data.local.LauncherPreferences
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvAccentGold
import com.example.util.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val EmeraldUp = Color(0xFF34D399)
private val CoralDown = Color(0xFFF87171)
private val NeutralGray = Color(0xFF94A3B8)

private val EconomicNoPadTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

@Composable
fun EconomicScreen(
    is24hFormat: Boolean = true,
    appLanguage: String = "en",
    openSettingsInitially: Boolean = false,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { LauncherPreferences(context) }
    val initialLang = remember { prefs.getCachedLanguage() }
    val savedLang by prefs.languageFlow.collectAsState(initial = initialLang)
    val resolvedLang = remember(savedLang, appLanguage) {
        if (savedLang != "system") savedLang else appLanguage
    }
    val effectiveLang = remember(resolvedLang) {
        LocaleHelper.getEffectiveLanguage(resolvedLang)
    }
    val isRtl = remember(resolvedLang) {
        LocaleHelper.isRtlLanguage(resolvedLang)
    }

    val selectedIndexIds by prefs.economicIndicesFlow.collectAsState(
        initial = listOf("sp500", "nasdaq", "dow", "dxy")
    )
    val selectedCommodityIds by prefs.economicCommoditiesFlow.collectAsState(
        initial = listOf("gold", "brent", "btc", "eth")
    )
    val selectedForexIds by prefs.economicForexFlow.collectAsState(
        initial = listOf("eur_usd", "gbp_usd", "usd_jpy", "usd_sar")
    )

    var showCustomizeDialog by remember { mutableStateOf(openSettingsInitially) }
    var activeChartIndexId by remember(selectedIndexIds) {
        mutableStateOf(selectedIndexIds.firstOrNull() ?: "sp500")
    }

    BackHandler(enabled = true) {
        if (showCustomizeDialog) {
            showCustomizeDialog = false
        } else {
            onBackToHome()
        }
    }

    LaunchedEffect(Unit) {
        EconomicRepository.ensureInitialized(context)
    }

    val dashboard by EconomicRepository.dashboardState.collectAsState()
    val strings = remember(effectiveLang) {
        EconomicRepository.getLocalizedStrings(effectiveLang)
    }
    val isArabicFont = remember(effectiveLang) {
        LocaleHelper.isArabicOrRtlLanguage(effectiveLang)
    }
    val textFont = if (isArabicFont) CairoFontFamily else FontFamily.Default

    val displayedIndices = remember(dashboard.indicesMap, selectedIndexIds) {
        selectedIndexIds.mapNotNull { dashboard.indicesMap[it] }
            .ifEmpty { dashboard.indicesMap.values.take(4) }
            .take(4)
    }
    val displayedCommodities = remember(dashboard.commoditiesMap, selectedCommodityIds) {
        selectedCommodityIds.mapNotNull { dashboard.commoditiesMap[it] }
            .ifEmpty { dashboard.commoditiesMap.values.take(4) }
            .take(4)
    }
    val displayedForex = remember(dashboard.forexMap, selectedForexIds) {
        selectedForexIds.mapNotNull { dashboard.forexMap[it] }
            .ifEmpty { dashboard.forexMap.values.take(4) }
            .take(4)
    }
    val activeChartQuote = remember(dashboard.indicesMap, activeChartIndexId, displayedIndices) {
        dashboard.indicesMap[activeChartIndexId] ?: displayedIndices.firstOrNull()
    }

    var currentTimeString by remember { mutableStateOf("") }

    LaunchedEffect(is24hFormat) {
        val pattern = if (is24hFormat) "HH:mm" else "hh:mm"
        val timeFormat = SimpleDateFormat(pattern, Locale.US)
        while (true) {
            val now = Calendar.getInstance()
            val rawTime = timeFormat.format(now.time)
            currentTimeString = rawTime.map { ch ->
                val digit = Character.getNumericValue(ch)
                if (digit in 0..9 && ch !in '0'..'9') digit.toString()[0] else ch
            }.joinToString("")
            delay(1000L)
        }
    }

    val customizeBtnFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(60L)
        try {
            customizeBtnFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        val nativeCode = event.nativeKeyEvent.keyCode
                        if (com.example.ui.common.isRemoteMenuKey(nativeCode) ||
                            nativeCode == android.view.KeyEvent.KEYCODE_SETTINGS
                        ) {
                            showCustomizeDialog = true
                            true
                        } else false
                    } else false
                }
                .testTag("economic_full_screen")
        ) {
            val isCompactHeight = maxHeight < 470.dp
            val outerHorizPad = if (isCompactHeight) 20.dp else 30.dp
            val outerVertPad = if (isCompactHeight) 8.dp else 14.dp
            val rowGap = if (isCompactHeight) 6.dp else 10.dp
            val colGap = if (isCompactHeight) 9.dp else 12.dp
            val footerGap = if (isCompactHeight) 4.dp else 6.dp

            // 1. Financial Dark Teal / Navy Candlestick Background
            EconomicFinancialBackground()

            // 2. Main Dashboard Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = outerHorizPad, vertical = outerVertPad),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // =================================================================
                // TOP HEADER: Title + Globe + Customize Button on Start | Live + Clock + Source on End
                // =================================================================
                EconomicTopHeader(
                    strings = strings,
                    timeString = currentTimeString,
                    textFont = textFont,
                    isCompact = isCompactHeight,
                    customizeFocusRequester = customizeBtnFocusRequester,
                    onOpenCustomize = { showCustomizeDialog = true },
                    onRefresh = { EconomicRepository.refreshMarketDataAsync(context) }
                )

                Spacer(modifier = Modifier.height(rowGap))

                // =================================================================
                // ROW 1: 4 Major Market Index Cards (S&P 500 | Nasdaq | Dow Jones | DXY)
                // =================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.285f),
                    horizontalArrangement = Arrangement.spacedBy(colGap)
                ) {
                    displayedIndices.forEach { quote ->
                        EconomicIndexTopCard(
                            quote = quote,
                            langCode = effectiveLang,
                            textFont = textFont,
                            isSelectedForChart = activeChartQuote?.id == quote.id,
                            onFocused = { activeChartIndexId = quote.id },
                            onClick = {
                                activeChartIndexId = quote.id
                                showCustomizeDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(rowGap))

                // =================================================================
                // ROW 2: Commodities & Crypto | Smooth 5-day Curve | Forex
                // =================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.415f),
                    horizontalArrangement = Arrangement.spacedBy(colGap)
                ) {
                    // Start Card: Commodities and Crypto
                    EconomicCommoditiesCard(
                        title = strings.commoditiesAndCryptoTitle,
                        items = displayedCommodities,
                        langCode = effectiveLang,
                        textFont = textFont,
                        onClick = { showCustomizeDialog = true },
                        modifier = Modifier
                            .weight(1.08f)
                            .fillMaxHeight()
                    )

                    // Center Card: Smooth 5-day Curve Chart
                    EconomicSmooth5DayCard(
                        strings = strings,
                        activeQuote = activeChartQuote,
                        langCode = effectiveLang,
                        textFont = textFont,
                        modifier = Modifier
                            .weight(1.24f)
                            .fillMaxHeight()
                    )

                    // End Card: Forex
                    EconomicForexCard(
                        title = strings.forexTitle,
                        items = displayedForex,
                        textFont = textFont,
                        onClick = { showCustomizeDialog = true },
                        modifier = Modifier
                            .weight(0.98f)
                            .fillMaxHeight()
                    )
                }

                Spacer(modifier = Modifier.height(rowGap))

                // =================================================================
                // ROW 3: Economic Calendar (5 Columns)
                // =================================================================
                EconomicCalendarCard(
                    strings = strings,
                    events = dashboard.calendarEvents,
                    langCode = effectiveLang,
                    textFont = textFont,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.300f)
                )

                Spacer(modifier = Modifier.height(footerGap))

                // =================================================================
                // FOOTER ROW: Last updated + Refresh + Sensor Indicator
                // =================================================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { EconomicRepository.refreshMarketDataAsync(context) }
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${strings.lastUpdatedPrefix} ${dashboard.lastUpdatedTime}",
                            color = Color.White.copy(alpha = 0.68f),
                            fontSize = if (isCompactHeight) 10.5.sp else 12.sp,
                            fontFamily = textFont,
                            fontWeight = FontWeight.Medium,
                            style = EconomicNoPadTextStyle,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.62f),
                            modifier = Modifier.size(if (isCompactHeight) 11.dp else 12.dp)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.85f))
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(if (isCompactHeight) 13.dp else 15.dp)
                        )
                    }
                }
            }

            // =====================================================================
            // CUSTOMIZE DIALOG: Choose Which Stock Exchanges, Currencies, & Commodities Display
            // =====================================================================
            if (showCustomizeDialog) {
                EconomicCustomizationDialog(
                    strings = strings,
                    dashboard = dashboard,
                    selectedIndices = selectedIndexIds,
                    selectedForex = selectedForexIds,
                    selectedCommodities = selectedCommodityIds,
                    langCode = effectiveLang,
                    textFont = textFont,
                    onSaveIndices = { ids -> scope.launch { prefs.setEconomicIndices(ids) } },
                    onSaveForex = { ids -> scope.launch { prefs.setEconomicForex(ids) } },
                    onSaveCommodities = { ids -> scope.launch { prefs.setEconomicCommodities(ids) } },
                    onResetDefaults = {
                        scope.launch {
                            prefs.setEconomicIndices(listOf("sp500", "nasdaq", "dow", "dxy"))
                            prefs.setEconomicCommodities(listOf("gold", "brent", "btc", "eth"))
                            prefs.setEconomicForex(listOf("eur_usd", "gbp_usd", "usd_jpy", "usd_sar"))
                        }
                    },
                    onDismiss = { showCustomizeDialog = false }
                )
            }
        }
    }
}

@Composable
private fun EconomicTopHeader(
    strings: EconomicLocalizedStrings,
    timeString: String,
    textFont: FontFamily,
    isCompact: Boolean,
    customizeFocusRequester: FocusRequester,
    onOpenCustomize: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start: Title + Wireframe Globe + Customize Button + "Global Market"
        Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 3.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 10.dp)
            ) {
                Text(
                    text = strings.dashboardTitle,
                    color = Color.White,
                    fontSize = if (isCompact) 20.sp else 27.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = textFont,
                    style = EconomicNoPadTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(if (isCompact) 18.dp else 23.dp)
                )

                // Focusable Customize Markets & Currencies Pill Button
                TvFocusableCard(
                    onClick = onOpenCustomize,
                    focusRequester = customizeFocusRequester,
                    shape = RoundedCornerShape(50),
                    scaleUpRatio = 1.05f,
                    useDefaultBorder = false,
                    elevation = 0.dp,
                    modifier = Modifier.height(if (isCompact) 24.dp else 29.dp)
                ) { isFocused ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(
                                if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(50)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isFocused) TvAccentGold else Color(0xFF38BDF8).copy(alpha = 0.45f),
                                shape = RoundedCornerShape(50)
                            )
                            .padding(horizontal = if (isCompact) 9.dp else 11.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = strings.customizeButtonLabel,
                            tint = if (isFocused) Color.Black else Color(0xFF38BDF8),
                            modifier = Modifier.size(if (isCompact) 12.dp else 13.5.dp)
                        )
                        Text(
                            text = strings.customizeButtonLabel,
                            color = if (isFocused) Color.Black else Color.White.copy(alpha = 0.92f),
                            fontSize = if (isCompact) 10.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = textFont,
                            style = EconomicNoPadTextStyle,
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = strings.globalMarketSubtitle,
                color = Color.White.copy(alpha = 0.68f),
                fontSize = if (isCompact) 11.sp else 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = textFont,
                style = EconomicNoPadTextStyle,
                maxLines = 1
            )
        }

        // End: "• Live" + Clock + "Source: Investing.com"
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(if (isCompact) 8.dp else 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val liveAnim = rememberInfiniteTransition(label = "econ_live_dot")
                    val livePulse = liveAnim.animateFloat(
                        initialValue = 0.42f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1400, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "econ_live_alpha"
                    )
                    Box(
                        modifier = Modifier
                            .size(5.5.dp)
                            .graphicsLayer {
                                val p = livePulse.value
                                alpha = p
                                val s = 0.88f + p * 0.24f
                                scaleX = s
                                scaleY = s
                            }
                            .clip(CircleShape)
                            .background(EmeraldUp)
                    )
                    Text(
                        text = strings.liveBadge,
                        color = EmeraldUp,
                        fontSize = if (isCompact) 11.sp else 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = textFont,
                        style = EconomicNoPadTextStyle,
                        maxLines = 1
                    )
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = timeString,
                        color = Color.White,
                        fontSize = if (isCompact) 25.sp else 33.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        style = EconomicNoPadTextStyle.copy(fontFeatureSettings = "tnum"),
                        maxLines = 1
                    )
                }
            }

            Text(
                text = strings.sourceLabel,
                color = Color.White.copy(alpha = 0.58f),
                fontSize = if (isCompact) 9.5.sp else 11.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = textFont,
                style = EconomicNoPadTextStyle,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EconomicGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    isHighlighted: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1E2D42).copy(alpha = 0.72f),
                        Color(0xFF142234).copy(alpha = 0.78f),
                        Color(0xFF0E1929).copy(alpha = 0.82f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(500f, 500f)
                )
            )
            .border(
                width = if (isHighlighted) 1.5.dp else 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isHighlighted) {
                        listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.85f),
                            Color.White.copy(alpha = 0.35f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.10f),
                            Color(0xFF38BDF8).copy(alpha = 0.18f)
                        )
                    }
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

@Composable
private fun EconomicIndexTopCard(
    quote: MarketQuoteItem,
    langCode: String,
    textFont: FontFamily,
    isSelectedForChart: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val changeColor = if (quote.isNeutral) NeutralGray else if (quote.isPositive) EmeraldUp else CoralDown
    val arrowChar = if (quote.isPositive) "↗" else "↘"

    TvFocusableCard(
        onClick = onClick,
        onFocused = onFocused,
        shape = RoundedCornerShape(14.dp),
        scaleUpRatio = 1.02f,
        useDefaultBorder = false,
        elevation = 0.dp,
        modifier = modifier
    ) { isFocused ->
        EconomicGlassCard(
            modifier = Modifier.fillMaxSize(),
            cornerRadius = 14.dp,
            isHighlighted = isFocused || isSelectedForChart
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                val isCompact = maxHeight < 82.dp
                val titleSize = if (isCompact) 11.5.sp else 13.5.sp
                val priceSize = if (isCompact) 19.sp else 26.sp
                val badgeSize = if (isCompact) 10.5.sp else 12.sp
                val sparkW = if (isCompact) 62.dp else 84.dp
                val sparkH = if (isCompact) 15.dp else 22.dp

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Row: Index Name + Top-End Change Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = quote.getLocalizedTitle(langCode),
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = titleSize,
                            fontWeight = FontWeight.Medium,
                            fontFamily = textFont,
                            style = EconomicNoPadTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        )
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "${quote.changeFormatted} $arrowChar",
                                color = changeColor,
                                fontSize = badgeSize,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Default,
                                style = EconomicNoPadTextStyle,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Middle Row: Price Value
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = quote.priceFormatted,
                            color = Color.White,
                            fontSize = priceSize,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Default,
                            style = EconomicNoPadTextStyle.copy(fontFeatureSettings = "tnum"),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Bottom Row: Change % + Mini Glowing Sparkline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "${quote.changeFormatted} $arrowChar",
                                color = changeColor,
                                fontSize = badgeSize,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Default,
                                style = EconomicNoPadTextStyle,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        MiniSparklineCanvas(
                            points = quote.sparklinePoints,
                            lineColor = changeColor,
                            modifier = Modifier
                                .width(sparkW)
                                .height(sparkH)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EconomicCommoditiesCard(
    title: String,
    items: List<MarketQuoteItem>,
    langCode: String,
    textFont: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EconomicGlassCard(
        modifier = modifier.clickable { onClick() },
        cornerRadius = 14.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp, vertical = 7.dp)
        ) {
            val isCompact = maxHeight < 130.dp
            val headerSize = if (isCompact) 11.5.sp else 14.sp
            val nameSize = if (isCompact) 10.5.sp else 13.sp
            val priceSize = if (isCompact) 10.5.sp else 13.sp
            val changeSize = if (isCompact) 9.5.sp else 12.sp
            val sparkW = if (isCompact) 34.dp else 46.dp
            val sparkH = if (isCompact) 11.dp else 15.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = headerSize,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = textFont,
                    style = EconomicNoPadTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                items.forEachIndexed { idx, item ->
                    if (idx > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.6.dp)
                                .background(Color.White.copy(alpha = 0.10f))
                        )
                    }
                    val changeColor = if (item.isNeutral) NeutralGray else if (item.isPositive) EmeraldUp else CoralDown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.getLocalizedTitle(langCode),
                            color = Color.White.copy(alpha = 0.90f),
                            fontSize = nameSize,
                            fontWeight = FontWeight.Medium,
                            fontFamily = textFont,
                            style = EconomicNoPadTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        )

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = item.priceFormatted,
                                    color = Color.White,
                                    fontSize = priceSize,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Default,
                                    style = EconomicNoPadTextStyle,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "(${item.changeFormatted})",
                                    color = changeColor,
                                    fontSize = changeSize,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Default,
                                    style = EconomicNoPadTextStyle,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        MiniSparklineCanvas(
                            points = item.sparklinePoints,
                            lineColor = changeColor,
                            modifier = Modifier
                                .width(sparkW)
                                .height(sparkH)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EconomicSmooth5DayCard(
    strings: EconomicLocalizedStrings,
    activeQuote: MarketQuoteItem?,
    langCode: String,
    textFont: FontFamily,
    modifier: Modifier = Modifier
) {
    val quote = activeQuote ?: return
    val pts = quote.sparklinePoints.ifEmpty {
        listOf(5910f, 5948f, 5942f, 5965f, 5989f, 5935f, 6012.45f)
    }

    EconomicGlassCard(
        modifier = modifier,
        cornerRadius = 14.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            val isCompact = maxHeight < 130.dp
            val titleSize = if (isCompact) 11.5.sp else 14.sp
            val pillSize = if (isCompact) 10.sp else 11.5.sp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Row: Title on Start + Non-Overlapping High / Low Summary Pill on End
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.smooth5DayTitle,
                        color = Color.White.copy(alpha = 0.94f),
                        fontSize = titleSize,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = textFont,
                        style = EconomicNoPadTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 6.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.10f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = strings.highPrefix,
                                color = Color(0xFFFBBF24),
                                fontSize = pillSize,
                                fontWeight = FontWeight.Bold,
                                fontFamily = textFont,
                                style = EconomicNoPadTextStyle,
                                maxLines = 1
                            )
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = quote.high5dFormatted,
                                    color = Color(0xFFFBBF24),
                                    fontSize = pillSize,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Default,
                                    style = EconomicNoPadTextStyle,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = strings.lowPrefix,
                                color = Color(0xFFBAE6FD),
                                fontSize = pillSize,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = textFont,
                                style = EconomicNoPadTextStyle,
                                maxLines = 1
                            )
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = quote.low5dFormatted,
                                    color = Color(0xFFBAE6FD),
                                    fontSize = pillSize,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Default,
                                    style = EconomicNoPadTextStyle,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // Smooth 5-Day Curve Canvas (Always LTR so time progresses left-to-right cleanly)
                val peakAnim = rememberInfiniteTransition(label = "econ_chart_peak")
                val peakPulse = peakAnim.animateFloat(
                    initialValue = 0.82f,
                    targetValue = 1.30f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "econ_peak_halo"
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 4.dp)
                    ) {
                        if (pts.size < 2 || size.width <= 16f || size.height <= 18f) return@Canvas
                        val haloMul = peakPulse.value
                        val rawMin = pts.minOrNull() ?: 0f
                        val rawMax = pts.maxOrNull() ?: 1f
                        val rawSpan = (rawMax - rawMin).coerceAtLeast(0.001f)
                        val padV = rawSpan * 0.12f
                        val minV = rawMin - padV
                        val maxV = rawMax + padV
                        val span = (maxV - minV).coerceAtLeast(0.001f)

                        val leftPad = 8.dp.toPx()
                        val rightPad = 8.dp.toPx()
                        val topPad = (size.height * 0.14f).coerceIn(4f, 12.dp.toPx())
                        val bottomClear = (size.height * 0.14f).coerceIn(4f, 12.dp.toPx())
                        val baseLineY = (size.height - 2.dp.toPx()).coerceAtLeast(topPad + 10f)
                        val usableW = (size.width - leftPad - rightPad).coerceAtLeast(10f)
                        val usableH = (baseLineY - topPad - bottomClear).coerceAtLeast(8f)
                        val stepX = usableW / (pts.size - 1).toFloat()

                        val coords = pts.mapIndexed { i, v ->
                            val x = leftPad + i * stepX
                            val normY = ((v - minV) / span).coerceIn(0f, 1f)
                            val y = topPad + usableH * (1f - normY)
                            Offset(x, y)
                        }

                        val curvePath = Path().apply {
                            moveTo(coords.first().x, coords.first().y)
                            for (i in 0 until coords.size - 1) {
                                val p0 = coords[i]
                                val p1 = coords[i + 1]
                                val cx1 = p0.x + (p1.x - p0.x) * 0.45f
                                val cx2 = p0.x + (p1.x - p0.x) * 0.55f
                                cubicTo(cx1, p0.y, cx2, p1.y, p1.x, p1.y)
                            }
                        }

                        val fillPath = Path().apply {
                            addPath(curvePath)
                            lineTo(coords.last().x, baseLineY)
                            lineTo(coords.first().x, baseLineY)
                            close()
                        }

                        drawPath(
                            path = fillPath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF38BDF8).copy(alpha = 0.16f),
                                    Color(0xFF7DD3FC).copy(alpha = 0.22f),
                                    Color(0xFFFBBF24).copy(alpha = 0.26f)
                                )
                            )
                        )

                        // Bottom axis hairline
                        drawLine(
                            color = Color.White.copy(alpha = 0.18f),
                            start = Offset(leftPad, baseLineY),
                            end = Offset(size.width - rightPad, baseLineY),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Find high & low points
                        var highIdx = 0
                        var lowIdx = 0
                        for (i in pts.indices) {
                            if (pts[i] >= pts[highIdx]) highIdx = i
                            if (pts[i] <= pts[lowIdx] && i > 0) lowIdx = i
                        }
                        val highPt = coords[highIdx]
                        val lowPt = coords[lowIdx]

                        // Vertical line from High point to bottom
                        drawLine(
                            color = Color(0xFFFBBF24).copy(alpha = 0.40f),
                            start = highPt,
                            end = Offset(highPt.x, baseLineY),
                            strokeWidth = 1.dp.toPx()
                        )

                        // Outer glow stroke
                        drawPath(
                            path = curvePath,
                            color = Color(0xFFBAE6FD).copy(alpha = 0.35f),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Crisp white curve stroke
                        drawPath(
                            path = curvePath,
                            color = Color.White,
                            style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Glowing dot at Low trough
                        drawCircle(
                            color = Color(0xFF7DD3FC).copy(alpha = 0.35f),
                            radius = 5.5.dp.toPx(),
                            center = lowPt
                        )
                        drawCircle(
                            color = Color(0xFFBAE6FD),
                            radius = 3.dp.toPx(),
                            center = lowPt
                        )

                        // Glowing breathing dot at High peak
                        drawCircle(
                            color = Color(0xFFFDE047).copy(alpha = 0.42f / haloMul),
                            radius = 6.5.dp.toPx() * haloMul,
                            center = highPt
                        )
                        drawCircle(
                            color = Color(0xFFFBBF24),
                            radius = 3.6.dp.toPx(),
                            center = highPt
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EconomicForexCard(
    title: String,
    items: List<MarketQuoteItem>,
    textFont: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EconomicGlassCard(
        modifier = modifier.clickable { onClick() },
        cornerRadius = 14.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 11.dp, vertical = 7.dp)
        ) {
            val isCompact = maxHeight < 130.dp
            val headerSize = if (isCompact) 11.5.sp else 14.sp
            val pairSize = if (isCompact) 10.5.sp else 13.sp
            val priceSize = if (isCompact) 10.5.sp else 13.sp
            val changeSize = if (isCompact) 9.5.sp else 12.sp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = headerSize,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = textFont,
                    style = EconomicNoPadTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                items.forEachIndexed { idx, item ->
                    if (idx > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.6.dp)
                                .background(Color.White.copy(alpha = 0.10f))
                        )
                    }
                    val changeColor = if (item.isNeutral) NeutralGray else if (item.isPositive) EmeraldUp else CoralDown
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.titleEn,
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = pairSize,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Default,
                                style = EconomicNoPadTextStyle,
                                maxLines = 1,
                                softWrap = false
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = item.priceFormatted,
                                    color = Color.White,
                                    fontSize = priceSize,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Default,
                                    style = EconomicNoPadTextStyle,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "(${item.changeFormatted})",
                                    color = changeColor,
                                    fontSize = changeSize,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Default,
                                    style = EconomicNoPadTextStyle,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EconomicCalendarCard(
    strings: EconomicLocalizedStrings,
    events: List<EconomicCalendarEvent>,
    langCode: String,
    textFont: FontFamily,
    modifier: Modifier = Modifier
) {
    EconomicGlassCard(
        modifier = modifier,
        cornerRadius = 14.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            val isCompact = maxHeight < 98.dp
            val headerSize = if (isCompact) 11.sp else 13.5.sp
            val timeSize = if (isCompact) 10.sp else 12.sp
            val flagSize = if (isCompact) 11.dp else 13.5.dp
            val titleSize = if (isCompact) 11.sp else 13.sp
            val detailSize = if (isCompact) 9.5.sp else 10.5.sp
            val dotSize = if (isCompact) 5.5.dp else 6.5.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.economicCalendarTitle,
                    color = Color.White.copy(alpha = 0.94f),
                    fontSize = headerSize,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = textFont,
                    style = EconomicNoPadTextStyle,
                    maxLines = 1
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = if (isCompact) 3.dp else 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    events.take(5).forEachIndexed { idx, ev ->
                        if (idx > 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight(0.80f)
                                    .background(Color.White.copy(alpha = 0.14f))
                            )
                        }

                        val activeDotColor = if (ev.impactLevel >= 3) Color(0xFFEF4444) else Color(0xFFF59E0B)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(horizontal = if (idx == 0) 3.dp else if (isCompact) 7.dp else 11.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Row: Time + Flag + Country (plus inline impact dots on compact screens)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = ev.timeLabel,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = timeSize,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = FontFamily.Default,
                                            style = EconomicNoPadTextStyle,
                                            maxLines = 1
                                        )
                                        CountryFlagCircle(
                                            countryCode = ev.countryCode,
                                            modifier = Modifier.size(flagSize)
                                        )
                                        Text(
                                            text = ev.countryCode,
                                            color = Color.White,
                                            fontSize = timeSize,
                                            fontWeight = FontWeight.SemiBold,
                                            fontFamily = FontFamily.Default,
                                            style = EconomicNoPadTextStyle,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (isCompact) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        for (dot in 1..3) {
                                            Box(
                                                modifier = Modifier
                                                    .size(dotSize)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (dot <= ev.impactLevel) activeDotColor
                                                        else Color.White.copy(alpha = 0.22f)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }

                            // Event Title
                            Text(
                                text = ev.getLocalizedTitle(langCode),
                                color = Color.White,
                                fontSize = titleSize,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = textFont,
                                style = EconomicNoPadTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Separate Impact Row on tall/TV screens
                            if (!isCompact) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = strings.impactLabel,
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 10.5.sp,
                                        fontFamily = textFont,
                                        style = EconomicNoPadTextStyle,
                                        maxLines = 1
                                    )
                                    for (dot in 1..3) {
                                        Box(
                                            modifier = Modifier
                                                .size(dotSize)
                                                .clip(CircleShape)
                                                .background(
                                                    if (dot <= ev.impactLevel) activeDotColor
                                                    else Color.White.copy(alpha = 0.22f)
                                                )
                                        )
                                    }
                                }
                            }

                            // Act / Fc / Prev
                            Text(
                                text = "${strings.actLabel} ${ev.actualValue} / ${strings.fcLabel} ${ev.forecastValue} / ${strings.prevLabel} ${ev.previousValue}",
                                color = Color.White.copy(alpha = 0.78f),
                                fontSize = detailSize,
                                fontFamily = textFont,
                                style = EconomicNoPadTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryFlagCircle(
    countryCode: String,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.clip(CircleShape)) {
        if (countryCode == "EU") {
            drawRect(color = Color(0xFF1D4ED8))
            drawCircle(
                color = Color(0xFFFACC15),
                radius = size.minDimension * 0.28f,
                style = Stroke(width = 1.5.dp.toPx())
            )
        } else {
            // US Stripes + Blue Canton
            drawRect(color = Color.White)
            val stripeH = size.height / 7f
            for (s in listOf(0, 2, 4, 6)) {
                drawRect(
                    color = Color(0xFFDC2626),
                    topLeft = Offset(0f, s * stripeH),
                    size = Size(size.width, stripeH)
                )
            }
            drawRect(
                color = Color(0xFF1E3A8A),
                topLeft = Offset.Zero,
                size = Size(size.width * 0.52f, size.height * 0.54f)
            )
        }
    }
}

@Composable
private fun MiniSparklineCanvas(
    points: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.size < 2 || size.width <= 4f || size.height <= 4f) return@Canvas
        val minV = points.minOrNull() ?: 0f
        val maxV = points.maxOrNull() ?: 1f
        val span = (maxV - minV).coerceAtLeast(0.001f)
        val stepX = size.width / (points.size - 1).toFloat()

        val coords = points.mapIndexed { idx, v ->
            val x = idx * stepX
            val normY = ((v - minV) / span).coerceIn(0f, 1f)
            val y = size.height * (0.85f - normY * 0.72f)
            Offset(x, y)
        }

        val linePath = Path().apply {
            moveTo(coords.first().x, coords.first().y)
            for (i in 0 until coords.size - 1) {
                val p0 = coords[i]
                val p1 = coords[i + 1]
                val cx1 = p0.x + (p1.x - p0.x) * 0.45f
                val cx2 = p0.x + (p1.x - p0.x) * 0.55f
                cubicTo(cx1, p0.y, cx2, p1.y, p1.x, p1.y)
            }
        }

        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(coords.last().x, size.height)
            lineTo(coords.first().x, size.height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f),
                    lineColor.copy(alpha = 0.02f)
                )
            )
        )

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun EconomicFinancialBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "econ_bg_ambient")
    val orbPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "econ_orb_phase"
    )
    val scanPhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "econ_scan_phase"
    )

    val navyColors = remember {
        listOf(
            Color(0xFF050B16),
            Color(0xFF0A182D),
            Color(0xFF0D233D),
            Color(0xFF071222)
        )
    }
    val cyanGlowColors = remember {
        listOf(
            Color(0xFF06B6D4).copy(alpha = 0.42f),
            Color(0xFF0284C7).copy(alpha = 0.20f),
            Color.Transparent
        )
    }
    val emeraldGlowColors = remember {
        listOf(
            Color(0xFF10B981).copy(alpha = 0.16f),
            Color(0xFF0EA5E9).copy(alpha = 0.06f),
            Color.Transparent
        )
    }
    val baseHeights = remember {
        floatArrayOf(
            0.52f, 0.48f, 0.44f, 0.49f, 0.42f, 0.38f, 0.43f, 0.35f,
            0.31f, 0.36f, 0.28f, 0.24f, 0.29f, 0.22f, 0.18f, 0.15f
        )
    }
    val wavePath = remember { Path() }

    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                val navyBrush = Brush.linearGradient(
                    colors = navyColors,
                    start = Offset.Zero,
                    end = Offset(w, h)
                )
                val waveStroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
                val haloRadiusPx = 5.5.dp.toPx()
                val dotRadiusPx = 2.dp.toPx()
                val wickStrokePx = 2.dp.toPx()
                val candleHalfWidthPx = 5.dp.toPx()
                val candleWidthPx = 10.dp.toPx()
                val candleCornerPx = 2.dp.toPx()
                val candleCorner = CornerRadius(candleCornerPx, candleCornerPx)
                val wave0Color = Color(0xFF38BDF8).copy(alpha = 0.09f)
                val wave1Color = Color(0xFF34D399).copy(alpha = 0.09f)
                val wave0DotColor = Color(0xFF7DD3FC).copy(alpha = 0.32f)
                val wave1DotColor = Color(0xFF6EE7B7).copy(alpha = 0.32f)
                val whiteCoreColor = Color.White.copy(alpha = 0.55f)

                onDrawBehind {
                    val orbAngle = orbPhase.value * 2f * PI.toFloat()
                    val scan = scanPhase.value

                    // 1. Deep Dark Navy Base Gradient
                    drawRect(brush = navyBrush)

                    // 2. Slowly Drifting Cyan-Aqua Top-Right Financial Glow
                    val glowCenter = Offset(
                        w * (0.70f + cos(orbAngle.toDouble()).toFloat() * 0.055f),
                        h * (0.14f + sin(orbAngle.toDouble()).toFloat() * 0.05f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = cyanGlowColors,
                            center = glowCenter,
                            radius = w * 0.54f
                        ),
                        radius = w * 0.54f,
                        center = glowCenter
                    )

                    // 3. Soft Counter-Orbiting Emerald Market Glow (Bottom-Left)
                    val emeraldCenter = Offset(
                        w * (0.18f + sin(orbAngle.toDouble()).toFloat() * 0.05f),
                        h * (0.82f + cos(orbAngle.toDouble()).toFloat() * 0.05f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = emeraldGlowColors,
                            center = emeraldCenter,
                            radius = w * 0.38f
                        ),
                        radius = w * 0.38f,
                        center = emeraldCenter
                    )

                    // 4. Gently Undulating Translucent Financial Data Ribbon Curves (reusing wavePath)
                    for (waveIdx in 0..1) {
                        wavePath.reset()
                        val baseY = h * (if (waveIdx == 0) 0.58f else 0.70f)
                        val amp = h * (if (waveIdx == 0) 0.045f else 0.032f)
                        val freq = if (waveIdx == 0) 2.2f else 3.1f
                        val phaseShift = orbAngle * (if (waveIdx == 0) 1f else -1.4f) + waveIdx * 1.7f
                        val steps = 36
                        for (s in 0..steps) {
                            val frac = s.toFloat() / steps
                            val x = w * frac
                            val y = baseY + sin((frac * freq * PI * 2 + phaseShift).toDouble()).toFloat() * amp
                            if (s == 0) wavePath.moveTo(x, y) else wavePath.lineTo(x, y)
                        }
                        drawPath(
                            path = wavePath,
                            color = if (waveIdx == 0) wave0Color else wave1Color,
                            style = waveStroke
                        )
                        // Soft traveling luminous pulse dot gliding along each ribbon
                        val dotFrac = (scan + waveIdx * 0.5f) % 1f
                        val dotX = w * dotFrac
                        val dotY = baseY + sin((dotFrac * freq * PI * 2 + phaseShift).toDouble()).toFloat() * amp
                        val dotCenter = Offset(dotX, dotY)
                        drawCircle(
                            color = if (waveIdx == 0) wave0DotColor else wave1DotColor,
                            radius = haloRadiusPx,
                            center = dotCenter
                        )
                        drawCircle(
                            color = whiteCoreColor,
                            radius = dotRadiusPx,
                            center = dotCenter
                        )
                    }

                    // 5. Breathing Translucent Candlestick Silhouettes with Smooth Market Scan Wave
                    val candleCount = 16
                    val startX = w * 0.45f
                    val stepX = (w * 0.52f) / candleCount
                    val wickH = h * 0.14f
                    for (i in 0 until candleCount) {
                        val frac = i.toFloat() / (candleCount - 1).toFloat()
                        val scanDist = abs(frac - scan)
                        val scanBoost = 1f - (scanDist / 0.18f).coerceIn(0f, 1f)
                        val breathe = sin((orbAngle * 2f + i * 0.55f).toDouble()).toFloat() * 0.015f

                        val cx = startX + i * stepX
                        val cy = h * (baseHeights[i % baseHeights.size] + breathe)
                        val bodyH = h * (0.068f + scanBoost * 0.018f)
                        val baseAlpha = 0.08f + scanBoost * 0.13f

                        drawLine(
                            color = Color(0xFF38BDF8).copy(alpha = baseAlpha + 0.02f),
                            start = Offset(cx, cy - wickH * 0.5f),
                            end = Offset(cx, cy + wickH * 0.5f),
                            strokeWidth = wickStrokePx
                        )
                        drawRoundRect(
                            color = Color(0xFF38BDF8).copy(alpha = baseAlpha),
                            topLeft = Offset(cx - candleHalfWidthPx, cy - bodyH * 0.5f),
                            size = Size(candleWidthPx, bodyH),
                            cornerRadius = candleCorner
                        )
                    }
                }
            }
    )
}

@Composable
private fun EconomicCustomizationDialog(
    strings: EconomicLocalizedStrings,
    dashboard: EconomicDashboardData,
    selectedIndices: List<String>,
    selectedForex: List<String>,
    selectedCommodities: List<String>,
    langCode: String,
    textFont: FontFamily,
    onSaveIndices: (List<String>) -> Unit,
    onSaveForex: (List<String>) -> Unit,
    onSaveCommodities: (List<String>) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0 = Indices, 1 = Forex, 2 = Commodities

    fun toggleSelection(current: List<String>, id: String, onSave: (List<String>) -> Unit) {
        val updated = if (current.contains(id)) {
            if (current.size > 1) current - id else current
        } else {
            if (current.size < 4) current + id else current.drop(1) + id
        }
        onSave(updated)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0A1426).copy(alpha = 0.97f),
            modifier = Modifier
                .width(680.dp)
                .height(470.dp)
                .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.45f), RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.customizeDialogTitle,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = textFont
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.customizeDialogSubtitle,
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 12.5.sp,
                            fontFamily = textFont
                        )
                    }

                    TvFocusableCard(
                        onClick = onDismiss,
                        shape = CircleShape,
                        scaleUpRatio = 1.08f,
                        useDefaultBorder = false,
                        elevation = 0.dp,
                        modifier = Modifier.size(34.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = strings.doneButton,
                                tint = if (isFocused) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Category Tabs: Stock Indices | Currencies (Forex) | Commodities & Crypto
                val tabs = listOf(
                    strings.tabIndices to selectedIndices.size,
                    strings.tabForex to selectedForex.size,
                    strings.tabCommodities to selectedCommodities.size
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tabs.forEachIndexed { idx, (tabTitle, count) ->
                        val isSelectedTab = activeTab == idx
                        TvFocusableCard(
                            onClick = { activeTab = idx },
                            onFocused = { activeTab = idx },
                            shape = RoundedCornerShape(12.dp),
                            scaleUpRatio = 1.03f,
                            useDefaultBorder = false,
                            elevation = 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        when {
                                            isFocused -> TvAccentGold
                                            isSelectedTab -> Color(0xFF38BDF8).copy(alpha = 0.25f)
                                            else -> Color.White.copy(alpha = 0.08f)
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelectedTab) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$tabTitle ($count/4)",
                                    color = if (isFocused) Color.Black else Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = textFont,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selectable Items List for Active Tab
                val currentItems: List<Triple<String, String, String>> = remember(
                    activeTab,
                    dashboard,
                    langCode
                ) {
                    when (activeTab) {
                        0 -> dashboard.indicesMap.values.map {
                            Triple(it.id, it.getLocalizedTitle(langCode), "${it.priceFormatted} (${it.changeFormatted})")
                        }
                        1 -> dashboard.forexMap.values.map {
                            Triple(
                                it.id,
                                EconomicRepository.getForexCurrencyDescription(it.id, langCode),
                                "${it.priceFormatted} (${it.changeFormatted})"
                            )
                        }
                        else -> dashboard.commoditiesMap.values.map {
                            Triple(it.id, it.getLocalizedTitle(langCode), "${it.priceFormatted} (${it.changeFormatted})")
                        }
                    }
                }

                val activeSelection = when (activeTab) {
                    0 -> selectedIndices
                    1 -> selectedForex
                    else -> selectedCommodities
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(currentItems, key = { it.first }) { (id, label, priceInfo) ->
                        val isChecked = activeSelection.contains(id)
                        TvFocusableCard(
                            onClick = {
                                when (activeTab) {
                                    0 -> toggleSelection(selectedIndices, id, onSaveIndices)
                                    1 -> toggleSelection(selectedForex, id, onSaveForex)
                                    else -> toggleSelection(selectedCommodities, id, onSaveCommodities)
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            scaleUpRatio = 1.01f,
                            useDefaultBorder = false,
                            elevation = 0.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                        ) { isFocused ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        when {
                                            isFocused -> TvAccentGold
                                            isChecked -> Color(0xFF38BDF8).copy(alpha = 0.18f)
                                            else -> Color.White.copy(alpha = 0.06f)
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isChecked) Color(0xFF38BDF8).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.10f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isChecked) {
                                                    if (isFocused) Color.Black else Color(0xFF38BDF8)
                                                } else {
                                                    Color.White.copy(alpha = 0.15f)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isChecked) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = if (isFocused) TvAccentGold else Color.Black,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = label,
                                        color = if (isFocused) Color.Black else Color.White,
                                        fontSize = 13.5.sp,
                                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = textFont,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                    Text(
                                        text = priceInfo,
                                        color = if (isFocused) Color.Black.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.78f),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Default
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Footer Buttons: Reset Default | Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvFocusableCard(
                        onClick = onResetDefaults,
                        shape = RoundedCornerShape(12.dp),
                        scaleUpRatio = 1.04f,
                        useDefaultBorder = false,
                        elevation = 0.dp,
                        modifier = Modifier.height(38.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(
                                    if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.10f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.resetDefaultButton,
                                color = if (isFocused) Color.Black else Color.White.copy(alpha = 0.88f),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = textFont
                            )
                        }
                    }

                    TvFocusableCard(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        scaleUpRatio = 1.04f,
                        useDefaultBorder = false,
                        elevation = 0.dp,
                        modifier = Modifier.height(38.dp)
                    ) { isFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(
                                    if (isFocused) TvAccentGold else Color(0xFF38BDF8),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 22.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.doneButton,
                                color = Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = textFont
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 960, heightDp = 540, showBackground = true)
@Composable
private fun EconomicScreenPreview() {
    EconomicScreen(
        is24hFormat = true,
        appLanguage = "en",
        onBackToHome = {}
    )
}
