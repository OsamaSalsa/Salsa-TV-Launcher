package com.example.data.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.roundToInt

enum class WeatherLocationSource {
    GPS,
    IP,
    TIMEZONE,
    CACHE
}

enum class WeatherConditionType {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY_DAY,
    PARTLY_CLOUDY_NIGHT,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

data class HourlyTempPoint(
    val timeLabel: String,
    val tempC: Int,
    val condition: WeatherConditionType
)

data class DailyForecastItem(
    val dateMillis: Long,
    val dayLabel: String,
    val condition: WeatherConditionType,
    val maxTempC: Int,
    val minTempC: Int
)

data class WeatherData(
    val cityName: String,
    val rawEnglishCity: String,
    val latitude: Double,
    val longitude: Double,
    val currentTempC: Int,
    val feelsLikeC: Int,
    val weatherCode: Int,
    val isDay: Boolean,
    val conditionType: WeatherConditionType,
    val humidityPercent: Int,
    val windSpeedKmh: Int,
    val windDirectionDeg: Int,
    val pressureHpa: Int,
    val uvIndex: Int,
    val hourlyPoints: List<HourlyTempPoint>,
    val high24hC: Int,
    val low24hC: Int,
    val high24hIndex: Int,
    val low24hIndex: Int,
    val dailyForecasts: List<DailyForecastItem>,
    val lastUpdatedTime: String,
    val fetchedAtMillis: Long,
    val languageCode: String,
    val isLoading: Boolean = false,
    val locationSource: WeatherLocationSource = WeatherLocationSource.CACHE
)

data class WeatherLocalizedStrings(
    val currentWeather: String,
    val feelsLikePrefix: String,
    val hourlyTitle: String,
    val humidity: String,
    val wind: String,
    val kmhUnit: String,
    val pressure: String,
    val hpaUnit: String,
    val uvIndex: String,
    val lastUpdatedPrefix: String,
    val liveBadge: String,
    val highPrefix: String,
    val lowPrefix: String,
    val liveSettingTitle: String,
    val showOption: String,
    val hideOption: String,
    val openWeatherPageOption: String,
    val refreshLocationOption: String
)

object WeatherRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fetchMutex = Mutex()

    private val _weatherState = MutableStateFlow(createDefaultWeather("en"))
    val weatherState: StateFlow<WeatherData> = _weatherState.asStateFlow()

    @Volatile
    private var lastDetectedLat: Double? = null
    @Volatile
    private var lastDetectedLon: Double? = null
    @Volatile
    private var lastDetectedRawCity: String? = null
    @Volatile
    private var lastDetectedSource: WeatherLocationSource? = null
    @Volatile
    private var isInitialized = false

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun ensureInitialized(context: Context, appLanguage: String) {
        val effectiveLang = LocaleHelper.getEffectiveLanguage(appLanguage)
        if (!isInitialized) {
            isInitialized = true
            loadCachedWeather(context, effectiveLang)
            refreshWeatherAsync(context.applicationContext, effectiveLang, forceLocation = false)
        } else {
            val current = _weatherState.value
            val ageMs = System.currentTimeMillis() - current.fetchedAtMillis
            val canUpgradeToGps = hasLocationPermission(context) &&
                current.locationSource != WeatherLocationSource.GPS
            if (current.languageCode != effectiveLang) {
                // Re-localize immediately and refresh city name in background
                _weatherState.value = relocalizeWeatherData(current, effectiveLang)
                refreshWeatherAsync(context.applicationContext, effectiveLang, forceLocation = canUpgradeToGps)
            } else if (canUpgradeToGps) {
                refreshWeatherAsync(context.applicationContext, effectiveLang, forceLocation = true)
            } else if (ageMs > 15 * 60 * 1000L) {
                refreshWeatherAsync(context.applicationContext, effectiveLang, forceLocation = false)
            }
        }
    }

    fun refreshWeatherAsync(
        context: Context,
        appLanguage: String,
        forceLocation: Boolean = false
    ) {
        val appContext = context.applicationContext
        val effectiveLang = LocaleHelper.getEffectiveLanguage(appLanguage)
        scope.launch {
            fetchMutex.withLock {
                try {
                    _weatherState.value = _weatherState.value.copy(isLoading = true)
                    if (forceLocation) {
                        lastDetectedLat = null
                        lastDetectedLon = null
                        lastDetectedRawCity = null
                        lastDetectedSource = null
                    }
                    val location = detectLocation(appContext, effectiveLang, forceLocation)
                    val fresh = fetchOpenMeteoWeather(
                        lat = location.lat,
                        lon = location.lon,
                        cityName = location.localizedCity,
                        rawCity = location.rawCity,
                        effectiveLang = effectiveLang,
                        locationSource = location.source
                    )
                    if (fresh != null) {
                        _weatherState.value = fresh
                        saveCachedWeather(appContext, fresh)
                    } else {
                        val relocalized = relocalizeWeatherData(_weatherState.value, effectiveLang).copy(
                            cityName = location.localizedCity,
                            rawEnglishCity = location.rawCity,
                            locationSource = location.source,
                            isLoading = false
                        )
                        _weatherState.value = relocalized
                    }
                } catch (_: Exception) {
                    _weatherState.value = _weatherState.value.copy(isLoading = false)
                }
            }
        }
    }

    private data class DetectedLocation(
        val lat: Double,
        val lon: Double,
        val rawCity: String,
        val localizedCity: String,
        val source: WeatherLocationSource
    )

