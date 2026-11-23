package com.example.ui.football

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.onKeyEvent
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.football.FootballCatalogs
import com.example.data.football.FootballClubCatalogItem
import com.example.data.football.FootballHeadlineItem
import com.example.data.football.FootballLeagueCatalogItem
import com.example.data.football.FootballLocalizedStrings
import com.example.data.football.FootballMatchItem
import com.example.data.football.FootballRepository
import com.example.data.football.LeagueStandingRow
import com.example.data.football.MatchProbabilities
import com.example.data.football.MatchStatusType
import com.example.data.football.TopScorerItem
import androidx.compose.ui.platform.testTag
import com.example.data.local.LauncherPreferences
import com.example.ui.common.isRemoteMenuKey
import com.example.ui.theme.CairoFontFamily
import com.example.util.LocaleHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

private val NoPadTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

@Composable
fun FootballScreen(
    is24hFormat: Boolean,
    appLanguage: String,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { LauncherPreferences(context) }
    val coroutineScope = rememberCoroutineScope()

    val initialLang = remember { prefs.getCachedLanguage() }
    val savedLang by prefs.languageFlow.collectAsState(initial = initialLang)
    val resolvedLang = remember(savedLang, appLanguage) {
        if (savedLang.isNotBlank() && savedLang != "system") savedLang else appLanguage
    }
    val effectiveLang = remember(resolvedLang) { LocaleHelper.getEffectiveLanguage(resolvedLang) }
    val isRtl = remember(resolvedLang) { LocaleHelper.isRtlLanguage(resolvedLang) }
    val isArabicFont = effectiveLang in setOf("ar", "fa", "ur")
    val strings = remember(effectiveLang) { FootballRepository.getLocalizedStrings(effectiveLang) }

    val selectedLeagueIds by prefs.footballLeaguesFlow.collectAsState(
        initial = listOf("premier_league", "la_liga", "serie_a", "champions_league")
    )
    val favoriteClubIds by prefs.footballClubsFlow.collectAsState(initial = emptySet())
    val footballState by FootballRepository.footballState.collectAsState()

    var activeLeagueId by remember { mutableStateOf("premier_league") }
    var selectedMatchOverrideId by remember { mutableStateOf<String?>(null) }
    var showCustomizeDialog by remember { mutableStateOf(false) }

    val firstLeagueTabFocusRequester = remember { FocusRequester() }

    LaunchedEffect(selectedLeagueIds) {
        if (selectedLeagueIds.isNotEmpty() && activeLeagueId !in selectedLeagueIds) {
            activeLeagueId = selectedLeagueIds.first()
            selectedMatchOverrideId = null
        }
    }

    LaunchedEffect(Unit) {
        delay(80)
        try {
            firstLeagueTabFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    LaunchedEffect(effectiveLang, activeLeagueId, selectedLeagueIds) {
        FootballRepository.ensureInitialized(
            context = context,
            appLanguage = effectiveLang,
            activeLeagueId = activeLeagueId,
            selectedLeagueIds = selectedLeagueIds,
            forceRefresh = false
        )
    }

    // Periodic refresh every 3 minutes while screen is open
    LaunchedEffect(effectiveLang, activeLeagueId, selectedLeagueIds) {
        while (true) {
            delay(3 * 60 * 1000L)
            FootballRepository.refreshFootballData(
                appLanguage = effectiveLang,
                priorityLeagueId = activeLeagueId,
                selectedLeagueIds = selectedLeagueIds,
                force = true
            )
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
        if (showCustomizeDialog) {
            showCustomizeDialog = false
        } else {
            onBackToHome()
        }
    }

    val displayedLeagues = remember(selectedLeagueIds) {
        val list = selectedLeagueIds.mapNotNull { id ->
            FootballRepository.ALL_LEAGUES.find { it.id == id }
        }
        if (list.isNotEmpty()) list.take(4) else FootballRepository.ALL_LEAGUES.take(4)
    }

    val activeLeagueData = remember(footballState, activeLeagueId) {
        footballState.leaguesData[activeLeagueId]
            ?: footballState.leaguesData["premier_league"]
            ?: footballState.leaguesData.values.firstOrNull()
            ?: FootballRepository.buildBaselineState().leaguesData.values.first()
    }

    val activeFeaturedMatch = remember(activeLeagueData, selectedMatchOverrideId) {
        activeLeagueData.fixtures.find { it.id == selectedMatchOverrideId }
            ?: activeLeagueData.featuredMatch
    }

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onKeyEvent { event ->
                    val native = event.nativeKeyEvent
                    if (isRemoteMenuKey(native.keyCode) || native.keyCode == android.view.KeyEvent.KEYCODE_SETTINGS) {
                        if (native.action == android.view.KeyEvent.ACTION_DOWN && native.repeatCount == 0) {
                            showCustomizeDialog = !showCustomizeDialog
                        }
                        true
                    } else {
                        false
                    }
                }
        ) {
            // 1. Stadium Floodlights & Lush Green Pitch Background
            FootballStadiumBackground()

            // 2. Main Foreground Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                // Top Header Bar (Fixed 44.dp height so it NEVER wraps vertically or pushes cards off-screen)
                FootballHeaderBar(
                    strings = strings,
                    isArabicFont = isArabicFont,
                    appLanguage = effectiveLang,
                    displayedLeagues = displayedLeagues,
                    activeLeagueId = activeLeagueId,
                    currentTimeStr = currentTimeStr,
                    firstTabFocusRequester = firstLeagueTabFocusRequester,
                    onSelectLeague = { leagueId ->
                        activeLeagueId = leagueId
                        selectedMatchOverrideId = null
                    },
                    onOpenCustomize = { showCustomizeDialog = true },
                    onRefresh = {
                        coroutineScope.launch {
                            FootballRepository.refreshFootballData(
                                appLanguage = effectiveLang,
                                priorityLeagueId = activeLeagueId,
                                selectedLeagueIds = selectedLeagueIds,
                                force = true
                            )
                        }
                    },
                    onBackToHome = onBackToHome
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Middle 3-Column Section (Left: Live Match + Top Scorers | Center: Today's Fixtures | Right: League Table)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.73f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Column: Featured Live Match (with Win Probabilities) + Top Scorers
                    Column(
                        modifier = Modifier
                            .weight(0.31f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val activeProbabilities = remember(activeFeaturedMatch, activeLeagueData.standings) {
                            activeFeaturedMatch.probabilities
                                ?: FootballCatalogs.computeMatchProbabilities(
                                    activeFeaturedMatch,
                                    activeLeagueData.standings
                                )
                        }
                        FeaturedMatchCard(
                            match = activeFeaturedMatch,
                            probabilities = activeProbabilities,
                            strings = strings,
                            appLanguage = effectiveLang,
                            isArabicFont = isArabicFont,
                            onClick = { showCustomizeDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.64f)
                        )

                        TopScorersCard(
                            scorers = activeLeagueData.topScorers,
                            strings = strings,
                            appLanguage = effectiveLang,
                            isArabicFont = isArabicFont,
                            onClick = { showCustomizeDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.36f)
                        )
                    }

                    // Center Column: Today's Fixtures (Scrollable up to 10 matches, favorite clubs starred)
                    val sortedFixtures = remember(activeLeagueData.fixtures, favoriteClubIds) {
                        if (favoriteClubIds.isEmpty()) {
                            activeLeagueData.fixtures
                        } else {
                            activeLeagueData.fixtures.sortedBy { match ->
                                val isFav = FootballCatalogs.isMatchFollowed(favoriteClubIds, match)
                                when {
                                    match.statusType == MatchStatusType.LIVE && isFav -> 0
                                    match.statusType == MatchStatusType.LIVE -> 1
                                    isFav -> 2
                                    else -> 3
                                }
                            }
                        }
                    }
                    TodaysFixturesCard(
                        fixtures = sortedFixtures,
                        selectedMatchId = activeFeaturedMatch.id,
                        favoriteClubIds = favoriteClubIds,
                        strings = strings,
                        appLanguage = effectiveLang,
                        isArabicFont = isArabicFont,
                        onSelectMatch = { match ->
                            selectedMatchOverrideId = match.id
                        },
                        modifier = Modifier
                            .weight(0.33f)
                            .fillMaxHeight()
                    )

                    // Right Column: League Table (Scrollable up to 20 clubs, favorite clubs highlighted)
                    LeagueTableCard(
                        standings = activeLeagueData.standings,
                        favoriteClubIds = favoriteClubIds,
                        strings = strings,
                        appLanguage = effectiveLang,
                        isArabicFont = isArabicFont,
                        onClick = { showCustomizeDialog = true },
                        modifier = Modifier
                            .weight(0.36f)
                            .fillMaxHeight()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Row: Latest Headlines
                LatestHeadlinesCard(
                    headlines = activeLeagueData.headlines,
                    strings = strings,
                    appLanguage = effectiveLang,
                    isArabicFont = isArabicFont,
                    onClickHeadline = {
                        coroutineScope.launch {
                            FootballRepository.refreshFootballData(
                                appLanguage = effectiveLang,
                                priorityLeagueId = activeLeagueId,
                                selectedLeagueIds = selectedLeagueIds,
                                force = true
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.27f)
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Footer Bar
                FootballFooterBar(
                    lastUpdatedTime = footballState.lastUpdatedTime,
                    isLoading = footballState.isLoading,
                    strings = strings,
                    isArabicFont = isArabicFont
                )
            }

            if (showCustomizeDialog) {
                FootballLeagueCustomizationDialog(
                    strings = strings,
                    appLanguage = effectiveLang,
                    isArabicFont = isArabicFont,
                    selectedLeagueIds = selectedLeagueIds,
                    favoriteClubIds = favoriteClubIds,
                    onUpdateLeagues = { updated ->
                        coroutineScope.launch {
                            prefs.setFootballLeagues(updated)
                        }
                    },
                    onUpdateFavoriteClubs = { updatedClubs ->
                        coroutineScope.launch {
                            prefs.setFootballClubs(updatedClubs)
                        }
                    },
                    onResetDefaults = {
                        coroutineScope.launch {
                            prefs.setFootballLeagues(
                                listOf("premier_league", "la_liga", "serie_a", "champions_league")
                            )
                            prefs.setFootballClubs(emptySet())
                        }
                    },
                    onDismiss = { showCustomizeDialog = false }
                )
            }
        }
    }
}

@Composable
private fun FootballStadiumBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "stadium_ambient")
    val beamSway = infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stadium_beam_sway"
    )
    val glowPulse = infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stadium_glow_pulse"
    )
    val motePhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "stadium_motes"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val sway = beamSway.value
        val pulse = glowPulse.value
        val mote = motePhase.value

        // 1. Stadium Night-to-Emerald Pitch Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A141D),
                    Color(0xFF0F2622),
                    Color(0xFF143E27),
                    Color(0xFF184D2C),
                    Color(0xFF0D2E1A)
                )
            )
        )

        // 2. Gracefully Sweeping Volumetric Stadium Floodlight Beams
        val leftTower = Offset(w * 0.08f, h * 0.16f)
        val rightTower = Offset(w * 0.92f, h * 0.16f)

        val leftBeam = Path().apply {
            moveTo(leftTower.x, leftTower.y)
            lineTo(w * (0.28f + sway * 0.12f), h)
            lineTo(w * (0.56f + sway * 0.12f), h)
            close()
        }
        drawPath(
            path = leftBeam,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFD1FAE5).copy(alpha = 0.095f * pulse),
                    Color(0xFF34D399).copy(alpha = 0.035f),
                    Color.Transparent
                )
            )
        )

        val rightBeam = Path().apply {
            moveTo(rightTower.x, rightTower.y)
            lineTo(w * (0.44f - sway * 0.12f), h)
            lineTo(w * (0.72f - sway * 0.12f), h)
            close()
        }
        drawPath(
            path = rightBeam,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFD1FAE5).copy(alpha = 0.090f * pulse),
                    Color(0xFF34D399).copy(alpha = 0.032f),
                    Color.Transparent
                )
            )
        )

        // 3. Breathing Left & Right Floodlight Halos
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE2F8F0).copy(alpha = 0.28f * pulse),
                    Color(0xFF34D399).copy(alpha = 0.12f * pulse),
                    Color.Transparent
                ),
                center = leftTower,
                radius = w * 0.36f * pulse
            ),
            radius = w * 0.36f * pulse,
            center = leftTower
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFE2F8F0).copy(alpha = 0.26f * pulse),
                    Color(0xFF34D399).copy(alpha = 0.11f * pulse),
                    Color.Transparent
                ),
                center = rightTower,
                radius = w * 0.36f * pulse
            ),
            radius = w * 0.36f * pulse,
            center = rightTower
        )

        // Stadium bulb clusters with gentle shimmer
        for (i in 0..3) {
            val bulbAlpha = 0.14f + 0.07f * ((sin((mote * 4f * PI + i * 1.1).toDouble()).toFloat() + 1f) * 0.5f)
            drawCircle(
                color = Color.White.copy(alpha = bulbAlpha),
                radius = 7f,
                center = Offset(w * 0.04f + i * 16f, h * 0.17f + i * 3f)
            )
            drawCircle(
                color = Color.White.copy(alpha = bulbAlpha),
                radius = 7f,
                center = Offset(w * 0.96f - i * 16f, h * 0.17f + i * 3f)
            )
        }

        // 4. Lower Pitch Grass Stripes + Soft Passing Stadium Light Sheen
        val pitchTop = h * 0.56f
        val stripeCount = 8
        val stripeHeight = (h - pitchTop) / stripeCount
        for (i in 0 until stripeCount) {
            if (i % 2 == 0) {
                drawRect(
                    color = Color(0xFF22C55E).copy(alpha = 0.045f),
                    topLeft = Offset(0f, pitchTop + i * stripeHeight),
                    size = Size(w, stripeHeight)
                )
            }
        }

        drawLine(
            color = Color.White.copy(alpha = 0.07f),
            start = Offset(0f, h * 0.72f),
            end = Offset(w, h * 0.72f),
            strokeWidth = 1.5f
        )

        // 5. Luminous Stadium Atmosphere Particles Drifting Gently Upward
        for (i in 0 until 18) {
            val seedX = ((i * 0.37f + 0.05f) % 0.90f) + 0.05f
            val seedY = ((i * 0.59f) % 1f)
            val py = (1.06f - ((seedY + mote * (0.65f + (i % 3) * 0.2f)) % 1.12f))
            val driftX = sin((mote * 2f * PI + i * 0.95).toDouble()).toFloat() * 0.018f
            val fade = (sin((py.coerceIn(0f, 1f) * PI).toDouble()).toFloat()).coerceIn(0f, 1f)
            val color = if (i % 4 == 0) Color(0xFFFDE68A) else Color(0xFFA7F3D0)
            drawCircle(
                color = color.copy(alpha = 0.24f * fade),
                radius = (1.5f + (i % 3) * 0.7f).dp.toPx(),
                center = Offset(w * (seedX + driftX), h * py)
            )
        }
    }
}

