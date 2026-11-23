package com.example.ui.weather

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
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
import com.example.data.local.LauncherPreferences
import com.example.data.weather.DailyForecastItem
import com.example.data.weather.HourlyTempPoint
import com.example.data.weather.WeatherConditionType
import com.example.data.weather.WeatherData
import com.example.data.weather.WeatherLocalizedStrings
import com.example.data.weather.WeatherLocationSource
import com.example.data.weather.WeatherRepository
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvAccentGold
import com.example.util.LocaleHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val WeatherNoPadTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)

@Volatile
private var hasRequestedWeatherGpsThisSession = false

@Composable
fun WeatherScreen(
    is24hFormat: Boolean = true,
    appLanguage: String = "en",
    onBackToHome: () -> Unit
) {
    BackHandler(enabled = true) {
        onBackToHome()
    }

    val context = LocalContext.current
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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        WeatherRepository.refreshWeatherAsync(
            context = context,
            appLanguage = effectiveLang,
            forceLocation = true
        )
    }

    fun requestGpsAndRefresh() {
        if (WeatherRepository.hasLocationPermission(context)) {
            WeatherRepository.refreshWeatherAsync(
                context = context,
                appLanguage = effectiveLang,
                forceLocation = true
            )
        } else {
            hasRequestedWeatherGpsThisSession = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(effectiveLang) {
        if (!WeatherRepository.hasLocationPermission(context) && !hasRequestedWeatherGpsThisSession) {
            hasRequestedWeatherGpsThisSession = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            WeatherRepository.ensureInitialized(context, effectiveLang)
        }
    }

    val weather by WeatherRepository.weatherState.collectAsState()
    val strings = remember(effectiveLang) {
        WeatherRepository.getLocalizedStrings(effectiveLang)
    }
    val isArabicFont = remember(effectiveLang) {
        LocaleHelper.isArabicOrRtlLanguage(effectiveLang)
    }
    val textFont = if (isArabicFont) CairoFontFamily else FontFamily.Default
    val layoutDirection = if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

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

    val mainFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(60L)
        try {
            mainFocusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(mainFocusRequester)
                .focusable()
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when (event.key) {
                            Key.Back, Key.Escape -> {
                                onBackToHome()
                                true
                            }
                            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                                requestGpsAndRefresh()
                                true
                            }
                            else -> false
                        }
                    } else false
                }
                .testTag("weather_full_screen")
        ) {
            val isCompactHeight = maxHeight < 470.dp
            val outerHorizPad = if (isCompactHeight) 22.dp else 34.dp
            val outerVertPad = if (isCompactHeight) 10.dp else 18.dp
            val sectionGapTop = if (isCompactHeight) 6.dp else 10.dp
            val sectionGapMid = if (isCompactHeight) 8.dp else 12.dp
            val sectionGapBottom = if (isCompactHeight) 5.dp else 8.dp
            val colSpacing = if (isCompactHeight) 10.dp else 14.dp

            // 1. Atmospheric Daylight Sky + Sunbeams + Volumetric Clouds Background
            WeatherAtmosphericSkyBackground(
                isDay = weather.isDay,
                condition = weather.conditionType
            )

            // 2. Main Weather Dashboard Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = outerHorizPad, vertical = outerVertPad),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // =================================================================
                // TOP HEADER: City + Current Weather on Start, Live + Clock on End
                // =================================================================
                WeatherTopHeader(
                    cityName = weather.cityName,
                    currentWeatherLabel = strings.currentWeather,
                    liveBadgeLabel = strings.liveBadge,
                    timeString = currentTimeString,
                    textFont = textFont,
                    isLoading = weather.isLoading,
                    isGpsActive = weather.locationSource == WeatherLocationSource.GPS,
                    isCompact = isCompactHeight,
                    onRefreshClick = { requestGpsAndRefresh() },
                    onBackClick = onBackToHome
                )

                Spacer(modifier = Modifier.height(sectionGapTop))

                // =================================================================
                // MIDDLE ROW: 3 Columns (Gauge Card | 24h Curve Chart | 4 Metrics)
                // =================================================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.66f),
                    horizontalArrangement = Arrangement.spacedBy(colSpacing)
                ) {
                    // Start Card: Circular Arc Temperature Gauge + Feels Like + 3D Icon + Condition
                    WeatherMainGaugeCard(
                        weather = weather,
                        strings = strings,
                        conditionLabel = WeatherRepository.getConditionLabel(weather.conditionType, effectiveLang),
                        textFont = textFont,
                        modifier = Modifier
                            .weight(0.92f)
                            .fillMaxHeight()
                    )

                    // Center Card: Hourly Temperature Curve for Next 24 Hours
                    WeatherHourlyChartCard(
                        weather = weather,
                        strings = strings,
                        textFont = textFont,
                        modifier = Modifier
                            .weight(1.36f)
                            .fillMaxHeight()
                    )

                    // End Column: Humidity + Wind (top), Pressure (middle), UV Index (bottom)
                    WeatherMetricsColumn(
                        weather = weather,
                        strings = strings,
                        windDirLabel = WeatherRepository.getWindDirectionLabel(weather.windDirectionDeg, effectiveLang),
                        uvCategoryLabel = WeatherRepository.getUvCategoryLabel(weather.uvIndex, effectiveLang),
                        textFont = textFont,
                        modifier = Modifier
                            .weight(0.96f)
                            .fillMaxHeight()
                    )
                }

                Spacer(modifier = Modifier.height(sectionGapMid))

                // =================================================================
                // BOTTOM CARD: 5-Day Forecast Strip
                // =================================================================
                WeatherFiveDayForecastCard(
                    dailyForecasts = weather.dailyForecasts,
                    textFont = textFont,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.34f)
                )

                Spacer(modifier = Modifier.height(sectionGapBottom))

                // =================================================================
                // FOOTER ROW: Last updated time + Live sensor indicator
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
                            .clickable { requestGpsAndRefresh() }
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${strings.lastUpdatedPrefix} ${weather.lastUpdatedTime}",
                            color = Color.White.copy(alpha = 0.80f),
                            fontSize = if (isCompactHeight) 11.5.sp else 12.5.sp,
                            fontFamily = textFont,
                            fontWeight = FontWeight.Medium,
                            style = WeatherNoPadTextStyle,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = strings.refreshLocationOption,
                            tint = Color.White.copy(alpha = 0.72f),
                            modifier = Modifier.size(if (isCompactHeight) 12.dp else 13.dp)
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
                                .background(Color.White.copy(alpha = 0.28f))
                                .padding(2.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.90f))
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherTopHeader(
    cityName: String,
    currentWeatherLabel: String,
    liveBadgeLabel: String,
    timeString: String,
    textFont: FontFamily,
    isLoading: Boolean,
    isGpsActive: Boolean,
    isCompact: Boolean,
    onRefreshClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Start: City Name + Location Icon + GPS badge + "Current Weather"
        Column(verticalArrangement = Arrangement.spacedBy(if (isCompact) 2.dp else 4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onRefreshClick() }
                    .testTag("weather_gps_city_refresh")
            ) {
                Text(
                    text = cityName,
                    color = Color.White,
                    fontSize = if (isCompact) 24.sp else 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = textFont,
                    style = WeatherNoPadTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = cityName,
                    tint = if (isGpsActive) {
                        Color(0xFF4ADE80).copy(alpha = if (isLoading) 0.55f else 0.95f)
                    } else {
                        Color.White.copy(alpha = if (isLoading) 0.5f else 0.85f)
                    },
                    modifier = Modifier.size(if (isCompact) 18.dp else 22.dp)
                )
                if (isGpsActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF4ADE80).copy(alpha = 0.22f))
                            .border(1.dp, Color(0xFF4ADE80).copy(alpha = 0.55f), RoundedCornerShape(50))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "GPS",
                            color = Color(0xFF86EFAC),
                            fontSize = if (isCompact) 9.5.sp else 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            style = WeatherNoPadTextStyle,
                            maxLines = 1
                        )
                    }
                }
            }
            Text(
                text = currentWeatherLabel,
                color = Color.White.copy(alpha = 0.84f),
                fontSize = if (isCompact) 12.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = textFont,
                style = WeatherNoPadTextStyle,
                maxLines = 1
            )
        }

        // End: Wi-Fi + "• Live" + Big Clock
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 10.dp else 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.88f),
                    modifier = Modifier.size(if (isCompact) 13.dp else 14.dp)
                )
                val liveTransition = rememberInfiniteTransition(label = "weather_live_dot")
                val livePulse = liveTransition.animateFloat(
                    initialValue = 0.45f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1400, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "live_dot_alpha"
                )
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .graphicsLayer {
                            val p = livePulse.value
                            alpha = p
                            val s = 0.88f + p * 0.24f
                            scaleX = s
                            scaleY = s
                        }
                        .clip(CircleShape)
                        .background(Color(0xFF4ADE80))
                )
                Text(
                    text = liveBadgeLabel,
                    color = Color(0xFF4ADE80),
                    fontSize = if (isCompact) 12.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = textFont,
                    style = WeatherNoPadTextStyle,
                    maxLines = 1
                )
            }

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = timeString,
                    color = Color.White,
                    fontSize = if (isCompact) 30.sp else 36.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default,
                    style = WeatherNoPadTextStyle.copy(
                        fontFeatureSettings = "tnum"
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun WeatherGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color(0xFF8AB4E1).copy(alpha = 0.16f),
                        Color(0xFF4B749F).copy(alpha = 0.22f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(600f, 600f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.48f),
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.30f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

@Composable
private fun WeatherMainGaugeCard(
    weather: WeatherData,
    strings: WeatherLocalizedStrings,
    conditionLabel: String,
    textFont: FontFamily,
    modifier: Modifier = Modifier
) {
    WeatherGlassCard(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            val isTight = maxHeight < 145.dp
            val isCompact = maxHeight < 210.dp

            val tempFontSize = when {
                isTight -> 28.sp
                isCompact -> 36.sp
                else -> 46.sp
            }
            val feelsFontSize = when {
                isTight -> 10.5.sp
                isCompact -> 12.sp
                else -> 14.sp
            }
            val iconSize = when {
                isTight -> 30.dp
                isCompact -> 40.dp
                else -> 58.dp
            }
            val conditionFontSize = when {
                isTight -> 11.5.sp
                isCompact -> 13.5.sp
                else -> 16.sp
            }
            val iconTopGap = when {
                isTight -> 2.dp
                isCompact -> 4.dp
                else -> 8.dp
            }
            val condTopGap = when {
                isTight -> 2.dp
                isCompact -> 3.dp
                else -> 6.dp
            }

            // Circular Arc Gauge Canvas
            val tempProgress = ((weather.currentTempC + 5).coerceIn(0, 50) / 50f).coerceIn(0.18f, 0.92f)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isCompact) 2.dp else 6.dp)
            ) {
                val diameter = minOf(size.width, size.height) * 0.90f
                if (diameter <= 16f) return@Canvas
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)
                val strokePx = if (isCompact) 6.5.dp.toPx() else 8.5.dp.toPx()
                val glowStrokePx = if (isCompact) 13.dp.toPx() else 18.dp.toPx()

                // Background Track Arc (from 140° sweep 260°)
                drawArc(
                    color = Color.White.copy(alpha = 0.22f),
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                val activeSweep = 260f * tempProgress
                val arcGradient = Brush.sweepGradient(
                    0.0f to Color(0xFF67E8F9),
                    0.35f to Color(0xFF5EEAD4),
                    0.68f to Color(0xFFFDE047),
                    0.85f to Color(0xFFF59E0B),
                    1.0f to Color(0xFF67E8F9),
                    center = Offset(size.width / 2f, size.height / 2f)
                )

                // Soft Outer Glow Arc
                drawArc(
                    brush = arcGradient,
                    startAngle = 140f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    alpha = 0.28f,
                    style = Stroke(width = glowStrokePx, cap = StrokeCap.Round)
                )

                // Main Active Gradient Arc
                drawArc(
                    brush = arcGradient,
                    startAngle = 140f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // Inner Gauge Content: Big Temperature + Feels Like + 3D Weather Icon + Condition
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = "${weather.currentTempC}°C",
                        color = Color.White,
                        fontSize = tempFontSize,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Default,
                        style = WeatherNoPadTextStyle,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = strings.feelsLikePrefix,
                        color = Color.White.copy(alpha = 0.86f),
                        fontSize = feelsFontSize,
                        fontWeight = FontWeight.Medium,
                        fontFamily = textFont,
                        style = WeatherNoPadTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(
                            text = "${weather.feelsLikeC}°",
                            color = Color.White.copy(alpha = 0.92f),
                            fontSize = feelsFontSize,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Default,
                            style = WeatherNoPadTextStyle,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(iconTopGap))

                val iconFloatTransition = rememberInfiniteTransition(label = "weather_icon_float")
                val iconFloatOffset = iconFloatTransition.animateFloat(
                    initialValue = -2.4f,
                    targetValue = 2.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "icon_float_y"
                )

                WeatherConditionIcon(
                    condition = weather.conditionType,
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer {
                            translationY = iconFloatOffset.value.dp.toPx()
                        }
                )

                Spacer(modifier = Modifier.height(condTopGap))

                Text(
                    text = conditionLabel,
                    color = Color.White,
                    fontSize = conditionFontSize,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = textFont,
                    style = WeatherNoPadTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun WeatherHourlyChartCard(
    weather: WeatherData,
    strings: WeatherLocalizedStrings,
    textFont: FontFamily,
    modifier: Modifier = Modifier
) {
    WeatherGlassCard(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            val isCompact = maxHeight < 185.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Row: Title on Start + High / Low summary pill on End (never overlaps curve)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.hourlyTitle,
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = if (isCompact) 12.5.sp else 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = textFont,
                        style = WeatherNoPadTextStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.14f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = strings.highPrefix,
                                color = Color(0xFFFDE047),
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1
                            )
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "${weather.high24hC}°",
                                    color = Color(0xFFFDE047),
                                    fontSize = if (isCompact) 11.sp else 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Default,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
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
                                fontSize = if (isCompact) 11.sp else 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1
                            )
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = "${weather.low24hC}°",
                                    color = Color(0xFFBAE6FD),
                                    fontSize = if (isCompact) 11.sp else 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Default,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                val points = weather.hourlyPoints
                val rawMin = points.minOfOrNull { it.tempC } ?: 18
                val rawMax = points.maxOfOrNull { it.tempC } ?: 31
                val padTemp = ((rawMax - rawMin) * 0.25f).coerceAtLeast(1.5f)
                val minTemp = rawMin - padTemp
                val maxTemp = rawMax + padTemp
                val tempSpan = (maxTemp - minTemp).coerceAtLeast(3f)

                // Curve Chart + X-Axis always LTR so time progresses smoothly left-to-right
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(top = 6.dp, bottom = 4.dp)
                        ) {
                            if (points.size < 2 || size.width <= 16f || size.height <= 20f) return@Canvas

                            val leftPad = 10.dp.toPx()
                            val rightPad = 10.dp.toPx()
                            val topPad = (size.height * 0.14f).coerceIn(4f, 16.dp.toPx())
                            val bottomClear = (size.height * 0.14f).coerceIn(4f, 14.dp.toPx())
                            val baselineY = (size.height - 2.dp.toPx()).coerceAtLeast(topPad + 12f)
                            val usableW = (size.width - leftPad - rightPad).coerceAtLeast(10f)
                            val usableH = (baselineY - topPad - bottomClear).coerceAtLeast(8f)
                            val stepX = usableW / (points.size - 1).toFloat()

                            val coords = points.mapIndexed { idx, pt ->
                                val x = leftPad + idx * stepX
                                val normY = ((pt.tempC - minTemp) / tempSpan).coerceIn(0f, 1f)
                                val y = topPad + usableH * (1f - normY)
                                Offset(x, y)
                            }

                            // Smooth cubic Bezier path
                            val curvePath = Path().apply {
                                moveTo(coords.first().x, coords.first().y)
                                for (i in 0 until coords.size - 1) {
                                    val p0 = coords[i]
                                    val p1 = coords[i + 1]
                                    val ctrlX1 = p0.x + (p1.x - p0.x) * 0.45f
                                    val ctrlX2 = p0.x + (p1.x - p0.x) * 0.55f
                                    cubicTo(ctrlX1, p0.y, ctrlX2, p1.y, p1.x, p1.y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(curvePath)
                                lineTo(coords.last().x, baselineY)
                                lineTo(coords.first().x, baselineY)
                                close()
                            }

                            // Strictly enforce startY < endY so Skia verticalGradient never renders black
                            val gradStartY = topPad.coerceAtMost(baselineY - 4f)
                            val gradEndY = baselineY.coerceAtLeast(gradStartY + 4f)

                            // Fill under curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF7DD3FC).copy(alpha = 0.42f),
                                        Color(0xFFFDE047).copy(alpha = 0.18f),
                                        Color(0xFF38BDF8).copy(alpha = 0.03f)
                                    ),
                                    startY = gradStartY,
                                    endY = gradEndY
                                )
                            )

                            // Bottom horizontal axis line
                            drawLine(
                                color = Color.White.copy(alpha = 0.24f),
                                start = Offset(leftPad, baselineY),
                                end = Offset(size.width - rightPad, baselineY),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Vertical highlight line at High peak
                            val highIdx = weather.high24hIndex.coerceIn(0, coords.lastIndex)
                            val lowIdx = weather.low24hIndex.coerceIn(0, coords.lastIndex)
                            val highPt = coords[highIdx]
                            val lowPt = coords[lowIdx]

                            drawLine(
                                color = Color(0xFFFDE047).copy(alpha = 0.40f),
                                start = Offset(highPt.x, highPt.y),
                                end = Offset(highPt.x, baselineY),
                                strokeWidth = 1.dp.toPx()
                            )

                            // Outer glow stroke for curve
                            drawPath(
                                path = curvePath,
                                color = Color(0xFFFDE68A).copy(alpha = 0.30f),
                                style = Stroke(
                                    width = 7.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Main crisp white curve stroke
                            drawPath(
                                path = curvePath,
                                color = Color.White,
                                style = Stroke(
                                    width = 2.6.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Glowing dot on High peak
                            drawCircle(
                                color = Color(0xFFFDE047).copy(alpha = 0.38f),
                                radius = 7.dp.toPx(),
                                center = highPt
                            )
                            drawCircle(
                                color = Color(0xFFFDE047),
                                radius = 3.8.dp.toPx(),
                                center = highPt
                            )

                            // Subtle ice-blue dot on Low trough (if distinct from High)
                            if (lowIdx != highIdx) {
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
                            }
                        }

                        // Bottom X-Axis Time Labels (06:00  09:00  12:00 ...)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            points.forEach { pt ->
                                Text(
                                    text = pt.timeLabel,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = if (isCompact) 10.5.sp else 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = FontFamily.Default,
                                    style = WeatherNoPadTextStyle,
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
private fun WeatherMetricsColumn(
    weather: WeatherData,
    strings: WeatherLocalizedStrings,
    windDirLabel: String,
    uvCategoryLabel: String,
    textFont: FontFamily,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val isCompact = maxHeight < 210.dp
        val rowGap = if (isCompact) 6.dp else 10.dp
        val cardRadius = if (isCompact) 14.dp else 16.dp
        val cardPadH = if (isCompact) 10.dp else 14.dp
        val cardPadV = if (isCompact) 6.dp else 10.dp

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(rowGap)
        ) {
            // Row 1: Humidity Card + Wind Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isCompact) 1.06f else 1.05f),
                horizontalArrangement = Arrangement.spacedBy(rowGap)
            ) {
                // Humidity Card
                WeatherGlassCard(
                    modifier = Modifier
                        .weight(0.88f)
                        .fillMaxHeight(),
                    cornerRadius = cardRadius
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = cardPadH, vertical = cardPadV),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.humidity,
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = if (isCompact) 11.sp else 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF7DD3FC),
                                modifier = Modifier.size(if (isCompact) 13.dp else 16.dp)
                            )
                        }

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = "${weather.humidityPercent}%",
                                color = Color.White,
                                fontSize = if (isCompact) 18.sp else 25.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Default,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Wind Card
                WeatherGlassCard(
                    modifier = Modifier
                        .weight(1.12f)
                        .fillMaxHeight(),
                    cornerRadius = cardRadius
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = cardPadH, vertical = cardPadV),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.wind,
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = if (isCompact) 11.sp else 13.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = null,
                                tint = Color(0xFF7DD3FC),
                                modifier = Modifier.size(if (isCompact) 13.dp else 16.dp)
                            )
                        }

                        if (isCompact) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${weather.windSpeedKmh}",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Default,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
                                )
                                Text(
                                    text = strings.kmhUnit,
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = textFont,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier
                                        .size(10.dp)
                                        .rotate(weather.windDirectionDeg.toFloat())
                                )
                                Text(
                                    text = windDirLabel,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = textFont,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "${weather.windSpeedKmh}",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Default,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
                                )
                                Text(
                                    text = strings.kmhUnit,
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = textFont,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = Color(0xFF7DD3FC),
                                    modifier = Modifier
                                        .size(11.dp)
                                        .rotate(weather.windDirectionDeg.toFloat())
                                )
                                Text(
                                    text = windDirLabel,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = textFont,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Row 2: Pressure Card
            WeatherGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isCompact) 0.88f else 0.92f),
                cornerRadius = cardRadius
            ) {
                if (isCompact) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = cardPadH, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color(0xFF7DD3FC),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = strings.pressure,
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${weather.pressureHpa}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Default,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
                                )
                                Text(
                                    text = strings.hpaUnit,
                                    color = Color.White.copy(alpha = 0.82f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = textFont,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1,
                                    modifier = Modifier.padding(bottom = 1.dp)
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = cardPadH, vertical = cardPadV),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.pressure,
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = Color(0xFF7DD3FC),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "${weather.pressureHpa}",
                                    color = Color.White,
                                    fontSize = 25.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Default,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
                                )
                                Text(
                                    text = strings.hpaUnit,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = textFont,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Row 3: UV Index Card
            WeatherGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isCompact) 1.06f else 1.03f),
                cornerRadius = cardRadius
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = cardPadH, vertical = cardPadV),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isCompact) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.uvIndex,
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 6.dp)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "${weather.uvIndex}",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Default,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
                                )
                                Text(
                                    text = "($uvCategoryLabel)",
                                    color = Color(0xFFFACC15),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = textFont,
                                    style = WeatherNoPadTextStyle,
                                    maxLines = 1
                                )
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.uvIndex,
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1
                            )
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${weather.uvIndex}",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Default,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1
                            )
                            Text(
                                text = "($uvCategoryLabel)",
                                color = Color(0xFFFACC15),
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = textFont,
                                style = WeatherNoPadTextStyle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }

                    // 6-Segment UV Spectrum Bar
                    val uvColors = listOf(
                        Color(0xFF38BDF8),
                        Color(0xFF4ADE80),
                        Color(0xFFA3E635),
                        Color(0xFFFBBF24),
                        Color(0xFFF97316),
                        Color(0xFF991B1B)
                    )
                    val activeBucket = (weather.uvIndex / 2).coerceIn(0, 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        uvColors.forEachIndexed { idx, segColor ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (isCompact) 4.5.dp else 5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        if (idx <= activeBucket) segColor
                                        else segColor.copy(alpha = 0.42f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherFiveDayForecastCard(
    dailyForecasts: List<DailyForecastItem>,
    textFont: FontFamily,
    modifier: Modifier = Modifier
) {
    WeatherGlassCard(
        modifier = modifier,
        cornerRadius = 18.dp
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isCompact = maxHeight < 105.dp
            val vertPad = if (isCompact) 7.dp else 12.dp
            val horizPad = if (isCompact) 10.dp else 16.dp
            val dayFontSize = if (isCompact) 13.sp else 15.sp
            val iconSize = if (isCompact) 28.dp else 36.dp
            val maxTempSize = if (isCompact) 13.sp else 15.sp
            val minTempSize = if (isCompact) 12.sp else 14.sp

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = vertPad),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dailyForecasts.take(5).forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(0.74f)
                                .background(Color.White.copy(alpha = 0.18f))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = horizPad),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.dayLabel,
                            color = Color.White,
                            fontSize = dayFontSize,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = textFont,
                            style = WeatherNoPadTextStyle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        WeatherConditionIcon(
                            condition = item.condition,
                            modifier = Modifier.size(iconSize)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.maxTempC}°",
                                        color = Color.White,
                                        fontSize = maxTempSize,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Default,
                                        style = WeatherNoPadTextStyle,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = " / ${item.minTempC}°",
                                        color = Color.White.copy(alpha = 0.72f),
                                        fontSize = minTempSize,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.Default,
                                        style = WeatherNoPadTextStyle,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Rainbow Temperature Range Pill Bar
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (isCompact) 4.5.dp else 5.5.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF38BDF8),
                                                Color(0xFF4ADE80),
                                                Color(0xFFFACC15),
                                                Color(0xFFF97316),
                                                Color(0xFFEF4444)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Multi-layered 3D-style Weather Icon drawn cleanly in Compose Canvas
 * (usable both in the Full Weather Page and in the Home Screen Top Bar between Clock and Date).
 */
@Composable
fun WeatherConditionIcon(
    condition: WeatherConditionType,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val minDim = minOf(w, h)

        when (condition) {
            WeatherConditionType.CLEAR_DAY -> {
                draw3dSun(
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = minDim * 0.28f
                )
            }
            WeatherConditionType.CLEAR_NIGHT -> {
                draw3dMoon(
                    center = Offset(w * 0.5f, h * 0.5f),
                    radius = minDim * 0.30f
                )
            }
            WeatherConditionType.PARTLY_CLOUDY_DAY -> {
                draw3dSun(
                    center = Offset(w * 0.38f, h * 0.38f),
                    radius = minDim * 0.22f
                )
                draw3dCloud(
                    center = Offset(w * 0.56f, h * 0.60f),
                    scale = minDim * 0.85f
                )
            }
            WeatherConditionType.PARTLY_CLOUDY_NIGHT -> {
                draw3dMoon(
                    center = Offset(w * 0.38f, h * 0.36f),
                    radius = minDim * 0.21f
                )
                draw3dCloud(
                    center = Offset(w * 0.56f, h * 0.60f),
                    scale = minDim * 0.85f
                )
            }
            WeatherConditionType.CLOUDY, WeatherConditionType.FOG -> {
                draw3dCloud(
                    center = Offset(w * 0.44f, h * 0.44f),
                    scale = minDim * 0.72f,
                    tintAlpha = 0.75f
                )
                draw3dCloud(
                    center = Offset(w * 0.54f, h * 0.58f),
                    scale = minDim * 0.90f
                )
            }
            WeatherConditionType.DRIZZLE, WeatherConditionType.RAIN -> {
                draw3dSun(
                    center = Offset(w * 0.34f, h * 0.34f),
                    radius = minDim * 0.18f
                )
                draw3dCloud(
                    center = Offset(w * 0.54f, h * 0.50f),
                    scale = minDim * 0.84f
                )
                drawRainDrops(
                    center = Offset(w * 0.52f, h * 0.80f),
                    span = minDim * 0.45f
                )
            }
            WeatherConditionType.THUNDERSTORM -> {
                draw3dCloud(
                    center = Offset(w * 0.52f, h * 0.48f),
                    scale = minDim * 0.88f
                )
                // Lightning bolt
                val bolt = Path().apply {
                    moveTo(w * 0.53f, h * 0.62f)
                    lineTo(w * 0.43f, h * 0.78f)
                    lineTo(w * 0.52f, h * 0.78f)
                    lineTo(w * 0.47f, h * 0.94f)
                    lineTo(w * 0.62f, h * 0.74f)
                    lineTo(w * 0.53f, h * 0.74f)
                    close()
                }
                drawPath(path = bolt, color = Color(0xFFFACC15))
            }
            WeatherConditionType.SNOW -> {
                draw3dCloud(
                    center = Offset(w * 0.52f, h * 0.50f),
                    scale = minDim * 0.84f
                )
                drawCircle(color = Color.White, radius = minDim * 0.045f, center = Offset(w * 0.38f, h * 0.80f))
                drawCircle(color = Color.White, radius = minDim * 0.045f, center = Offset(w * 0.52f, h * 0.84f))
                drawCircle(color = Color.White, radius = minDim * 0.045f, center = Offset(w * 0.66f, h * 0.80f))
            }
        }
    }
}

private fun DrawScope.draw3dSun(center: Offset, radius: Float) {
    // 8 rounded sun rays
    val rayCount = 8
    val rayInner = radius * 1.22f
    val rayOuter = radius * 1.56f
    val rayStroke = (radius * 0.24f).coerceAtLeast(2f)
    for (i in 0 until rayCount) {
        val angle = (i * (360.0 / rayCount)) * PI / 180.0
        val start = Offset(
            center.x + (cos(angle) * rayInner).toFloat(),
            center.y + (sin(angle) * rayInner).toFloat()
        )
        val end = Offset(
            center.x + (cos(angle) * rayOuter).toFloat(),
            center.y + (sin(angle) * rayOuter).toFloat()
        )
        drawLine(
            color = Color(0xFFFBBF24),
            start = start,
            end = end,
            strokeWidth = rayStroke,
            cap = StrokeCap.Round
        )
    }

    // Soft golden glow
    drawCircle(
        color = Color(0xFFFDE047).copy(alpha = 0.28f),
        radius = radius * 1.25f,
        center = center
    )

    // 3D shaded sun sphere
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFEF08A),
                Color(0xFFFACC15),
                Color(0xFFF59E0B)
            ),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.draw3dMoon(center: Offset, radius: Float) {
    drawCircle(
        color = Color(0xFFFDE68A).copy(alpha = 0.24f),
        radius = radius * 1.25f,
        center = center
    )
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFEF9C3), Color(0xFFFBBF24)),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        ),
        radius = radius,
        center = center
    )
}