    private suspend fun detectLocation(
        context: Context,
        effectiveLang: String,
        forceFreshGps: Boolean
    ): DetectedLocation = withContext(Dispatchers.IO) {
        val hasPerm = hasLocationPermission(context)
        val cachedLat = lastDetectedLat
        val cachedLon = lastDetectedLon
        val cachedRawCity = lastDetectedRawCity
        val cachedSource = lastDetectedSource
        if (
            !forceFreshGps &&
            cachedLat != null &&
            cachedLon != null &&
            !cachedRawCity.isNullOrBlank() &&
            cachedSource != null &&
            !(hasPerm && cachedSource != WeatherLocationSource.GPS)
        ) {
            val localized = if (cachedSource == WeatherLocationSource.GPS) {
                reverseGeocodeCity(context, cachedLat, cachedLon, effectiveLang)?.first
                    ?: resolveLocalizedCityName(cachedRawCity, cachedLat, cachedLon, effectiveLang)
            } else {
                resolveLocalizedCityName(cachedRawCity, cachedLat, cachedLon, effectiveLang)
            }
            return@withContext DetectedLocation(cachedLat, cachedLon, cachedRawCity, localized, cachedSource)
        }

        // 0. Primary: Device GPS / LocationManager (when location permission is granted)
        if (hasPerm) {
            val gpsLocation = getDeviceGpsLocation(context, forceFresh = forceFreshGps)
            if (gpsLocation != null) {
                val lat = gpsLocation.latitude
                val lon = gpsLocation.longitude
                val geocoded = reverseGeocodeCity(context, lat, lon, effectiveLang)
                val rawCity = geocoded?.second?.takeIf { it.isNotBlank() }
                    ?: geocoded?.first?.takeIf { it.isNotBlank() }
                    ?: String.format(Locale.US, "%.2f, %.2f", lat, lon)
                val localizedCity = geocoded?.first?.takeIf { it.isNotBlank() }
                    ?: resolveLocalizedCityName(rawCity, lat, lon, effectiveLang)
                lastDetectedLat = lat
                lastDetectedLon = lon
                lastDetectedRawCity = rawCity
                lastDetectedSource = WeatherLocationSource.GPS
                return@withContext DetectedLocation(
                    lat = lat,
                    lon = lon,
                    rawCity = rawCity,
                    localizedCity = LocaleHelper.sanitizeForCairoFont(localizedCity),
                    source = WeatherLocationSource.GPS
                )
            }
        }

        // 1. Fallback: ipwho.is
        try {
            val ipWhoLang = when (effectiveLang) {
                "ar", "en", "de", "es", "fr", "ja", "pt", "ru", "zh" -> effectiveLang
                else -> "en"
            }
            val jsonStr = httpGet("https://ipwho.is/?lang=$ipWhoLang", timeoutMs = 3800)
            if (!jsonStr.isNullOrBlank()) {
                val obj = JSONObject(jsonStr)
                if (obj.optBoolean("success", true)) {
                    val lat = obj.optDouble("latitude", Double.NaN)
                    val lon = obj.optDouble("longitude", Double.NaN)
                    val city = obj.optString("city", "").trim()
                    if (!lat.isNaN() && !lon.isNaN() && city.isNotBlank()) {
                        lastDetectedLat = lat
                        lastDetectedLon = lon
                        lastDetectedRawCity = city
                        lastDetectedSource = WeatherLocationSource.IP
                        val localized = resolveLocalizedCityName(city, lat, lon, effectiveLang)
                        return@withContext DetectedLocation(lat, lon, city, localized, WeatherLocationSource.IP)
                    }
                }
            }
        } catch (_: Exception) {}

        // 2. Fallback: ip-api.com
        try {
            val ipApiLang = when (effectiveLang) {
                "de", "es", "pt", "fr", "ja", "zh", "ru" -> effectiveLang
                else -> "en"
            }
            val jsonStr = httpGet("http://ip-api.com/json/?fields=status,city,lat,lon&lang=$ipApiLang", timeoutMs = 3500)
            if (!jsonStr.isNullOrBlank()) {
                val obj = JSONObject(jsonStr)
                if (obj.optString("status") == "success") {
                    val lat = obj.optDouble("lat", Double.NaN)
                    val lon = obj.optDouble("lon", Double.NaN)
                    val city = obj.optString("city", "").trim()
                    if (!lat.isNaN() && !lon.isNaN() && city.isNotBlank()) {
                        lastDetectedLat = lat
                        lastDetectedLon = lon
                        lastDetectedRawCity = city
                        lastDetectedSource = WeatherLocationSource.IP
                        val localized = resolveLocalizedCityName(city, lat, lon, effectiveLang)
                        return@withContext DetectedLocation(lat, lon, city, localized, WeatherLocationSource.IP)
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Fallback: freeipapi.com
        try {
            val jsonStr = httpGet("https://freeipapi.com/api/json", timeoutMs = 3500)
            if (!jsonStr.isNullOrBlank()) {
                val obj = JSONObject(jsonStr)
                val lat = obj.optDouble("latitude", Double.NaN)
                val lon = obj.optDouble("longitude", Double.NaN)
                val city = obj.optString("cityName", "").trim()
                if (!lat.isNaN() && !lon.isNaN() && city.isNotBlank() && city != "-") {
                    lastDetectedLat = lat
                    lastDetectedLon = lon
                    lastDetectedRawCity = city
                    lastDetectedSource = WeatherLocationSource.IP
                    val localized = resolveLocalizedCityName(city, lat, lon, effectiveLang)
                    return@withContext DetectedLocation(lat, lon, city, localized, WeatherLocationSource.IP)
                }
            }
        } catch (_: Exception) {}

        // 4. Final fallback: device TimeZone
        val tzFallback = getFallbackLocationFromTimeZone()
        val localized = resolveLocalizedCityName(tzFallback.first, tzFallback.second, tzFallback.third, effectiveLang)
        DetectedLocation(
            lat = tzFallback.second,
            lon = tzFallback.third,
            rawCity = tzFallback.first,
            localizedCity = localized,
            source = WeatherLocationSource.TIMEZONE
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun getDeviceGpsLocation(
        context: Context,
        forceFresh: Boolean
    ): Location? {
        if (!hasLocationPermission(context)) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val candidateProviders = buildList {
            add(LocationManager.GPS_PROVIDER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(LocationManager.FUSED_PROVIDER)
            }
            add(LocationManager.NETWORK_PROVIDER)
            add(LocationManager.PASSIVE_PROVIDER)
        }

        // Gather best last-known fix across providers
        var bestLastKnown: Location? = null
        for (provider in candidateProviders) {
            try {
                val loc = lm.getLastKnownLocation(provider)
                if (loc != null && isValidCoordinate(loc.latitude, loc.longitude)) {
                    val prev = bestLastKnown
                    if (prev == null ||
                        loc.time > prev.time + 2 * 60 * 1000L ||
                        (abs(loc.time - prev.time) <= 2 * 60 * 1000L && loc.accuracy < prev.accuracy)
                    ) {
                        bestLastKnown = loc
                    }
                }
            } catch (_: Exception) {}
        }

        val now = System.currentTimeMillis()
        val isLastKnownFresh = bestLastKnown != null && (now - bestLastKnown.time) < 10 * 60 * 1000L
        if (!forceFresh && isLastKnownFresh) {
            return bestLastKnown
        }

        // Request a fresh single fix from active providers (GPS first, then FUSED/NETWORK)
        val activeProviders = buildList {
            if (isProviderEnabledSafe(lm, LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isProviderEnabledSafe(lm, LocationManager.FUSED_PROVIDER)) {
                add(LocationManager.FUSED_PROVIDER)
            }
            if (isProviderEnabledSafe(lm, LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
        }

        for (provider in activeProviders) {
            val fresh = requestSingleFix(context, lm, provider, timeoutMs = if (provider == LocationManager.GPS_PROVIDER) 4500L else 3000L)
            if (fresh != null && isValidCoordinate(fresh.latitude, fresh.longitude)) {
                return fresh
            }
        }

        return bestLastKnown
    }

    private fun isProviderEnabledSafe(lm: LocationManager, provider: String): Boolean {
        return try {
            lm.allProviders.contains(provider) && lm.isProviderEnabled(provider)
        } catch (_: Exception) {
            false
        }
    }

    private fun isValidCoordinate(lat: Double, lon: Double): Boolean {
        return !lat.isNaN() && !lon.isNaN() &&
            lat in -90.0..90.0 && lon in -180.0..180.0 &&
            !(abs(lat) < 0.0001 && abs(lon) < 0.0001)
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleFix(
        context: Context,
        lm: LocationManager,
        provider: String,
        timeoutMs: Long
    ): Location? = withTimeoutOrNull(timeoutMs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            suspendCancellableCoroutine { cont ->
                val cancelSignal = CancellationSignal()
                cont.invokeOnCancellation { cancelSignal.cancel() }
                try {
                    lm.getCurrentLocation(
                        provider,
                        cancelSignal,
                        context.mainExecutor
                    ) { location ->
                        if (cont.isActive) cont.resume(location)
                    }
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        } else {
            suspendCancellableCoroutine { cont ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try {
                            lm.removeUpdates(this)
                        } catch (_: Exception) {}
                        if (cont.isActive) cont.resume(location)
                    }
                }
                cont.invokeOnCancellation {
                    try {
                        lm.removeUpdates(listener)
                    } catch (_: Exception) {}
                }
                try {
                    lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    /**
     * Reverse-geocodes GPS coordinates (lat, lon) into Pair(localizedCityName, englishRawCityName).
     * Tries Android Geocoder first, then BigDataCloud free reverse geocoding API, then OpenStreetMap Nominatim.
     */
    private suspend fun reverseGeocodeCity(
        context: Context,
        lat: Double,
        lon: Double,
        effectiveLang: String
    ): Pair<String, String>? {
        // 1. Android built-in Geocoder (fast, on-device / Play Services backed)
        if (Geocoder.isPresent()) {
            try {
                val locale = LocaleHelper.getLocale(effectiveLang)
                val addresses = fetchGeocoderAddresses(context, locale, lat, lon)
                val localized = extractCityFromAddresses(addresses)
                if (!localized.isNullOrBlank()) {
                    val englishName = if (effectiveLang == "en") {
                        localized
                    } else {
                        extractCityFromAddresses(fetchGeocoderAddresses(context, Locale.US, lat, lon)) ?: localized
                    }
                    return LocaleHelper.sanitizeForCairoFont(localized) to englishName
                }
            } catch (_: Exception) {}
        }

        // 2. BigDataCloud free reverse geocoding API (no key required, fast, multilingual incl. Arabic)
        try {
            val url = String.format(
                Locale.US,
                "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=%.5f&longitude=%.5f&localityLanguage=%s",
                lat,
                lon,
                effectiveLang
            )
            val jsonStr = httpGet(url, timeoutMs = 3500)
            if (!jsonStr.isNullOrBlank()) {
                val obj = JSONObject(jsonStr)
                val city = obj.optString("city", "").trim()
                    .ifBlank { obj.optString("locality", "").trim() }
                    .ifBlank { obj.optString("principalSubdivision", "").trim() }
                if (city.isNotBlank()) {
                    val localized = translateKnownCityOrNull(city, effectiveLang) ?: city
                    return LocaleHelper.sanitizeForCairoFont(localized) to city
                }
            }
        } catch (_: Exception) {}

        // 3. OpenStreetMap Nominatim reverse geocoding fallback
        try {
            val url = String.format(
                Locale.US,
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%.5f&lon=%.5f&accept-language=%s&zoom=10",
                lat,
                lon,
                effectiveLang
            )
            val jsonStr = httpGet(url, timeoutMs = 3500)
            if (!jsonStr.isNullOrBlank()) {
                val root = JSONObject(jsonStr)
                val addr = root.optJSONObject("address")
                val city = addr?.optString("city", "")?.trim().orEmpty()
                    .ifBlank { addr?.optString("town", "")?.trim().orEmpty() }
                    .ifBlank { addr?.optString("municipality", "")?.trim().orEmpty() }
                    .ifBlank { addr?.optString("village", "")?.trim().orEmpty() }
                    .ifBlank { addr?.optString("state", "")?.trim().orEmpty() }
                if (city.isNotBlank()) {
                    val localized = translateKnownCityOrNull(city, effectiveLang) ?: city
                    return LocaleHelper.sanitizeForCairoFont(localized) to city
                }
            }
        } catch (_: Exception) {}

        return null
    }

    private suspend fun fetchGeocoderAddresses(
        context: Context,
        locale: Locale,
        lat: Double,
        lon: Double
    ): List<Address> = withTimeoutOrNull(2500L) {
        val geocoder = Geocoder(context, locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                try {
                    geocoder.getFromLocation(lat, lon, 3) { results ->
                        if (cont.isActive) cont.resume(results)
                    }
                } catch (_: Exception) {
                    if (cont.isActive) cont.resume(emptyList())
                }
            }
        } else {
            withContext(Dispatchers.IO) {
                try {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(lat, lon, 3).orEmpty()
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }
    }.orEmpty()

    private fun extractCityFromAddresses(addresses: List<Address>): String? {
        for (addr in addresses) {
            val candidate = addr.locality?.trim().orEmpty()
                .ifBlank { addr.subAdminArea?.trim().orEmpty() }
                .ifBlank { addr.adminArea?.trim().orEmpty() }
            if (candidate.isNotBlank()) return candidate
        }
        return null
    }

    private fun resolveLocalizedCityName(
        rawCity: String,
        lat: Double,
        lon: Double,
        effectiveLang: String
    ): String {
        val clean = rawCity.trim()
        if (clean.isBlank()) return translateKnownCity("Riyadh", effectiveLang)

        // Check built-in dictionary first for instant accurate translation
        val known = translateKnownCityOrNull(clean, effectiveLang)
        if (known != null) return known

        // Query Open-Meteo multilingual geocoding API for the localized city name
        try {
            val encoded = URLEncoder.encode(clean, "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=5&language=$effectiveLang&format=json"
            val jsonStr = httpGet(url, timeoutMs = 3000)
            if (!jsonStr.isNullOrBlank()) {
                val obj = JSONObject(jsonStr)
                val results = obj.optJSONArray("results")
                if (results != null && results.length() > 0) {
                    var bestName = results.getJSONObject(0).optString("name", clean)
                    var bestDist = Double.MAX_VALUE
                    for (i in 0 until results.length()) {
                        val item = results.getJSONObject(i)
                        val rLat = item.optDouble("latitude", Double.NaN)
                        val rLon = item.optDouble("longitude", Double.NaN)
                        val rName = item.optString("name", "")
                        if (!rLat.isNaN() && !rLon.isNaN() && rName.isNotBlank()) {
                            val dist = abs(rLat - lat) + abs(rLon - lon)
                            if (dist < bestDist) {
                                bestDist = dist
                                bestName = rName
                            }
                        }
                    }
                    if (bestName.isNotBlank()) {
                        return LocaleHelper.sanitizeForCairoFont(bestName)
                    }
                }
            }
        } catch (_: Exception) {}

        return LocaleHelper.sanitizeForCairoFont(clean)
    }

    private fun fetchOpenMeteoWeather(
        lat: Double,
        lon: Double,
        cityName: String,
        rawCity: String,
        effectiveLang: String,
        locationSource: WeatherLocationSource
    ): WeatherData? {
        val url = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$lat&longitude=$lon" +
            "&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,uv_index" +
            "&hourly=temperature_2m,weather_code,uv_index" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,uv_index_max" +
            "&timezone=auto&forecast_days=6"

        val jsonStr = httpGet(url, timeoutMs = 5000) ?: return null
        val root = JSONObject(jsonStr)
        val current = root.optJSONObject("current") ?: return null

        val currentTempC = current.optDouble("temperature_2m", 27.0).roundToInt()
        val feelsLikeC = current.optDouble("apparent_temperature", currentTempC.toDouble()).roundToInt()
        val humidity = current.optInt("relative_humidity_2m", 38).coerceIn(0, 100)
        val windSpeed = current.optDouble("wind_speed_10m", 14.0).roundToInt()
        val windDir = current.optInt("wind_direction_10m", 45)
        val pressure = current.optDouble("surface_pressure", 1014.0).roundToInt()
        val weatherCode = current.optInt("weather_code", 2)
        val isDay = current.optInt("is_day", 1) == 1
        var uvIndex = current.optDouble("uv_index", Double.NaN).let {
            if (it.isNaN()) -1 else it.roundToInt()
        }

        // Parse hourly next 24h (8 points spaced by 3 hours)
        val hourlyObj = root.optJSONObject("hourly")
        val hourlyTimes = hourlyObj?.optJSONArray("time")
        val hourlyTemps = hourlyObj?.optJSONArray("temperature_2m")
        val hourlyCodes = hourlyObj?.optJSONArray("weather_code")
        val hourlyUvs = hourlyObj?.optJSONArray("uv_index")

        val nowCal = Calendar.getInstance()
        val currentHour = nowCal.get(Calendar.HOUR_OF_DAY)
        val hourlyPoints = mutableListOf<HourlyTempPoint>()

        if (hourlyTimes != null && hourlyTemps != null && hourlyTimes.length() >= 24) {
            // Find starting index matching current hour
            var startIndex = 0
            val hourPrefix = String.format(Locale.US, "T%02d:", currentHour)
            for (i in 0 until minOf(26, hourlyTimes.length())) {
                if (hourlyTimes.optString(i, "").contains(hourPrefix)) {
                    startIndex = i
                    break
                }
            }
            if (uvIndex < 0 && hourlyUvs != null && startIndex < hourlyUvs.length()) {
                uvIndex = hourlyUvs.optDouble(startIndex, 5.0).roundToInt()
            }
            for (step in 0 until 8) {
                val idx = (startIndex + step * 3).coerceAtMost(hourlyTimes.length() - 1)
                val rawTime = hourlyTimes.optString(idx, "")
                val timeLabel = rawTime.substringAfter("T", "14:00").take(5).ifBlank {
                    String.format(Locale.US, "%02d:00", (currentHour + step * 3) % 24)
                }
                val t = hourlyTemps.optDouble(idx, currentTempC.toDouble()).roundToInt()
                val c = hourlyCodes?.optInt(idx, weatherCode) ?: weatherCode
                val hourInt = timeLabel.substringBefore(":").toIntOrNull() ?: 12
                val pointIsDay = hourInt in 6..18
                hourlyPoints.add(
                    HourlyTempPoint(
                        timeLabel = timeLabel,
                        tempC = t,
                        condition = mapWmoCodeToCondition(c, pointIsDay)
                    )
                )
            }
        }

        if (hourlyPoints.size < 8) {
            hourlyPoints.clear()
            val offsets = listOf(0, 2, 3, 4, -2, -6, -4, -1)
            for (step in 0 until 8) {
                val h = (currentHour + step * 3) % 24
                hourlyPoints.add(
                    HourlyTempPoint(
                        timeLabel = String.format(Locale.US, "%02d:00", h),
                        tempC = currentTempC + offsets[step],
                        condition = mapWmoCodeToCondition(weatherCode, h in 6..18)
                    )
                )
            }
        }

        if (uvIndex < 0) {
            uvIndex = if (isDay) 5 else 1
        }

        var highIdx = 0
        var lowIdx = 0
        for (i in hourlyPoints.indices) {
            if (hourlyPoints[i].tempC > hourlyPoints[highIdx].tempC) highIdx = i
            if (hourlyPoints[i].tempC < hourlyPoints[lowIdx].tempC) lowIdx = i
        }

        // Parse 5-day forecast
        val dailyObj = root.optJSONObject("daily")
        val dailyTimes = dailyObj?.optJSONArray("time")
        val dailyCodes = dailyObj?.optJSONArray("weather_code")
        val dailyMax = dailyObj?.optJSONArray("temperature_2m_max")
        val dailyMin = dailyObj?.optJSONArray("temperature_2m_min")

        val dailyForecasts = mutableListOf<DailyForecastItem>()
        val sdfParse = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        if (dailyTimes != null && dailyMax != null && dailyMin != null && dailyTimes.length() >= 2) {
            val startDay = if (dailyTimes.length() >= 6) 1 else 0
            val count = minOf(5, dailyTimes.length() - startDay)
            for (i in 0 until count) {
                val idx = startDay + i
                val dateStr = dailyTimes.optString(idx, "")
                val dateMillis = try {
                    sdfParse.parse(dateStr)?.time ?: (System.currentTimeMillis() + (i + 1) * 86400_000L)
                } catch (_: Exception) {
                    System.currentTimeMillis() + (i + 1) * 86400_000L
                }
                val code = dailyCodes?.optInt(idx, weatherCode) ?: weatherCode
                val maxT = dailyMax.optDouble(idx, (currentTempC + 3).toDouble()).roundToInt()
                val minT = dailyMin.optDouble(idx, (currentTempC - 8).toDouble()).roundToInt()
                dailyForecasts.add(
                    DailyForecastItem(
                        dateMillis = dateMillis,
                        dayLabel = formatDayAndDate(dateMillis, effectiveLang),
                        condition = mapWmoCodeToCondition(code, isDay = true),
                        maxTempC = maxT,
                        minTempC = minT
                    )
                )
            }
        }

        if (dailyForecasts.size < 5) {
            dailyForecasts.clear()
            for (i in 1..5) {
                val dm = System.currentTimeMillis() + i * 86400_000L
                dailyForecasts.add(
                    DailyForecastItem(
                        dateMillis = dm,
                        dayLabel = formatDayAndDate(dm, effectiveLang),
                        condition = if (i % 2 == 0) WeatherConditionType.PARTLY_CLOUDY_DAY else WeatherConditionType.CLEAR_DAY,
                        maxTempC = currentTempC + 4,
                        minTempC = currentTempC - 8
                    )
                )
            }
        }

        val updatedTime = SimpleDateFormat("HH:mm", Locale.US).format(Date())

        return WeatherData(
            cityName = cityName,
            rawEnglishCity = rawCity,
            latitude = lat,
            longitude = lon,
            currentTempC = currentTempC,
            feelsLikeC = feelsLikeC,
            weatherCode = weatherCode,
            isDay = isDay,
            conditionType = mapWmoCodeToCondition(weatherCode, isDay),
            humidityPercent = humidity,
            windSpeedKmh = windSpeed,
            windDirectionDeg = windDir,
            pressureHpa = pressure,
            uvIndex = uvIndex.coerceIn(0, 12),
            hourlyPoints = hourlyPoints,
            high24hC = hourlyPoints[highIdx].tempC,
            low24hC = hourlyPoints[lowIdx].tempC,
            high24hIndex = highIdx,
            low24hIndex = lowIdx,
            dailyForecasts = dailyForecasts,
            lastUpdatedTime = updatedTime,
            fetchedAtMillis = System.currentTimeMillis(),
            languageCode = effectiveLang,
            isLoading = false,
            locationSource = locationSource
        )
    }

    fun mapWmoCodeToCondition(code: Int, isDay: Boolean): WeatherConditionType {
        return when (code) {
            0, 1 -> if (isDay) WeatherConditionType.CLEAR_DAY else WeatherConditionType.CLEAR_NIGHT
            2 -> if (isDay) WeatherConditionType.PARTLY_CLOUDY_DAY else WeatherConditionType.PARTLY_CLOUDY_NIGHT
            3 -> WeatherConditionType.CLOUDY
            45, 48 -> WeatherConditionType.FOG
            51, 53, 55, 56, 57 -> WeatherConditionType.DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> WeatherConditionType.RAIN
            71, 73, 75, 77, 85, 86 -> WeatherConditionType.SNOW
            95, 96, 99 -> WeatherConditionType.THUNDERSTORM
            else -> if (isDay) WeatherConditionType.PARTLY_CLOUDY_DAY else WeatherConditionType.PARTLY_CLOUDY_NIGHT
        }
    }

    private fun relocalizeWeatherData(data: WeatherData, effectiveLang: String): WeatherData {
        val localizedCity = translateKnownCityOrNull(data.rawEnglishCity, effectiveLang)
            ?: translateKnownCityOrNull(data.cityName, effectiveLang)
            ?: data.cityName
        val updatedDaily = data.dailyForecasts.map { item ->
            item.copy(dayLabel = formatDayAndDate(item.dateMillis, effectiveLang))
        }
        return data.copy(
            cityName = localizedCity,
            dailyForecasts = updatedDaily,
            languageCode = effectiveLang
        )
    }

    fun formatDayAndDate(dateMillis: Long, effectiveLang: String): String {
        val locale = LocaleHelper.getLocale(effectiveLang)
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val dayNum = cal.get(Calendar.DAY_OF_MONTH)
        val dayName = SimpleDateFormat("EEE", locale).format(Date(dateMillis))
            .replace(".", "")
            .trim()
        val cleanDay = LocaleHelper.sanitizeForCairoFont(dayName)
        return "$cleanDay $dayNum"
    }

    fun getConditionLabel(condition: WeatherConditionType, languageCode: String): String {
        val lang = LocaleHelper.getEffectiveLanguage(languageCode)
        return when (lang) {
            "ar" -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "مشمس وصافٍ"
                WeatherConditionType.CLEAR_NIGHT -> "سماء صافية"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "غائم جزئياً"
                WeatherConditionType.CLOUDY -> "غائم"
                WeatherConditionType.FOG -> "ضبابي"
                WeatherConditionType.DRIZZLE -> "رذاذ خفيف"
                WeatherConditionType.RAIN -> "ممطر"
                WeatherConditionType.SNOW -> "ثلوج"
                WeatherConditionType.THUNDERSTORM -> "عاصفة رعدية"
            }
            "fr" -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "Ensoleillé"
                WeatherConditionType.CLEAR_NIGHT -> "Ciel dégagé"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "Partiellement nuageux"
                WeatherConditionType.CLOUDY -> "Nuageux"
                WeatherConditionType.FOG -> "Brouillard"
                WeatherConditionType.DRIZZLE -> "Bruine"
                WeatherConditionType.RAIN -> "Pluie"
                WeatherConditionType.SNOW -> "Neige"
                WeatherConditionType.THUNDERSTORM -> "Orage"
            }
            "de" -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "Sonnig"
                WeatherConditionType.CLEAR_NIGHT -> "Klar"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "Teilweise bewölkt"
                WeatherConditionType.CLOUDY -> "Bewölkt"
                WeatherConditionType.FOG -> "Nebel"
                WeatherConditionType.DRIZZLE -> "Nieselregen"
                WeatherConditionType.RAIN -> "Regen"
                WeatherConditionType.SNOW -> "Schnee"
                WeatherConditionType.THUNDERSTORM -> "Gewitter"
            }
            "tr" -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "Güneşli"
                WeatherConditionType.CLEAR_NIGHT -> "Açık"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "Parçalı Bulutlu"
                WeatherConditionType.CLOUDY -> "Bulutlu"
                WeatherConditionType.FOG -> "Sisli"
                WeatherConditionType.DRIZZLE -> "Çisenti"
                WeatherConditionType.RAIN -> "Yağmurlu"
                WeatherConditionType.SNOW -> "Karlı"
                WeatherConditionType.THUNDERSTORM -> "Fırtınalı"
            }
            "es" -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "Soleado"
                WeatherConditionType.CLEAR_NIGHT -> "Despejado"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "Parcialmente nublado"
                WeatherConditionType.CLOUDY -> "Nublado"
                WeatherConditionType.FOG -> "Niebla"
                WeatherConditionType.DRIZZLE -> "Llovizna"
                WeatherConditionType.RAIN -> "Lluvia"
                WeatherConditionType.SNOW -> "Nieve"
                WeatherConditionType.THUNDERSTORM -> "Tormenta"
            }
            "ru" -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "Солнечно"
                WeatherConditionType.CLEAR_NIGHT -> "Ясно"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "Переменная облачность"
                WeatherConditionType.CLOUDY -> "Облачно"
                WeatherConditionType.FOG -> "Туман"
                WeatherConditionType.DRIZZLE -> "Морось"
                WeatherConditionType.RAIN -> "Дождь"
                WeatherConditionType.SNOW -> "Снег"
                WeatherConditionType.THUNDERSTORM -> "Гроза"
            }
            "fa" -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "آفتابی"
                WeatherConditionType.CLEAR_NIGHT -> "آسمان صاف"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "نیمه‌ابری"
                WeatherConditionType.CLOUDY -> "ابری"
                WeatherConditionType.FOG -> "مه‌آلود"
                WeatherConditionType.DRIZZLE -> "نم‌نم باران"
                WeatherConditionType.RAIN -> "بارانی"
                WeatherConditionType.SNOW -> "برفی"
                WeatherConditionType.THUNDERSTORM -> "رعد و برق"
            }
            "ur" -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "دھوپ"
                WeatherConditionType.CLEAR_NIGHT -> "صاف آسمان"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "جزوی ابر آلود"
                WeatherConditionType.CLOUDY -> "ابر آلود"
                WeatherConditionType.FOG -> "دھند"
                WeatherConditionType.DRIZZLE -> "بوندا باندی"
                WeatherConditionType.RAIN -> "بارش"
                WeatherConditionType.SNOW -> "برف باری"
                WeatherConditionType.THUNDERSTORM -> "گرج چمک"
            }
            else -> when (condition) {
                WeatherConditionType.CLEAR_DAY -> "Clear Sky"
                WeatherConditionType.CLEAR_NIGHT -> "Clear Night"
                WeatherConditionType.PARTLY_CLOUDY_DAY,
                WeatherConditionType.PARTLY_CLOUDY_NIGHT -> "Partly Cloudy"
                WeatherConditionType.CLOUDY -> "Cloudy"
                WeatherConditionType.FOG -> "Foggy"
                WeatherConditionType.DRIZZLE -> "Light Drizzle"
                WeatherConditionType.RAIN -> "Rainy"
                WeatherConditionType.SNOW -> "Snowy"
                WeatherConditionType.THUNDERSTORM -> "Thunderstorm"
            }
        }
    }

    fun getUvCategoryLabel(uvIndex: Int, languageCode: String): String {
        val lang = LocaleHelper.getEffectiveLanguage(languageCode)
        val level = when {
            uvIndex <= 2 -> 0 // Low
            uvIndex <= 5 -> 1 // Moderate
            uvIndex <= 7 -> 2 // High
            uvIndex <= 10 -> 3 // Very High
            else -> 4 // Extreme
        }
        return when (lang) {
            "ar" -> listOf("منخفض", "متوسط", "مرتفع", "مرتفع جداً", "شديد")[level]
            "fr" -> listOf("Faible", "Modéré", "Élevé", "Très élevé", "Extrême")[level]
            "de" -> listOf("Niedrig", "Mäßig", "Hoch", "Sehr hoch", "Extrem")[level]
            "tr" -> listOf("Düşük", "Orta", "Yüksek", "Çok Yüksek", "Aşırı")[level]
            "es" -> listOf("Bajo", "Moderado", "Alto", "Muy alto", "Extremo")[level]
            "ru" -> listOf("Низкий", "Умеренный", "Высокий", "Очень высокий", "Экстремальный")[level]
            "fa" -> listOf("کم", "متوسط", "زیاد", "بسیار زیاد", "شدید")[level]
            "ur" -> listOf("کم", "معتدل", "زیادہ", "بہت زیادہ", "شدید")[level]
            else -> listOf("Low", "Moderate", "High", "Very High", "Extreme")[level]
        }
    }

    fun getWindDirectionLabel(degrees: Int, languageCode: String): String {
        val lang = LocaleHelper.getEffectiveLanguage(languageCode)
        val idx = (((degrees % 360 + 360) % 360 + 22.5) / 45.0).toInt() % 8
        return when (lang) {
            "ar" -> listOf("شمال", "شمال شرق", "شرق", "جنوب شرق", "جنوب", "جنوب غرب", "غرب", "شمال غرب")[idx]
            "fr" -> listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")[idx]
            "de" -> listOf("N", "NO", "O", "SO", "S", "SW", "W", "NW")[idx]
            "es" -> listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")[idx]
            "ru" -> listOf("С", "СВ", "В", "ЮВ", "Ю", "ЮЗ", "З", "СЗ")[idx]
            "tr" -> listOf("K", "KD", "D", "GD", "G", "GB", "B", "KB")[idx]
            else -> listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")[idx]
        }
    }

    fun getLocalizedStrings(languageCode: String): WeatherLocalizedStrings {
        val lang = LocaleHelper.getEffectiveLanguage(languageCode)
        return when (lang) {
            "ar" -> WeatherLocalizedStrings(
                currentWeather = "الطقس الحالي",
                feelsLikePrefix = "الشعور الفعلي",
                hourlyTitle = "درجات الحرارة المتوقعة خلال الـ 24 ساعة القادمة",
                humidity = "الرطوبة",
                wind = "الرياح",
                kmhUnit = "كم/س",
                pressure = "الضغط الجوي",
                hpaUnit = "hPa",
                uvIndex = "مؤشر الأشعة UV",
                lastUpdatedPrefix = "آخر تحديث:",
                liveBadge = "مباشر",
                highPrefix = "ع:",
                lowPrefix = "ص:",
                liveSettingTitle = "درجة الحرارة (الطقس)",
                showOption = "إظهار",
                hideOption = "إخفاء",
                openWeatherPageOption = "فتح صفحة الطقس الكاملة",
                refreshLocationOption = "تحديث المدينة تلقائياً"
            )
            "fr" -> WeatherLocalizedStrings(
                currentWeather = "Météo actuelle",
                feelsLikePrefix = "Ressenti",
                hourlyTitle = "Température horaire pour les prochaines 24 heures",
                humidity = "Humidité",
                wind = "Vent",
                kmhUnit = "km/h",
                pressure = "Pression",
                hpaUnit = "hPa",
                uvIndex = "Indice UV",
                lastUpdatedPrefix = "Dernière mise à jour :",
                liveBadge = "En direct",
                highPrefix = "H:",
                lowPrefix = "B:",
                liveSettingTitle = "Température (Météo)",
                showOption = "Afficher",
                hideOption = "Masquer",
                openWeatherPageOption = "Ouvrir la page météo",
                refreshLocationOption = "Actualiser la ville auto"
            )
            "de" -> WeatherLocalizedStrings(
                currentWeather = "Aktuelles Wetter",
                feelsLikePrefix = "Gefühlt wie",
                hourlyTitle = "Stündliche Temperatur für die nächsten 24 Stunden",
                humidity = "Luftfeuchtigkeit",
                wind = "Wind",
                kmhUnit = "km/h",
                pressure = "Luftdruck",
                hpaUnit = "hPa",
                uvIndex = "UV-Index",
                lastUpdatedPrefix = "Zuletzt aktualisiert:",
                liveBadge = "Live",
                highPrefix = "H:",
                lowPrefix = "T:",
                liveSettingTitle = "Temperatur (Wetter)",
                showOption = "Anzeigen",
                hideOption = "Ausblenden",
                openWeatherPageOption = "Wetterseite öffnen",
                refreshLocationOption = "Stadt automatisch aktualisieren"
            )
            "tr" -> WeatherLocalizedStrings(
                currentWeather = "Güncel Hava Durumu",
                feelsLikePrefix = "Hissedilen",
                hourlyTitle = "Gelecek 24 saatlik sıcaklık tahmini",
                humidity = "Nem",
                wind = "Rüzgar",
                kmhUnit = "km/s",
                pressure = "Basınç",
                hpaUnit = "hPa",
                uvIndex = "UV İndeksi",
                lastUpdatedPrefix = "Son güncelleme:",
                liveBadge = "Canlı",
                highPrefix = "Y:",
                lowPrefix = "D:",
                liveSettingTitle = "Sıcaklık (Hava Durumu)",
                showOption = "Göster",
                hideOption = "Gizle",
                openWeatherPageOption = "Hava durumu sayfasını aç",
                refreshLocationOption = "Şehri otomatik yenile"
            )
            "es" -> WeatherLocalizedStrings(
                currentWeather = "Clima actual",
                feelsLikePrefix = "Sensación de",
                hourlyTitle = "Temperatura por hora para las próximas 24 horas",
                humidity = "Humedad",
                wind = "Viento",
                kmhUnit = "km/h",
                pressure = "Presión",
                hpaUnit = "hPa",
                uvIndex = "Índice UV",
                lastUpdatedPrefix = "Última actualización:",
                liveBadge = "En vivo",
                highPrefix = "Máx:",
                lowPrefix = "Mín:",
                liveSettingTitle = "Temperatura (Clima)",
                showOption = "Mostrar",
                hideOption = "Ocultar",
                openWeatherPageOption = "Abrir página del clima",
                refreshLocationOption = "Actualizar ciudad auto"
            )
            "ru" -> WeatherLocalizedStrings(
                currentWeather = "Текущая погода",
                feelsLikePrefix = "Ощущается как",
                hourlyTitle = "Почасовая температура на ближайшие 24 часа",
                humidity = "Влажность",
                wind = "Ветер",
                kmhUnit = "км/ч",
                pressure = "Давление",
                hpaUnit = "гПа",
                uvIndex = "УФ-индекс",
                lastUpdatedPrefix = "Обновлено:",
                liveBadge = "Live",
                highPrefix = "Макс:",
                lowPrefix = "Мин:",
                liveSettingTitle = "Температура (Погода)",
                showOption = "Показать",
                hideOption = "Скрыть",
                openWeatherPageOption = "Открыть страницу погоды",
                refreshLocationOption = "Обновить город авто"
            )
            "fa" -> WeatherLocalizedStrings(
                currentWeather = "آب‌وهوای فعلی",
                feelsLikePrefix = "دمای محسوس",
                hourlyTitle = "دمای ساعتی برای ۲۴ ساعت آینده",
                humidity = "رطوبت",
                wind = "باد",
                kmhUnit = "km/h",
                pressure = "فشار هوا",
                hpaUnit = "hPa",
                uvIndex = "شاخص UV",
                lastUpdatedPrefix = "آخرین بروزرسانی:",
                liveBadge = "زنده",
                highPrefix = "بیشینه:",
                lowPrefix = "کمینه:",
                liveSettingTitle = "دمای هوا (آب‌وهوا)",
                showOption = "نمایش",
                hideOption = "پنهان",
                openWeatherPageOption = "باز کردن صفحه آب‌وهوا",
                refreshLocationOption = "بروزرسانی خودکار شهر"
            )
            "ur" -> WeatherLocalizedStrings(
                currentWeather = "موجودہ موسم",
                feelsLikePrefix = "محسوس درجہ حرارت",
                hourlyTitle = "اگلے 24 گھنٹوں کا درجہ حرارت",
                humidity = "نمی",
                wind = "ہوا",
                kmhUnit = "km/h",
                pressure = "دباؤ",
                hpaUnit = "hPa",
                uvIndex = "UV انڈیکس",
                lastUpdatedPrefix = "آخری اپڈیٹ:",
                liveBadge = "لائیو",
                highPrefix = "زیادہ:",
                lowPrefix = "کم:",
                liveSettingTitle = "درجہ حرارت (موسم)",
                showOption = "دکھائیں",
                hideOption = "چھپائیں",
                openWeatherPageOption = "موسم کا مکمل صفحہ کھولیں",
                refreshLocationOption = "شہر خودکار تحدیث"
            )
            "it" -> WeatherLocalizedStrings(
                currentWeather = "Meteo attuale",
                feelsLikePrefix = "Percepiti",
                hourlyTitle = "Temperatura oraria prossime 24 ore",
                humidity = "Umidità",
                wind = "Vento",
                kmhUnit = "km/h",
                pressure = "Pressione",
                hpaUnit = "hPa",
                uvIndex = "Indice UV",
                lastUpdatedPrefix = "Ultimo aggiornamento:",
                liveBadge = "Live",
                highPrefix = "Max:",
                lowPrefix = "Min:",
                liveSettingTitle = "Temperatura (Meteo)",
                showOption = "Mostra",
                hideOption = "Nascondi",
                openWeatherPageOption = "Apri pagina Meteo",
                refreshLocationOption = "Rileva città automaticamente"
            )
            "pt" -> WeatherLocalizedStrings(
                currentWeather = "Clima atual",
                feelsLikePrefix = "Sensação",
                hourlyTitle = "Temperatura horária nas próximas 24h",
                humidity = "Umidade",
                wind = "Vento",
                kmhUnit = "km/h",
                pressure = "Pressão",
                hpaUnit = "hPa",
                uvIndex = "Índice UV",
                lastUpdatedPrefix = "Atualizado:",
                liveBadge = "Ao vivo",
                highPrefix = "Máx:",
                lowPrefix = "Mín:",
                liveSettingTitle = "Temperatura (Clima)",
                showOption = "Mostrar",
                hideOption = "Ocultar",
                openWeatherPageOption = "Abrir página do Clima",
                refreshLocationOption = "Detectar cidade automaticamente"
            )
            "pl" -> WeatherLocalizedStrings(
                currentWeather = "Aktualna pogoda",
                feelsLikePrefix = "Odczuwalna",
                hourlyTitle = "Temperatura godzinowa na 24h",
                humidity = "Wilgotność",
                wind = "Wiatr",
                kmhUnit = "km/h",
                pressure = "Ciśnienie",
                hpaUnit = "hPa",
                uvIndex = "Indeks UV",
                lastUpdatedPrefix = "Zaktualizowano:",
                liveBadge = "Na żywo",
                highPrefix = "Maks:",
                lowPrefix = "Min:",
                liveSettingTitle = "Temperatura (Pogoda)",
                showOption = "Pokaż",
                hideOption = "Ukryj",
                openWeatherPageOption = "Otwórz stronę Pogody",
                refreshLocationOption = "Wykryj miasto automatycznie"
            )
            "nl" -> WeatherLocalizedStrings(
                currentWeather = "Huidig weer",
                feelsLikePrefix = "Gevoelstemperatuur",
                hourlyTitle = "Uurlijkse temperatuur komende 24u",
                humidity = "Luchtvochtigheid",
                wind = "Wind",
                kmhUnit = "km/u",
                pressure = "Luchtdruk",
                hpaUnit = "hPa",
                uvIndex = "UV-index",
                lastUpdatedPrefix = "Bijgewerkt:",
                liveBadge = "Live",
                highPrefix = "Max:",
                lowPrefix = "Min:",
                liveSettingTitle = "Temperatuur (Weer)",
                showOption = "Tonen",
                hideOption = "Verbergen",
                openWeatherPageOption = "Volledige weerpagina openen",
                refreshLocationOption = "Stad automatisch detecteren"
            )
            "el" -> WeatherLocalizedStrings(
                currentWeather = "Τρέχων καιρός",
                feelsLikePrefix = "Αίσθηση",
                hourlyTitle = "Ωριαία θερμοκρασία επόμενων 24 ωρών",
                humidity = "Υγρασία",
                wind = "Άνεμος",
                kmhUnit = "χλμ/ώ",
                pressure = "Πίεση",
                hpaUnit = "hPa",
                uvIndex = "Δείκτης UV",
                lastUpdatedPrefix = "Ενημέρωση:",
                liveBadge = "Ζωντανά",
                highPrefix = "Μέγ:",
                lowPrefix = "Ελάχ:",
                liveSettingTitle = "Θερμοκρασία (Καιρός)",
                showOption = "Εμφάνιση",
                hideOption = "Απόκρυψη",
                openWeatherPageOption = "Άνοιγμα σελίδας Καιρού",
                refreshLocationOption = "Αυτόματος εντοπισμός πόλης"
            )
            "id", "in" -> WeatherLocalizedStrings(
                currentWeather = "Cuaca saat ini",
                feelsLikePrefix = "Terasa seperti",
                hourlyTitle = "Suhu per jam untuk 24 jam ke depan",
                humidity = "Kelembapan",
                wind = "Angin",
                kmhUnit = "km/j",
                pressure = "Tekanan",
                hpaUnit = "hPa",
                uvIndex = "Indeks UV",
                lastUpdatedPrefix = "Diperbarui:",
                liveBadge = "Langsung",
                highPrefix = "T:",
                lowPrefix = "R:",
                liveSettingTitle = "Suhu (Cuaca)",
                showOption = "Tampilkan",
                hideOption = "Sembunyikan",
                openWeatherPageOption = "Buka halaman Cuaca",
                refreshLocationOption = "Deteksi kota otomatis"
            )
            "vi" -> WeatherLocalizedStrings(
                currentWeather = "Thời tiết hiện tại",
                feelsLikePrefix = "Cảm giác như",
                hourlyTitle = "Nhiệt độ theo giờ 24 giờ tới",
                humidity = "Độ ẩm",
                wind = "Gió",
                kmhUnit = "km/h",
                pressure = "Áp suất",
                hpaUnit = "hPa",
                uvIndex = "Chỉ số UV",
                lastUpdatedPrefix = "Cập nhật:",
                liveBadge = "Trực tiếp",
                highPrefix = "C:",
                lowPrefix = "T:",
                liveSettingTitle = "Nhiệt độ (Thời tiết)",
                showOption = "Hiện",
                hideOption = "Ẩn",
                openWeatherPageOption = "Mở trang Thời tiết",
                refreshLocationOption = "Tự động tìm thành phố"
            )
            "th" -> WeatherLocalizedStrings(
                currentWeather = "สภาพอากาศปัจจุบัน",
                feelsLikePrefix = "รู้สึกเหมือน",
                hourlyTitle = "อุณหภูมิรายชั่วโมง 24 ชั่วโมงข้างหน้า",
                humidity = "ความชื้น",
                wind = "ลม",
                kmhUnit = "กม./ชม.",
                pressure = "ความกดอากาศ",
                hpaUnit = "hPa",
                uvIndex = "ดัชนี UV",
                lastUpdatedPrefix = "อัปเดตล่าสุด:",
                liveBadge = "สด",
                highPrefix = "สูง:",
                lowPrefix = "ต่ำ:",
                liveSettingTitle = "อุณหภูมิ (สภาพอากาศ)",
                showOption = "แสดง",
                hideOption = "ซ่อน",
                openWeatherPageOption = "เปิดหน้าสภาพอากาศ",
                refreshLocationOption = "ตรวจหาเมืองอัตโนมัติ"
            )
            "zh" -> WeatherLocalizedStrings(
                currentWeather = "当前天气",
                feelsLikePrefix = "体感温度",
                hourlyTitle = "未来24小时逐时气温",
                humidity = "湿度",
                wind = "风速",
                kmhUnit = "公里/时",
                pressure = "气压",
                hpaUnit = "hPa",
                uvIndex = "紫外线指数",
                lastUpdatedPrefix = "最后更新:",
                liveBadge = "实时",
                highPrefix = "高:",
                lowPrefix = "低:",
                liveSettingTitle = "温度（天气）",
                showOption = "显示",
                hideOption = "隐藏",
                openWeatherPageOption = "打开完整天气页",
                refreshLocationOption = "自动定位城市"
            )
            "ja" -> WeatherLocalizedStrings(
                currentWeather = "現在の天気",
                feelsLikePrefix = "体感温度",
                hourlyTitle = "今後24時間の気温推移",
                humidity = "湿度",
                wind = "風速",
                kmhUnit = "km/h",
                pressure = "気圧",
                hpaUnit = "hPa",
                uvIndex = "UV指数",
                lastUpdatedPrefix = "最終更新:",
                liveBadge = "ライブ",
                highPrefix = "最高:",
                lowPrefix = "最低:",
                liveSettingTitle = "気温（天気）",
                showOption = "表示",
                hideOption = "非表示",
                openWeatherPageOption = "天気ページを開く",
                refreshLocationOption = "都市を自動検出"
            )
            "ko" -> WeatherLocalizedStrings(
                currentWeather = "현재 날씨",
                feelsLikePrefix = "체감 온도",
                hourlyTitle = "향후 24시간 시간별 기온",
                humidity = "습도",
                wind = "바람",
                kmhUnit = "km/h",
                pressure = "기압",
                hpaUnit = "hPa",
                uvIndex = "자외선 지수",
                lastUpdatedPrefix = "업데이트:",
                liveBadge = "실시간",
                highPrefix = "최고:",
                lowPrefix = "최저:",
                liveSettingTitle = "기온 (날씨)",
                showOption = "표시",
                hideOption = "숨김",
                openWeatherPageOption = "전체 날씨 페이지 열기",
                refreshLocationOption = "도시 자동 감지"
            )
            "hi" -> WeatherLocalizedStrings(
                currentWeather = "वर्तमान मौसम",
                feelsLikePrefix = "महसूस तापमान",
                hourlyTitle = "अगले 24 घंटों का तापमान",
                humidity = "नमी",
                wind = "हवा",
                kmhUnit = "किमी/घं",
                pressure = "दबाव",
                hpaUnit = "hPa",
                uvIndex = "यूवी इंडेक्स",
                lastUpdatedPrefix = "अंतिम अपडेट:",
                liveBadge = "लाइव",
                highPrefix = "अधिक:",
                lowPrefix = "न्यून:",
                liveSettingTitle = "तापमान (मौसम)",
                showOption = "दिखाएं",
                hideOption = "छिपाएं",
                openWeatherPageOption = "पूरा मौसम पेज खोलें",
                refreshLocationOption = "शहर स्वतः पहचानें"
            )
            else -> WeatherLocalizedStrings(
                currentWeather = "Current Weather",
                feelsLikePrefix = "Feels like",
                hourlyTitle = "Hourly temperature left for next 24 hours",
                humidity = "Humidity",
                wind = "Wind",
                kmhUnit = "km/h",
                pressure = "Pressure",
                hpaUnit = "hPa",
                uvIndex = "UV Index",
                lastUpdatedPrefix = "Last updated:",
                liveBadge = "Live",
                highPrefix = "H:",
                lowPrefix = "L:",
                liveSettingTitle = "Temperature (Weather)",
                showOption = "Show",
                hideOption = "Hide",
                openWeatherPageOption = "Open Full Weather Page",
                refreshLocationOption = "Auto-Detect City Now"
            )
        }
    }

    private fun getFallbackLocationFromTimeZone(): Triple<String, Double, Double> {
        val tzId = TimeZone.getDefault().id
        return when {
            tzId.contains("Riyadh", ignoreCase = true) -> Triple("Riyadh", 24.7136, 46.6753)
            tzId.contains("Gaza", ignoreCase = true) -> Triple("Gaza", 31.5017, 34.4668)
            tzId.contains("Hebron", ignoreCase = true) ||
                tzId.contains("Jerusalem", ignoreCase = true) -> Triple("Jerusalem", 31.7683, 35.2137)
            tzId.contains("Cairo", ignoreCase = true) -> Triple("Cairo", 30.0444, 31.2357)
            tzId.contains("Dubai", ignoreCase = true) -> Triple("Dubai", 25.2048, 55.2708)
            tzId.contains("Qatar", ignoreCase = true) ||
                tzId.contains("Doha", ignoreCase = true) -> Triple("Doha", 25.2854, 51.5310)
            tzId.contains("Kuwait", ignoreCase = true) -> Triple("Kuwait City", 29.3759, 47.9774)
            tzId.contains("Amman", ignoreCase = true) -> Triple("Amman", 31.9454, 35.9284)
            tzId.contains("Beirut", ignoreCase = true) -> Triple("Beirut", 33.8938, 35.5018)
            tzId.contains("Baghdad", ignoreCase = true) -> Triple("Baghdad", 33.3152, 44.3661)
            tzId.contains("Damascus", ignoreCase = true) -> Triple("Damascus", 33.5138, 36.2765)
            tzId.contains("Casablanca", ignoreCase = true) -> Triple("Casablanca", 33.5731, -7.5898)
            tzId.contains("Algiers", ignoreCase = true) -> Triple("Algiers", 36.7538, 3.0588)
            tzId.contains("Tunis", ignoreCase = true) -> Triple("Tunis", 36.8065, 10.1815)
            tzId.contains("Istanbul", ignoreCase = true) -> Triple("Istanbul", 41.0082, 28.9784)
            tzId.contains("London", ignoreCase = true) -> Triple("London", 51.5072, -0.1276)
            tzId.contains("Paris", ignoreCase = true) -> Triple("Paris", 48.8566, 2.3522)
            tzId.contains("Berlin", ignoreCase = true) -> Triple("Berlin", 52.5200, 13.4050)
            tzId.contains("Madrid", ignoreCase = true) -> Triple("Madrid", 40.4168, -3.7038)
            tzId.contains("Rome", ignoreCase = true) -> Triple("Rome", 41.9028, 12.4964)
            tzId.contains("Moscow", ignoreCase = true) -> Triple("Moscow", 55.7558, 37.6173)
            else -> Triple("Riyadh", 24.7136, 46.6753)
        }
    }

    private fun translateKnownCity(rawCity: String, effectiveLang: String): String {
        return translateKnownCityOrNull(rawCity, effectiveLang) ?: rawCity
    }

    private fun translateKnownCityOrNull(rawCity: String, effectiveLang: String): String? {
        val norm = rawCity.trim().lowercase(Locale.ROOT)
        val table: Map<String, Pair<String, String>> = mapOf(
            "riyadh" to ("الرياض" to "Riyadh"),
            "الرياض" to ("الرياض" to "Riyadh"),
            "jeddah" to ("جدة" to "Jeddah"),
            "جدة" to ("جدة" to "Jeddah"),
            "mecca" to ("مكة المكرمة" to "Makkah"),
            "makkah" to ("مكة المكرمة" to "Makkah"),
            "medina" to ("المدينة المنورة" to "Madinah"),
            "madinah" to ("المدينة المنورة" to "Madinah"),
            "dammam" to ("الدمام" to "Dammam"),
            "jerusalem" to ("القدس" to "Jerusalem"),
            "القدس" to ("القدس" to "Jerusalem"),
            "gaza" to ("غزة" to "Gaza"),
            "غزة" to ("غزة" to "Gaza"),
            "ramallah" to ("رام الله" to "Ramallah"),
            "رام الله" to ("رام الله" to "Ramallah"),
            "hebron" to ("الخليل" to "Hebron"),
            "الخليل" to ("الخليل" to "Hebron"),
            "nablus" to ("نابلس" to "Nablus"),
            "نابلس" to ("نابلس" to "Nablus"),
            "jenin" to ("جنين" to "Jenin"),
            "bethlehem" to ("بيت لحم" to "Bethlehem"),
            "tulkarm" to ("طولكرم" to "Tulkarm"),
            "qalqilya" to ("قلقيلية" to "Qalqilya"),
            "jericho" to ("أريحا" to "Jericho"),
            "cairo" to ("القاهرة" to "Cairo"),
            "القاهرة" to ("القاهرة" to "Cairo"),
            "alexandria" to ("الإسكندرية" to "Alexandria"),
            "dubai" to ("دبي" to "Dubai"),
            "دبي" to ("دبي" to "Dubai"),
            "abu dhabi" to ("أبوظبي" to "Abu Dhabi"),
            "doha" to ("الدوحة" to "Doha"),
            "الدوحة" to ("الدوحة" to "Doha"),
            "kuwait city" to ("الكويت" to "Kuwait City"),
            "kuwait" to ("الكويت" to "Kuwait"),
            "amman" to ("عمّان" to "Amman"),
            "عمان" to ("عمّان" to "Amman"),
            "beirut" to ("بيروت" to "Beirut"),
            "بيروت" to ("بيروت" to "Beirut"),
            "baghdad" to ("بغداد" to "Baghdad"),
            "بغداد" to ("بغداد" to "Baghdad"),
            "damascus" to ("دمشق" to "Damascus"),
            "دمشق" to ("دمشق" to "Damascus"),
            "casablanca" to ("الدار البيضاء" to "Casablanca"),
            "rabat" to ("الرباط" to "Rabat"),
            "algiers" to ("الجزائر" to "Algiers"),
            "tunis" to ("تونس" to "Tunis"),
            "khartoum" to ("الخرطوم" to "Khartoum"),
            "sanaa" to ("صنعاء" to "Sanaa"),
            "muscat" to ("مسقط" to "Muscat"),
            "manama" to ("المنامة" to "Manama"),
            "istanbul" to ("إسطنبول" to "Istanbul"),
            "ankara" to ("أنقرة" to "Ankara"),
            "london" to ("لندن" to "London"),
            "لندن" to ("لندن" to "London"),
            "paris" to ("باريس" to "Paris"),
            "berlin" to ("برلين" to "Berlin"),
            "madrid" to ("مدريد" to "Madrid"),
            "rome" to ("روما" to "Rome"),
            "moscow" to ("موسكو" to "Moscow"),
            "new york" to ("نيويورك" to "New York"),
            "los angeles" to ("لوس أنجلوس" to "Los Angeles"),
            "tokyo" to ("طوكيو" to "Tokyo")
        )
        val match = table[norm] ?: return null
        return if (effectiveLang in setOf("ar", "fa", "ur")) match.first else match.second
    }

    private fun createDefaultWeather(effectiveLang: String): WeatherData {
        val tzFallback = getFallbackLocationFromTimeZone()
        val city = translateKnownCity(tzFallback.first, effectiveLang)
        val hourly = listOf(
            HourlyTempPoint("14:00", 27, WeatherConditionType.PARTLY_CLOUDY_DAY),
            HourlyTempPoint("17:00", 29, WeatherConditionType.PARTLY_CLOUDY_DAY),
            HourlyTempPoint("20:00", 30, WeatherConditionType.CLEAR_NIGHT),
            HourlyTempPoint("23:00", 31, WeatherConditionType.CLEAR_NIGHT),
            HourlyTempPoint("02:00", 23, WeatherConditionType.CLEAR_NIGHT),
            HourlyTempPoint("05:00", 18, WeatherConditionType.CLEAR_NIGHT),
            HourlyTempPoint("08:00", 22, WeatherConditionType.CLEAR_DAY),
            HourlyTempPoint("11:00", 28, WeatherConditionType.PARTLY_CLOUDY_DAY)
        )
        val now = System.currentTimeMillis()
        val daily = (1..5).map { dayOffset ->
            val dm = now + dayOffset * 86400_000L
            DailyForecastItem(
                dateMillis = dm,
                dayLabel = formatDayAndDate(dm, effectiveLang),
                condition = if (dayOffset in listOf(2, 3)) WeatherConditionType.PARTLY_CLOUDY_DAY else WeatherConditionType.CLEAR_DAY,
                maxTempC = 31,
                minTempC = when (dayOffset) {
                    1 -> 19
                    2 -> 19
                    3 -> 16
                    4 -> 18
                    else -> 17
                }
            )
        }
        return WeatherData(
            cityName = city,
            rawEnglishCity = tzFallback.first,
            latitude = tzFallback.second,
            longitude = tzFallback.third,
            currentTempC = 27,
            feelsLikeC = 26,
            weatherCode = 2,
            isDay = true,
            conditionType = WeatherConditionType.PARTLY_CLOUDY_DAY,
            humidityPercent = 38,
            windSpeedKmh = 14,
            windDirectionDeg = 45,
            pressureHpa = 1014,
            uvIndex = 5,
            hourlyPoints = hourly,
            high24hC = 31,
            low24hC = 18,
            high24hIndex = 3,
            low24hIndex = 5,
            dailyForecasts = daily,
            lastUpdatedTime = SimpleDateFormat("HH:mm", Locale.US).format(Date(now)),
            fetchedAtMillis = 0L,
            languageCode = effectiveLang,
            isLoading = false
        )
    }

    private fun saveCachedWeather(context: Context, data: WeatherData) {
        try {
            val prefs = context.getSharedPreferences("salsa_weather_cache", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("city_name", data.cityName)
                .putString("raw_city", data.rawEnglishCity)
                .putFloat("lat", data.latitude.toFloat())
                .putFloat("lon", data.longitude.toFloat())
                .putInt("temp_c", data.currentTempC)
                .putInt("feels_c", data.feelsLikeC)
                .putInt("wmo_code", data.weatherCode)
                .putBoolean("is_day", data.isDay)
                .putInt("humidity", data.humidityPercent)
                .putInt("wind_kmh", data.windSpeedKmh)
                .putInt("wind_deg", data.windDirectionDeg)
                .putInt("pressure", data.pressureHpa)
                .putInt("uv", data.uvIndex)
                .putString("updated_time", data.lastUpdatedTime)
                .putString("location_source", data.locationSource.name)
                .apply()
        } catch (_: Exception) {}
    }

    private fun loadCachedWeather(context: Context, effectiveLang: String) {
        try {
            val prefs = context.getSharedPreferences("salsa_weather_cache", Context.MODE_PRIVATE)
            if (!prefs.contains("temp_c")) {
                _weatherState.value = createDefaultWeather(effectiveLang)
                return
            }
            val rawCity = prefs.getString("raw_city", "Riyadh") ?: "Riyadh"
            val savedCity = prefs.getString("city_name", rawCity) ?: rawCity
            val localizedCity = translateKnownCityOrNull(rawCity, effectiveLang) ?: savedCity
            val code = prefs.getInt("wmo_code", 2)
            val isDay = prefs.getBoolean("is_day", true)
            val savedSource = try {
                WeatherLocationSource.valueOf(prefs.getString("location_source", WeatherLocationSource.CACHE.name) ?: WeatherLocationSource.CACHE.name)
            } catch (_: Exception) {
                WeatherLocationSource.CACHE
            }
            val defaultData = createDefaultWeather(effectiveLang)
            _weatherState.value = defaultData.copy(
                cityName = localizedCity,
                rawEnglishCity = rawCity,
                latitude = prefs.getFloat("lat", defaultData.latitude.toFloat()).toDouble(),
                longitude = prefs.getFloat("lon", defaultData.longitude.toFloat()).toDouble(),
                currentTempC = prefs.getInt("temp_c", 27),
                feelsLikeC = prefs.getInt("feels_c", 26),
                weatherCode = code,
                isDay = isDay,
                conditionType = mapWmoCodeToCondition(code, isDay),
                humidityPercent = prefs.getInt("humidity", 38),
                windSpeedKmh = prefs.getInt("wind_kmh", 14),
                windDirectionDeg = prefs.getInt("wind_deg", 45),
                pressureHpa = prefs.getInt("pressure", 1014),
                uvIndex = prefs.getInt("uv", 5),
                lastUpdatedTime = prefs.getString("updated_time", defaultData.lastUpdatedTime) ?: defaultData.lastUpdatedTime,
                languageCode = effectiveLang,
                locationSource = savedSource
            )
        } catch (_: Exception) {}
    }

    private fun httpGet(urlStr: String, timeoutMs: Int): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("User-Agent", "SalsaTVLauncher/${com.example.BuildConfig.VERSION_NAME}")
                setRequestProperty("Accept", "application/json")
            }
            if (conn.responseCode in 200..299) {
                BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
            } else null
        } catch (_: Exception) {
            null
        } finally {
            try {
                conn?.disconnect()
            } catch (_: Exception) {}
        }
    }
}