@Composable
private fun FootballHeaderBar(
    strings: FootballLocalizedStrings,
    isArabicFont: Boolean,
    appLanguage: String,
    displayedLeagues: List<FootballLeagueCatalogItem>,
    activeLeagueId: String,
    currentTimeStr: String,
    firstTabFocusRequester: FocusRequester,
    onSelectLeague: (String) -> Unit,
    onOpenCustomize: () -> Unit,
    onRefresh: () -> Unit,
    onBackToHome: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: Back button + "Football Center" + Soccer Ball Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var backFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (backFocused) Color(0xFF22C55E).copy(alpha = 0.28f)
                        else Color.White.copy(alpha = 0.08f)
                    )
                    .border(
                        width = if (backFocused) 1.5.dp else 1.dp,
                        color = if (backFocused) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.16f),
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
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = strings.footballCenterTitle,
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = if (isArabicFont) CairoFontFamily else null,
                style = NoPadTextStyle,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            SoccerBallHeaderBadge(modifier = Modifier.size(23.dp))
        }

        // Center: League Selector Pill Bar + Customize Button + Refresh Button
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E293B).copy(alpha = 0.72f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                displayedLeagues.forEachIndexed { index, league ->
                    val isSelected = league.id == activeLeagueId
                    var isFocused by remember { mutableStateOf(false) }
                    val bgColor by animateColorAsState(
                        targetValue = when {
                            isFocused -> Color(0xFF22C55E).copy(alpha = 0.35f)
                            isSelected -> Color.White.copy(alpha = 0.22f)
                            else -> Color.Transparent
                        },
                        label = "league_tab_bg"
                    )

                    Box(
                        modifier = Modifier
                            .then(
                                if (index == 0) Modifier.focusRequester(firstTabFocusRequester)
                                else Modifier
                            )
                            .clip(RoundedCornerShape(7.dp))
                            .background(bgColor)
                            .then(
                                if (isFocused) Modifier.border(1.5.dp, Color(0xFF4ADE80), RoundedCornerShape(7.dp))
                                else Modifier
                            )
                            .onFocusChanged { state ->
                                isFocused = state.isFocused
                                if (state.isFocused && activeLeagueId != league.id) {
                                    onSelectLeague(league.id)
                                }
                            }
                            .focusable()
                            .clickable { onSelectLeague(league.id) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = league.getLocalizedTitle(appLanguage),
                            color = if (isSelected || isFocused) Color.White else Color.White.copy(alpha = 0.72f),
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.SemiBold,
                            fontFamily = if (isArabicFont) CairoFontFamily else null,
                            style = NoPadTextStyle,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Compact Customize Leagues & Clubs Button
            var customizeFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .testTag("football_customize_btn")
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (customizeFocused) Color(0xFF22C55E).copy(alpha = 0.30f)
                        else Color(0xFF1E293B).copy(alpha = 0.72f)
                    )
                    .border(
                        width = if (customizeFocused) 1.5.dp else 1.dp,
                        color = if (customizeFocused) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onFocusChanged { customizeFocused = it.isFocused }
                    .focusable()
                    .clickable { onOpenCustomize() }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = strings.customizeLeaguesBtn,
                        tint = if (customizeFocused) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = strings.customizeLeaguesBtn,
                        color = Color.White,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                        style = NoPadTextStyle,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Refresh Button
            var refreshFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (refreshFocused) Color(0xFF22C55E).copy(alpha = 0.30f)
                        else Color(0xFF1E293B).copy(alpha = 0.72f)
                    )
                    .border(
                        width = if (refreshFocused) 1.5.dp else 1.dp,
                        color = if (refreshFocused) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.18f),
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
                    tint = if (refreshFocused) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Right: • Live + Large Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                )
                Text(
                    text = "Live",
                    color = Color(0xFF4ADE80),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = NoPadTextStyle,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Text(
                text = currentTimeStr,
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
                style = NoPadTextStyle,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun SoccerBallHeaderBadge(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8), Color(0xFF1D4ED8)),
                center = Offset(center.x - r * 0.25f, center.y - r * 0.25f),
                radius = r * 1.3f
            ),
            radius = r,
            center = center
        )

        val pentPath = Path()
        for (i in 0 until 5) {
            val angle = Math.toRadians((i * 72 - 90).toDouble())
            val px = center.x + (r * 0.36f * cos(angle)).toFloat()
            val py = center.y + (r * 0.36f * sin(angle)).toFloat()
            if (i == 0) pentPath.moveTo(px, py) else pentPath.lineTo(px, py)
        }
        pentPath.close()
        drawPath(pentPath, color = Color(0xFF0F172A))

        for (i in 0 until 5) {
            val angle = Math.toRadians((i * 72 - 90).toDouble())
            val sx = center.x + (r * 0.35f * cos(angle)).toFloat()
            val sy = center.y + (r * 0.35f * sin(angle)).toFloat()
            val ex = center.x + (r * 0.85f * cos(angle)).toFloat()
            val ey = center.y + (r * 0.85f * sin(angle)).toFloat()
            drawLine(
                color = Color(0xFF0F172A),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = 2f
            )
            drawCircle(
                color = Color(0xFF0F172A),
                radius = r * 0.16f,
                center = Offset(ex, ey)
            )
        }

        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = r,
            center = center,
            style = Stroke(width = 1.4f)
        )
    }
}