private fun DrawScope.draw3dCloud(center: Offset, scale: Float, tintAlpha: Float = 1f) {
    val cw = scale * 0.64f
    val ch = scale * 0.26f
    val left = center.x - cw * 0.5f
    val top = center.y - ch * 0.4f

    val cloudBrush = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = tintAlpha),
            Color(0xFFE2E8F0).copy(alpha = tintAlpha),
            Color(0xFFCBD5E1).copy(alpha = tintAlpha)
        ),
        startY = top - ch * 0.6f,
        endY = top + ch
    )

    // Cloud base pill
    drawRoundRect(
        brush = cloudBrush,
        topLeft = Offset(left, top),
        size = Size(cw, ch),
        cornerRadius = CornerRadius(ch * 0.5f, ch * 0.5f)
    )
    // Left puff
    drawCircle(
        brush = cloudBrush,
        radius = ch * 0.55f,
        center = Offset(left + cw * 0.28f, top + ch * 0.15f)
    )
    // Main top puff
    drawCircle(
        brush = cloudBrush,
        radius = ch * 0.72f,
        center = Offset(left + cw * 0.54f, top - ch * 0.05f)
    )
    // Right puff
    drawCircle(
        brush = cloudBrush,
        radius = ch * 0.48f,
        center = Offset(left + cw * 0.76f, top + ch * 0.22f)
    )
}

