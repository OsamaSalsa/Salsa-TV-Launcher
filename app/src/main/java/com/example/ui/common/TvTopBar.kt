package com.example.ui.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvMidnightBackground
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class NetworkType {
    WIFI, WIFI_NO_INTERNET, ETHERNET, DISCONNECTED
}

@Composable
fun TvTopBar(
    is24hFormat: Boolean,
    showSeconds: Boolean = false,
    clockSize: String = "large",
    dateFormatStyle: String = "full",
    greetingMode: String = "poetic",
    customGreetingText: String = "",
    onOpenSettings: () -> Unit,
    onOpenIptv: () -> Unit,
    onOpenSearch: (() -> Unit)? = null,
    onOpenControlCenter: (() -> Unit)? = null,
    onOpenLiveUiSettings: (() -> Unit)? = null,
    onOpenWeather: (() -> Unit)? = null,
    onOpenEconomic: (() -> Unit)? = null,
    onOpenFootball: (() -> Unit)? = null,
    onOpenNews: (() -> Unit)? = null,
    isScrolled: Boolean = false,
    isEditingMode: Boolean = false,
    onToggleEditMode: (() -> Unit)? = null,
    onNonAppFocused: (() -> Unit)? = null,
    onRequestFocusDown: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    canFocusButtons: Boolean = true,
    glassOpacity: Int = 75,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTimeString by remember { mutableStateOf("") }
    var currentAmPmString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }
    var currentGreeting by remember { mutableStateOf("") }
    var networkType by remember { mutableStateOf(NetworkType.DISCONNECTED) }
    var networkName by remember { mutableStateOf("") }
    
    val prefs = remember(context) {
        (context.applicationContext as? com.example.AuraTvApp)?.preferences
            ?: com.example.data.local.LauncherPreferences(context)
    }
    val initialLang = remember { prefs.getCachedLanguage() }
    val lang by prefs.languageFlow.collectAsState(initial = initialLang)
    val headerControlMode by prefs.headerControlModeFlow.collectAsState(initial = "control_center")
    val isWeatherEnabled by prefs.isWeatherEnabledFlow.collectAsState(initial = true)
    val weatherData by com.example.data.weather.WeatherRepository.weatherState.collectAsState()

    LaunchedEffect(lang, isWeatherEnabled) {
        if (isWeatherEnabled) {
            com.example.data.weather.WeatherRepository.ensureInitialized(context, lang)
        }
    }

    // Live network callback
    DisposableEffect(context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        
        fun updateNetworkStatus() {
            val active = cm?.activeNetwork
            val caps = cm?.getNetworkCapabilities(active)
            
            val hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                             caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true

            networkType = when {
                caps == null -> NetworkType.DISCONNECTED
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    if (hasInternet) NetworkType.WIFI else NetworkType.WIFI_NO_INTERNET
                }
                else -> NetworkType.DISCONNECTED
            }

            // Try to get SSID or connection type name
            networkName = when (networkType) {
                NetworkType.WIFI, NetworkType.WIFI_NO_INTERNET -> {
                    "Wi-Fi"
                }
                NetworkType.ETHERNET -> "Ethernet"
                NetworkType.DISCONNECTED -> "Offline"
            }
        }

        updateNetworkStatus()
        
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(n: android.net.Network) { postOnMain { updateNetworkStatus() } }
            override fun onLost(n: android.net.Network) { postOnMain { updateNetworkStatus() } }
            override fun onCapabilitiesChanged(n: android.net.Network, nc: NetworkCapabilities) { postOnMain { updateNetworkStatus() } }
        }
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
            
        try { cm?.registerNetworkCallback(request, callback) } catch (_: Exception) {}
        onDispose { try { cm?.unregisterNetworkCallback(callback) } catch (_: Exception) {} }
    }
    
    // Live clock and greeting update
    LaunchedEffect(is24hFormat, showSeconds, dateFormatStyle, greetingMode, customGreetingText, lang) {
        val timePattern = when {
            is24hFormat && showSeconds -> "HH:mm:ss"
            is24hFormat -> "HH:mm"
            showSeconds -> "hh:mm:ss"
            else -> "hh:mm"
        }
        val timeFormat = SimpleDateFormat(timePattern, Locale.US)
        
        val effectiveLang = com.example.util.LocaleHelper.getEffectiveLanguage(lang)
        var lastMinuteKey = -1
        var lastHourKey = -1

        while (true) {
            val now = java.util.Calendar.getInstance()
            val minuteKey = now.get(java.util.Calendar.DAY_OF_YEAR) * 1440 +
                now.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                now.get(java.util.Calendar.MINUTE)
            val hourOfDay = now.get(java.util.Calendar.HOUR_OF_DAY)

            if (showSeconds || minuteKey != lastMinuteKey) {
                val rawTime = timeFormat.format(now.time)
                // Explicitly force English digits (0-9) to prevent any system-level override
                currentTimeString = rawTime.map { ch ->
                    val digit = Character.getNumericValue(ch)
                    if (digit in 0..9 && ch !in '0'..'9' && ch !in 'a'..'z' && ch !in 'A'..'Z') {
                        digit.toString()[0]
                    } else ch
                }.joinToString("")
            }

            if (minuteKey != lastMinuteKey) {
                lastMinuteKey = minuteKey
                val hourKey = now.get(java.util.Calendar.DAY_OF_YEAR) * 24 + hourOfDay
                if (hourKey != lastHourKey) {
                    lastHourKey = hourKey
                    currentAmPmString = if (!is24hFormat) {
                        com.example.util.LocaleHelper.getAmPmString(hourOfDay, lang)
                    } else {
                        ""
                    }
                    currentDateString = com.example.util.LocaleHelper.formatDate(now.time, dateFormatStyle, lang)
                    currentGreeting = com.example.util.GreetingHelper.getGreeting(hourOfDay, effectiveLang, greetingMode, customGreetingText)
                }
            }

            delay(if (showSeconds) 500L else 1000L)
        }
    }

    val topBarBgAlpha by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isScrolled) 0.95f else 0.0f,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "top_bar_bg_alpha"
    )

    val baseTimeSize = when (clockSize) {
        "compact" -> 36f
        "medium" -> 48f
        else -> 60f
    }

    val timeFontSize by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isScrolled) 22f else baseTimeSize,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "time_font_size"
    )

    val dateFontSize by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isScrolled) 13f else 18f,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "date_font_size"
    )

    val baselinePadding by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isScrolled) 2.dp else 8.dp,
        animationSpec = androidx.compose.animation.core.tween(250),
        label = "baseline_padding"
    )

    val topTextShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.70f),
            blurRadius = 8f,
            offset = Offset(0f, 2f)
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090D1A).copy(alpha = if (isScrolled) 0.98f else 0.70f),
                        Color(0xFF090D1A).copy(alpha = if (isScrolled) topBarBgAlpha * 0.90f else 0.35f),
                        Color.Transparent
                    )
                )
            )
    ) {
        val barWidth = maxWidth
        val horizPad = when {
            barWidth < 860.dp -> 22.dp
            barWidth < 940.dp -> 34.dp
            else -> 44.dp
        }
        val actionButtonSize = when {
            barWidth < 840.dp -> 29.dp
            barWidth < 920.dp -> 30.dp
            else -> 32.dp
        }
        val actionIconSize = when {
            barWidth < 840.dp -> 13.5.dp
            barWidth < 920.dp -> 14.dp
            else -> 14.5.dp
        }
        val actionSpacing = when {
            barWidth < 840.dp -> 4.dp
            barWidth < 920.dp -> 5.dp
            else -> 5.5.dp
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizPad)
                .padding(top = if (isScrolled) 12.dp else 20.dp, bottom = if (isScrolled) 14.dp else 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Greeting at top in normal state; Time, Temperature (between Time & Date), Date, and Greeting beside Date when scrolled.
            // Weighted with weight(1f) so the right-side action buttons are ALWAYS measured first at full width and NEVER clip or disappear.
            val isArabicLang = com.example.util.LocaleHelper.isArabicOrRtlLanguage(lang)
            val isArabicGreeting = isArabicLang || com.example.util.LocaleHelper.containsArabicScript(currentGreeting)
            val isArabicDate = isArabicLang || com.example.util.LocaleHelper.containsArabicScript(currentDateString)
            val isArabicAmPm = isArabicLang || com.example.util.LocaleHelper.containsArabicScript(currentAmPmString)

            val greetingFont = if (isArabicGreeting) CairoFontFamily else FontFamily.Default
            val dateFont = if (isArabicDate) CairoFontFamily else FontFamily.Default
            val amPmFont = if (isArabicAmPm) CairoFontFamily else FontFamily.Default

            val hasDate = dateFormatStyle != "hidden" && currentDateString.isNotBlank()
            val hasGreeting = greetingMode != "hidden" && currentGreeting.isNotBlank()
            val hasAmPm = !is24hFormat && currentAmPmString.isNotBlank()
            val hasWeather = isWeatherEnabled

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                val leftAvailWidth = maxWidth
                val widthScale = when {
                    isScrolled -> 1f
                    leftAvailWidth < 340.dp -> 0.72f
                    leftAvailWidth < 400.dp -> 0.80f
                    leftAvailWidth < 450.dp -> 0.88f
                    else -> 1f
                }
                val effectiveTimeSp = (timeFontSize * widthScale).coerceAtLeast(if (isScrolled) 18f else 28f)
                val effectiveDateSp = (dateFontSize * (0.82f + 0.18f * widthScale)).coerceAtLeast(if (isScrolled) 11.5f else 13.5f)
                val effectiveWeatherIconDp = if (isScrolled) 15.dp else (20f * (0.80f + 0.20f * widthScale)).dp
                val effectiveBaselinePad = if (isScrolled) baselinePadding else (baselinePadding.value * widthScale).dp

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Normal state: Greeting is at its original position above the Time and Date
                    if (!isScrolled && hasGreeting) {
                        Text(
                            text = currentGreeting,
                            color = TvAccentGold,
                            fontSize = if (leftAvailWidth < 380.dp) 12.5.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = greetingFont,
                            letterSpacing = if (isArabicGreeting) 0.sp else 1.0.sp,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                fontFamily = greetingFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (leftAvailWidth < 380.dp) 12.5.sp else 14.sp,
                                letterSpacing = if (isArabicGreeting) 0.sp else 1.0.sp,
                                shadow = topTextShadow
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (isScrolled) 0.dp else 4.dp)
                    ) {
                        // Time: Force English (ASCII) Digits and Original System Font
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = currentTimeString,
                                color = TvTextPrimary,
                                fontSize = effectiveTimeSp.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Default,
                                letterSpacing = (-1.0).sp,
                                maxLines = 1,
                                softWrap = false,
                                style = TextStyle(
                                    fontFamily = FontFamily.Default,
                                    fontFeatureSettings = "tnum",
                                    shadow = topTextShadow
                                )
                            )
                        }

                        if (hasDate || hasAmPm || hasWeather) {
                            Spacer(modifier = Modifier.width(if (isScrolled) 8.dp else if (leftAvailWidth < 400.dp) 10.dp else 14.dp))

                            // AM/PM, Temperature (between Clock and Date), and Date aligned on the same baseline
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f, fill = false)
                                    .padding(bottom = effectiveBaselinePad)
                            ) {
                                if (hasAmPm) {
                                    Text(
                                        text = currentAmPmString,
                                        color = TvAccentGold,
                                        fontSize = effectiveDateSp.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = amPmFont,
                                        letterSpacing = 0.sp,
                                        maxLines = 1,
                                        softWrap = false,
                                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = amPmFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = effectiveDateSp.sp,
                                            letterSpacing = 0.sp,
                                            shadow = topTextShadow
                                        )
                                    )
                                }

                                if (hasAmPm && hasWeather) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "•",
                                        color = TvTextSecondary.copy(alpha = 0.6f),
                                        fontSize = effectiveDateSp.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = dateFont,
                                        style = TextStyle(
                                            fontFamily = dateFont,
                                            shadow = topTextShadow
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                }

                                // Temperature displayed strictly BETWEEN the Clock and the Date
                                if (hasWeather) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = if (onOpenWeather != null) {
                                            Modifier.clickable { onOpenWeather() }
                                        } else {
                                            Modifier
                                        }
                                    ) {
                                        com.example.ui.weather.WeatherConditionIcon(
                                            condition = weatherData.conditionType,
                                            modifier = Modifier.size(effectiveWeatherIconDp)
                                        )
                                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                            Text(
                                                text = "${weatherData.currentTempC}°C",
                                                color = TvTextPrimary,
                                                fontSize = effectiveDateSp.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Default,
                                                maxLines = 1,
                                                softWrap = false,
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Default,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = effectiveDateSp.sp,
                                                    shadow = topTextShadow
                                                )
                                            )
                                        }
                                    }
                                }

                                if ((hasWeather || hasAmPm) && hasDate) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "•",
                                        color = TvTextSecondary.copy(alpha = 0.6f),
                                        fontSize = effectiveDateSp.sp,
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = dateFont,
                                        style = TextStyle(
                                            fontFamily = dateFont,
                                            shadow = topTextShadow
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                if (hasDate) {
                                    // Date: Cairo Font in Arabic, ellipsizes gracefully if space is ever tight instead of pushing the right menu
                                    Text(
                                        text = currentDateString,
                                        color = TvTextPrimary.copy(alpha = 0.85f),
                                        fontSize = effectiveDateSp.sp,
                                        fontWeight = if (isArabicDate) FontWeight.Bold else FontWeight.Medium,
                                        fontFamily = dateFont,
                                        letterSpacing = 0.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = dateFont,
                                            fontWeight = if (isArabicDate) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = effectiveDateSp.sp,
                                            letterSpacing = 0.sp,
                                            shadow = topTextShadow
                                        ),
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                            }
                        }

                        // Scrolled state: Greeting appears beside/after the date in the same line
                        if (isScrolled && hasGreeting) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = currentGreeting,
                                color = TvAccentGold,
                                fontSize = effectiveDateSp.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = greetingFont,
                                letterSpacing = 0.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = greetingFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = effectiveDateSp.sp,
                                    letterSpacing = 0.sp,
                                    shadow = topTextShadow
                                ),
                                modifier = Modifier
                                    .weight(0.6f, fill = false)
                                    .padding(bottom = effectiveBaselinePad)
                            )
                        }
                    }
                }
            }

            // Right: Action Buttons — measured first so ALL icons stay 100% visible and circular
            val weatherLabel = remember(lang) { com.example.data.weather.WeatherRepository.getLocalizedStrings(lang).currentWeather }
            val economicLabel = remember(lang) { com.example.data.economic.EconomicRepository.getLocalizedStrings(lang).dashboardTitle }
            val footballLabel = remember(lang) { com.example.data.football.FootballRepository.getLocalizedStrings(lang).footballCenterTitle }
            val newsLabel = remember(lang) { com.example.data.news.NewsRepository.getLocalizedStrings(lang).newsCenterTitle }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(actionSpacing)
            ) {
                if (onOpenWeather != null) {
                    TvActionButton(
                        icon = Icons.Default.WbSunny,
                        text = "",
                        contentDescription = weatherLabel,
                        onClick = onOpenWeather,
                        onFocused = onNonAppFocused,
                        onRequestFocusDown = onRequestFocusDown,
                        canFocus = canFocusButtons,
                        glassOpacity = glassOpacity,
                        showText = false,
                        buttonSize = actionButtonSize,
                        iconSize = actionIconSize
                    )
                }

                if (onOpenEconomic != null) {
                    TvActionButton(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        text = "",
                        contentDescription = economicLabel,
                        onClick = onOpenEconomic,
                        onFocused = onNonAppFocused,
                        onRequestFocusDown = onRequestFocusDown,
                        canFocus = canFocusButtons,
                        glassOpacity = glassOpacity,
                        showText = false,
                        buttonSize = actionButtonSize,
                        iconSize = actionIconSize
                    )
                }

                if (onOpenFootball != null) {
                    TvActionButton(
                        icon = Icons.Default.SportsSoccer,
                        text = "",
                        contentDescription = footballLabel,
                        onClick = onOpenFootball,
                        onFocused = onNonAppFocused,
                        onRequestFocusDown = onRequestFocusDown,
                        canFocus = canFocusButtons,
                        glassOpacity = glassOpacity,
                        showText = false,
                        buttonSize = actionButtonSize,
                        iconSize = actionIconSize
                    )
                }

                if (onOpenNews != null) {
                    TvActionButton(
                        icon = Icons.Default.Newspaper,
                        text = "",
                        contentDescription = newsLabel,
                        onClick = onOpenNews,
                        onFocused = onNonAppFocused,
                        onRequestFocusDown = onRequestFocusDown,
                        canFocus = canFocusButtons,
                        glassOpacity = glassOpacity,
                        showText = false,
                        buttonSize = actionButtonSize,
                        iconSize = actionIconSize
                    )
                }

                if (onOpenSearch != null) {
                    TvActionButton(
                        icon = Icons.Default.Search,
                        text = "",
                        contentDescription = stringResource(R.string.control_center_search),
                        onClick = onOpenSearch,
                        onFocused = onNonAppFocused,
                        onRequestFocusDown = onRequestFocusDown,
                        canFocus = canFocusButtons,
                        glassOpacity = glassOpacity,
                        showText = false,
                        buttonSize = actionButtonSize,
                        iconSize = actionIconSize
                    )
                }

                val netInfo = when (networkType) {
                    NetworkType.WIFI -> Icons.Default.Wifi to stringResource(R.string.network_connected_wifi)
                    NetworkType.WIFI_NO_INTERNET -> Icons.Default.WifiOff to stringResource(R.string.network_disconnected)
                    NetworkType.ETHERNET -> Icons.Default.SettingsEthernet to stringResource(R.string.network_connected_ethernet)
                    NetworkType.DISCONNECTED -> Icons.Default.WifiOff to stringResource(R.string.network_disconnected)
                }

                TvActionButton(
                    icon = netInfo.first,
                    text = "",
                    contentDescription = netInfo.second,
                    onClick = {
                        context.startActivity(android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
                    },
                    onFocused = onNonAppFocused,
                    onRequestFocusDown = onRequestFocusDown,
                    canFocus = canFocusButtons,
                    glassOpacity = glassOpacity,
                    showText = false,
                    buttonSize = actionButtonSize,
                    iconSize = actionIconSize
                )

                TvActionButton(
                    icon = Icons.Default.LiveTv,
                    text = "",
                    contentDescription = stringResource(R.string.iptv_title),
                    onClick = onOpenIptv,
                    onLongClick = onOpenSettings, // Long click on TV shows settings/menu
                    onFocused = onNonAppFocused,
                    onRequestFocusDown = onRequestFocusDown,
                    canFocus = canFocusButtons,
                    glassOpacity = glassOpacity,
                    showText = false,
                    buttonSize = actionButtonSize,
                    iconSize = actionIconSize
                )

                // Edit Button in Top Bar
                if (onToggleEditMode != null) {
                    TvActionButton(
                        icon = if (isEditingMode) Icons.Default.Check else Icons.Default.Edit,
                        text = "",
                        contentDescription = stringResource(if (isEditingMode) R.string.action_stop_editing else R.string.action_edit),
                        onClick = onToggleEditMode,
                        onFocused = onNonAppFocused,
                        onRequestFocusDown = onRequestFocusDown,
                        canFocus = canFocusButtons,
                        glassOpacity = glassOpacity,
                        showText = false,
                        buttonSize = actionButtonSize,
                        iconSize = actionIconSize
                    )
                }

                // Live UI Settings Button in Top Bar
                if (onOpenLiveUiSettings != null) {
                    TvActionButton(
                        icon = Icons.Default.AutoAwesome,
                        text = "",
                        contentDescription = stringResource(R.string.live_ui_settings_title),
                        onClick = onOpenLiveUiSettings,
                        onFocused = onNonAppFocused,
                        onRequestFocusDown = onRequestFocusDown,
                        canFocus = canFocusButtons,
                        glassOpacity = glassOpacity,
                        showText = false,
                        buttonSize = actionButtonSize,
                        iconSize = actionIconSize
                    )
                }

                when (headerControlMode) {
                    "direct_settings" -> {
                        // Skip Control Center -> Direct Settings Button on Home Screen
                        TvActionButton(
                            icon = Icons.Default.Settings,
                            text = "",
                            contentDescription = stringResource(R.string.settings_title),
                            onClick = onOpenSettings,
                            onLongClick = onOpenControlCenter ?: onOpenSettings,
                            onFocused = onNonAppFocused,
                            onRequestFocusDown = onRequestFocusDown,
                            focusRequester = focusRequester,
                            canFocus = canFocusButtons,
                            glassOpacity = glassOpacity,
                            showText = false,
                            buttonSize = actionButtonSize,
                            iconSize = actionIconSize
                        )
                    }
                    "both" -> {
                        // Show Both Together: Control Center + Direct Settings Icons
                        if (onOpenControlCenter != null) {
                            TvActionButton(
                                icon = Icons.Default.Tune,
                                text = "",
                                contentDescription = stringResource(R.string.control_center_title),
                                onClick = onOpenControlCenter,
                                onLongClick = onOpenSettings,
                                onFocused = onNonAppFocused,
                                onRequestFocusDown = onRequestFocusDown,
                                canFocus = canFocusButtons,
                                glassOpacity = glassOpacity,
                                showText = false,
                                buttonSize = actionButtonSize,
                                iconSize = actionIconSize
                            )
                        }
                        TvActionButton(
                            icon = Icons.Default.Settings,
                            text = "",
                            contentDescription = stringResource(R.string.settings_title),
                            onClick = onOpenSettings,
                            onLongClick = onOpenControlCenter ?: onOpenSettings,
                            onFocused = onNonAppFocused,
                            onRequestFocusDown = onRequestFocusDown,
                            focusRequester = focusRequester,
                            canFocus = canFocusButtons,
                            glassOpacity = glassOpacity,
                            showText = false,
                            buttonSize = actionButtonSize,
                            iconSize = actionIconSize
                        )
                    }
                    else -> {
                        // Default: Control Center Button (long-press opens full Settings)
                        TvActionButton(
                            icon = if (onOpenControlCenter != null) Icons.Default.Tune else Icons.Default.Settings,
                            text = "",
                            contentDescription = stringResource(if (onOpenControlCenter != null) R.string.control_center_title else R.string.settings_title),
                            onClick = onOpenControlCenter ?: onOpenSettings,
                            onLongClick = onOpenSettings,
                            onFocused = onNonAppFocused,
                            onRequestFocusDown = onRequestFocusDown,
                            focusRequester = focusRequester,
                            canFocus = canFocusButtons,
                            glassOpacity = glassOpacity,
                            showText = false,
                            buttonSize = actionButtonSize,
                            iconSize = actionIconSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TvActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null,
    onRequestFocusDown: (() -> Unit)? = null,
    focusRequester: FocusRequester? = null,
    canFocus: Boolean = true,
    glassOpacity: Int = 75,
    showText: Boolean = true,
    contentDescription: String? = null,
    buttonSize: androidx.compose.ui.unit.Dp = 32.dp,
    iconSize: androidx.compose.ui.unit.Dp = 14.5.dp
) {
    val isIconOnly = !showText || text.isBlank()
    TvFocusableCard(
        onClick = onClick,
        onLongClick = onLongClick,
        onFocused = onFocused,
        focusRequester = focusRequester ?: remember { FocusRequester() },
        canFocus = canFocus,
        shape = RoundedCornerShape(50),
        scaleUpRatio = 1.06f,
        useDefaultBorder = false,
        elevation = 0.dp,
        modifier = (if (isIconOnly) Modifier.size(buttonSize) else Modifier.height(buttonSize))
            .onKeyEvent { keyEvent ->
                val native = keyEvent.nativeKeyEvent
                if (native.action == android.view.KeyEvent.ACTION_DOWN) {
                    if (keyEvent.key == androidx.compose.ui.input.key.Key.DirectionDown ||
                        native.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN
                    ) {
                        if (onRequestFocusDown != null) {
                            onRequestFocusDown()
                            true
                        } else false
                    } else false
                } else false
            }
    ) { isFocused ->
        val pillBg = if (isFocused) {
            TvAccentGold
        } else {
            Color(0xFF0E1628).copy(alpha = 0.58f)
        }
        val contentColor = if (isFocused) {
            Color(0xFF090D1A)
        } else {
            Color.White
        }

        if (isIconOnly) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(pillBg, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(pillBg, RoundedCornerShape(50))
                    .padding(horizontal = 11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = text,
                    color = contentColor,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