@Composable
private fun FeaturedMatchCard(
    match: FootballMatchItem,
    probabilities: MatchProbabilities,
    strings: FootballLocalizedStrings,
    appLanguage: String,
    isArabicFont: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF16382C).copy(alpha = 0.88f),
                        Color(0xFF102422).copy(alpha = 0.90f)
                    )
                )
            )
            .border(
                width = if (isFocused) 2.dp else 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4ADE80),
                        Color(0xFF22C55E).copy(alpha = if (isFocused) 0.9f else 0.55f)
                    )
                ),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: LIVE label + Red • LIVE Pill
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when (match.statusType) {
                        MatchStatusType.LIVE -> strings.liveBadge
                        MatchStatusType.FINISHED -> strings.ftBadge
                        MatchStatusType.UPCOMING -> match.statusBadgeText
                    },
                    color = Color.White.copy(alpha = 0.88f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = if (isArabicFont) CairoFontFamily else null,
                    style = NoPadTextStyle,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                val isLive = match.statusType == MatchStatusType.LIVE
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isLive) Color(0xFF7F1D1D).copy(alpha = 0.85f)
                            else Color.White.copy(alpha = 0.14f)
                        )
                        .padding(horizontal = 9.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    val liveDotPulse = if (isLive) {
                        rememberInfiniteTransition(label = "football_live_dot").animateFloat(
                            initialValue = 0.35f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1100, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "football_live_alpha"
                        )
                    } else null
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .graphicsLayer {
                                if (liveDotPulse != null) {
                                    val p = liveDotPulse.value
                                    alpha = p
                                    val s = 0.85f + p * 0.30f
                                    scaleX = s
                                    scaleY = s
                                }
                            }
                            .clip(CircleShape)
                            .background(if (isLive) Color(0xFFEF4444) else Color(0xFF94A3B8))
                    )
                    Text(
                        text = if (isLive) strings.liveBadge else match.statusBadgeText,
                        color = if (isLive) Color(0xFFFCA5A5) else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                        style = NoPadTextStyle,
                        maxLines = 1
                    )
                }
            }

            // Center Row: Home Crest & Name | Score & Minute | Away Crest & Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    ClubCrestBadge(
                        code = match.homeCode,
                        primaryColor = Color(match.homePrimaryColor),
                        secondaryColor = Color(match.homeSecondaryColor),
                        crestStyle = match.homeCrestStyle,
                        logoUrl = match.homeLogoUrl,
                        badgeSize = 44.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.getLocalizedHomeName(appLanguage),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                        style = NoPadTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    val scoreText = if (match.homeScore != null && match.awayScore != null) {
                        "${match.homeScore} - ${match.awayScore}"
                    } else {
                        "v"
                    }
                    Text(
                        text = scoreText,
                        color = Color.White,
                        fontSize = 31.sp,
                        fontWeight = FontWeight.ExtraBold,
                        style = NoPadTextStyle,
                        maxLines = 1
                    )
                    Text(
                        text = match.statusBadgeText,
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        style = NoPadTextStyle,
                        maxLines = 1
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    ClubCrestBadge(
                        code = match.awayCode,
                        primaryColor = Color(match.awayPrimaryColor),
                        secondaryColor = Color(match.awaySecondaryColor),
                        crestStyle = match.awayCrestStyle,
                        logoUrl = match.awayLogoUrl,
                        badgeSize = 44.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.getLocalizedAwayName(appLanguage),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                        style = NoPadTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Glowing Match Progress Timeline with red indicator dot
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            ) {
                val trackY = size.height / 2f
                val progressX = (size.width * match.progressRatio.coerceIn(0.05f, 1f))

                drawLine(
                    color = Color.White.copy(alpha = 0.16f),
                    start = Offset(0f, trackY),
                    end = Offset(size.width, trackY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                drawLine(
                    color = Color(0xFF4ADE80),
                    start = Offset(0f, trackY),
                    end = Offset(progressX, trackY),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )

                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.35f),
                    radius = 6f,
                    center = Offset(progressX, trackY)
                )
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 3.5f,
                    center = Offset(progressX, trackY)
                )
            }

            // Compact Win Probability Bar (Home % • Draw % • Away %)
            WinProbabilityStrip(
                probabilities = probabilities,
                strings = strings,
                isArabicFont = isArabicFont
            )

            // Goal Scorers / Events Section
            val homeGoals = match.goalEvents.filter { it.isHomeTeam }
            val awayGoals = match.goalEvents.filter { !it.isHomeTeam }
            if (homeGoals.isNotEmpty() || awayGoals.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val leftGoal = homeGoals.getOrNull(0)
                        val rightGoal = homeGoals.getOrNull(1) ?: awayGoals.getOrNull(0)

                        if (leftGoal != null) {
                            GoalEventLabel(
                                player = leftGoal.getLocalizedPlayer(appLanguage),
                                minute = leftGoal.minuteLabel,
                                isArabicFont = isArabicFont,
                                iconLeading = false
                            )
                        } else {
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        if (rightGoal != null && rightGoal !== leftGoal) {
                            GoalEventLabel(
                                player = rightGoal.getLocalizedPlayer(appLanguage),
                                minute = rightGoal.minuteLabel,
                                isArabicFont = isArabicFont,
                                iconLeading = true
                            )
                        }
                    }

                    val secondRowGoal = if (homeGoals.size >= 2) awayGoals.getOrNull(0) else awayGoals.getOrNull(1)
                    if (secondRowGoal != null) {
                        GoalEventLabel(
                            player = secondRowGoal.getLocalizedPlayer(appLanguage),
                            minute = secondRowGoal.minuteLabel,
                            isArabicFont = isArabicFont,
                            iconLeading = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WinProbabilityStrip(
    probabilities: MatchProbabilities,
    strings: FootballLocalizedStrings,
    isArabicFont: Boolean
) {
    val homeW = probabilities.homeWinPct.coerceIn(0, 100)
    val drawW = probabilities.drawPct.coerceIn(0, 100)
    val awayW = probabilities.awayWinPct.coerceIn(0, 100)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$homeW%",
                color = Color(0xFF4ADE80),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                style = NoPadTextStyle,
                maxLines = 1
            )
            Text(
                text = "${strings.winProbabilityTitle} • ${strings.drawLabel} $drawW%",
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = if (isArabicFont) CairoFontFamily else null,
                style = NoPadTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$awayW%",
                color = Color(0xFF38BDF8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                style = NoPadTextStyle,
                maxLines = 1
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.12f)),
            horizontalArrangement = Arrangement.spacedBy(1.5.dp)
        ) {
            if (homeW > 0) {
                Box(
                    modifier = Modifier
                        .weight(homeW.toFloat())
                        .fillMaxHeight()
                        .background(Color(0xFF4ADE80))
                )
            }
            if (drawW > 0) {
                Box(
                    modifier = Modifier
                        .weight(drawW.toFloat())
                        .fillMaxHeight()
                        .background(Color(0xFF94A3B8))
                )
            }
            if (awayW > 0) {
                Box(
                    modifier = Modifier
                        .weight(awayW.toFloat())
                        .fillMaxHeight()
                        .background(Color(0xFF38BDF8))
                )
            }
        }
    }
}