private fun DrawScope.drawRainDrops(center: Offset, span: Float) {
    val dropOffsets = listOf(-span * 0.35f, 0f, span * 0.35f)
    for (dx in dropOffsets) {
        drawLine(
            color = Color(0xFF38BDF8),
            start = Offset(center.x + dx, center.y - span * 0.12f),
            end = Offset(center.x + dx - span * 0.10f, center.y + span * 0.16f),
            strokeWidth = (span * 0.10f).coerceAtLeast(2f),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun WeatherAtmosphericSkyBackground(
    isDay: Boolean,
    condition: WeatherConditionType
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sky_ambient")
    val sunPulse = infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(5500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_pulse"
    )
    val cloudDrift = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(34000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cloud_drift"
    )
    val particlePhase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )
    val raySway = infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ray_sway"
    )

    val skyColors = if (isDay) {
        listOf(
            Color(0xFF1F4B7A),
            Color(0xFF2E5F93),
            Color(0xFF4B7EB3),
            Color(0xFF6B9BCC)
        )
    } else {
        listOf(
            Color(0xFF0F2542),
            Color(0xFF1B3A63),
            Color(0xFF284F80),
            Color(0xFF386498)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val pulse = sunPulse.value
        val drift = cloudDrift.value
        val pPhase = particlePhase.value
        val sway = raySway.value

        // 1. Base Sky Gradient
        drawRect(
            brush = Brush.linearGradient(
                colors = skyColors,
                start = Offset(w * 0.8f, 0f),
                end = Offset(w * 0.2f, h)
            )
        )

        // 2. Top-Left Breathing Sunburst / Moonlight Glow + Gentle Diagonal Light Rays
        val sunOrigin = Offset(w * (0.17f + sway * 0.015f), -h * 0.06f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    (if (isDay) Color(0xFFFFFBEB) else Color(0xFFE0F2FE)).copy(alpha = (if (isDay) 0.52f else 0.30f) * pulse),
                    (if (isDay) Color(0xFFFEF08A) else Color(0xFF93C5FD)).copy(alpha = (if (isDay) 0.22f else 0.14f) * pulse),
                    Color(0xFFBAE6FD).copy(alpha = 0.10f),
                    Color.Transparent
                ),
                center = sunOrigin,
                radius = w * 0.54f * pulse
            ),
            radius = w * 0.54f * pulse,
            center = sunOrigin
        )

        // Soft volumetric sunbeams fanning gently
        for (i in 0 until 3) {
            val spread = 0.14f + i * 0.18f + sway * 0.025f
            val beamPath = Path().apply {
                moveTo(sunOrigin.x, sunOrigin.y)
                lineTo(w * (spread + 0.05f), h)
                lineTo(w * (spread + 0.19f), h)
                close()
            }
            drawPath(
                path = beamPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDay) 0.065f else 0.035f),
                        Color.Transparent
                    )
                )
            )
        }

        // 3. Smoothly Drifting Parallax Volumetric Clouds (wrapping seamlessly)
        val cloudSpecs = listOf(
            Triple(0.05f, 0.14f, 0.12f),
            Triple(0.24f, 0.11f, 0.15f),
            Triple(0.46f, 0.16f, 0.11f),
            Triple(0.70f, 0.12f, 0.14f),
            Triple(0.90f, 0.17f, 0.10f)
        )
        cloudSpecs.forEachIndexed { idx, (baseX, baseY, radFrac) ->
            val speed = if (idx % 2 == 0) 1f else 0.65f
            val wrappedX = ((baseX + drift * speed) % 1.32f) - 0.16f
            val bobY = sin((drift * 2f * PI + idx * 1.1).toDouble()).toFloat() * 0.012f
            val cx = w * wrappedX
            val cy = h * (baseY + bobY)
            val rad = w * radFrac
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isDay) 0.24f else 0.12f),
                        Color(0xFFE0F2FE).copy(alpha = if (isDay) 0.10f else 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = rad
                ),
                radius = rad,
                center = Offset(cx, cy)
            )
        }

        // 4. Condition-Aware Delicate Ambient Particles
        when (condition) {
            WeatherConditionType.RAIN, WeatherConditionType.DRIZZLE, WeatherConditionType.THUNDERSTORM -> {
                val dropCount = if (condition == WeatherConditionType.DRIZZLE) 18 else 28
                for (i in 0 until dropCount) {
                    val seedX = ((i * 0.37f) % 1f)
                    val seedY = ((i * 0.61f) % 1f)
                    val speedMul = 1.4f + (i % 3) * 0.45f
                    val py = ((seedY + pPhase * speedMul) % 1.12f) - 0.06f
                    val px = ((seedX - pPhase * 0.18f + 1f) % 1f)
                    val startPt = Offset(w * px, h * py)
                    val len = (if (condition == WeatherConditionType.DRIZZLE) 12.dp else 18.dp).toPx()
                    drawLine(
                        color = Color(0xFFE0F2FE).copy(alpha = 0.18f),
                        start = startPt,
                        end = Offset(startPt.x - len * 0.22f, startPt.y + len),
                        strokeWidth = 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
            WeatherConditionType.SNOW -> {
                for (i in 0 until 24) {
                    val seedX = ((i * 0.29f) % 1f)
                    val seedY = ((i * 0.53f) % 1f)
                    val py = ((seedY + pPhase * (0.7f + (i % 3) * 0.2f)) % 1.08f) - 0.04f
                    val swayX = sin((pPhase * 2f * PI + i * 0.8).toDouble()).toFloat() * 0.018f
                    drawCircle(
                        color = Color.White.copy(alpha = 0.32f),
                        radius = (1.6f + (i % 3) * 0.8f).dp.toPx(),
                        center = Offset(w * (seedX + swayX), h * py)
                    )
                }
            }
            else -> {
                if (!isDay) {
                    // Twinkling stars in upper night sky
                    for (i in 0 until 22) {
                        val sx = w * (((i * 0.31f + 0.07f) % 0.92f) + 0.04f)
                        val sy = h * (((i * 0.47f + 0.05f) % 0.45f) + 0.04f)
                        val twinkle = (0.35f + 0.65f * ((sin((pPhase * 4f * PI + i * 1.3).toDouble()).toFloat() + 1f) * 0.5f))
                        drawCircle(
                            color = Color.White.copy(alpha = 0.45f * twinkle),
                            radius = (1.0f + (i % 2) * 0.6f).dp.toPx(),
                            center = Offset(sx, sy)
                        )
                    }
                } else {
                    // Gentle sunlit atmospheric light motes floating softly
                    for (i in 0 until 14) {
                        val seedX = ((i * 0.33f + 0.08f) % 0.92f) + 0.04f
                        val seedY = ((i * 0.57f + 0.1f) % 0.84f) + 0.08f
                        val dx = sin((pPhase * 2f * PI + i * 0.9).toDouble()).toFloat() * 0.02f
                        val dy = cos((pPhase * 2f * PI + i * 1.2).toDouble()).toFloat() * 0.025f
                        val alphaFade = 0.12f + 0.14f * ((sin((pPhase * 2f * PI + i).toDouble()).toFloat() + 1f) * 0.5f)
                        drawCircle(
                            color = Color(0xFFFFFBEB).copy(alpha = alphaFade),
                            radius = (1.8f + (i % 3) * 0.9f).dp.toPx(),
                            center = Offset(w * (seedX + dx), h * (seedY + dy))
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 960, heightDp = 540, showBackground = true)
@Composable
private fun WeatherScreenPreview() {
    WeatherScreen(
        is24hFormat = true,
        appLanguage = "en",
        onBackToHome = {}
    )
}
