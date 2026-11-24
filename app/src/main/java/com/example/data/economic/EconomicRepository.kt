package com.example.data.economic

import android.content.Context
import com.example.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MarketQuoteItem(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val yahooSymbol: String,
    val priceFormatted: String,
    val rawPrice: Double,
    val changePercent: Double,
    val changeFormatted: String,
    val isPositive: Boolean,
    val isNeutral: Boolean = false,
    val sparklinePoints: List<Float>,
    val high5dFormatted: String,
    val low5dFormatted: String
) {
    fun getLocalizedTitle(langCode: String): String {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) titleAr else titleEn
    }
}

data class EconomicCalendarEvent(
    val timeLabel: String,
    val countryCode: String,
    val titleEn: String,
    val titleAr: String,
    val impactLevel: Int, // 1..3
    val actualValue: String,
    val forecastValue: String,
    val previousValue: String
) {
    fun getLocalizedTitle(langCode: String): String {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) titleAr else titleEn
    }
}

data class EconomicDashboardData(
    val indicesMap: Map<String, MarketQuoteItem>,
    val commoditiesMap: Map<String, MarketQuoteItem>,
    val forexMap: Map<String, MarketQuoteItem>,
    val calendarEvents: List<EconomicCalendarEvent>,
    val lastUpdatedTime: String,
    val fetchedAtMillis: Long,
    val isLoading: Boolean = false
)

data class EconomicLocalizedStrings(
    val dashboardTitle: String,
    val globalMarketSubtitle: String,
    val liveBadge: String,
    val sourceLabel: String,
    val commoditiesAndCryptoTitle: String,
    val smooth5DayTitle: String,
    val forexTitle: String,
    val economicCalendarTitle: String,
    val impactLabel: String,
    val actLabel: String,
    val fcLabel: String,
    val prevLabel: String,
    val lastUpdatedPrefix: String,
    val highPrefix: String,
    val lowPrefix: String,
    val customizeButtonLabel: String,
    val customizeDialogTitle: String,
    val customizeDialogSubtitle: String,
    val tabIndices: String,
    val tabForex: String,
    val tabCommodities: String,
    val doneButton: String,
    val resetDefaultButton: String,
    val selectedCountFormat: String
)

object EconomicRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fetchMutex = Mutex()

    private val _dashboardState = MutableStateFlow(createInitialDashboardData())
    val dashboardState: StateFlow<EconomicDashboardData> = _dashboardState.asStateFlow()

    @Volatile
    private var isInitialized = false

    fun ensureInitialized(context: Context) {
        if (!isInitialized) {
            isInitialized = true
            refreshMarketDataAsync(context.applicationContext)
        } else {
            val age = System.currentTimeMillis() - _dashboardState.value.fetchedAtMillis
            if (age > 10 * 60 * 1000L) {
                refreshMarketDataAsync(context.applicationContext)
            }
        }
    }

    fun refreshMarketDataAsync(context: Context) {
        scope.launch {
            fetchMutex.withLock {
                try {
                    _dashboardState.value = _dashboardState.value.copy(isLoading = true)
                    val current = _dashboardState.value
                    val updatedForex = fetchLiveForexUpdates(current.forexMap)
                    val updatedCommodities = fetchLiveCryptoAndMetalsUpdates(current.commoditiesMap)
                    val updatedIndices = fetchLiveYahooIndicesUpdates(current.indicesMap)

                    val nowTime = SimpleDateFormat("HH:mm", Locale.US).format(Date())
                    _dashboardState.value = current.copy(
                        indicesMap = updatedIndices,
                        commoditiesMap = updatedCommodities,
                        forexMap = updatedForex,
                        lastUpdatedTime = nowTime,
                        fetchedAtMillis = System.currentTimeMillis(),
                        isLoading = false
                    )
                } catch (_: Exception) {
                    _dashboardState.value = _dashboardState.value.copy(isLoading = false)
                }
            }
        }
    }

    private fun fetchLiveForexUpdates(existing: Map<String, MarketQuoteItem>): Map<String, MarketQuoteItem> {
        val result = existing.toMutableMap()
        try {
            val jsonStr = httpGet("https://open.er-api.com/v6/latest/USD", timeoutMs = 4500) ?: return result
            val root = JSONObject(jsonStr)
            val rates = root.optJSONObject("rates") ?: return result

            fun updatePair(
                id: String,
                rateValue: Double,
                decimals: Int
            ) {
                val old = result[id] ?: return
                if (rateValue <= 0.0 || rateValue.isNaN()) return
                val diffPct = if (old.rawPrice > 0.0) {
                    ((rateValue - old.rawPrice) / old.rawPrice * 100.0).let {
                        if (kotlin.math.abs(it) < 0.005) old.changePercent else it.coerceIn(-9.9, 9.9)
                    }
                } else old.changePercent
                val isNeutral = kotlin.math.abs(diffPct) < 0.005
                val isPos = diffPct >= 0.0
                result[id] = old.copy(
                    rawPrice = rateValue,
                    priceFormatted = formatNumber(rateValue, decimals, useGrouping = false),
                    changePercent = diffPct,
                    changeFormatted = formatSignedPercent(diffPct, decimals = 2),
                    isPositive = isPos,
                    isNeutral = isNeutral
                )
            }

            val eurPerUsd = rates.optDouble("EUR", Double.NaN)
            if (!eurPerUsd.isNaN() && eurPerUsd > 0) {
                updatePair("eur_usd", 1.0 / eurPerUsd, 4)
            }
            val gbpPerUsd = rates.optDouble("GBP", Double.NaN)
            if (!gbpPerUsd.isNaN() && gbpPerUsd > 0) {
                updatePair("gbp_usd", 1.0 / gbpPerUsd, 4)
            }
            val jpy = rates.optDouble("JPY", Double.NaN)
            if (!jpy.isNaN()) updatePair("usd_jpy", jpy, 2)

            val sar = rates.optDouble("SAR", Double.NaN)
            if (!sar.isNaN()) updatePair("usd_sar", sar, 4)

            val ils = rates.optDouble("ILS", Double.NaN)
            if (!ils.isNaN()) updatePair("usd_ils", ils, 4)

            if (!eurPerUsd.isNaN() && eurPerUsd > 0 && !ils.isNaN()) {
                updatePair("eur_ils", ils / eurPerUsd, 4)
            }
            val jod = rates.optDouble("JOD", Double.NaN)
            if (!jod.isNaN()) updatePair("usd_jod", jod, 4)
            if (!jod.isNaN() && jod > 0 && !ils.isNaN()) {
                updatePair("jod_ils", ils / jod, 4)
            }

            val egp = rates.optDouble("EGP", Double.NaN)
            if (!egp.isNaN()) updatePair("usd_egp", egp, 2)

            val aed = rates.optDouble("AED", Double.NaN)
            if (!aed.isNaN()) updatePair("usd_aed", aed, 4)

            val qar = rates.optDouble("QAR", Double.NaN)
            if (!qar.isNaN()) updatePair("usd_qar", qar, 4)

            val kwd = rates.optDouble("KWD", Double.NaN)
            if (!kwd.isNaN()) updatePair("usd_kwd", kwd, 4)

            val tryRate = rates.optDouble("TRY", Double.NaN)
            if (!tryRate.isNaN()) updatePair("usd_try", tryRate, 2)

            val cad = rates.optDouble("CAD", Double.NaN)
            if (!cad.isNaN()) updatePair("usd_cad", cad, 4)

            val chf = rates.optDouble("CHF", Double.NaN)
            if (!chf.isNaN()) updatePair("usd_chf", chf, 4)
        } catch (_: Exception) {}
        return result
    }

    private fun fetchLiveCryptoAndMetalsUpdates(existing: Map<String, MarketQuoteItem>): Map<String, MarketQuoteItem> {
        val result = existing.toMutableMap()
        try {
            val url = "https://api.coingecko.com/api/v3/simple/price" +
                "?ids=bitcoin,ethereum,solana,binancecoin,ripple,pax-gold" +
                "&vs_currencies=usd&include_24hr_change=true"
            val jsonStr = httpGet(url, timeoutMs = 4500)
            if (!jsonStr.isNullOrBlank()) {
                val root = JSONObject(jsonStr)
                val mapping = listOf(
                    Triple("btc", "bitcoin", 0),
                    Triple("eth", "ethereum", 0),
                    Triple("sol", "solana", 2),
                    Triple("bnb", "binancecoin", 1),
                    Triple("xrp", "ripple", 3),
                    Triple("gold", "pax-gold", 2)
                )
                for ((itemId, cgId, decimals) in mapping) {
                    val obj = root.optJSONObject(cgId) ?: continue
                    val price = obj.optDouble("usd", Double.NaN)
                    val chg = obj.optDouble("usd_24h_change", Double.NaN)
                    val old = result[itemId] ?: continue
                    if (!price.isNaN() && price > 0) {
                        val validChg = if (chg.isNaN()) old.changePercent else chg
                        val isPos = validChg >= 0.0
                        result[itemId] = old.copy(
                            rawPrice = price,
                            priceFormatted = formatNumber(price, decimals, useGrouping = true),
                            changePercent = validChg,
                            changeFormatted = formatSignedPercent(validChg, decimals = 1),
                            isPositive = isPos
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        return result
    }

    private fun fetchLiveYahooIndicesUpdates(existing: Map<String, MarketQuoteItem>): Map<String, MarketQuoteItem> {
        val result = existing.toMutableMap()
        // Fetch top 4 default indices from Yahoo Finance v8 chart API if reachable
        val mainIds = listOf("sp500", "nasdaq", "dow", "dxy")
        for (id in mainIds) {
            val old = result[id] ?: continue
            if (old.yahooSymbol.isBlank()) continue
            try {
                val enc = URLEncoder.encode(old.yahooSymbol, "UTF-8")
                val url = "https://query1.finance.yahoo.com/v8/finance/chart/$enc?interval=1d&range=5d"
                val jsonStr = httpGet(url, timeoutMs = 3000) ?: continue
                val root = JSONObject(jsonStr)
                val chartRes = root.optJSONObject("chart")
                    ?.optJSONArray("result")
                    ?.optJSONObject(0) ?: continue
                val meta = chartRes.optJSONObject("meta") ?: continue
                val regPrice = meta.optDouble("regularMarketPrice", Double.NaN)
                val prevClose = meta.optDouble("chartPreviousClose", meta.optDouble("previousClose", Double.NaN))
                if (!regPrice.isNaN() && regPrice > 0) {
                    val chg = if (!prevClose.isNaN() && prevClose > 0) {
                        ((regPrice - prevClose) / prevClose) * 100.0
                    } else old.changePercent

                    val closes = chartRes.optJSONObject("indicators")
                        ?.optJSONArray("quote")
                        ?.optJSONObject(0)
                        ?.optJSONArray("close")
                    val pts = mutableListOf<Float>()
                    if (closes != null) {
                        for (i in 0 until closes.length()) {
                            val v = closes.optDouble(i, Double.NaN)
                            if (!v.isNaN() && v > 0) pts.add(v.toFloat())
                        }
                    }
                    val finalPts = if (pts.size >= 4) pts else old.sparklinePoints
                    val maxP = finalPts.maxOrNull()?.toDouble() ?: (regPrice * 1.01)
                    val minP = finalPts.minOrNull()?.toDouble() ?: (regPrice * 0.985)
                    result[id] = old.copy(
                        rawPrice = regPrice,
                        priceFormatted = formatNumber(regPrice, 2, useGrouping = true),
                        changePercent = chg,
                        changeFormatted = formatSignedPercent(chg, 2),
                        isPositive = chg >= 0.0,
                        sparklinePoints = finalPts,
                        high5dFormatted = formatNumber(maxP, 2, useGrouping = true),
                        low5dFormatted = formatNumber(minP, 2, useGrouping = true)
                    )
                }
            } catch (_: Exception) {}
        }
        return result
    }

    private fun createInitialDashboardData(): EconomicDashboardData {
        val indices = linkedMapOf(
            "sp500" to MarketQuoteItem(
                id = "sp500",
                titleEn = "S&P 500",
                titleAr = "S&P 500",
                yahooSymbol = "^GSPC",
                priceFormatted = "6,012.45",
                rawPrice = 6012.45,
                changePercent = 0.82,
                changeFormatted = "+0.82%",
                isPositive = true,
                sparklinePoints = listOf(5910f, 5948f, 5942f, 5965f, 5989f, 5935f, 6012.45f),
                high5dFormatted = "6,012.45",
                low5dFormatted = "5,910.20"
            ),
            "nasdaq" to MarketQuoteItem(
                id = "nasdaq",
                titleEn = "Nasdaq",
                titleAr = "ناسداك Nasdaq",
                yahooSymbol = "^IXIC",
                priceFormatted = "19,840.10",
                rawPrice = 19840.10,
                changePercent = 1.05,
                changeFormatted = "+1.05%",
                isPositive = true,
                sparklinePoints = listOf(19420f, 19510f, 19490f, 19660f, 19780f, 19610f, 19840.10f),
                high5dFormatted = "19,840.10",
                low5dFormatted = "19,420.00"
            ),
            "dow" to MarketQuoteItem(
                id = "dow",
                titleEn = "Dow Jones",
                titleAr = "داو جونز Dow",
                yahooSymbol = "^DJI",
                priceFormatted = "44,210.55",
                rawPrice = 44210.55,
                changePercent = -0.21,
                changeFormatted = "-0.21%",
                isPositive = false,
                sparklinePoints = listOf(44480f, 44390f, 44410f, 44310f, 44340f, 44260f, 44210.55f),
                high5dFormatted = "44,480.00",
                low5dFormatted = "44,210.55"
            ),
            "dxy" to MarketQuoteItem(
                id = "dxy",
                titleEn = "DXY",
                titleAr = "مؤشر الدولار DXY",
                yahooSymbol = "DX-Y.NYB",
                priceFormatted = "104.32",
                rawPrice = 104.32,
                changePercent = -0.15,
                changeFormatted = "-0.15%",
                isPositive = false,
                sparklinePoints = listOf(104.85f, 104.62f, 104.70f, 104.48f, 104.55f, 104.40f, 104.32f),
                high5dFormatted = "104.85",
                low5dFormatted = "104.32"
            ),
            "tasi" to MarketQuoteItem(
                id = "tasi",
                titleEn = "Tadawul TASI",
                titleAr = "تداول السعودية TASI",
                yahooSymbol = "^TASI.SR",
                priceFormatted = "11,892.40",
                rawPrice = 11892.40,
                changePercent = 0.54,
                changeFormatted = "+0.54%",
                isPositive = true,
                sparklinePoints = listOf(11760f, 11790f, 11810f, 11845f, 11820f, 11870f, 11892.4f),
                high5dFormatted = "11,892.40",
                low5dFormatted = "11,760.00"
            ),
            "egx30" to MarketQuoteItem(
                id = "egx30",
                titleEn = "EGX 30",
                titleAr = "بورصة مصر EGX 30",
                yahooSymbol = "^CASE30",
                priceFormatted = "30,645.20",
                rawPrice = 30645.20,
                changePercent = 0.92,
                changeFormatted = "+0.92%",
                isPositive = true,
                sparklinePoints = listOf(30120f, 30250f, 30310f, 30480f, 30390f, 30550f, 30645.2f),
                high5dFormatted = "30,645.20",
                low5dFormatted = "30,120.00"
            ),
            "dfm" to MarketQuoteItem(
                id = "dfm",
                titleEn = "Dubai DFM",
                titleAr = "سوق دبي DFM",
                yahooSymbol = "DFMGI.AE",
                priceFormatted = "4,718.65",
                rawPrice = 4718.65,
                changePercent = 0.68,
                changeFormatted = "+0.68%",
                isPositive = true,
                sparklinePoints = listOf(4640f, 4660f, 4675f, 4690f, 4680f, 4705f, 4718.65f),
                high5dFormatted = "4,718.65",
                low5dFormatted = "4,640.00"
            ),
            "qe" to MarketQuoteItem(
                id = "qe",
                titleEn = "Qatar QE",
                titleAr = "بورصة قطر QE",
                yahooSymbol = "^GNRI.QA",
                priceFormatted = "10,452.80",
                rawPrice = 10452.80,
                changePercent = 0.35,
                changeFormatted = "+0.35%",
                isPositive = true,
                sparklinePoints = listOf(10380f, 10395f, 10410f, 10425f, 10415f, 10440f, 10452.8f),
                high5dFormatted = "10,452.80",
                low5dFormatted = "10,380.00"
            ),
            "ftse" to MarketQuoteItem(
                id = "ftse",
                titleEn = "FTSE 100",
                titleAr = "فوتسي لندن FTSE",
                yahooSymbol = "^FTSE",
                priceFormatted = "8,262.10",
                rawPrice = 8262.10,
                changePercent = 0.41,
                changeFormatted = "+0.41%",
                isPositive = true,
                sparklinePoints = listOf(8190f, 8210f, 8205f, 8230f, 8245f, 8225f, 8262.1f),
                high5dFormatted = "8,262.10",
                low5dFormatted = "8,190.00"
            ),
            "dax" to MarketQuoteItem(
                id = "dax",
                titleEn = "DAX 40",
                titleAr = "داكس ألمانيا DAX",
                yahooSymbol = "^GDAXI",
                priceFormatted = "19,322.50",
                rawPrice = 19322.50,
                changePercent = 0.64,
                changeFormatted = "+0.64%",
                isPositive = true,
                sparklinePoints = listOf(19110f, 19180f, 19160f, 19240f, 19290f, 19210f, 19322.5f),
                high5dFormatted = "19,322.50",
                low5dFormatted = "19,110.00"
            ),
            "nikkei" to MarketQuoteItem(
                id = "nikkei",
                titleEn = "Nikkei 225",
                titleAr = "نيكاي اليابان",
                yahooSymbol = "^N225",
                priceFormatted = "38,283.85",
                rawPrice = 38283.85,
                changePercent = 0.72,
                changeFormatted = "+0.72%",
                isPositive = true,
                sparklinePoints = listOf(37890f, 37980f, 38040f, 38120f, 38090f, 38210f, 38283.85f),
                high5dFormatted = "38,283.85",
                low5dFormatted = "37,890.00"
            ),
            "bist" to MarketQuoteItem(
                id = "bist",
                titleEn = "BIST 100",
                titleAr = "بورصة إسطنبول BIST",
                yahooSymbol = "XU100.IS",
                priceFormatted = "9,548.20",
                rawPrice = 9548.20,
                changePercent = 1.18,
                changeFormatted = "+1.18%",
                isPositive = true,
                sparklinePoints = listOf(9320f, 9380f, 9410f, 9460f, 9440f, 9510f, 9548.2f),
                high5dFormatted = "9,548.20",
                low5dFormatted = "9,320.00"
            )
        )

        val commodities = linkedMapOf(
            "gold" to MarketQuoteItem(
                id = "gold",
                titleEn = "Gold",
                titleAr = "الذهب Gold",
                yahooSymbol = "GC=F",
                priceFormatted = "2,685.40",
                rawPrice = 2685.40,
                changePercent = 0.6,
                changeFormatted = "+0.6%",
                isPositive = true,
                sparklinePoints = listOf(2645f, 2658f, 2652f, 2668f, 2675f, 2671f, 2685.4f),
                high5dFormatted = "2,685.40",
                low5dFormatted = "2,645.00"
            ),
            "brent" to MarketQuoteItem(
                id = "brent",
                titleEn = "Brent Oil",
                titleAr = "نفط برنت",
                yahooSymbol = "BZ=F",
                priceFormatted = "73.85",
                rawPrice = 73.85,
                changePercent = -1.1,
                changeFormatted = "-1.1%",
                isPositive = false,
                sparklinePoints = listOf(75.4f, 75.1f, 74.8f, 74.9f, 74.3f, 74.1f, 73.85f),
                high5dFormatted = "75.40",
                low5dFormatted = "73.85"
            ),
            "btc" to MarketQuoteItem(
                id = "btc",
                titleEn = "Bitcoin",
                titleAr = "بيتكوين BTC",
                yahooSymbol = "BTC-USD",
                priceFormatted = "98,450",
                rawPrice = 98450.0,
                changePercent = 2.4,
                changeFormatted = "+2.4%",
                isPositive = true,
                sparklinePoints = listOf(94200f, 95100f, 95800f, 96400f, 97600f, 97200f, 98450f),
                high5dFormatted = "98,450",
                low5dFormatted = "94,200"
            ),
            "eth" to MarketQuoteItem(
                id = "eth",
                titleEn = "Ethereum",
                titleAr = "إيثيريوم ETH",
                yahooSymbol = "ETH-USD",
                priceFormatted = "3,420",
                rawPrice = 3420.0,
                changePercent = 1.8,
                changeFormatted = "+1.8%",
                isPositive = true,
                sparklinePoints = listOf(3280f, 3310f, 3340f, 3325f, 3380f, 3395f, 3420f),
                high5dFormatted = "3,420",
                low5dFormatted = "3,280"
            ),
            "silver" to MarketQuoteItem(
                id = "silver",
                titleEn = "Silver",
                titleAr = "الفضة Silver",
                yahooSymbol = "SI=F",
                priceFormatted = "31.25",
                rawPrice = 31.25,
                changePercent = 0.8,
                changeFormatted = "+0.8%",
                isPositive = true,
                sparklinePoints = listOf(30.6f, 30.8f, 30.75f, 30.95f, 31.1f, 31.05f, 31.25f),
                high5dFormatted = "31.25",
                low5dFormatted = "30.60"
            ),
            "wti" to MarketQuoteItem(
                id = "wti",
                titleEn = "WTI Crude",
                titleAr = "خام غرب تكساس",
                yahooSymbol = "CL=F",
                priceFormatted = "70.10",
                rawPrice = 70.10,
                changePercent = -0.9,
                changeFormatted = "-0.9%",
                isPositive = false,
                sparklinePoints = listOf(71.5f, 71.2f, 70.9f, 71.0f, 70.5f, 70.3f, 70.1f),
                high5dFormatted = "71.50",
                low5dFormatted = "70.10"
            ),
            "natgas" to MarketQuoteItem(
                id = "natgas",
                titleEn = "Natural Gas",
                titleAr = "الغاز الطبيعي",
                yahooSymbol = "NG=F",
                priceFormatted = "3.28",
                rawPrice = 3.28,
                changePercent = 1.5,
                changeFormatted = "+1.5%",
                isPositive = true,
                sparklinePoints = listOf(3.12f, 3.15f, 3.18f, 3.21f, 3.20f, 3.25f, 3.28f),
                high5dFormatted = "3.28",
                low5dFormatted = "3.12"
            ),
            "sol" to MarketQuoteItem(
                id = "sol",
                titleEn = "Solana",
                titleAr = "سولانا SOL",
                yahooSymbol = "SOL-USD",
                priceFormatted = "254.60",
                rawPrice = 254.60,
                changePercent = 3.2,
                changeFormatted = "+3.2%",
                isPositive = true,
                sparklinePoints = listOf(238f, 241f, 245f, 244f, 249f, 252f, 254.6f),
                high5dFormatted = "254.60",
                low5dFormatted = "238.00"
            ),
            "bnb" to MarketQuoteItem(
                id = "bnb",
                titleEn = "BNB",
                titleAr = "باينانس BNB",
                yahooSymbol = "BNB-USD",
                priceFormatted = "658.40",
                rawPrice = 658.40,
                changePercent = 1.4,
                changeFormatted = "+1.4%",
                isPositive = true,
                sparklinePoints = listOf(640f, 644f, 646f, 650f, 652f, 655f, 658.4f),
                high5dFormatted = "658.40",
                low5dFormatted = "640.00"
            ),
            "xrp" to MarketQuoteItem(
                id = "xrp",
                titleEn = "XRP",
                titleAr = "ريبل XRP",
                yahooSymbol = "XRP-USD",
                priceFormatted = "1.485",
                rawPrice = 1.485,
                changePercent = 4.1,
                changeFormatted = "+4.1%",
                isPositive = true,
                sparklinePoints = listOf(1.36f, 1.39f, 1.41f, 1.43f, 1.45f, 1.46f, 1.485f),
                high5dFormatted = "1.485",
                low5dFormatted = "1.360"
            )
        )

        val forex = linkedMapOf(
            "eur_usd" to MarketQuoteItem(
                id = "eur_usd",
                titleEn = "EUR/USD",
                titleAr = "EUR/USD",
                yahooSymbol = "EURUSD=X",
                priceFormatted = "1.0540",
                rawPrice = 1.0540,
                changePercent = -0.08,
                changeFormatted = "-0.08%",
                isPositive = false,
                sparklinePoints = emptyList(),
                high5dFormatted = "1.0580",
                low5dFormatted = "1.0510"
            ),
            "gbp_usd" to MarketQuoteItem(
                id = "gbp_usd",
                titleEn = "GBP/USD",
                titleAr = "GBP/USD",
                yahooSymbol = "GBPUSD=X",
                priceFormatted = "1.2680",
                rawPrice = 1.2680,
                changePercent = 0.12,
                changeFormatted = "+0.12%",
                isPositive = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "1.2710",
                low5dFormatted = "1.2620"
            ),
            "usd_jpy" to MarketQuoteItem(
                id = "usd_jpy",
                titleEn = "USD/JPY",
                titleAr = "USD/JPY",
                yahooSymbol = "JPY=X",
                priceFormatted = "154.20",
                rawPrice = 154.20,
                changePercent = 0.35,
                changeFormatted = "+0.35%",
                isPositive = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "154.80",
                low5dFormatted = "153.60"
            ),
            "usd_sar" to MarketQuoteItem(
                id = "usd_sar",
                titleEn = "USD/SAR",
                titleAr = "USD/SAR",
                yahooSymbol = "SAR=X",
                priceFormatted = "3.7500",
                rawPrice = 3.7500,
                changePercent = 0.00,
                changeFormatted = "0.00%",
                isPositive = true,
                isNeutral = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "3.7510",
                low5dFormatted = "3.7495"
            ),
            "usd_ils" to MarketQuoteItem(
                id = "usd_ils",
                titleEn = "USD/ILS",
                titleAr = "USD/ILS",
                yahooSymbol = "ILS=X",
                priceFormatted = "3.7240",
                rawPrice = 3.7240,
                changePercent = -0.14,
                changeFormatted = "-0.14%",
                isPositive = false,
                sparklinePoints = emptyList(),
                high5dFormatted = "3.7480",
                low5dFormatted = "3.7120"
            ),
            "eur_ils" to MarketQuoteItem(
                id = "eur_ils",
                titleEn = "EUR/ILS",
                titleAr = "EUR/ILS",
                yahooSymbol = "EURILS=X",
                priceFormatted = "3.9250",
                rawPrice = 3.9250,
                changePercent = -0.22,
                changeFormatted = "-0.22%",
                isPositive = false,
                sparklinePoints = emptyList(),
                high5dFormatted = "3.9520",
                low5dFormatted = "3.9100"
            ),
            "jod_ils" to MarketQuoteItem(
                id = "jod_ils",
                titleEn = "JOD/ILS",
                titleAr = "JOD/ILS",
                yahooSymbol = "JODILS=X",
                priceFormatted = "5.2520",
                rawPrice = 5.2520,
                changePercent = -0.10,
                changeFormatted = "-0.10%",
                isPositive = false,
                sparklinePoints = emptyList(),
                high5dFormatted = "5.2700",
                low5dFormatted = "5.2350"
            ),
            "usd_jod" to MarketQuoteItem(
                id = "usd_jod",
                titleEn = "USD/JOD",
                titleAr = "USD/JOD",
                yahooSymbol = "JOD=X",
                priceFormatted = "0.7090",
                rawPrice = 0.7090,
                changePercent = 0.00,
                changeFormatted = "0.00%",
                isPositive = true,
                isNeutral = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "0.7095",
                low5dFormatted = "0.7085"
            ),
            "usd_egp" to MarketQuoteItem(
                id = "usd_egp",
                titleEn = "USD/EGP",
                titleAr = "USD/EGP",
                yahooSymbol = "EGP=X",
                priceFormatted = "49.65",
                rawPrice = 49.65,
                changePercent = 0.18,
                changeFormatted = "+0.18%",
                isPositive = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "49.75",
                low5dFormatted = "49.40"
            ),
            "usd_aed" to MarketQuoteItem(
                id = "usd_aed",
                titleEn = "USD/AED",
                titleAr = "USD/AED",
                yahooSymbol = "AED=X",
                priceFormatted = "3.6725",
                rawPrice = 3.6725,
                changePercent = 0.00,
                changeFormatted = "0.00%",
                isPositive = true,
                isNeutral = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "3.6730",
                low5dFormatted = "3.6720"
            ),
            "usd_qar" to MarketQuoteItem(
                id = "usd_qar",
                titleEn = "USD/QAR",
                titleAr = "USD/QAR",
                yahooSymbol = "QAR=X",
                priceFormatted = "3.6400",
                rawPrice = 3.6400,
                changePercent = 0.00,
                changeFormatted = "0.00%",
                isPositive = true,
                isNeutral = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "3.6410",
                low5dFormatted = "3.6395"
            ),
            "usd_kwd" to MarketQuoteItem(
                id = "usd_kwd",
                titleEn = "USD/KWD",
                titleAr = "USD/KWD",
                yahooSymbol = "KWD=X",
                priceFormatted = "0.3075",
                rawPrice = 0.3075,
                changePercent = 0.04,
                changeFormatted = "+0.04%",
                isPositive = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "0.3080",
                low5dFormatted = "0.3070"
            ),
            "usd_try" to MarketQuoteItem(
                id = "usd_try",
                titleEn = "USD/TRY",
                titleAr = "USD/TRY",
                yahooSymbol = "TRY=X",
                priceFormatted = "34.58",
                rawPrice = 34.58,
                changePercent = 0.24,
                changeFormatted = "+0.24%",
                isPositive = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "34.65",
                low5dFormatted = "34.42"
            ),
            "usd_cad" to MarketQuoteItem(
                id = "usd_cad",
                titleEn = "USD/CAD",
                titleAr = "USD/CAD",
                yahooSymbol = "CAD=X",
                priceFormatted = "1.3980",
                rawPrice = 1.3980,
                changePercent = -0.11,
                changeFormatted = "-0.11%",
                isPositive = false,
                sparklinePoints = emptyList(),
                high5dFormatted = "1.4020",
                low5dFormatted = "1.3950"
            ),
            "usd_chf" to MarketQuoteItem(
                id = "usd_chf",
                titleEn = "USD/CHF",
                titleAr = "USD/CHF",
                yahooSymbol = "CHF=X",
                priceFormatted = "0.8875",
                rawPrice = 0.8875,
                changePercent = 0.09,
                changeFormatted = "+0.09%",
                isPositive = true,
                sparklinePoints = emptyList(),
                high5dFormatted = "0.8900",
                low5dFormatted = "0.8840"
            )
        )

        val calendarEvents = listOf(
            EconomicCalendarEvent(
                timeLabel = "15:30",
                countryCode = "US",
                titleEn = "CPI",
                titleAr = "مؤشر أسعار المستهلك CPI",
                impactLevel = 3,
                actualValue = "--",
                forecastValue = "2.1%",
                previousValue = "2.4%"
            ),
            EconomicCalendarEvent(
                timeLabel = "16:00",
                countryCode = "US",
                titleEn = "Fed Rate Decision",
                titleAr = "قرار الفائدة الفيدرالي",
                impactLevel = 3,
                actualValue = "--",
                forecastValue = "4.75%",
                previousValue = "5.00%"
            ),
            EconomicCalendarEvent(
                timeLabel = "17:30",
                countryCode = "US",
                titleEn = "Nonfarm Payrolls",
                titleAr = "الوظائف غير الزراعية NFP",
                impactLevel = 2,
                actualValue = "--",
                forecastValue = "150K",
                previousValue = "120K"
            ),
            EconomicCalendarEvent(
                timeLabel = "18:00",
                countryCode = "EU",
                titleEn = "GDP q/q",
                titleAr = "الناتج المحلي الأوروبي GDP",
                impactLevel = 2,
                actualValue = "--",
                forecastValue = "0.2%",
                previousValue = "0.2%"
            ),
            EconomicCalendarEvent(
                timeLabel = "19:30",
                countryCode = "US",
                titleEn = "Unemployment Rate",
                titleAr = "معدل البطالة الأمريكي",
                impactLevel = 2,
                actualValue = "--",
                forecastValue = "4.1%",
                previousValue = "4.1%"
            )
        )

        val nowTime = SimpleDateFormat("HH:mm", Locale.US).format(Date())
        return EconomicDashboardData(
            indicesMap = indices,
            commoditiesMap = commodities,
            forexMap = forex,
            calendarEvents = calendarEvents,
            lastUpdatedTime = nowTime,
            fetchedAtMillis = 0L,
            isLoading = false
        )
    }

    fun getLocalizedStrings(languageCode: String): EconomicLocalizedStrings {
        val lang = LocaleHelper.getEffectiveLanguage(languageCode)
        return when (lang) {
            "ar" -> EconomicLocalizedStrings(
                dashboardTitle = "لوحة الاقتصاد والأسواق",
                globalMarketSubtitle = "الأسواق العالمية",
                liveBadge = "مباشر",
                sourceLabel = "المصدر: Investing.com",
                commoditiesAndCryptoTitle = "السلع والعملات الرقمية",
                smooth5DayTitle = "منحنى 5 أيام",
                forexTitle = "أسعار العملات (Forex)",
                economicCalendarTitle = "التقويم الاقتصادي",
                impactLabel = "التأثير:",
                actLabel = "الفعلي:",
                fcLabel = "المتوقع:",
                prevLabel = "السابق:",
                lastUpdatedPrefix = "آخر تحديث:",
                highPrefix = "ع:",
                lowPrefix = "ص:",
                customizeButtonLabel = "تخصيص البورصة والعملات",
                customizeDialogTitle = "إعدادات البورصة والعملات",
                customizeDialogSubtitle = "اختر 4 مؤشرات بورصة، و4 عملات، و4 سلع أو عملات رقمية لعرضها على الشاشة",
                tabIndices = "البورصات والمؤشرات",
                tabForex = "العملات (Forex)",
                tabCommodities = "السلع والكريبتو",
                doneButton = "حفظ وإغلاق",
                resetDefaultButton = "استعادة الافتراضي",
                selectedCountFormat = "تم اختيار %d من 4"
            )
            "fr" -> EconomicLocalizedStrings(
                dashboardTitle = "Tableau Économique",
                globalMarketSubtitle = "Marché Mondial",
                liveBadge = "En direct",
                sourceLabel = "Source : Investing.com",
                commoditiesAndCryptoTitle = "Matières Premières & Crypto",
                smooth5DayTitle = "Courbe 5 jours",
                forexTitle = "Devises (Forex)",
                economicCalendarTitle = "Calendrier Économique",
                impactLabel = "Impact :",
                actLabel = "Act :",
                fcLabel = "Prév :",
                prevLabel = "Préc :",
                lastUpdatedPrefix = "Dernière mise à jour :",
                highPrefix = "H:",
                lowPrefix = "B:",
                customizeButtonLabel = "Personnaliser Devises & Bourse",
                customizeDialogTitle = "Paramètres Bourse & Devises",
                customizeDialogSubtitle = "Sélectionnez 4 indices, 4 devises et 4 matières premières/crypto",
                tabIndices = "Indices Boursiers",
                tabForex = "Devises (Forex)",
                tabCommodities = "Or, Pétrole & Crypto",
                doneButton = "Terminé",
                resetDefaultButton = "Par défaut",
                selectedCountFormat = "%d / 4 sélectionnés"
            )
            "de" -> EconomicLocalizedStrings(
                dashboardTitle = "Wirtschafts-Dashboard",
                globalMarketSubtitle = "Weltmarkt",
                liveBadge = "Live",
                sourceLabel = "Quelle: Investing.com",
                commoditiesAndCryptoTitle = "Rohstoffe und Krypto",
                smooth5DayTitle = "5-Tage-Trend",
                forexTitle = "Devisen (Forex)",
                economicCalendarTitle = "Wirtschaftskalender",
                impactLabel = "Einfluss:",
                actLabel = "Akt:",
                fcLabel = "Prog:",
                prevLabel = "Vorh:",
                lastUpdatedPrefix = "Zuletzt aktualisiert:",
                highPrefix = "H:",
                lowPrefix = "T:",
                customizeButtonLabel = "Börse & Währungen anpassen",
                customizeDialogTitle = "Börsen- & Währungseinstellungen",
                customizeDialogSubtitle = "Wählen Sie jeweils 4 Indizes, Währungen und Rohstoffe/Krypto",
                tabIndices = "Börsenindizes",
                tabForex = "Währungen (Forex)",
                tabCommodities = "Rohstoffe & Krypto",
                doneButton = "Fertig",
                resetDefaultButton = "Standard",
                selectedCountFormat = "%d von 4 ausgewählt"
            )
            "tr" -> EconomicLocalizedStrings(
                dashboardTitle = "Ekonomi Paneli",
                globalMarketSubtitle = "Küresel Piyasa",
                liveBadge = "Canlı",
                sourceLabel = "Kaynak: Investing.com",
                commoditiesAndCryptoTitle = "Emtia ve Kripto",
                smooth5DayTitle = "5 Günlük Grafik",
                forexTitle = "Döviz (Forex)",
                economicCalendarTitle = "Ekonomik Takvim",
                impactLabel = "Etki:",
                actLabel = "Açıklanan:",
                fcLabel = "Beklenti:",
                prevLabel = "Önceki:",
                lastUpdatedPrefix = "Son güncelleme:",
                highPrefix = "Y:",
                lowPrefix = "D:",
                customizeButtonLabel = "Borsa ve Döviz Ayarları",
                customizeDialogTitle = "Borsa ve Döviz Özelleştirme",
                customizeDialogSubtitle = "Ekranda gösterilecek 4 borsa endeksi, 4 döviz kuru ve 4 emtia/kripto seçin",
                tabIndices = "Borsa Endeksleri",
                tabForex = "Döviz Kurları",
                tabCommodities = "Emtia ve Kripto",
                doneButton = "Kaydet",
                resetDefaultButton = "Varsayılan",
                selectedCountFormat = "%d / 4 seçildi"
            )
            "es" -> EconomicLocalizedStrings(
                dashboardTitle = "Panel Económico",
                globalMarketSubtitle = "Mercado Global",
                liveBadge = "En vivo",
                sourceLabel = "Fuente: Investing.com",
                commoditiesAndCryptoTitle = "Materias Primas y Cripto",
                smooth5DayTitle = "Tendencia de 5 días",
                forexTitle = "Divisas (Forex)",
                economicCalendarTitle = "Calendario Económico",
                impactLabel = "Impacto:",
                actLabel = "Act:",
                fcLabel = "Prev:",
                prevLabel = "Ant:",
                lastUpdatedPrefix = "Última actualización:",
                highPrefix = "Máx:",
                lowPrefix = "Mín:",
                customizeButtonLabel = "Personalizar Bolsa y Divisas",
                customizeDialogTitle = "Ajustes de Bolsa y Divisas",
                customizeDialogSubtitle = "Elija 4 índices, 4 divisas y 4 materias primas o cripto",
                tabIndices = "Índices Bursátiles",
                tabForex = "Divisas (Forex)",
                tabCommodities = "Materias Primas y Cripto",
                doneButton = "Listo",
                resetDefaultButton = "Restablecer",
                selectedCountFormat = "%d de 4 seleccionados"
            )
            "ru" -> EconomicLocalizedStrings(
                dashboardTitle = "Экономическая панель",
                globalMarketSubtitle = "Мировой рынок",
                liveBadge = "Live",
                sourceLabel = "Источник: Investing.com",
                commoditiesAndCryptoTitle = "Сырье и Крипто",
                smooth5DayTitle = "График за 5 дней",
                forexTitle = "Валюты (Forex)",
                economicCalendarTitle = "Экономический календарь",
                impactLabel = "Важность:",
                actLabel = "Факт:",
                fcLabel = "Прогн:",
                prevLabel = "Пред:",
                lastUpdatedPrefix = "Обновлено:",
                highPrefix = "Макс:",
                lowPrefix = "Мин:",
                customizeButtonLabel = "Настроить биржи и валюты",
                customizeDialogTitle = "Настройки бирж и валют",
                customizeDialogSubtitle = "Выберите 4 биржевых индекса, 4 валюты и 4 товара/крипто",
                tabIndices = "Биржевые индексы",
                tabForex = "Валюты (Forex)",
                tabCommodities = "Сырье и Крипто",
                doneButton = "Готово",
                resetDefaultButton = "По умолчанию",
                selectedCountFormat = "Выбрано %d из 4"
            )
            "fa" -> EconomicLocalizedStrings(
                dashboardTitle = "داشبورد اقتصادی",
                globalMarketSubtitle = "بازار جهانی",
                liveBadge = "زنده",
                sourceLabel = "منبع: Investing.com",
                commoditiesAndCryptoTitle = "کالاها و ارز دیجیتال",
                smooth5DayTitle = "نمودار ۵ روزه",
                forexTitle = "نرخ ارزها (Forex)",
                economicCalendarTitle = "تقویم اقتصادی",
                impactLabel = "تأثیر:",
                actLabel = "واقعی:",
                fcLabel = "پیش‌بینی:",
                prevLabel = "قبلی:",
                lastUpdatedPrefix = "آخرین بروزرسانی:",
                highPrefix = "بیشینه:",
                lowPrefix = "کمینه:",
                customizeButtonLabel = "تنظیمات بورس و ارزها",
                customizeDialogTitle = "شخصی‌سازی بورس و ارزها",
                customizeDialogSubtitle = "۴ شاخص بورس، ۴ جفت ارز و ۴ کالا یا رمزارز را انتخاب کنید",
                tabIndices = "شاخص‌های بورس",
                tabForex = "ارزها (Forex)",
                tabCommodities = "طلا، نفت و رمزارز",
                doneButton = "ذخیره",
                resetDefaultButton = "پیش‌فرض",
                selectedCountFormat = "%d از ۴ انتخاب شده"
            )
            "ur" -> EconomicLocalizedStrings(
                dashboardTitle = "اقتصادی ڈیش بورڈ",
                globalMarketSubtitle = "عالمی مارکیٹ",
                liveBadge = "لائیو",
                sourceLabel = "ماخذ: Investing.com",
                commoditiesAndCryptoTitle = "اشیاء اور کرپٹو",
                smooth5DayTitle = "5 دن کا رجحان",
                forexTitle = "کرنسی ریٹس (Forex)",
                economicCalendarTitle = "اقتصادی کیلنڈر",
                impactLabel = "اثر:",
                actLabel = "حقیقی:",
                fcLabel = "متوقع:",
                prevLabel = "سابقہ:",
                lastUpdatedPrefix = "آخری اپڈیٹ:",
                highPrefix = "زیادہ:",
                lowPrefix = "کم:",
                customizeButtonLabel = "اسٹاک اور کرنسی ترتیبات",
                customizeDialogTitle = "اسٹاک اور کرنسی منتخب کریں",
                customizeDialogSubtitle = "اسکرین پر دکھانے کے لیے 4 انڈیکس، 4 کرنسیاں اور 4 اشیاء/کرپٹو منتخب کریں",
                tabIndices = "اسٹاک انڈیکس",
                tabForex = "کرنسیاں (Forex)",
                tabCommodities = "سونا، تیل اور کرپٹو",
                doneButton = "محفوظ کریں",
                resetDefaultButton = "طے شدہ",
                selectedCountFormat = "4 میں سے %d منتخب"
            )
            "it" -> EconomicLocalizedStrings(
                dashboardTitle = "Pannello Economico",
                globalMarketSubtitle = "Mercati globali",
                liveBadge = "Live",
                sourceLabel = "Fonte: Investing.com",
                commoditiesAndCryptoTitle = "Materie prime e Crypto",
                smooth5DayTitle = "Andamento 5 giorni",
                forexTitle = "Valute (Forex)",
                economicCalendarTitle = "Calendario Economico",
                impactLabel = "Impatto:",
                actLabel = "Att:",
                fcLabel = "Prev:",
                prevLabel = "Prec:",
                lastUpdatedPrefix = "Aggiornato:",
                highPrefix = "Max:",
                lowPrefix = "Min:",
                customizeButtonLabel = "Personalizza Mercati e Valute",
                customizeDialogTitle = "Impostazioni Mercati e Valute",
                customizeDialogSubtitle = "Scegli 4 indici, 4 valute e 4 materie prime/crypto",
                tabIndices = "Indici",
                tabForex = "Valute (Forex)",
                tabCommodities = "Materie prime & Crypto",
                doneButton = "Fatto",
                resetDefaultButton = "Predefiniti",
                selectedCountFormat = "%d di 4 selezionati"
            )
            "pt" -> EconomicLocalizedStrings(
                dashboardTitle = "Painel Econômico",
                globalMarketSubtitle = "Mercado Global",
                liveBadge = "Ao vivo",
                sourceLabel = "Fonte: Investing.com",
                commoditiesAndCryptoTitle = "Commodities e Cripto",
                smooth5DayTitle = "Tendência 5 dias",
                forexTitle = "Moedas (Forex)",
                economicCalendarTitle = "Calendário Econômico",
                impactLabel = "Impacto:",
                actLabel = "Atual:",
                fcLabel = "Prev:",
                prevLabel = "Ant:",
                lastUpdatedPrefix = "Atualizado:",
                highPrefix = "Máx:",
                lowPrefix = "Mín:",
                customizeButtonLabel = "Personalizar Mercados e Moedas",
                customizeDialogTitle = "Configurações de Mercados",
                customizeDialogSubtitle = "Escolha 4 índices, 4 moedas e 4 commodities/cripto",
                tabIndices = "Índices",
                tabForex = "Moedas (Forex)",
                tabCommodities = "Commodities e Cripto",
                doneButton = "Concluído",
                resetDefaultButton = "Padrão",
                selectedCountFormat = "%d de 4 selecionados"
            )
            "pl" -> EconomicLocalizedStrings(
                dashboardTitle = "Panel Ekonomiczny",
                globalMarketSubtitle = "Rynki Globalne",
                liveBadge = "Na żywo",
                sourceLabel = "Źródło: Investing.com",
                commoditiesAndCryptoTitle = "Surowce i Krypto",
                smooth5DayTitle = "Wykres 5-dniowy",
                forexTitle = "Waluty (Forex)",
                economicCalendarTitle = "Kalendarz Ekonomiczny",
                impactLabel = "Wpływ:",
                actLabel = "Akt:",
                fcLabel = "Prog:",
                prevLabel = "Poprz:",
                lastUpdatedPrefix = "Zaktualizowano:",
                highPrefix = "Maks:",
                lowPrefix = "Min:",
                customizeButtonLabel = "Dostosuj Rynki i Waluty",
                customizeDialogTitle = "Ustawienia Rynków",
                customizeDialogSubtitle = "Wybierz 4 indeksy, 4 waluty i 4 surowce/krypto",
                tabIndices = "Indeksy",
                tabForex = "Waluty (Forex)",
                tabCommodities = "Surowce i Krypto",
                doneButton = "Gotowe",
                resetDefaultButton = "Domyślne",
                selectedCountFormat = "Wybrano %d z 4"
            )
            "nl" -> EconomicLocalizedStrings(
                dashboardTitle = "Economie & Markten",
                globalMarketSubtitle = "Wereldmarkten",
                liveBadge = "Live",
                sourceLabel = "Bron: Investing.com",
                commoditiesAndCryptoTitle = "Grondstoffen & Crypto",
                smooth5DayTitle = "5-daagse trend",
                forexTitle = "Valuta (Forex)",
                economicCalendarTitle = "Economische Kalender",
                impactLabel = "Impact:",
                actLabel = "Act:",
                fcLabel = "Verw:",
                prevLabel = "Vorig:",
                lastUpdatedPrefix = "Bijgewerkt:",
                highPrefix = "Max:",
                lowPrefix = "Min:",
                customizeButtonLabel = "Markten & Valuta aanpassen",
                customizeDialogTitle = "Markten & Valuta Instellingen",
                customizeDialogSubtitle = "Kies 4 indices, 4 valuta's en 4 grondstoffen/crypto",
                tabIndices = "Indices",
                tabForex = "Valuta (Forex)",
                tabCommodities = "Grondstoffen & Crypto",
                doneButton = "Gereed",
                resetDefaultButton = "Standaard",
                selectedCountFormat = "%d van 4 geselecteerd"
            )
            "el" -> EconomicLocalizedStrings(
                dashboardTitle = "Οικονομία & Αγορές",
                globalMarketSubtitle = "Παγκόσμιες Αγορές",
                liveBadge = "Ζωντανά",
                sourceLabel = "Πηγή: Investing.com",
                commoditiesAndCryptoTitle = "Εμπορεύματα & Κρυπτο",
                smooth5DayTitle = "Τάση 5 ημερών",
                forexTitle = "Νομίσματα (Forex)",
                economicCalendarTitle = "Οικονομικό Ημερολόγιο",
                impactLabel = "Επίδραση:",
                actLabel = "Πραγμ:",
                fcLabel = "Πρόβλ:",
                prevLabel = "Προηγ:",
                lastUpdatedPrefix = "Ενημέρωση:",
                highPrefix = "Μέγ:",
                lowPrefix = "Ελάχ:",
                customizeButtonLabel = "Προσαρμογή Αγορών",
                customizeDialogTitle = "Ρυθμίσεις Αγορών & Νομισμάτων",
                customizeDialogSubtitle = "Επιλέξτε 4 δείκτες, 4 νομίσματα και 4 εμπορεύματα",
                tabIndices = "Δείκτες",
                tabForex = "Νομίσματα",
                tabCommodities = "Εμπορεύματα & Κρυπτο",
                doneButton = "Τέλος",
                resetDefaultButton = "Προεπιλογή",
                selectedCountFormat = "%d από 4 επιλεγμένα"
            )
            "id", "in" -> EconomicLocalizedStrings(
                dashboardTitle = "Dasbor Ekonomi",
                globalMarketSubtitle = "Pasar Global",
                liveBadge = "Langsung",
                sourceLabel = "Sumber: Investing.com",
                commoditiesAndCryptoTitle = "Komoditas & Kripto",
                smooth5DayTitle = "Grafik 5 hari",
                forexTitle = "Mata Uang (Forex)",
                economicCalendarTitle = "Kalender Ekonomi",
                impactLabel = "Dampak:",
                actLabel = "Akt:",
                fcLabel = "Perk:",
                prevLabel = "Seb:",
                lastUpdatedPrefix = "Diperbarui:",
                highPrefix = "T:",
                lowPrefix = "R:",
                customizeButtonLabel = "Atur Pasar & Mata Uang",
                customizeDialogTitle = "Pengaturan Pasar & Mata Uang",
                customizeDialogSubtitle = "Pilih 4 indeks, 4 mata uang, dan 4 komoditas/kripto",
                tabIndices = "Indeks Saham",
                tabForex = "Mata Uang",
                tabCommodities = "Komoditas & Kripto",
                doneButton = "Selesai",
                resetDefaultButton = "Reset",
                selectedCountFormat = "%d dari 4 dipilih"
            )
            "vi" -> EconomicLocalizedStrings(
                dashboardTitle = "Bảng Kinh Tế",
                globalMarketSubtitle = "Thị trường Toàn cầu",
                liveBadge = "Trực tiếp",
                sourceLabel = "Nguồn: Investing.com",
                commoditiesAndCryptoTitle = "Hàng hóa & Tiền số",
                smooth5DayTitle = "Xu hướng 5 ngày",
                forexTitle = "Ngoại hối (Forex)",
                economicCalendarTitle = "Lịch Kinh Tế",
                impactLabel = "Tác động:",
                actLabel = "TT:",
                fcLabel = "DB:",
                prevLabel = "Trước:",
                lastUpdatedPrefix = "Cập nhật:",
                highPrefix = "C:",
                lowPrefix = "T:",
                customizeButtonLabel = "Tùy chỉnh Thị trường",
                customizeDialogTitle = "Cài đặt Thị trường & Tiền tệ",
                customizeDialogSubtitle = "Chọn 4 chỉ số, 4 cặp tiền và 4 hàng hóa/tiền số",
                tabIndices = "Chỉ số",
                tabForex = "Tiền tệ (Forex)",
                tabCommodities = "Hàng hóa & Crypto",
                doneButton = "Xong",
                resetDefaultButton = "Mặc định",
                selectedCountFormat = "Đã chọn %d/4"
            )
            "th" -> EconomicLocalizedStrings(
                dashboardTitle = "แดชบอร์ดเศรษฐกิจ",
                globalMarketSubtitle = "ตลาดโลก",
                liveBadge = "สด",
                sourceLabel = "ที่มา: Investing.com",
                commoditiesAndCryptoTitle = "สินค้าโภคภัณฑ์และคริปโต",
                smooth5DayTitle = "แนวโน้ม 5 วัน",
                forexTitle = "อัตราแลกเปลี่ยน (Forex)",
                economicCalendarTitle = "ปฏิทินเศรษฐกิจ",
                impactLabel = "ผลกระทบ:",
                actLabel = "จริง:",
                fcLabel = "คาด:",
                prevLabel = "ก่อน:",
                lastUpdatedPrefix = "อัปเดต:",
                highPrefix = "สูง:",
                lowPrefix = "ต่ำ:",
                customizeButtonLabel = "ปรับแต่งตลาดและสกุลเงิน",
                customizeDialogTitle = "ตั้งค่าตลาดและสกุลเงิน",
                customizeDialogSubtitle = "เลือก 4 ดัชนี 4 สกุลเงิน และ 4 สินค้า/คริปโต",
                tabIndices = "ดัชนีหุ้น",
                tabForex = "สกุลเงิน",
                tabCommodities = "สินค้าและคริปโต",
                doneButton = "เสร็จสิ้น",
                resetDefaultButton = "ค่าเริ่มต้น",
                selectedCountFormat = "เลือกแล้ว %d จาก 4"
            )
            "zh" -> EconomicLocalizedStrings(
                dashboardTitle = "财经与市场看板",
                globalMarketSubtitle = "全球市场",
                liveBadge = "实时",
                sourceLabel = "来源: Investing.com",
                commoditiesAndCryptoTitle = "大宗商品与加密货币",
                smooth5DayTitle = "5日走势",
                forexTitle = "外汇汇率 (Forex)",
                economicCalendarTitle = "财经日历",
                impactLabel = "影响:",
                actLabel = "公布:",
                fcLabel = "预期:",
                prevLabel = "前值:",
                lastUpdatedPrefix = "更新于:",
                highPrefix = "高:",
                lowPrefix = "低:",
                customizeButtonLabel = "自定义市场与货币",
                customizeDialogTitle = "市场与货币设置",
                customizeDialogSubtitle = "选择4个指数、4种汇率和4种商品/加密货币",
                tabIndices = "股票指数",
                tabForex = "外汇汇率",
                tabCommodities = "商品与加密",
                doneButton = "完成",
                resetDefaultButton = "恢复默认",
                selectedCountFormat = "已选 %d/4"
            )
            "ja" -> EconomicLocalizedStrings(
                dashboardTitle = "経済・マーケット",
                globalMarketSubtitle = "グローバル市場",
                liveBadge = "ライブ",
                sourceLabel = "出典: Investing.com",
                commoditiesAndCryptoTitle = "商品・暗号資産",
                smooth5DayTitle = "5日間推移",
                forexTitle = "為替 (Forex)",
                economicCalendarTitle = "経済指標カレンダー",
                impactLabel = "重要度:",
                actLabel = "結果:",
                fcLabel = "予想:",
                prevLabel = "前回:",
                lastUpdatedPrefix = "更新:",
                highPrefix = "高:",
                lowPrefix = "安:",
                customizeButtonLabel = "市場・通貨をカスタマイズ",
                customizeDialogTitle = "市場・通貨設定",
                customizeDialogSubtitle = "指数4件・通貨4件・商品/暗号資産4件を選択",
                tabIndices = "株価指数",
                tabForex = "為替 (Forex)",
                tabCommodities = "商品・暗号資産",
                doneButton = "完了",
                resetDefaultButton = "初期値",
                selectedCountFormat = "%d/4 選択中"
            )
            "ko" -> EconomicLocalizedStrings(
                dashboardTitle = "경제 및 시장",
                globalMarketSubtitle = "글로벌 시장",
                liveBadge = "실시간",
                sourceLabel = "출처: Investing.com",
                commoditiesAndCryptoTitle = "원자재 및 암호화폐",
                smooth5DayTitle = "5일 추이",
                forexTitle = "환율 (Forex)",
                economicCalendarTitle = "경제 캘린더",
                impactLabel = "영향:",
                actLabel = "실제:",
                fcLabel = "예상:",
                prevLabel = "이전:",
                lastUpdatedPrefix = "업데이트:",
                highPrefix = "고:",
                lowPrefix = "저:",
                customizeButtonLabel = "시장 및 통화 설정",
                customizeDialogTitle = "시장 및 통화 설정",
                customizeDialogSubtitle = "지수 4개, 환율 4개, 원자재/암호화폐 4개 선택",
                tabIndices = "주가지수",
                tabForex = "환율 (Forex)",
                tabCommodities = "원자재/암호화폐",
                doneButton = "완료",
                resetDefaultButton = "초기화",
                selectedCountFormat = "4개 중 %d개 선택"
            )
            "hi" -> EconomicLocalizedStrings(
                dashboardTitle = "अर्थव्यवस्था और बाज़ार",
                globalMarketSubtitle = "वैश्विक बाज़ार",
                liveBadge = "लाइव",
                sourceLabel = "स्रोत: Investing.com",
                commoditiesAndCryptoTitle = "कमोडिटी और क्रिप्टो",
                smooth5DayTitle = "5-दिन का रुझान",
                forexTitle = "मुद्रा (Forex)",
                economicCalendarTitle = "आर्थिक कैलेंडर",
                impactLabel = "प्रभाव:",
                actLabel = "वास्त:",
                fcLabel = "अनुमान:",
                prevLabel = "पिछला:",
                lastUpdatedPrefix = "अपडेट:",
                highPrefix = "उच्च:",
                lowPrefix = "न्यून:",
                customizeButtonLabel = "बाज़ार और मुद्रा चुनें",
                customizeDialogTitle = "बाज़ार और मुद्रा सेटिंग्स",
                customizeDialogSubtitle = "4 सूचकांक, 4 मुद्राएँ और 4 कमोडिटी/क्रिप्टो चुनें",
                tabIndices = "शेयर सूचकांक",
                tabForex = "मुद्राएँ (Forex)",
                tabCommodities = "कमोडिटी व क्रिप्टो",
                doneButton = "पूर्ण",
                resetDefaultButton = "डिफ़ॉल्ट",
                selectedCountFormat = "4 में से %d चयनित"
            )
            else -> EconomicLocalizedStrings(
                dashboardTitle = "Economic Dashboard",
                globalMarketSubtitle = "Global Market",
                liveBadge = "Live",
                sourceLabel = "Source: Investing.com",
                commoditiesAndCryptoTitle = "Commodities and Crypto",
                smooth5DayTitle = "Smooth 5-day",
                forexTitle = "Forex",
                economicCalendarTitle = "Economic Calendar",
                impactLabel = "Impact:",
                actLabel = "Act:",
                fcLabel = "Fc:",
                prevLabel = "Prev:",
                lastUpdatedPrefix = "Last updated:",
                highPrefix = "H:",
                lowPrefix = "L:",
                customizeButtonLabel = "Customize Markets & Currencies",
                customizeDialogTitle = "Markets & Currencies Settings",
                customizeDialogSubtitle = "Choose which 4 Stock Indices, 4 Currencies, and 4 Commodities/Crypto appear on screen",
                tabIndices = "Stock Indices",
                tabForex = "Currencies (Forex)",
                tabCommodities = "Commodities & Crypto",
                doneButton = "Done",
                resetDefaultButton = "Reset Default",
                selectedCountFormat = "%d of 4 selected"
            )
        }
    }

    fun getForexCurrencyDescription(id: String, languageCode: String): String {
        val eff = LocaleHelper.getEffectiveLanguage(languageCode)
        val isAr = eff in setOf("ar", "fa", "ur")
        return when (id) {
            "eur_usd" -> if (isAr) "EUR/USD — يورو / دولار أمريكي" else "EUR/USD — Euro / US Dollar"
            "gbp_usd" -> if (isAr) "GBP/USD — جنيه إسترليني / دولار" else "GBP/USD — British Pound / USD"
            "usd_jpy" -> if (isAr) "USD/JPY — دولار / ين ياباني" else "USD/JPY — US Dollar / Japanese Yen"
            "usd_sar" -> if (isAr) "USD/SAR — دولار / ريال سعودي" else "USD/SAR — US Dollar / Saudi Riyal"
            "usd_ils" -> if (isAr) "USD/ILS — دولار / شيكل" else "USD/ILS — US Dollar / Shekel"
            "eur_ils" -> if (isAr) "EUR/ILS — يورو / شيكل" else "EUR/ILS — Euro / Shekel"
            "jod_ils" -> if (isAr) "JOD/ILS — دينار أردني / شيكل" else "JOD/ILS — Jordanian Dinar / Shekel"
            "usd_jod" -> if (isAr) "USD/JOD — دولار / دينار أردني" else "USD/JOD — US Dollar / Jordanian Dinar"
            "usd_egp" -> if (isAr) "USD/EGP — دولار / جنيه مصري" else "USD/EGP — US Dollar / Egyptian Pound"
            "usd_aed" -> if (isAr) "USD/AED — دولار / درهم إماراتي" else "USD/AED — US Dollar / UAE Dirham"
            "usd_qar" -> if (isAr) "USD/QAR — دولار / ريال قطري" else "USD/QAR — US Dollar / Qatari Riyal"
            "usd_kwd" -> if (isAr) "USD/KWD — دولار / دينار كويتي" else "USD/KWD — US Dollar / Kuwaiti Dinar"
            "usd_try" -> if (isAr) "USD/TRY — دولار / ليرة تركية" else "USD/TRY — US Dollar / Turkish Lira"
            "usd_cad" -> if (isAr) "USD/CAD — دولار / دولار كندي" else "USD/CAD — US Dollar / Canadian Dollar"
            "usd_chf" -> if (isAr) "USD/CHF — دولار / فرنك سويسري" else "USD/CHF — US Dollar / Swiss Franc"
            else -> id.uppercase(Locale.US)
        }
    }

    private fun formatNumber(value: Double, decimals: Int, useGrouping: Boolean): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = buildString {
            append(if (useGrouping) "#,##0" else "0")
            if (decimals > 0) {
                append(".")
                repeat(decimals) { append("0") }
            }
        }
        return DecimalFormat(pattern, symbols).format(value)
    }

    private fun formatSignedPercent(value: Double, decimals: Int): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = buildString {
            append("0.")
            repeat(decimals) { append("0") }
        }
        val formatted = DecimalFormat(pattern, symbols).format(kotlin.math.abs(value))
        return when {
            kotlin.math.abs(value) < 0.005 -> "$formatted%"
            value > 0 -> "+$formatted%"
            else -> "-$formatted%"
        }
    }

    private fun httpGet(urlStr: String, timeoutMs: Int): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlStr)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android TV; SalsaTVLauncher/${com.example.BuildConfig.VERSION_NAME})")
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