@Composable
private fun GoalEventLabel(
    player: String,
    minute: String,
    isArabicFont: Boolean,
    iconLeading: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (iconLeading) {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = "$player $minute",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.5.sp,
                fontFamily = if (isArabicFont) CairoFontFamily else null,
                style = NoPadTextStyle,
                maxLines = 1
            )
        } else {
            Text(
                text = player,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.5.sp,
                fontFamily = if (isArabicFont) CairoFontFamily else null,
                style = NoPadTextStyle,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = minute,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 10.5.sp,
                style = NoPadTextStyle,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun TopScorersCard(
    scorers: List<TopScorerItem>,
    strings: FootballLocalizedStrings,
    appLanguage: String,
    isArabicFont: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF142824).copy(alpha = 0.84f))
            .border(
                width = if (isFocused) 1.8.dp else 1.dp,
                color = if (isFocused) Color(0xFF4ADE80) else Color.White.copy(alpha = 0.16f),
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = strings.topScorersTitle,
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isArabicFont) CairoFontFamily else null,
            style = NoPadTextStyle,
            maxLines = 1
        )

        scorers.take(4).forEach { scorer ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(0.56f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${scorer.rank}. ",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.5.sp,
                        style = NoPadTextStyle,
                        maxLines = 1
                    )
                    Text(
                        text = scorer.getLocalizedPlayerName(appLanguage),
                        color = Color.White,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                        style = NoPadTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " (${scorer.teamCode})",
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 10.5.sp,
                        style = NoPadTextStyle,
                        maxLines = 1
                    )
                }

                val ratio = (scorer.goals.toFloat() / scorer.maxGoals.coerceAtLeast(1)).coerceIn(0.2f, 1f)
                Box(
                    modifier = Modifier
                        .weight(0.32f)
                        .height(4.5.dp)
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(ratio)
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF34D399), Color(0xFF4ADE80))
                                )
                            )
                    )
                }

                Text(
                    text = scorer.goals.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    style = NoPadTextStyle,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.width(20.dp)
                )
            }
        }
    }
}

@Composable
private fun TodaysFixturesCard(
    fixtures: List<FootballMatchItem>,
    selectedMatchId: String,
    favoriteClubIds: Set<String>,
    strings: FootballLocalizedStrings,
    appLanguage: String,
    isArabicFont: Boolean,
    onSelectMatch: (FootballMatchItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val visibleFixtures = remember(fixtures) { fixtures.take(10) }

    Column(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF142824).copy(alpha = 0.84f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.todaysFixturesTitle,
                color = Color.White,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = if (isArabicFont) CairoFontFamily else null,
                style = NoPadTextStyle,
                maxLines = 1
            )
            Text(
                text = "${visibleFixtures.size}",
                color = Color(0xFF4ADE80),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                style = NoPadTextStyle,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.16f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(visibleFixtures, key = { _, item -> item.id }) { index, fixture ->
                var rowFocused by remember { mutableStateOf(false) }
                val isSelected = fixture.id == selectedMatchId
                val isLive = fixture.statusType == MatchStatusType.LIVE
                val isFavoriteMatch = remember(favoriteClubIds, fixture) {
                    FootballCatalogs.isMatchFollowed(favoriteClubIds, fixture)
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    rowFocused -> Color(0xFF22C55E).copy(alpha = 0.24f)
                                    isFavoriteMatch -> Color(0xFFFACC15).copy(alpha = 0.10f)
                                    isSelected -> Color.White.copy(alpha = 0.07f)
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                when {
                                    rowFocused -> Modifier.border(1.dp, Color(0xFF4ADE80), RoundedCornerShape(8.dp))
                                    isFavoriteMatch -> Modifier.border(0.8.dp, Color(0xFFFACC15).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    else -> Modifier
                                }
                            )
                            .onFocusChanged { state ->
                                rowFocused = state.isFocused
                                if (state.isFocused) {
                                    onSelectMatch(fixture)
                                }
                            }
                            .focusable()
                            .clickable { onSelectMatch(fixture) }
                            .padding(horizontal = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.width(38.dp)
                        ) {
                            if (isFavoriteMatch) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = strings.followedLabel,
                                    tint = Color(0xFFFACC15),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                            Text(
                                text = when (fixture.statusType) {
                                    MatchStatusType.LIVE -> fixture.statusBadgeText
                                    MatchStatusType.FINISHED -> strings.ftBadge
                                    MatchStatusType.UPCOMING -> fixture.statusBadgeText
                                },
                                color = if (isLive) Color(0xFFEF4444) else Color.White.copy(alpha = 0.65f),
                                fontSize = 9.5.sp,
                                fontWeight = if (isLive) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = if (isArabicFont) CairoFontFamily else null,
                                style = NoPadTextStyle,
                                maxLines = 1
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = fixture.getLocalizedHomeName(appLanguage),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = if (isArabicFont) CairoFontFamily else null,
                                    style = NoPadTextStyle,
                                    textAlign = TextAlign.End,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                ClubCrestBadge(
                                    code = fixture.homeCode,
                                    primaryColor = Color(fixture.homePrimaryColor),
                                    secondaryColor = Color(fixture.homeSecondaryColor),
                                    crestStyle = fixture.homeCrestStyle,
                                    logoUrl = fixture.homeLogoUrl,
                                    badgeSize = 18.dp
                                )
                            }

                            val centerScoreText = if (fixture.homeScore != null && fixture.awayScore != null) {
                                "${fixture.homeScore} - ${fixture.awayScore}"
                            } else {
                                "vs"
                            }
                            Text(
                                text = centerScoreText,
                                color = if (fixture.homeScore != null) Color.White else Color.White.copy(alpha = 0.65f),
                                fontSize = if (fixture.homeScore != null) 12.5.sp else 11.sp,
                                fontWeight = if (fixture.homeScore != null) FontWeight.ExtraBold else FontWeight.Medium,
                                style = NoPadTextStyle,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.width(34.dp)
                            )

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start
                            ) {
                                ClubCrestBadge(
                                    code = fixture.awayCode,
                                    primaryColor = Color(fixture.awayPrimaryColor),
                                    secondaryColor = Color(fixture.awaySecondaryColor),
                                    crestStyle = fixture.awayCrestStyle,
                                    logoUrl = fixture.awayLogoUrl,
                                    badgeSize = 18.dp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = fixture.getLocalizedAwayName(appLanguage),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = if (isArabicFont) CairoFontFamily else null,
                                    style = NoPadTextStyle,
                                    textAlign = TextAlign.Start,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }

                        val probText = fixture.probabilities?.let { p ->
                            "${p.homeWinPct}/${p.awayWinPct}%"
                        }
                        val rightLabel = when {
                            isLive -> strings.liveBadge
                            probText != null && fixture.statusType == MatchStatusType.UPCOMING -> probText
                            fixture.statusType == MatchStatusType.FINISHED -> strings.ftBadge
                            else -> fixture.statusBadgeText
                        }
                        Text(
                            text = rightLabel,
                            color = when {
                                isLive -> Color(0xFFEF4444)
                                probText != null && fixture.statusType == MatchStatusType.UPCOMING -> Color(0xFF4ADE80).copy(alpha = 0.85f)
                                else -> Color.White.copy(alpha = 0.58f)
                            },
                            fontSize = 9.5.sp,
                            fontWeight = if (isLive) FontWeight.Bold else FontWeight.Medium,
                            fontFamily = if (isArabicFont) CairoFontFamily else null,
                            style = NoPadTextStyle,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            modifier = Modifier.width(42.dp)
                        )
                    }

                    if (index < visibleFixtures.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.8.dp)
                                .background(Color.White.copy(alpha = 0.07f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeagueTableCard(
    standings: List<LeagueStandingRow>,
    favoriteClubIds: Set<String>,
    strings: FootballLocalizedStrings,
    appLanguage: String,
    isArabicFont: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val visibleStandings = remember(standings) { standings.take(20) }

    Column(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF142824).copy(alpha = 0.84f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
            .padding(horizontal = 13.dp, vertical = 10.dp)
    ) {
        // Title + Column Headers
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.leagueTableTitle,
                    color = Color.White,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = if (isArabicFont) CairoFontFamily else null,
                    style = NoPadTextStyle,
                    maxLines = 1
                )
                Text(
                    text = "${visibleStandings.size}",
                    color = Color(0xFF4ADE80),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    style = NoPadTextStyle,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF22C55E).copy(alpha = 0.16f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = strings.colPlayed,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.5.sp,
                    style = NoPadTextStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.width(22.dp)
                )
                Text(
                    text = strings.colGoalDiff,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.5.sp,
                    style = NoPadTextStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.width(26.dp)
                )
                Text(
                    text = strings.colPoints,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.5.sp,
                    style = NoPadTextStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.width(26.dp)
                )
                Text(
                    text = strings.colForm5,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 9.5.sp,
                    style = NoPadTextStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.width(70.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Scrollable Full Standings (up to 20 clubs, D-pad navigable)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            itemsIndexed(visibleStandings, key = { idx, row -> "${row.rank}_${row.teamCode}_$idx" }) { index, row ->
                var rowFocused by remember { mutableStateOf(false) }
                val isFavClub = remember(favoriteClubIds, row) {
                    FootballCatalogs.isClubFollowed(
                        favoriteClubIds,
                        row.teamCode,
                        row.teamNameEn,
                        row.teamNameAr,
                        row.localizedTeamName
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(31.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    rowFocused -> Color(0xFF22C55E).copy(alpha = 0.24f)
                                    isFavClub -> Color(0xFFFACC15).copy(alpha = 0.13f)
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (rowFocused) Modifier.border(1.dp, Color(0xFF4ADE80), RoundedCornerShape(6.dp))
                                else Modifier
                            )
                            .onFocusChanged { rowFocused = it.isFocused }
                            .focusable()
                            .clickable { onClick() }
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when {
                                        isFavClub -> Color(0xFFFACC15)
                                        row.isTopQualification -> Color(0xFF4ADE80)
                                        else -> Color.Transparent
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = "${row.rank}.",
                            color = if (isFavClub) Color(0xFFFACC15) else Color.White.copy(alpha = 0.80f),
                            fontSize = 11.sp,
                            fontWeight = if (isFavClub) FontWeight.Bold else FontWeight.Normal,
                            style = NoPadTextStyle,
                            maxLines = 1,
                            modifier = Modifier.width(20.dp)
                        )

                        ClubCrestBadge(
                            code = row.teamCode,
                            primaryColor = Color(row.primaryColor),
                            secondaryColor = Color(row.secondaryColor),
                            crestStyle = row.crestStyle,
                            logoUrl = row.logoUrl,
                            badgeSize = 17.dp
                        )

                        Spacer(modifier = Modifier.width(5.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = row.getLocalizedTeamName(appLanguage),
                                color = if (isFavClub) Color(0xFFFEF08A) else Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = if (isFavClub) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = if (isArabicFont) CairoFontFamily else null,
                                style = NoPadTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isFavClub) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = strings.followedLabel,
                                    tint = Color(0xFFFACC15),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }

                        Text(
                            text = row.played.toString(),
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 11.sp,
                            style = NoPadTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.width(22.dp)
                        )

                        Text(
                            text = row.goalDiff,
                            color = Color.White.copy(alpha = 0.78f),
                            fontSize = 11.sp,
                            style = NoPadTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.width(26.dp)
                        )

                        Text(
                            text = row.points.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            style = NoPadTextStyle,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier.width(26.dp)
                        )

                        Row(
                            modifier = Modifier.width(70.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.5.dp, Alignment.End),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.form5.take(5).forEach { resultChar ->
                                val badgeColor = when (resultChar) {
                                    'W' -> Color(0xFF16A34A)
                                    'L' -> Color(0xFFDC2626)
                                    else -> Color(0xFF64748B)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(11.5.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = resultChar.toString(),
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        style = NoPadTextStyle,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    if (index < visibleStandings.lastIndex) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.7.dp)
                                .background(Color.White.copy(alpha = 0.07f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LatestHeadlinesCard(
    headlines: List<FootballHeadlineItem>,
    strings: FootballLocalizedStrings,
    appLanguage: String,
    isArabicFont: Boolean,
    onClickHeadline: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF142824).copy(alpha = 0.86f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), shape)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = strings.latestHeadlinesTitle,
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isArabicFont) CairoFontFamily else null,
            style = NoPadTextStyle,
            maxLines = 1
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            headlines.take(4).forEach { item ->
                var itemFocused by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (itemFocused) Color(0xFF22C55E).copy(alpha = 0.20f)
                            else Color.Transparent
                        )
                        .then(
                            if (itemFocused) Modifier.border(1.dp, Color(0xFF4ADE80), RoundedCornerShape(10.dp))
                            else Modifier
                        )
                        .onFocusChanged { itemFocused = it.isFocused }
                        .focusable()
                        .clickable { onClickHeadline() }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    val context = LocalContext.current
                    var useFallbackPhoto by remember(item.id, item.imageUrl) { mutableStateOf(false) }
                    val fallbackUrl = FootballRepository.FALLBACK_HEADLINE_PHOTOS[
                        (item.id.hashCode() and 0x7FFFFFFF) % FootballRepository.FALLBACK_HEADLINE_PHOTOS.size
                    ]
                    val resolvedPhotoUrl = if (useFallbackPhoto || item.imageUrl.isBlank()) fallbackUrl else item.imageUrl
                    val photoRequest = remember(resolvedPhotoUrl) {
                        ImageRequest.Builder(context)
                            .data(resolvedPhotoUrl)
                            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                            .addHeader("Accept", "image/jpeg, image/png, */*;q=0.8")
                            .crossfade(250)
                            .build()
                    }
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF0F172A), Color(0xFF14532D))
                                )
                            )
                    ) {
                        AsyncImage(
                            model = photoRequest,
                            contentDescription = item.getLocalizedTitle(appLanguage),
                            contentScale = ContentScale.Crop,
                            onError = {
                                if (!useFallbackPhoto) useFallbackPhoto = true
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = item.getLocalizedTitle(appLanguage),
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isArabicFont) CairoFontFamily else null,
                            style = NoPadTextStyle,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 14.sp
                        )
                        Text(
                            text = item.getLocalizedMeta(appLanguage),
                            color = Color.White.copy(alpha = 0.58f),
                            fontSize = 10.sp,
                            fontFamily = if (isArabicFont) CairoFontFamily else null,
                            style = NoPadTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClubCrestBadge(
    code: String,
    primaryColor: Color,
    secondaryColor: Color,
    crestStyle: String,
    logoUrl: String,
    badgeSize: Dp
) {
    val context = LocalContext.current
    var imageLoaded by remember(logoUrl) { mutableStateOf(false) }
    var imageFailed by remember(logoUrl) { mutableStateOf(false) }

    val logoRequest = remember(logoUrl) {
        if (logoUrl.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(logoUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://www.365scores.com/")
                .crossfade(200)
                .build()
        } else null
    }

    Box(
        modifier = Modifier
            .size(badgeSize)
            .clip(CircleShape)
            .background(
                if (imageLoaded) Color.White.copy(alpha = 0.10f)
                else Color.Transparent
            ),
        contentAlignment = Alignment.Center
    ) {
        if (!imageLoaded) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val r = size.minDimension / 2f
                val center = Offset(w / 2f, h / 2f)

                if (crestStyle == "shield") {
                    val shieldPath = Path().apply {
                        moveTo(w * 0.14f, h * 0.10f)
                        lineTo(w * 0.86f, h * 0.10f)
                        lineTo(w * 0.86f, h * 0.54f)
                        cubicTo(
                            w * 0.86f, h * 0.78f,
                            w * 0.50f, h * 0.94f,
                            w * 0.50f, h * 0.94f
                        )
                        cubicTo(
                            w * 0.50f, h * 0.94f,
                            w * 0.14f, h * 0.78f,
                            w * 0.14f, h * 0.54f
                        )
                        close()
                    }
                    drawPath(shieldPath, color = primaryColor)
                    drawPath(
                        shieldPath,
                        color = secondaryColor,
                        style = Stroke(width = (w * 0.08f).coerceAtLeast(1.5f))
                    )
                } else {
                    drawCircle(
                        color = primaryColor,
                        radius = r * 0.92f,
                        center = center
                    )
                    drawCircle(
                        color = secondaryColor,
                        radius = r * 0.92f,
                        center = center,
                        style = Stroke(width = (w * 0.08f).coerceAtLeast(1.5f))
                    )
                }
            }

            if (logoRequest == null || imageFailed) {
                Text(
                    text = code.take(3),
                    color = Color.White,
                    fontSize = (badgeSize.value * 0.27f).sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = NoPadTextStyle,
                    maxLines = 1
                )
            }
        }

        if (logoRequest != null && !imageFailed) {
            AsyncImage(
                model = logoRequest,
                contentDescription = code,
                contentScale = ContentScale.Fit,
                onSuccess = { imageLoaded = true },
                onError = {
                    imageFailed = true
                    imageLoaded = false
                },
                modifier = Modifier.size(badgeSize * 0.86f)
            )
        }
    }
}

@Composable
private fun FootballFooterBar(
    lastUpdatedTime: String,
    isLoading: Boolean,
    strings: FootballLocalizedStrings,
    isArabicFont: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(18.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${strings.sourcePrefix}  •  ${strings.lastUpdatedPrefix}$lastUpdatedTime",
            color = Color.White.copy(alpha = 0.62f),
            fontSize = 10.5.sp,
            fontFamily = if (isArabicFont) CairoFontFamily else null,
            style = NoPadTextStyle,
            maxLines = 1
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(26.dp)
                    .height(13.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.22f))
                    .padding(2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLoading) Color(0xFF4ADE80)
                            else Color.White.copy(alpha = 0.75f)
                        )
                )
            }

            Text(
                text = "((📡))",
                color = Color.White.copy(alpha = 0.50f),
                fontSize = 10.sp,
                style = NoPadTextStyle,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FootballLeagueCustomizationDialog(
    strings: FootballLocalizedStrings,
    appLanguage: String,
    isArabicFont: Boolean,
    selectedLeagueIds: List<String>,
    favoriteClubIds: Set<String>,
    onUpdateLeagues: (List<String>) -> Unit,
    onUpdateFavoriteClubs: (Set<String>) -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf("leagues") } // "leagues" | "clubs"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.76f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(780.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F2922), Color(0xFF091916))
                        )
                    )
                    .border(1.5.dp, Color(0xFF4ADE80).copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                    .clickable(enabled = false) {}
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = strings.customizeDialogTitle,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = if (isArabicFont) CairoFontFamily else null,
                            style = NoPadTextStyle
                        )
                        Text(
                            text = if (activeTab == "leagues") {
                                "${strings.customizeDialogSubtitle} (${selectedLeagueIds.size}/4)"
                            } else {
                                "${strings.customizeClubsSubtitle} (${favoriteClubIds.size}/12)"
                            },
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 12.sp,
                            fontFamily = if (isArabicFont) CairoFontFamily else null,
                            style = NoPadTextStyle
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        var resetFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .testTag("football_customize_reset_btn")
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (resetFocused) Color.White.copy(alpha = 0.22f)
                                    else Color.White.copy(alpha = 0.10f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (resetFocused) Color.White else Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .onFocusChanged { resetFocused = it.isFocused }
                                .focusable()
                                .clickable { onResetDefaults() }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = strings.resetDefaults,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = if (isArabicFont) CairoFontFamily else null,
                                style = NoPadTextStyle
                            )
                        }

                        var doneFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .testTag("football_customize_done_btn")
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (doneFocused) Color(0xFF22C55E)
                                    else Color(0xFF16A34A)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (doneFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .onFocusChanged { doneFocused = it.isFocused }
                                .focusable()
                                .clickable { onDismiss() }
                                .padding(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = strings.doneAction,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = if (isArabicFont) CairoFontFamily else null,
                                style = NoPadTextStyle
                            )
                        }
                    }
                }

                // 2-Tab Selector: Leagues (23) | Clubs (42)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf(
                        "leagues" to "${strings.leaguesTabTitle} (${FootballRepository.ALL_LEAGUES.size})",
                        "clubs" to "${strings.clubsTabTitle} (${FootballRepository.ALL_CLUBS.size})"
                    )
                    tabs.forEach { (tabId, label) ->
                        val isSelectedTab = activeTab == tabId
                        var tabFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("football_customize_tab_$tabId")
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    when {
                                        tabFocused -> Color(0xFF22C55E).copy(alpha = 0.35f)
                                        isSelectedTab -> Color(0xFF16A34A).copy(alpha = 0.42f)
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (tabFocused) 1.5.dp else if (isSelectedTab) 1.dp else 0.dp,
                                    color = when {
                                        tabFocused -> Color(0xFF4ADE80)
                                        isSelectedTab -> Color(0xFF4ADE80).copy(alpha = 0.65f)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(9.dp)
                                )
                                .onFocusChanged { tabFocused = it.isFocused }
                                .focusable()
                                .clickable { activeTab = tabId }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelectedTab || tabFocused) Color.White else Color.White.copy(alpha = 0.72f),
                                fontSize = 13.sp,
                                fontWeight = if (isSelectedTab || tabFocused) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = if (isArabicFont) CairoFontFamily else null,
                                style = NoPadTextStyle
                            )
                        }
                    }
                }

                if (activeTab == "leagues") {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(330.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(FootballRepository.ALL_LEAGUES, key = { it.id }) { league ->
                            val isSelected = league.id in selectedLeagueIds
                            var itemFocused by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            itemFocused -> Color(0xFF22C55E).copy(alpha = 0.28f)
                                            isSelected -> Color(0xFF16A34A).copy(alpha = 0.22f)
                                            else -> Color.White.copy(alpha = 0.06f)
                                        }
                                    )
                                    .border(
                                        width = if (itemFocused) 2.dp else 1.dp,
                                        color = when {
                                            itemFocused -> Color(0xFF4ADE80)
                                            isSelected -> Color(0xFF22C55E).copy(alpha = 0.65f)
                                            else -> Color.White.copy(alpha = 0.14f)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .onFocusChanged { itemFocused = it.isFocused }
                                    .focusable()
                                    .clickable {
                                        val current = selectedLeagueIds.toMutableList()
                                        if (isSelected) {
                                            if (current.size > 1) {
                                                current.remove(league.id)
                                                onUpdateLeagues(current)
                                            }
                                        } else {
                                            if (current.size >= 4) {
                                                current.removeAt(current.lastIndex)
                                            }
                                            current.add(league.id)
                                            onUpdateLeagues(current)
                                        }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = league.getLocalizedTitle(appLanguage),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                                        style = NoPadTextStyle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = league.getLocalizedCountry(appLanguage),
                                        color = Color.White.copy(alpha = 0.62f),
                                        fontSize = 11.sp,
                                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                                        style = NoPadTextStyle
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(0xFF22C55E)
                                            else Color.White.copy(alpha = 0.12f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(330.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        items(FootballRepository.ALL_CLUBS, key = { it.id }) { club ->
                            val isFollowed = club.id in favoriteClubIds
                            var clubFocused by remember { mutableStateOf(false) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            clubFocused -> Color(0xFF22C55E).copy(alpha = 0.28f)
                                            isFollowed -> Color(0xFFFACC15).copy(alpha = 0.16f)
                                            else -> Color.White.copy(alpha = 0.06f)
                                        }
                                    )
                                    .border(
                                        width = if (clubFocused) 2.dp else 1.dp,
                                        color = when {
                                            clubFocused -> Color(0xFF4ADE80)
                                            isFollowed -> Color(0xFFFACC15).copy(alpha = 0.70f)
                                            else -> Color.White.copy(alpha = 0.14f)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .onFocusChanged { clubFocused = it.isFocused }
                                    .focusable()
                                    .clickable {
                                        val updated = favoriteClubIds.toMutableSet()
                                        if (isFollowed) {
                                            updated.remove(club.id)
                                        } else if (updated.size < 12) {
                                            updated.add(club.id)
                                        }
                                        onUpdateFavoriteClubs(updated)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ClubCrestBadge(
                                    code = club.code,
                                    primaryColor = Color(club.primaryColor),
                                    secondaryColor = Color(club.secondaryColor),
                                    crestStyle = club.crestStyle,
                                    logoUrl = club.logoUrl,
                                    badgeSize = 26.dp
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = club.getLocalizedName(appLanguage),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                                        style = NoPadTextStyle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = club.getLocalizedLeague(appLanguage),
                                        color = Color.White.copy(alpha = 0.60f),
                                        fontSize = 10.sp,
                                        fontFamily = if (isArabicFont) CairoFontFamily else null,
                                        style = NoPadTextStyle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isFollowed) Color(0xFFFACC15)
                                            else Color.White.copy(alpha = 0.12f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isFollowed) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = strings.followedLabel,
                                            tint = Color.Black,
                                            modifier = Modifier.size(13.dp)
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
