package com.example.data.football

import android.content.Context
import com.example.util.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class MatchStatusType {
    LIVE, FINISHED, UPCOMING
}

data class FootballLeagueCatalogItem(
    val id: String,
    val scores365CompId: Int,
    val titleEn: String,
    val titleAr: String,
    val countryEn: String,
    val countryAr: String
) {
    fun getLocalizedTitle(langCode: String): String {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) titleAr else titleEn
    }

    fun getLocalizedCountry(langCode: String): String {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) countryAr else countryEn
    }
}

data class FootballGoalEvent(
    val playerNameEn: String,
    val playerNameAr: String,
    val minuteLabel: String,
    val isHomeTeam: Boolean,
    val localizedPlayer: String = ""
) {
    fun getLocalizedPlayer(langCode: String): String {
        if (localizedPlayer.isNotBlank()) return localizedPlayer
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) playerNameAr else playerNameEn
    }
}

data class FootballMatchItem(
    val id: String,
    val homeCode: String,
    val homeNameEn: String,
    val homeNameAr: String,
    val homePrimaryColor: Long,
    val homeSecondaryColor: Long,
    val homeCrestStyle: String = "shield",
    val homeLogoUrl: String = "",
    val awayCode: String,
    val awayNameEn: String,
    val awayNameAr: String,
    val awayPrimaryColor: Long,
    val awaySecondaryColor: Long,
    val awayCrestStyle: String = "circle",
    val awayLogoUrl: String = "",
    val homeScore: Int?,
    val awayScore: Int?,
    val statusType: MatchStatusType,
    val statusBadgeText: String,
    val progressRatio: Float,
    val goalEvents: List<FootballGoalEvent> = emptyList(),
    val homeNameLocalized: String = "",
    val awayNameLocalized: String = "",
    val probabilities: MatchProbabilities? = null
) {
    fun getLocalizedHomeName(langCode: String): String {
        if (homeNameLocalized.isNotBlank()) return homeNameLocalized
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) homeNameAr else homeNameEn
    }

    fun getLocalizedAwayName(langCode: String): String {
        if (awayNameLocalized.isNotBlank()) return awayNameLocalized
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) awayNameAr else awayNameEn
    }
}

data class TopScorerItem(
    val rank: Int,
    val playerNameEn: String,
    val playerNameAr: String,
    val teamCode: String,
    val goals: Int,
    val maxGoals: Int,
    val localizedPlayerName: String = ""
) {
    fun getLocalizedPlayerName(langCode: String): String {
        if (localizedPlayerName.isNotBlank()) return localizedPlayerName
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) playerNameAr else playerNameEn
    }
}

data class LeagueStandingRow(
    val rank: Int,
    val teamCode: String,
    val teamNameEn: String,
    val teamNameAr: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val crestStyle: String = "circle",
    val logoUrl: String = "",
    val played: Int,
    val goalDiff: String,
    val points: Int,
    val form5: List<Char>,
    val isTopQualification: Boolean = rank <= 4,
    val localizedTeamName: String = ""
) {
    fun getLocalizedTeamName(langCode: String): String {
        if (localizedTeamName.isNotBlank()) return localizedTeamName
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) teamNameAr else teamNameEn
    }
}

data class FootballHeadlineItem(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val sourceName: String,
    val timeAgoEn: String,
    val timeAgoAr: String,
    val imageUrl: String,
    val visualTheme: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val localizedTitle: String = "",
    val localizedTimeAgo: String = ""
) {
    fun getLocalizedTitle(langCode: String): String {
        if (localizedTitle.isNotBlank()) return localizedTitle
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) titleAr else titleEn
    }

    fun getLocalizedMeta(langCode: String): String {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        val time = localizedTimeAgo.ifBlank {
            if (eff in setOf("ar", "fa", "ur")) timeAgoAr else timeAgoEn
        }
        return if (sourceName.isNotBlank()) "$sourceName • $time" else time
    }
}

data class LeagueDashboardData(
    val leagueId: String,
    val featuredMatch: FootballMatchItem,
    val fixtures: List<FootballMatchItem>,
    val topScorers: List<TopScorerItem>,
    val standings: List<LeagueStandingRow>,
    val headlines: List<FootballHeadlineItem>,
    val fetchedLanguage: String = "",
    val fetchedAtMillis: Long = 0L,
    val isLiveFrom365Scores: Boolean = false
)

data class FootballCenterState(
    val leaguesData: Map<String, LeagueDashboardData>,
    val lastUpdatedTime: String,
    val fetchedAtMillis: Long,
    val activeLanguage: String = "",
    val sourceName: String = "365Scores",
    val isLoading: Boolean = false
)

data class FootballLocalizedStrings(
    val footballCenterTitle: String,
    val liveBadge: String,
    val ftBadge: String,
    val todaysFixturesTitle: String,
    val topScorersTitle: String,
    val leagueTableTitle: String,
    val latestHeadlinesTitle: String,
    val colPlayed: String,
    val colGoalDiff: String,
    val colPoints: String,
    val colForm5: String,
    val lastUpdatedPrefix: String,
    val customizeLeaguesBtn: String,
    val customizeDialogTitle: String,
    val customizeDialogSubtitle: String,
    val resetDefaults: String,
    val doneAction: String,
    val backToHomeHint: String,
    val refreshAction: String,
    val sourcePrefix: String,
    val winProbabilityTitle: String,
    val drawLabel: String,
    val leaguesTabTitle: String,
    val clubsTabTitle: String,
    val customizeClubsSubtitle: String,
    val followedLabel: String
)

object FootballRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fetchMutex = Mutex()

    private const val SCORES365_BASE = "https://webws.365scores.com/web"
    private const val SCORES365_IMG_BASE = "https://imagecache.365scores.com/image/upload"

    val FALLBACK_HEADLINE_PHOTOS: List<String> = listOf(
        "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=480&q=80&fm=jpg&fit=crop",
        "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=480&q=80&fm=jpg&fit=crop",
        "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=480&q=80&fm=jpg&fit=crop",
        "https://images.unsplash.com/photo-1518091043644-c1d4457512c6?w=480&q=80&fm=jpg&fit=crop"
    )

    val ALL_LEAGUES: List<FootballLeagueCatalogItem> = FootballCatalogs.ALL_LEAGUES
    val ALL_CLUBS: List<FootballClubCatalogItem> = FootballCatalogs.ALL_CLUBS

    private val _footballState = MutableStateFlow(buildBaselineState())
    val footballState: StateFlow<FootballCenterState> = _footballState.asStateFlow()

    /**
     * Maps the app's effective language code to the native 365Scores API `langId`.
     * 365Scores supports Arabic (27), English (1), French (3), German (5), Italian (4),
     * Portuguese (6), Turkish (7), Russian (8), Spanish (29).
     */
    fun get365ScoresLangId(langCode: String): Int {
        return when (LocaleHelper.getEffectiveLanguage(langCode)) {
            "ar" -> 27
            "fr" -> 3
            "it" -> 4
            "de" -> 5
            "pt" -> 6
            "tr" -> 7
            "ru" -> 8
            "es" -> 29
            else -> 1
        }
    }

    fun ensureInitialized(
        context: Context,
        appLanguage: String = "en",
        activeLeagueId: String = "premier_league",
        selectedLeagueIds: List<String> = listOf("premier_league", "la_liga", "serie_a", "champions_league"),
        forceRefresh: Boolean = false
    ) {
        val effLang = LocaleHelper.getEffectiveLanguage(appLanguage)
        val current = _footballState.value
        val activeData = current.leaguesData[activeLeagueId]
        val now = System.currentTimeMillis()
        val isActiveStale = activeData == null ||
            activeData.fetchedLanguage != effLang ||
            activeData.fetchedAtMillis == 0L ||
            (now - activeData.fetchedAtMillis) >= 3 * 60 * 1000L

        if (!forceRefresh && !isActiveStale) {
            return
        }

        scope.launch {
            refreshFootballData(
                appLanguage = effLang,
                priorityLeagueId = activeLeagueId,
                selectedLeagueIds = selectedLeagueIds,
                force = forceRefresh || isActiveStale
            )
        }
    }

    suspend fun refreshFootballData(
        appLanguage: String = "en",
        priorityLeagueId: String = "premier_league",
        selectedLeagueIds: List<String> = listOf("premier_league", "la_liga", "serie_a", "champions_league"),
        force: Boolean = false
    ) {
        val effLang = LocaleHelper.getEffectiveLanguage(appLanguage)
        val langId = get365ScoresLangId(effLang)
        val tzEncoded = try {
            URLEncoder.encode(TimeZone.getDefault().id, "UTF-8")
        } catch (_: Exception) {
            "UTC"
        }

        fetchMutex.withLock {
            val current = _footballState.value
            val now = System.currentTimeMillis()
            val priorityExisting = current.leaguesData[priorityLeagueId]
            val priorityFresh = priorityExisting != null &&
                priorityExisting.fetchedLanguage == effLang &&
                priorityExisting.fetchedAtMillis > 0L &&
                (now - priorityExisting.fetchedAtMillis) < 90 * 1000L

            if (!force && priorityFresh) {
                return
            }

            _footballState.value = current.copy(isLoading = true, activeLanguage = effLang)

            // Prioritize the currently viewed league first, then the remaining selected leagues
            val orderedIds = buildList {
                add(priorityLeagueId)
                selectedLeagueIds.forEach { if (it != priorityLeagueId) add(it) }
            }.distinct()

            val updatedLeagues = current.leaguesData.toMutableMap()
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

            for ((idx, leagueId) in orderedIds.withIndex()) {
                val league = ALL_LEAGUES.find { it.id == leagueId } ?: continue
                val existing = updatedLeagues[leagueId]
                val alreadyFreshForLang = existing != null &&
                    existing.fetchedLanguage == effLang &&
                    existing.fetchedAtMillis > 0L &&
                    (System.currentTimeMillis() - existing.fetchedAtMillis) < 3 * 60 * 1000L

                if (!force && idx > 0 && alreadyFreshForLang) {
                    continue
                }

                try {
                    val fetched = fetch365ScoresLeagueData(
                        league = league,
                        langId = langId,
                        effLang = effLang,
                        tzEncoded = tzEncoded,
                        fallback = existing ?: buildBaselineState().leaguesData[leagueId]
                    )
                    if (fetched != null) {
                        updatedLeagues[leagueId] = fetched
                        // Push immediately after the active/priority league loads so UI updates fast!
                        if (idx == 0) {
                            _footballState.value = FootballCenterState(
                                leaguesData = updatedLeagues.toMap(),
                                lastUpdatedTime = timeFormat.format(Date()),
                                fetchedAtMillis = System.currentTimeMillis(),
                                activeLanguage = effLang,
                                sourceName = "365Scores",
                                isLoading = orderedIds.size > 1
                            )
                        }
                    }
                } catch (_: Exception) {
                    // Keep baseline data seamlessly if offline or rate-limited
                }
            }

            _footballState.value = FootballCenterState(
                leaguesData = updatedLeagues.toMap(),
                lastUpdatedTime = timeFormat.format(Date()),
                fetchedAtMillis = System.currentTimeMillis(),
                activeLanguage = effLang,
                sourceName = "365Scores",
                isLoading = false
            )
        }
    }

    private suspend fun fetch365ScoresLeagueData(
        league: FootballLeagueCatalogItem,
        langId: Int,
        effLang: String,
        tzEncoded: String,
        fallback: LeagueDashboardData?
    ): LeagueDashboardData? = coroutineScope {
        val base = fallback ?: return@coroutineScope null
        val compId = league.scores365CompId
        val commonParams = "appTypeId=5&langId=$langId&timezoneName=$tzEncoded&userCountryId=-1&competitions=$compId"

        // Fetch Fixtures/Games, Standings, Top Scorers, and News concurrently from 365Scores
        val gamesDeferred = async(Dispatchers.IO) {
            fetch365Games(compId, langId, tzEncoded, effLang)
        }
        val standingsDeferred = async(Dispatchers.IO) {
            fetch365Standings(commonParams)
        }
        val scorersDeferred = async(Dispatchers.IO) {
            fetch365TopScorers(commonParams)
        }
        val headlinesDeferred = async(Dispatchers.IO) {
            fetch365News(commonParams, effLang)
        }

        val liveGames = gamesDeferred.await()
        val liveStandings = standingsDeferred.await()
        val liveScorers = scorersDeferred.await()
        val liveHeadlines = headlinesDeferred.await()

        val gotAnyLive = liveGames.isNotEmpty() || liveStandings.isNotEmpty() || liveScorers.isNotEmpty() || liveHeadlines.isNotEmpty()

        val finalFixtures = if (liveGames.isNotEmpty()) {
            liveGames.take(10)
        } else {
            base.fixtures
        }

        var finalFeatured = if (liveGames.isNotEmpty()) {
            liveGames.firstOrNull { it.statusType == MatchStatusType.LIVE }
                ?: liveGames.firstOrNull { it.statusType == MatchStatusType.FINISHED }
                ?: liveGames.first()
        } else {
            base.featuredMatch
        }

        // If the featured match from 365Scores has goals and no events yet, fetch exact localized goal scorers
        val totalGoals = (finalFeatured.homeScore ?: 0) + (finalFeatured.awayScore ?: 0)
        if (liveGames.isNotEmpty() && totalGoals > 0 && finalFeatured.goalEvents.isEmpty()) {
            val gameIdLong = finalFeatured.id.toLongOrNull()
            if (gameIdLong != null) {
                val events = fetch365GameGoalEvents(gameIdLong, langId, tzEncoded)
                if (events.isNotEmpty()) {
                    finalFeatured = finalFeatured.copy(goalEvents = events)
                }
            }
        }

        // Keep featured match synced inside fixtures list
        val syncedFixtures = finalFixtures.map { m ->
            if (m.id == finalFeatured.id) finalFeatured else m
        }

        val updated = base.copy(
            featuredMatch = finalFeatured,
            fixtures = syncedFixtures,
            topScorers = if (liveScorers.isNotEmpty()) liveScorers.take(4) else base.topScorers,
            standings = if (liveStandings.isNotEmpty()) liveStandings.take(20) else base.standings,
            headlines = if (liveHeadlines.isNotEmpty()) liveHeadlines.take(4) else base.headlines,
            fetchedLanguage = effLang,
            fetchedAtMillis = System.currentTimeMillis(),
            isLiveFrom365Scores = gotAnyLive
        )
        FootballCatalogs.enrichLeagueWithProbabilities(updated)
    }

    private fun fetch365Games(
        compId: Int,
        langId: Int,
        tzEncoded: String,
        effLang: String
    ): List<FootballMatchItem> {
        val baseParams = "appTypeId=5&langId=$langId&timezoneName=$tzEncoded&userCountryId=-1&competitions=$compId"
        val collected = LinkedHashMap<String, FootballMatchItem>()

        // 1. Current matchday games (live + today's results + upcoming) with odds/win probabilities enabled
        val endpoints = listOf(
            "$SCORES365_BASE/games/current/?$baseParams&showOdds=true&includeTopBettingOpportunity=0",
            "$SCORES365_BASE/games/allscores/?$baseParams&showOdds=true&includeTopBettingOpportunity=0",
            "$SCORES365_BASE/games/results/?$baseParams",
            "$SCORES365_BASE/games/fixtures/?$baseParams&showOdds=true"
        )

        for (endpoint in endpoints) {
            if (collected.size >= 10) break
            val json = httpGet(endpoint, timeoutMs = 4500) ?: continue
            val parsed = parse365GamesJson(json, effLang)
            for (item in parsed) {
                if (!collected.containsKey(item.id)) {
                    collected[item.id] = item
                }
            }
        }

        if (collected.isEmpty()) return emptyList()

        // Order: LIVE first, then FINISHED (most recent), then UPCOMING (nearest)
        return collected.values
            .sortedWith(
                compareBy<FootballMatchItem> {
                    when (it.statusType) {
                        MatchStatusType.LIVE -> 0
                        MatchStatusType.FINISHED -> 1
                        MatchStatusType.UPCOMING -> 2
                    }
                }
            )
            .take(10)
    }

    private fun parse365GamesJson(jsonStr: String, effLang: String): List<FootballMatchItem> {
        val list = mutableListOf<FootballMatchItem>()
        try {
            val root = JSONObject(jsonStr)
            val games = root.optJSONArray("games") ?: return emptyList()
            val isAr = effLang in setOf("ar", "fa", "ur")

            for (i in 0 until games.length()) {
                val g = games.optJSONObject(i) ?: continue
                val gameId = g.optLong("id", 0L).takeIf { it > 0L }?.toString() ?: continue
                val home = g.optJSONObject("homeCompetitor") ?: continue
                val away = g.optJSONObject("awayCompetitor") ?: continue

                val homeId = home.optInt("id", 0)
                val awayId = away.optInt("id", 0)
                val homeName = home.optString("shortName").ifBlank { home.optString("name", "HOME") }.trim()
                val awayName = away.optString("shortName").ifBlank { away.optString("name", "AWAY") }.trim()
                val homeCode = home.optString("symbolicName")
                    .ifBlank { deriveShortCode(homeName) }
                    .uppercase(Locale.US)
                    .take(3)
                val awayCode = away.optString("symbolicName")
                    .ifBlank { deriveShortCode(awayName) }
                    .uppercase(Locale.US)
                    .take(3)

                val homeImgVer = home.optInt("imageVersion", 5).coerceAtLeast(1)
                val awayImgVer = away.optInt("imageVersion", 5).coerceAtLeast(1)
                val homeLogo = if (homeId > 0) build365CompetitorLogoUrl(homeId, homeImgVer) else ""
                val awayLogo = if (awayId > 0) build365CompetitorLogoUrl(awayId, awayImgVer) else ""

                val homeColor = parseHexColor(home.optString("color"), 0xFFEF0107L)
                val awayColor = parseHexColor(away.optString("color"), 0xFF034694L)
                val homeSecColor = parseHexColor(home.optString("awayColor"), 0xFFFFFFFFL)
                val awaySecColor = parseHexColor(away.optString("awayColor"), 0xFFFFFFFFL)

                // 365Scores statusGroup: 2 = Scheduled/Upcoming, 3 = Live/Active, 4 = Finished
                val statusGroup = g.optInt("statusGroup", 2)
                val statusType = when (statusGroup) {
                    3 -> MatchStatusType.LIVE
                    4 -> MatchStatusType.FINISHED
                    else -> MatchStatusType.UPCOMING
                }

                val rawHomeScore = home.optDouble("score", -1.0)
                val rawAwayScore = away.optDouble("score", -1.0)
                val homeScore = if (statusType != MatchStatusType.UPCOMING && rawHomeScore >= 0.0) rawHomeScore.toInt() else null
                val awayScore = if (statusType != MatchStatusType.UPCOMING && rawAwayScore >= 0.0) rawAwayScore.toInt() else null

                val gameTimeDisplay = g.optString("gameTimeDisplay", "").trim()
                val gameTimeNum = g.optDouble("gameTime", -1.0).toInt()
                val shortStatusText = g.optString("shortStatusText", "").trim()
                val statusText = g.optString("statusText", "").trim()
                val startTimeStr = g.optString("startTime", "")

                val badgeText = when (statusType) {
                    MatchStatusType.LIVE -> {
                        when {
                            gameTimeDisplay.isNotBlank() -> if (gameTimeDisplay.endsWith("'")) gameTimeDisplay else "$gameTimeDisplay'"
                            gameTimeNum in 1..125 -> "$gameTimeNum'"
                            shortStatusText.isNotBlank() -> shortStatusText
                            else -> if (isAr) "مباشر" else "LIVE"
                        }
                    }
                    MatchStatusType.FINISHED -> shortStatusText.ifBlank { if (isAr) "انتهت" else "FT" }
                    MatchStatusType.UPCOMING -> extractLocalKickoffTime(startTimeStr).ifBlank {
                        shortStatusText.ifBlank { "20:00" }
                    }
                }

                val progress = when (statusType) {
                    MatchStatusType.LIVE -> {
                        val m = if (gameTimeNum > 0) gameTimeNum else gameTimeDisplay.filter { it.isDigit() }.toIntOrNull() ?: 45
                        (m / 90f).coerceIn(0.08f, 0.96f)
                    }
                    MatchStatusType.FINISHED -> 1.0f
                    MatchStatusType.UPCOMING -> 0.0f
                }

                // Parse inline events & live win probabilities if 365Scores included them
                val events = parse365EventsArray(g.optJSONArray("events"), g.optJSONArray("members"), homeId)
                val liveProbs = parse365Probabilities(g)

                list.add(
                    FootballMatchItem(
                        id = gameId,
                        homeCode = homeCode,
                        homeNameEn = homeName,
                        homeNameAr = homeName,
                        homePrimaryColor = homeColor,
                        homeSecondaryColor = homeSecColor,
                        homeCrestStyle = if (i % 2 == 0) "shield" else "circle",
                        homeLogoUrl = homeLogo,
                        awayCode = awayCode,
                        awayNameEn = awayName,
                        awayNameAr = awayName,
                        awayPrimaryColor = awayColor,
                        awaySecondaryColor = awaySecColor,
                        awayCrestStyle = if (i % 2 == 0) "circle" else "shield",
                        awayLogoUrl = awayLogo,
                        homeScore = homeScore,
                        awayScore = awayScore,
                        statusType = statusType,
                        statusBadgeText = badgeText,
                        progressRatio = progress,
                        goalEvents = events,
                        homeNameLocalized = homeName,
                        awayNameLocalized = awayName,
                        probabilities = liveProbs
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun parse365Probabilities(gameObj: JSONObject): MatchProbabilities? {
        try {
            // 1. Check 365Scores mainOdds / odds (decimal 1-X-2 odds converted to normalized win percentages)
            val mainOdds = gameObj.optJSONObject("mainOdds")
                ?: gameObj.optJSONArray("odds")?.optJSONObject(0)
            val options = mainOdds?.optJSONArray("options")
            if (options != null && options.length() >= 3) {
                var r1 = 0.0
                var rX = 0.0
                var r2 = 0.0
                for (idx in 0 until options.length()) {
                    val opt = options.optJSONObject(idx) ?: continue
                    val rate = opt.optJSONObject("rate")?.optDouble("decimal", 0.0)
                        ?: opt.optDouble("rate", 0.0)
                    if (rate <= 1.01) continue
                    val name = opt.optString("name", "").trim().uppercase(Locale.US)
                    val num = opt.optInt("num", idx + 1)
                    when {
                        name == "1" || num == 1 -> r1 = rate
                        name == "X" || num == 2 -> rX = rate
                        name == "2" || num == 3 -> r2 = rate
                    }
                }
                if (r1 > 1.0 && rX > 1.0 && r2 > 1.0) {
                    val inv1 = 1.0 / r1
                    val invX = 1.0 / rX
                    val inv2 = 1.0 / r2
                    val sum = inv1 + invX + inv2
                    if (sum > 0.0) {
                        val h = ((inv1 / sum) * 100.0).toInt().coerceIn(2, 95)
                        val a = ((inv2 / sum) * 100.0).toInt().coerceIn(2, 95)
                        val d = (100 - h - a).coerceAtLeast(1)
                        return MatchProbabilities(h, d, a, isFromLiveOdds = true)
                    }
                }
            }

            // 2. Check 365Scores community prediction votes (whoWillWinReults)
            val whoWin = gameObj.optJSONObject("whoWillWinReults")
                ?: gameObj.optJSONObject("whoWillWinResults")
            if (whoWin != null) {
                val v1 = whoWin.optDouble("vote1", -1.0)
                val vX = whoWin.optDouble("voteX", -1.0)
                val v2 = whoWin.optDouble("vote2", -1.0)
                val total = v1 + vX + v2
                if (v1 >= 0.0 && vX >= 0.0 && v2 >= 0.0 && total > 0.0) {
                    val h = ((v1 / total) * 100.0).toInt().coerceIn(2, 95)
                    val a = ((v2 / total) * 100.0).toInt().coerceIn(2, 95)
                    val d = (100 - h - a).coerceAtLeast(1)
                    return MatchProbabilities(h, d, a, isFromLiveOdds = true)
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun fetch365GameGoalEvents(gameId: Long, langId: Int, tzEncoded: String): List<FootballGoalEvent> {
        val url = "$SCORES365_BASE/game/?appTypeId=5&langId=$langId&timezoneName=$tzEncoded&userCountryId=-1&gameId=$gameId"
        val jsonStr = httpGet(url, timeoutMs = 4000) ?: return emptyList()
        return try {
            val root = JSONObject(jsonStr)
            val game = root.optJSONObject("game") ?: return emptyList()
            val homeId = game.optJSONObject("homeCompetitor")?.optInt("id", 0) ?: 0
            parse365EventsArray(game.optJSONArray("events"), game.optJSONArray("members"), homeId)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parse365EventsArray(
        eventsArr: JSONArray?,
        membersArr: JSONArray?,
        homeCompId: Int
    ): List<FootballGoalEvent> {
        if (eventsArr == null || eventsArr.length() == 0) return emptyList()
        val membersMap = HashMap<Int, String>()
        if (membersArr != null) {
            for (i in 0 until membersArr.length()) {
                val m = membersArr.optJSONObject(i) ?: continue
                val id = m.optInt("id", 0)
                val name = m.optString("shortName").ifBlank { m.optString("name", "") }.trim()
                if (id > 0 && name.isNotBlank()) membersMap[id] = name
            }
        }

        val goals = mutableListOf<FootballGoalEvent>()
        for (i in 0 until eventsArr.length()) {
            val ev = eventsArr.optJSONObject(i) ?: continue
            val evTypeObj = ev.optJSONObject("eventType")
            val evTypeId = evTypeObj?.optInt("id", -1) ?: ev.optInt("eventTypeId", -1)
            val evTypeName = evTypeObj?.optString("name", "")?.lowercase(Locale.US).orEmpty()
            val isGoal = evTypeId == 1 || evTypeName.contains("goal") || evTypeName.contains("هدف")
            if (!isGoal) continue

            val compId = ev.optInt("competitorId", 0)
            val playerId = ev.optInt("playerId", 0)
            val playerName = ev.optString("playerName").ifBlank {
                membersMap[playerId].orEmpty()
            }.trim()
            if (playerName.isBlank()) continue

            val displayMin = ev.optString("gameTimeDisplay", "").trim().ifBlank {
                val t = ev.optInt("gameTime", 0)
                if (t > 0) "$t'" else ""
            }
            val minuteLabel = if (displayMin.isNotBlank() && !displayMin.endsWith("'")) "$displayMin'" else displayMin

            goals.add(
                FootballGoalEvent(
                    playerNameEn = playerName,
                    playerNameAr = playerName,
                    minuteLabel = minuteLabel,
                    isHomeTeam = (compId == homeCompId),
                    localizedPlayer = playerName
                )
            )
            if (goals.size >= 4) break
        }
        return goals
    }

    private fun fetch365Standings(commonParams: String): List<LeagueStandingRow> {
        val url = "$SCORES365_BASE/standings/?$commonParams"
        val jsonStr = httpGet(url, timeoutMs = 4500) ?: return emptyList()
        val rowsResult = mutableListOf<LeagueStandingRow>()
        try {
            val root = JSONObject(jsonStr)
            val standingsArr = root.optJSONArray("standings") ?: return emptyList()
            if (standingsArr.length() == 0) return emptyList()
            val firstTable = standingsArr.optJSONObject(0) ?: return emptyList()
            val rows = firstTable.optJSONArray("rows") ?: return emptyList()

            for (i in 0 until minOf(rows.length(), 20)) {
                val row = rows.optJSONObject(i) ?: continue
                val comp = row.optJSONObject("competitor") ?: continue
                val compId = comp.optInt("id", 0)
                val teamName = comp.optString("shortName").ifBlank { comp.optString("name", "") }.trim()
                if (teamName.isBlank()) continue

                val teamCode = comp.optString("symbolicName")
                    .ifBlank { deriveShortCode(teamName) }
                    .uppercase(Locale.US)
                    .take(3)
                val imgVer = comp.optInt("imageVersion", 5).coerceAtLeast(1)
                val logoUrl = if (compId > 0) build365CompetitorLogoUrl(compId, imgVer) else ""

                val rank = row.optInt("position", i + 1)
                val played = row.optInt("gamePlayed", row.optInt("gamesPlayed", 0))
                val points = row.optDouble("points", 0.0).toInt()

                val rawDiff = when {
                    row.has("ratio") && !row.isNull("ratio") -> row.optDouble("ratio", 0.0).toInt()
                    row.has("goalDiff") && !row.isNull("goalDiff") -> row.optInt("goalDiff", 0)
                    row.has("for") && row.has("against") -> (row.optDouble("for", 0.0) - row.optDouble("against", 0.0)).toInt()
                    else -> 0
                }
                val diffStr = if (rawDiff > 0) "+$rawDiff" else rawDiff.toString()

                val formList = mutableListOf<Char>()
                val formArr = row.optJSONArray("recentForm")
                if (formArr != null) {
                    for (fIdx in 0 until minOf(formArr.length(), 5)) {
                        val v = formArr.opt(fIdx)
                        val ch = when (v) {
                            is Number -> when (v.toInt()) {
                                1 -> 'W'
                                0 -> 'D'
                                else -> 'L'
                            }
                            is String -> when (v.uppercase(Locale.US)) {
                                "W", "1" -> 'W'
                                "D", "0", "T" -> 'D'
                                else -> 'L'
                            }
                            else -> 'D'
                        }
                        formList.add(ch)
                    }
                }
                while (formList.size < 5) formList.add('D')

                val primaryColor = parseHexColor(comp.optString("color"), 0xFF1E3A8AL)
                val secondaryColor = parseHexColor(comp.optString("awayColor"), 0xFFFFFFFFL)

                rowsResult.add(
                    LeagueStandingRow(
                        rank = rank,
                        teamCode = teamCode,
                        teamNameEn = teamName,
                        teamNameAr = teamName,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        crestStyle = if (i % 2 == 0) "shield" else "circle",
                        logoUrl = logoUrl,
                        played = played,
                        goalDiff = diffStr,
                        points = points,
                        form5 = formList.take(5),
                        isTopQualification = rank <= 4,
                        localizedTeamName = teamName
                    )
                )
            }
        } catch (_: Exception) {}
        return rowsResult
    }

    private fun fetch365TopScorers(commonParams: String): List<TopScorerItem> {
        val url = "$SCORES365_BASE/stats/?$commonParams"
        val jsonStr = httpGet(url, timeoutMs = 4500) ?: return emptyList()
        val result = mutableListOf<TopScorerItem>()
        try {
            val root = JSONObject(jsonStr)
            val compMap = HashMap<Int, String>()
            val comps = root.optJSONArray("competitors")
            if (comps != null) {
                for (i in 0 until comps.length()) {
                    val c = comps.optJSONObject(i) ?: continue
                    val id = c.optInt("id", 0)
                    val code = c.optString("symbolicName")
                        .ifBlank { deriveShortCode(c.optString("shortName").ifBlank { c.optString("name", "") }) }
                        .uppercase(Locale.US)
                        .take(3)
                    if (id > 0 && code.isNotBlank()) compMap[id] = code
                }
            }

            val statsObj = root.optJSONObject("stats")
            val athletesStats = statsObj?.optJSONArray("athletesStats")
                ?: root.optJSONArray("athletesStats")
                ?: return emptyList()
            if (athletesStats.length() == 0) return emptyList()

            // Find the "Goals" / "الأهداف" category (usually first item or type == 1)
            var goalsCat: JSONObject? = null
            for (i in 0 until athletesStats.length()) {
                val cat = athletesStats.optJSONObject(i) ?: continue
                val name = cat.optString("name", "").lowercase(Locale.US)
                val type = cat.optInt("type", -1)
                if (type == 1 || name.contains("goal") || name.contains("أهداف") || name.contains("الأهداف") ||
                    name.contains("goles") || name.contains("buts") || name.contains("tore") || name.contains("gol")
                ) {
                    goalsCat = cat
                    break
                }
            }
            if (goalsCat == null) goalsCat = athletesStats.optJSONObject(0)
            val rows = goalsCat?.optJSONArray("rows") ?: return emptyList()

            val parsedRaw = mutableListOf<Triple<String, String, Int>>()
            for (i in 0 until minOf(rows.length(), 4)) {
                val r = rows.optJSONObject(i) ?: continue
                val entity = r.optJSONObject("entity") ?: r.optJSONObject("athlete") ?: continue
                val name = entity.optString("shortName").ifBlank { entity.optString("name", "") }.trim()
                if (name.isBlank()) continue
                val compId = entity.optInt("competitorId", 0)
                val teamCode = compMap[compId] ?: "---"

                var goalsVal = 0
                val statVals = r.optJSONArray("stats")
                if (statVals != null && statVals.length() > 0) {
                    for (sIdx in 0 until statVals.length()) {
                        val sv = statVals.optJSONObject(sIdx) ?: continue
                        val parsedNum = sv.optString("value", "").filter { it.isDigit() }.toIntOrNull()
                        if (parsedNum != null) {
                            goalsVal = parsedNum
                            break
                        }
                    }
                }
                if (goalsVal <= 0) {
                    goalsVal = r.optString("value", "").filter { it.isDigit() }.toIntOrNull() ?: 1
                }
                parsedRaw.add(Triple(name, teamCode, goalsVal))
            }

            val maxGoals = parsedRaw.maxOfOrNull { it.third }?.coerceAtLeast(1) ?: 1
            parsedRaw.forEachIndexed { idx, (name, teamCode, goals) ->
                result.add(
                    TopScorerItem(
                        rank = idx + 1,
                        playerNameEn = name,
                        playerNameAr = name,
                        teamCode = teamCode,
                        goals = goals,
                        maxGoals = maxGoals,
                        localizedPlayerName = name
                    )
                )
            }
        } catch (_: Exception) {}
        return result
    }

    private fun fetch365News(commonParams: String, effLang: String): List<FootballHeadlineItem> {
        val url = "$SCORES365_BASE/news/?$commonParams"
        val jsonStr = httpGet(url, timeoutMs = 4500) ?: return emptyList()
        val list = mutableListOf<FootballHeadlineItem>()
        try {
            val root = JSONObject(jsonStr)
            val sourcesMap = HashMap<Int, String>()
            val sourcesArr = root.optJSONArray("newsSources")
            if (sourcesArr != null) {
                for (i in 0 until sourcesArr.length()) {
                    val s = sourcesArr.optJSONObject(i) ?: continue
                    val id = s.optInt("id", 0)
                    val name = s.optString("name", "").trim()
                    if (id > 0 && name.isNotBlank()) sourcesMap[id] = name
                }
            }

            val items = root.optJSONArray("newsItems")
                ?: root.optJSONArray("items")
                ?: root.optJSONArray("news")
                ?: return emptyList()

            for (i in 0 until minOf(items.length(), 4)) {
                val item = items.optJSONObject(i) ?: continue
                val title = item.optString("title", "").trim()
                if (title.length < 6) continue

                val id = item.optString("id", "365_news_$i")
                val sourceId = item.optInt("newsSourceId", 0)
                val sourceName = item.optString("sourceName")
                    .ifBlank { sourcesMap[sourceId].orEmpty() }
                    .ifBlank { "365Scores" }

                var imgUrl = item.optString("imageUrl").ifBlank {
                    item.optString("mainImageUrl")
                }.ifBlank {
                    item.optJSONArray("images")?.optJSONObject(0)?.optString("url").orEmpty()
                }.trim()
                if (imgUrl.startsWith("//")) imgUrl = "https:$imgUrl"
                if (imgUrl.startsWith("http://")) imgUrl = imgUrl.replaceFirst("http://", "https://")
                if (imgUrl.isBlank()) {
                    imgUrl = FALLBACK_HEADLINE_PHOTOS[i % FALLBACK_HEADLINE_PHOTOS.size]
                }

                val publishDate = item.optString("publishDate", "")
                val relTime = formatRelativePublishTime(publishDate, effLang, (i + 1) * 14)

                list.add(
                    FootballHeadlineItem(
                        id = id,
                        titleEn = title,
                        titleAr = title,
                        sourceName = sourceName,
                        timeAgoEn = relTime,
                        timeAgoAr = relTime,
                        imageUrl = imgUrl,
                        visualTheme = "365_news",
                        primaryColor = 0xFF1E3A8AL,
                        secondaryColor = 0xFF4ADE80L,
                        localizedTitle = title,
                        localizedTimeAgo = relTime
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    internal fun build365CompetitorLogoUrl(competitorId: Int, imageVersion: Int = 5): String {
        return "$SCORES365_IMG_BASE/f_png,w_128,h_128,c_limit,q_auto:eco,dpr_2,d_Competitors:default1.png/v$imageVersion/Competitors/$competitorId"
    }

    private fun deriveShortCode(name: String): String {
        val lettersOnly = name.filter { it.isLetter() }
        return if (lettersOnly.length >= 3) lettersOnly.take(3) else name.take(3).ifBlank { "FC" }
    }

    private fun parseHexColor(raw: String?, fallback: Long): Long {
        if (raw.isNullOrBlank()) return fallback
        val clean = raw.trim().removePrefix("#")
        return if (clean.length == 6) {
            clean.toLongOrNull(16)?.let { 0xFF000000L or it } ?: fallback
        } else fallback
    }

    private fun extractLocalKickoffTime(isoStr: String): String {
        if (isoStr.isBlank()) return ""
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US)
                if (pattern.endsWith("'Z'")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(isoStr)
                if (date != null) {
                    val out = SimpleDateFormat("HH:mm", Locale.US)
                    out.timeZone = TimeZone.getDefault()
                    return out.format(date)
                }
            } catch (_: Exception) {}
        }
        val match = Regex("""T(\d{2}:\d{2})""").find(isoStr)
        return match?.groupValues?.getOrNull(1).orEmpty()
    }

    private fun formatRelativePublishTime(isoStr: String, effLang: String, fallbackMinutes: Int): String {
        var mins = fallbackMinutes
        if (isoStr.isNotBlank()) {
            val patterns = listOf("yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss'Z'", "yyyy-MM-dd'T'HH:mm:ss")
            for (p in patterns) {
                try {
                    val sdf = SimpleDateFormat(p, Locale.US)
                    if (p.endsWith("'Z'")) sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val dt = sdf.parse(isoStr)
                    if (dt != null) {
                        val diffMin = ((System.currentTimeMillis() - dt.time) / 60_000L).toInt()
                        if (diffMin in 1..1440) {
                            mins = diffMin
                            break
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        val isAr = effLang in setOf("ar", "fa", "ur")
        return if (mins >= 60) {
            val hrs = mins / 60
            when {
                isAr -> "منذ $hrs س"
                effLang == "fr" -> "il y a ${hrs}h"
                effLang == "es" -> "hace ${hrs}h"
                effLang == "tr" -> "${hrs} sa önce"
                else -> "${hrs}h ago"
            }
        } else {
            when {
                isAr -> "منذ $mins د"
                effLang == "fr" -> "il y a ${mins} min"
                effLang == "es" -> "hace ${mins} min"
                effLang == "tr" -> "${mins} dk önce"
                else -> "$mins min ago"
            }
        }
    }

    private fun httpGet(urlString: String, timeoutMs: Int = 4500): String? {
        var conn: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                setRequestProperty("Accept", "application/json, text/plain, */*")
                setRequestProperty("Origin", "https://www.365scores.com")
                setRequestProperty("Referer", "https://www.365scores.com/")
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

    internal fun buildBaselineState(): FootballCenterState {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.US)

        // 1. PREMIER LEAGUE (Official 365Scores competitor logos)
        val eplFeatured = FootballMatchItem(
            id = "epl_ars_che",
            homeCode = "ARS",
            homeNameEn = "ARSENAL",
            homeNameAr = "أرسنال",
            homePrimaryColor = 0xFFEF0107L,
            homeSecondaryColor = 0xFFD4AF37L,
            homeCrestStyle = "shield",
            homeLogoUrl = build365CompetitorLogoUrl(104),
            awayCode = "CHE",
            awayNameEn = "CHELSEA",
            awayNameAr = "تشيلسي",
            awayPrimaryColor = 0xFF034694L,
            awaySecondaryColor = 0xFFFFFFFFL,
            awayCrestStyle = "circle",
            awayLogoUrl = build365CompetitorLogoUrl(106),
            homeScore = 2,
            awayScore = 1,
            statusType = MatchStatusType.LIVE,
            statusBadgeText = "67'",
            progressRatio = 67f / 90f,
            goalEvents = listOf(
                FootballGoalEvent("Saka", "ساكا", "23'", isHomeTeam = true),
                FootballGoalEvent("Palmer", "بالمر", "41'", isHomeTeam = false),
                FootballGoalEvent("Havertz", "هافيرتز", "58'", isHomeTeam = true)
            )
        )

        val eplFixtures = listOf(
            FootballMatchItem(
                id = "epl_mci_wol",
                homeCode = "MCI",
                homeNameEn = "MAN CITY",
                homeNameAr = "مانشستر سيتي",
                homePrimaryColor = 0xFF6CABDDL,
                homeSecondaryColor = 0xFFFFFFFFL,
                homeCrestStyle = "circle",
                homeLogoUrl = build365CompetitorLogoUrl(110),
                awayCode = "WOL",
                awayNameEn = "WOLVES",
                awayNameAr = "وولفرهامبتون",
                awayPrimaryColor = 0xFFFDB913L,
                awaySecondaryColor = 0xFF231F20L,
                awayCrestStyle = "hex",
                awayLogoUrl = build365CompetitorLogoUrl(120),
                homeScore = 3,
                awayScore = 1,
                statusType = MatchStatusType.FINISHED,
                statusBadgeText = "FT",
                progressRatio = 1.0f,
                goalEvents = listOf(
                    FootballGoalEvent("Haaland", "هالاند", "14'", isHomeTeam = true),
                    FootballGoalEvent("Cunha", "كونيا", "33'", isHomeTeam = false),
                    FootballGoalEvent("Foden", "فودين", "52'", isHomeTeam = true),
                    FootballGoalEvent("Haaland", "هالاند", "78'", isHomeTeam = true)
                )
            ),
            eplFeatured,
            FootballMatchItem(
                id = "epl_tot_ast",
                homeCode = "TOT",
                homeNameEn = "TOTTENHAM",
                homeNameAr = "توتنهام",
                homePrimaryColor = 0xFF132257L,
                homeSecondaryColor = 0xFFFFFFFFL,
                homeCrestStyle = "crest",
                homeLogoUrl = build365CompetitorLogoUrl(114),
                awayCode = "AST",
                awayNameEn = "ASTON VILLA",
                awayNameAr = "أستون فيلا",
                awayPrimaryColor = 0xFF670E36L,
                awaySecondaryColor = 0xFF95BFE5L,
                awayCrestStyle = "circle",
                awayLogoUrl = build365CompetitorLogoUrl(117),
                homeScore = null,
                awayScore = null,
                statusType = MatchStatusType.UPCOMING,
                statusBadgeText = "17:30",
                progressRatio = 0.0f
            ),
            FootballMatchItem(
                id = "epl_new_mun",
                homeCode = "NEW",
                homeNameEn = "NEWCASTLE",
                homeNameAr = "نيوكاسل",
                homePrimaryColor = 0xFF241F20L,
                homeSecondaryColor = 0xFFFFFFFFL,
                homeCrestStyle = "shield",
                homeLogoUrl = build365CompetitorLogoUrl(112),
                awayCode = "MUN",
                awayNameEn = "MAN UTD",
                awayNameAr = "مانشستر يونايتد",
                awayPrimaryColor = 0xFFDA291CL,
                awaySecondaryColor = 0xFFFBE122L,
                awayCrestStyle = "shield",
                awayLogoUrl = build365CompetitorLogoUrl(105),
                homeScore = null,
                awayScore = null,
                statusType = MatchStatusType.UPCOMING,
                statusBadgeText = "20:00",
                progressRatio = 0.0f
            ),
            FootballMatchItem(
                id = "epl_liv_eve",
                homeCode = "LIV",
                homeNameEn = "LIVERPOOL",
                homeNameAr = "ليفربول",
                homePrimaryColor = 0xFFC8102EL,
                homeSecondaryColor = 0xFF00B2A9L,
                homeCrestStyle = "shield",
                homeLogoUrl = build365CompetitorLogoUrl(108),
                awayCode = "EVE",
                awayNameEn = "EVERTON",
                awayNameAr = "إيفرتون",
                awayPrimaryColor = 0xFF003399L,
                awaySecondaryColor = 0xFFFFFFFFL,
                awayCrestStyle = "shield",
                awayLogoUrl = build365CompetitorLogoUrl(111),
                homeScore = null,
                awayScore = null,
                statusType = MatchStatusType.UPCOMING,
                statusBadgeText = "22:00",
                progressRatio = 0.0f
            )
        )

        val eplScorers = listOf(
            TopScorerItem(1, "Haaland", "هالاند", "MCI", 12, 12),
            TopScorerItem(2, "Salah", "محمد صلاح", "LIV", 9, 12),
            TopScorerItem(3, "Saka", "بوكايو ساكا", "ARS", 8, 12),
            TopScorerItem(4, "Palmer", "كول بالمر", "CHE", 8, 12)
        )

        val eplStandings = listOf(
            LeagueStandingRow(1, "MCI", "Manchester City", "مانشستر سيتي", 0xFF6CABDDL, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(110), 12, "+18", 28, listOf('W', 'W', 'D', 'W', 'W'), true),
            LeagueStandingRow(2, "ARS", "Arsenal", "أرسنال", 0xFFEF0107L, 0xFFD4AF37L, "shield", build365CompetitorLogoUrl(104), 12, "+14", 26, listOf('W', 'D', 'W', 'W', 'W'), true),
            LeagueStandingRow(3, "LIV", "Liverpool", "ليفربول", 0xFFC8102EL, 0xFF00B2A9L, "shield", build365CompetitorLogoUrl(108), 12, "+15", 25, listOf('W', 'L', 'W', 'W', 'D'), true),
            LeagueStandingRow(4, "TOT", "Tottenham", "توتنهام", 0xFF132257L, 0xFFFFFFFFL, "crest", build365CompetitorLogoUrl(114), 12, "+10", 24, listOf('W', 'W', 'L', 'W', 'W'), true),
            LeagueStandingRow(5, "CHE", "Chelsea", "تشيلسي", 0xFF034694L, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(106), 12, "+8", 22, listOf('L', 'W', 'W', 'D', 'D'), false),
            LeagueStandingRow(6, "MUN", "Manchester Utd", "مانشستر يونايتد", 0xFFDA291CL, 0xFFFBE122L, "shield", build365CompetitorLogoUrl(105), 12, "+2", 19, listOf('D', 'D', 'W', 'L', 'L'), false),
            LeagueStandingRow(7, "AST", "Aston Villa", "أستون فيلا", 0xFF670E36L, 0xFF95BFE5L, "shield", build365CompetitorLogoUrl(117), 12, "+1", 18, listOf('L', 'W', 'L', 'W', 'D'), false),
            LeagueStandingRow(8, "NEW", "Newcastle", "نيوكاسل", 0xFF241F20L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(112), 12, "-1", 16, listOf('L', 'D', 'W', 'W', 'L'), false),
            LeagueStandingRow(9, "BHA", "Brighton", "برايتون", 0xFF0057B8L, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(118), 12, "+2", 16, listOf('D', 'W', 'L', 'W', 'D'), false),
            LeagueStandingRow(10, "NFO", "Nottingham Forest", "نوتنغهام فورست", 0xFFDD0000L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(122), 12, "+1", 15, listOf('W', 'L', 'W', 'D', 'L'), false),
            LeagueStandingRow(11, "FUL", "Fulham", "فولهام", 0xFF111111L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(107), 12, "0", 15, listOf('D', 'W', 'L', 'W', 'L'), false),
            LeagueStandingRow(12, "WHU", "West Ham", "وست هام", 0xFF7A263AL, 0xFF1BB1E7L, "shield", build365CompetitorLogoUrl(116), 12, "-3", 13, listOf('L', 'D', 'W', 'L', 'D'), false)
        )

        val defaultHeadlines = listOf(
            FootballHeadlineItem(
                id = "news_1",
                titleEn = "Title race heats up after dramatic derby",
                titleAr = "سباق الصدارة يشتعل بعد ديربي مثير وحاسم",
                sourceName = "365Scores",
                timeAgoEn = "12 min ago",
                timeAgoAr = "منذ 12 دقيقة",
                imageUrl = FALLBACK_HEADLINE_PHOTOS[0],
                visualTheme = "derby_action",
                primaryColor = 0xFFEF0107L,
                secondaryColor = 0xFF6CABDDL
            ),
            FootballHeadlineItem(
                id = "news_2",
                titleEn = "Star striker returns from injury",
                titleAr = "عودة المهاجم النجم للتدريبات الجماعية بعد الإصابة",
                sourceName = "365Scores",
                timeAgoEn = "18 min ago",
                timeAgoAr = "منذ 18 دقيقة",
                imageUrl = FALLBACK_HEADLINE_PHOTOS[1],
                visualTheme = "striker_return",
                primaryColor = 0xFFC8102EL,
                secondaryColor = 0xFF10B981L
            ),
            FootballHeadlineItem(
                id = "news_3",
                titleEn = "Transfer window: clubs eye key targets",
                titleAr = "سوق الانتقالات: الأندية الكبرى تستعد لصفقات جديدة",
                sourceName = "365Scores",
                timeAgoEn = "25 min ago",
                timeAgoAr = "منذ 25 دقيقة",
                imageUrl = FALLBACK_HEADLINE_PHOTOS[2],
                visualTheme = "transfer_window",
                primaryColor = 0xFF6CABDDL,
                secondaryColor = 0xFFC8102EL
            ),
            FootballHeadlineItem(
                id = "news_4",
                titleEn = "Champions League fixtures confirmed",
                titleAr = "اعتماد مواجهات الجولة القادمة في دوري أبطال أوروبا",
                sourceName = "365Scores",
                timeAgoEn = "32 min ago",
                timeAgoAr = "منذ 32 دقيقة",
                imageUrl = FALLBACK_HEADLINE_PHOTOS[3],
                visualTheme = "ucl_trophy",
                primaryColor = 0xFF1E3A8AL,
                secondaryColor = 0xFF38BDF8L
            )
        )

        val premierLeagueData = LeagueDashboardData(
            leagueId = "premier_league",
            featuredMatch = eplFeatured,
            fixtures = eplFixtures,
            topScorers = eplScorers,
            standings = eplStandings,
            headlines = defaultHeadlines
        )

        // 2. LA LIGA
        val laLigaFeatured = FootballMatchItem(
            id = "esp_rma_bar",
            homeCode = "RMA",
            homeNameEn = "REAL MADRID",
            homeNameAr = "ريال مدريد",
            homePrimaryColor = 0xFF1D3557L,
            homeSecondaryColor = 0xFFD4AF37L,
            homeCrestStyle = "circle",
            homeLogoUrl = build365CompetitorLogoUrl(131),
            awayCode = "BAR",
            awayNameEn = "BARCELONA",
            awayNameAr = "برشلونة",
            awayPrimaryColor = 0xFFA50044L,
            awaySecondaryColor = 0xFF004D98L,
            awayCrestStyle = "shield",
            awayLogoUrl = build365CompetitorLogoUrl(132),
            homeScore = 2,
            awayScore = 2,
            statusType = MatchStatusType.LIVE,
            statusBadgeText = "74'",
            progressRatio = 74f / 90f,
            goalEvents = listOf(
                FootballGoalEvent("Mbappé", "مبابي", "18'", isHomeTeam = true),
                FootballGoalEvent("Lewandowski", "ليفاندوفسكي", "34'", isHomeTeam = false),
                FootballGoalEvent("Yamal", "لامين يامال", "55'", isHomeTeam = false),
                FootballGoalEvent("Vinícius Jr", "فينيسيوس", "69'", isHomeTeam = true)
            )
        )
        val laLigaData = LeagueDashboardData(
            leagueId = "la_liga",
            featuredMatch = laLigaFeatured,
            fixtures = listOf(
                FootballMatchItem("esp_atm_sev", "ATM", "ATLETICO", "أتلتيكو مدريد", 0xFFCB3524L, 0xFF272E61L, "shield", build365CompetitorLogoUrl(130), "SEV", "SEVILLA", "إشبيلية", 0xFFF43333L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(137), 2, 0, MatchStatusType.FINISHED, "FT", 1f),
                laLigaFeatured,
                FootballMatchItem("esp_vil_ath", "VIL", "VILLARREAL", "فياريال", 0xFFFBE106L, 0xFF005187L, "circle", build365CompetitorLogoUrl(141), "ATH", "ATHLETIC", "أتلتيك بلباو", 0xFFEE2523L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(129), null, null, MatchStatusType.UPCOMING, "18:30", 0f),
                FootballMatchItem("esp_rso_bet", "RSO", "REAL SOCIEDAD", "ريال سوسيداد", 0xFF0067B1L, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(136), "BET", "REAL BETIS", "ريال بيتيس", 0xFF0BB363L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(135), null, null, MatchStatusType.UPCOMING, "21:00", 0f),
                FootballMatchItem("esp_val_gir", "VAL", "VALENCIA", "فالنسيا", 0xFFFF6600L, 0xFF000000L, "shield", build365CompetitorLogoUrl(140), "GIR", "GIRONA", "جيرونا", 0xFFD00027L, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(1424), null, null, MatchStatusType.UPCOMING, "23:00", 0f)
            ),
            topScorers = listOf(
                TopScorerItem(1, "Lewandowski", "ليفاندوفسكي", "BAR", 14, 14),
                TopScorerItem(2, "Mbappé", "كيليان مبابي", "RMA", 11, 14),
                TopScorerItem(3, "Vinícius Jr", "فينيسيوس", "RMA", 9, 14),
                TopScorerItem(4, "Raphinha", "رافينيا", "BAR", 8, 14)
            ),
            standings = listOf(
                LeagueStandingRow(1, "BAR", "Barcelona", "برشلونة", 0xFFA50044L, 0xFF004D98L, "shield", build365CompetitorLogoUrl(132), 13, "+24", 33, listOf('W', 'W', 'W', 'D', 'W'), true),
                LeagueStandingRow(2, "RMA", "Real Madrid", "ريال مدريد", 0xFF1D3557L, 0xFFD4AF37L, "circle", build365CompetitorLogoUrl(131), 13, "+17", 30, listOf('W', 'D', 'W', 'W', 'W'), true),
                LeagueStandingRow(3, "ATM", "Atlético Madrid", "أتلتيكو مدريد", 0xFFCB3524L, 0xFF272E61L, "shield", build365CompetitorLogoUrl(130), 13, "+12", 26, listOf('W', 'W', 'L', 'W', 'D'), true),
                LeagueStandingRow(4, "VIL", "Villarreal", "فياريال", 0xFFFBE106L, 0xFF005187L, "circle", build365CompetitorLogoUrl(141), 13, "+7", 24, listOf('W', 'D', 'W', 'W', 'L'), true),
                LeagueStandingRow(5, "ATH", "Athletic Club", "أتلتيك بلباو", 0xFFEE2523L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(129), 13, "+6", 21, listOf('D', 'D', 'W', 'D', 'W'), false),
                LeagueStandingRow(6, "BET", "Real Betis", "ريال بيتيس", 0xFF0BB363L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(135), 13, "+2", 20, listOf('W', 'D', 'L', 'W', 'D'), false),
                LeagueStandingRow(7, "RSO", "Real Sociedad", "ريال سوسيداد", 0xFF0067B1L, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(136), 13, "+1", 18, listOf('L', 'W', 'W', 'L', 'W'), false),
                LeagueStandingRow(8, "GIR", "Girona", "جيرونا", 0xFFD00027L, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(1424), 13, "-1", 18, listOf('W', 'W', 'L', 'D', 'L'), false),
                LeagueStandingRow(9, "OSA", "Osasuna", "أوساسونا", 0xFFD91A21L, 0xFF0A346FL, "shield", build365CompetitorLogoUrl(134), 13, "-2", 17, listOf('W', 'D', 'L', 'W', 'L'), false),
                LeagueStandingRow(10, "MLL", "Mallorca", "مايوركا", 0xFFE20613L, 0xFF111111L, "shield", build365CompetitorLogoUrl(133), 13, "-1", 17, listOf('D', 'W', 'L', 'D', 'W'), false),
                LeagueStandingRow(11, "CEL", "Celta Vigo", "سيلتا فيغو", 0xFF8AC3EEL, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(145), 13, "-2", 16, listOf('L', 'W', 'D', 'L', 'W'), false),
                LeagueStandingRow(12, "SEV", "Sevilla", "إشبيلية", 0xFFF43333L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(137), 13, "-4", 15, listOf('D', 'L', 'W', 'D', 'L'), false)
            ),
            headlines = defaultHeadlines
        )

        // 3. SERIE A
        val serieAFeatured = FootballMatchItem(
            id = "ita_int_nap",
            homeCode = "INT",
            homeNameEn = "INTER MILAN",
            homeNameAr = "إنتر ميلان",
            homePrimaryColor = 0xFF010E80L,
            homeSecondaryColor = 0xFFD4AF37L,
            homeCrestStyle = "circle",
            homeLogoUrl = build365CompetitorLogoUrl(219),
            awayCode = "NAP",
            awayNameEn = "NAPOLI",
            awayNameAr = "نابولي",
            awayPrimaryColor = 0xFF12A0D7L,
            awaySecondaryColor = 0xFFFFFFFFL,
            awayCrestStyle = "circle",
            awayLogoUrl = build365CompetitorLogoUrl(227),
            homeScore = 2,
            awayScore = 1,
            statusType = MatchStatusType.LIVE,
            statusBadgeText = "63'",
            progressRatio = 63f / 90f,
            goalEvents = listOf(
                FootballGoalEvent("Lautaro", "لاوتارو", "21'", isHomeTeam = true),
                FootballGoalEvent("McTominay", "ماكتوميناي", "38'", isHomeTeam = false),
                FootballGoalEvent("Calhanoglu", "تشالهان أوغلو", "56'", isHomeTeam = true)
            )
        )
        val serieAData = LeagueDashboardData(
            leagueId = "serie_a",
            featuredMatch = serieAFeatured,
            fixtures = listOf(
                FootballMatchItem("ita_juv_mil", "JUV", "JUVENTUS", "يوفنتوس", 0xFF1E293BL, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(220), "MIL", "AC MILAN", "ميلان", 0xFFFB090BL, 0xFF000000L, "circle", build365CompetitorLogoUrl(226), 1, 1, MatchStatusType.FINISHED, "FT", 1f),
                serieAFeatured,
                FootballMatchItem("ita_ata_rom", "ATA", "ATALANTA", "أتالانتا", 0xFF1E71B8L, 0xFF000000L, "circle", build365CompetitorLogoUrl(214), "ROM", "AS ROMA", "روما", 0xFF8E1F2FL, 0xFFF0BC42L, "shield", build365CompetitorLogoUrl(228), null, null, MatchStatusType.UPCOMING, "18:00", 0f),
                FootballMatchItem("ita_laz_fio", "LAZ", "LAZIO", "لاتسيو", 0xFF87D8F7L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(225), "FIO", "FIORENTINA", "فيورنتينا", 0xFF482E92L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(217), null, null, MatchStatusType.UPCOMING, "20:45", 0f),
                FootballMatchItem("ita_bol_tor", "BOL", "BOLOGNA", "بولونيا", 0xFF1A2F48L, 0xFFA21C26L, "circle", build365CompetitorLogoUrl(215), "TOR", "TORINO", "تورينو", 0xFF8A1E03L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(231), null, null, MatchStatusType.UPCOMING, "22:45", 0f)
            ),
            topScorers = listOf(
                TopScorerItem(1, "Retegui", "ريتيغي", "ATA", 11, 11),
                TopScorerItem(2, "Thuram", "تورام", "INT", 9, 11),
                TopScorerItem(3, "Vlahović", "فلاهوفيتش", "JUV", 8, 11),
                TopScorerItem(4, "Lautaro", "لاوتارو", "INT", 7, 11)
            ),
            standings = listOf(
                LeagueStandingRow(1, "NAP", "Napoli", "نابولي", 0xFF12A0D7L, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(227), 12, "+11", 26, listOf('W', 'W', 'W', 'L', 'D'), true),
                LeagueStandingRow(2, "INT", "Inter Milan", "إنتر ميلان", 0xFF010E80L, 0xFFD4AF37L, "circle", build365CompetitorLogoUrl(219), 12, "+13", 25, listOf('W', 'D', 'W', 'W', 'D'), true),
                LeagueStandingRow(3, "ATA", "Atalanta", "أتالانتا", 0xFF1E71B8L, 0xFF000000L, "circle", build365CompetitorLogoUrl(214), 12, "+16", 25, listOf('W', 'W', 'W', 'W', 'W'), true),
                LeagueStandingRow(4, "FIO", "Fiorentina", "فيورنتينا", 0xFF482E92L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(217), 12, "+15", 25, listOf('W', 'W', 'W', 'W', 'W'), true),
                LeagueStandingRow(5, "LAZ", "Lazio", "لاتسيو", 0xFF87D8F7L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(225), 12, "+11", 25, listOf('L', 'W', 'W', 'W', 'W'), false),
                LeagueStandingRow(6, "JUV", "Juventus", "يوفنتوس", 0xFF1E293BL, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(220), 12, "+14", 24, listOf('W', 'D', 'D', 'W', 'W'), false),
                LeagueStandingRow(7, "MIL", "AC Milan", "ميلان", 0xFFFB090BL, 0xFF000000L, "circle", build365CompetitorLogoUrl(226), 11, "+6", 18, listOf('L', 'W', 'L', 'W', 'D'), false),
                LeagueStandingRow(8, "BOL", "Bologna", "بولونيا", 0xFF1A2F48L, 0xFFA21C26L, "circle", build365CompetitorLogoUrl(215), 11, "+2", 18, listOf('D', 'D', 'W', 'W', 'W'), false),
                LeagueStandingRow(9, "UDI", "Udinese", "أودينيزي", 0xFF111111L, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(232), 12, "0", 16, listOf('W', 'L', 'L', 'W', 'D'), false),
                LeagueStandingRow(10, "TOR", "Torino", "تورينو", 0xFF8A1E03L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(231), 12, "-2", 15, listOf('L', 'D', 'W', 'L', 'D'), false),
                LeagueStandingRow(11, "ROM", "AS Roma", "روما", 0xFF8E1F2FL, 0xFFF0BC42L, "shield", build365CompetitorLogoUrl(228), 12, "-1", 14, listOf('L', 'W', 'L', 'D', 'W'), false),
                LeagueStandingRow(12, "EMP", "Empoli", "إمبولي", 0xFF00579CL, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(216), 12, "-3", 14, listOf('D', 'L', 'W', 'D', 'L'), false)
            ),
            headlines = defaultHeadlines
        )

        // 4. CHAMPIONS LEAGUE
        val uclFeatured = FootballMatchItem(
            id = "ucl_liv_rma",
            homeCode = "LIV",
            homeNameEn = "LIVERPOOL",
            homeNameAr = "ليفربول",
            homePrimaryColor = 0xFFC8102EL,
            homeSecondaryColor = 0xFF00B2A9L,
            homeCrestStyle = "shield",
            homeLogoUrl = build365CompetitorLogoUrl(108),
            awayCode = "RMA",
            awayNameEn = "REAL MADRID",
            awayNameAr = "ريال مدريد",
            awayPrimaryColor = 0xFF1D3557L,
            awaySecondaryColor = 0xFFD4AF37L,
            awayCrestStyle = "circle",
            awayLogoUrl = build365CompetitorLogoUrl(131),
            homeScore = 2,
            awayScore = 1,
            statusType = MatchStatusType.LIVE,
            statusBadgeText = "71'",
            progressRatio = 71f / 90f,
            goalEvents = listOf(
                FootballGoalEvent("Mac Allister", "ماك أليستر", "24'", isHomeTeam = true),
                FootballGoalEvent("Bellingham", "بيلينغهام", "44'", isHomeTeam = false),
                FootballGoalEvent("Salah", "محمد صلاح", "64'", isHomeTeam = true)
            )
        )
        val uclData = LeagueDashboardData(
            leagueId = "champions_league",
            featuredMatch = uclFeatured,
            fixtures = listOf(
                FootballMatchItem("ucl_bay_psg", "BAY", "BAYERN", "بايرن ميونخ", 0xFFDC052DL, 0xFF0066B2L, "circle", build365CompetitorLogoUrl(335), "PSG", "PSG", "باريس", 0xFF004170L, 0xFFDA291CL, "circle", build365CompetitorLogoUrl(498), 1, 0, MatchStatusType.FINISHED, "FT", 1f),
                uclFeatured,
                FootballMatchItem("ucl_bar_int", "BAR", "BARCELONA", "برشلونة", 0xFFA50044L, 0xFF004D98L, "shield", build365CompetitorLogoUrl(132), "INT", "INTER", "إنتر ميلان", 0xFF010E80L, 0xFFD4AF37L, "circle", build365CompetitorLogoUrl(219), null, null, MatchStatusType.UPCOMING, "21:00", 0f),
                FootballMatchItem("ucl_mci_juv", "MCI", "MAN CITY", "مانشستر سيتي", 0xFF6CABDDL, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(110), "JUV", "JUVENTUS", "يوفنتوس", 0xFF1E293BL, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(220), null, null, MatchStatusType.UPCOMING, "22:00", 0f),
                FootballMatchItem("ucl_ars_bvb", "BVB", "DORTMUND", "دورتموند", 0xFFFDE100L, 0xFF000000L, "circle", build365CompetitorLogoUrl(337), "ARS", "ARSENAL", "أرسنال", 0xFFEF0107L, 0xFFD4AF37L, "shield", build365CompetitorLogoUrl(104), null, null, MatchStatusType.UPCOMING, "22:00", 0f)
            ),
            topScorers = listOf(
                TopScorerItem(1, "Kane", "هاري كين", "BAY", 5, 5),
                TopScorerItem(2, "Raphinha", "رافينيا", "BAR", 5, 5),
                TopScorerItem(3, "Lewandowski", "ليفاندوفسكي", "BAR", 5, 5),
                TopScorerItem(4, "Vinícius Jr", "فينيسيوس", "RMA", 4, 5)
            ),
            standings = listOf(
                LeagueStandingRow(1, "LIV", "Liverpool", "ليفربول", 0xFFC8102EL, 0xFF00B2A9L, "shield", build365CompetitorLogoUrl(108), 5, "+11", 15, listOf('W', 'W', 'W', 'W', 'W'), true),
                LeagueStandingRow(2, "INT", "Inter Milan", "إنتر ميلان", 0xFF010E80L, 0xFFD4AF37L, "circle", build365CompetitorLogoUrl(219), 5, "+7", 13, listOf('D', 'W', 'W', 'W', 'W'), true),
                LeagueStandingRow(3, "BAR", "Barcelona", "برشلونة", 0xFFA50044L, 0xFF004D98L, "shield", build365CompetitorLogoUrl(132), 5, "+13", 12, listOf('L', 'W', 'W', 'W', 'W'), true),
                LeagueStandingRow(4, "BVB", "Dortmund", "دورتموند", 0xFFFDE100L, 0xFF000000L, "circle", build365CompetitorLogoUrl(337), 5, "+10", 12, listOf('W', 'W', 'L', 'W', 'W'), true),
                LeagueStandingRow(5, "ARS", "Arsenal", "أرسنال", 0xFFEF0107L, 0xFFD4AF37L, "shield", build365CompetitorLogoUrl(104), 5, "+6", 10, listOf('D', 'W', 'W', 'L', 'W'), false),
                LeagueStandingRow(6, "LEV", "Leverkusen", "ليفركوزن", 0xFFE32221L, 0xFF000000L, "circle", build365CompetitorLogoUrl(334), 5, "+6", 10, listOf('W', 'W', 'D', 'L', 'W'), false),
                LeagueStandingRow(7, "BAY", "Bayern Munich", "بايرن ميونخ", 0xFFDC052DL, 0xFF0066B2L, "circle", build365CompetitorLogoUrl(335), 5, "+5", 9, listOf('W', 'L', 'L', 'W', 'W'), false),
                LeagueStandingRow(8, "MCI", "Manchester City", "مانشستر سيتي", 0xFF6CABDDL, 0xFFFFFFFFL, "circle", build365CompetitorLogoUrl(110), 5, "+6", 8, listOf('D', 'W', 'W', 'L', 'D'), false),
                LeagueStandingRow(9, "RMA", "Real Madrid", "ريال مدريد", 0xFF1D3557L, 0xFFD4AF37L, "circle", build365CompetitorLogoUrl(131), 5, "+2", 8, listOf('W', 'L', 'W', 'L', 'W'), false),
                LeagueStandingRow(10, "ATM", "Atlético Madrid", "أتلتيكو مدريد", 0xFFCB3524L, 0xFF272E61L, "shield", build365CompetitorLogoUrl(130), 5, "+2", 9, listOf('W', 'L', 'L', 'W', 'W'), false),
                LeagueStandingRow(11, "MIL", "AC Milan", "ميلان", 0xFFFB090BL, 0xFF000000L, "circle", build365CompetitorLogoUrl(226), 5, "+3", 9, listOf('L', 'L', 'W', 'W', 'W'), false),
                LeagueStandingRow(12, "PSG", "Paris SG", "باريس سان جيرمان", 0xFF004170L, 0xFFDA291CL, "circle", build365CompetitorLogoUrl(498), 5, "+1", 7, listOf('W', 'L', 'D', 'L', 'W'), false)
            ),
            headlines = defaultHeadlines
        )

        // 5. SAUDI ROSHN LEAGUE
        val saudiFeatured = FootballMatchItem(
            id = "sau_hil_nas",
            homeCode = "HIL",
            homeNameEn = "AL HILAL",
            homeNameAr = "الهلال",
            homePrimaryColor = 0xFF0054A6L,
            homeSecondaryColor = 0xFFFFFFFFL,
            homeCrestStyle = "shield",
            homeLogoUrl = build365CompetitorLogoUrl(8111),
            awayCode = "NAS",
            awayNameEn = "AL NASSR",
            awayNameAr = "النصر",
            awayPrimaryColor = 0xFFFDD000L,
            awaySecondaryColor = 0xFF004B87L,
            awayCrestStyle = "circle",
            awayLogoUrl = build365CompetitorLogoUrl(8112),
            homeScore = 2,
            awayScore = 1,
            statusType = MatchStatusType.LIVE,
            statusBadgeText = "68'",
            progressRatio = 68f / 90f,
            goalEvents = listOf(
                FootballGoalEvent("Mitrović", "ميتروفيتش", "22'", isHomeTeam = true),
                FootballGoalEvent("Ronaldo", "رونالدو", "45'", isHomeTeam = false),
                FootballGoalEvent("Salem", "سالم الدوسري", "61'", isHomeTeam = true)
            )
        )
        val saudiData = LeagueDashboardData(
            leagueId = "saudi_league",
            featuredMatch = saudiFeatured,
            fixtures = listOf(
                FootballMatchItem("sau_itt_ahl", "ITT", "AL ITTIHAD", "الاتحاد", 0xFFF5C400L, 0xFF111111L, "shield", build365CompetitorLogoUrl(8114), "AHL", "AL AHLI", "الأهلي", 0xFF007A3DL, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(8110), 1, 0, MatchStatusType.FINISHED, "FT", 1f),
                saudiFeatured,
                FootballMatchItem("sau_shb_qad", "SHB", "AL SHABAB", "الشباب", 0xFF1F2937L, 0xFFF97316L, "circle", build365CompetitorLogoUrl(8115), "QAD", "AL QADSIAH", "القادسية", 0xFFDC2626L, 0xFFFACC15L, "shield", "", null, null, MatchStatusType.UPCOMING, "18:00", 0f),
                FootballMatchItem("sau_ett_taa", "ETT", "AL ETTIFAQ", "الاتفاق", 0xFF15803DL, 0xFFDC2626L, "circle", "", "TAA", "AL TAAWOUN", "التعاون", 0xFFEAB308L, 0xFF1E3A8AL, "shield", "", null, null, MatchStatusType.UPCOMING, "20:00", 0f),
                FootballMatchItem("sau_fat_rhd", "FAT", "AL FATEH", "الفتح", 0xFF2563EBL, 0xFF16A34AL, "circle", "", "RIY", "AL RIYADH", "الرياض", 0xFFDC2626L, 0xFF111111L, "shield", "", null, null, MatchStatusType.UPCOMING, "21:00", 0f)
            ),
            topScorers = listOf(
                TopScorerItem(1, "Mitrović", "ميتروفيتش", "HIL", 12, 12),
                TopScorerItem(2, "Benzema", "كريم بنزيما", "ITT", 10, 12),
                TopScorerItem(3, "Ronaldo", "كريستيانو رونالدو", "NAS", 9, 12),
                TopScorerItem(4, "Salem", "سالم الدوسري", "HIL", 7, 12)
            ),
            standings = listOf(
                LeagueStandingRow(1, "HIL", "Al Hilal", "الهلال", 0xFF0054A6L, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(8111), 11, "+21", 31, listOf('W', 'W', 'W', 'D', 'W'), true),
                LeagueStandingRow(2, "ITT", "Al Ittihad", "الاتحاد", 0xFFF5C400L, 0xFF111111L, "shield", build365CompetitorLogoUrl(8114), 11, "+18", 30, listOf('W', 'W', 'W', 'W', 'W'), true),
                LeagueStandingRow(3, "NAS", "Al Nassr", "النصر", 0xFFFDD000L, 0xFF004B87L, "circle", build365CompetitorLogoUrl(8112), 11, "+12", 25, listOf('W', 'D', 'D', 'W', 'W'), true),
                LeagueStandingRow(4, "SHB", "Al Shabab", "الشباب", 0xFF1F2937L, 0xFFF97316L, "circle", build365CompetitorLogoUrl(8115), 11, "+6", 22, listOf('L', 'W', 'W', 'W', 'D'), true),
                LeagueStandingRow(5, "QAD", "Al Qadsiah", "القادسية", 0xFFDC2626L, 0xFFFACC15L, "shield", "", 11, "+5", 22, listOf('W', 'W', 'W', 'W', 'L'), false),
                LeagueStandingRow(6, "AHL", "Al Ahli", "الأهلي", 0xFF007A3DL, 0xFFFFFFFFL, "shield", build365CompetitorLogoUrl(8110), 11, "+4", 19, listOf('D', 'L', 'W', 'W', 'W'), false),
                LeagueStandingRow(7, "TAA", "Al Taawoun", "التعاون", 0xFFEAB308L, 0xFF1E3A8AL, "shield", "", 11, "+1", 17, listOf('W', 'D', 'L', 'W', 'D'), false),
                LeagueStandingRow(8, "ETT", "Al Ettifaq", "الاتفاق", 0xFF15803DL, 0xFFDC2626L, "circle", "", 11, "-2", 15, listOf('L', 'L', 'D', 'W', 'D'), false),
                LeagueStandingRow(9, "KHL", "Al Khaleej", "الخليج", 0xFFFACC15L, 0xFF15803DL, "shield", "", 11, "-1", 15, listOf('W', 'W', 'L', 'W', 'L'), false),
                LeagueStandingRow(10, "RIY", "Al Riyadh", "الرياض", 0xFFDC2626L, 0xFF111111L, "shield", "", 11, "-2", 14, listOf('D', 'W', 'L', 'D', 'W'), false),
                LeagueStandingRow(11, "DAM", "Damac FC", "ضمك", 0xFFFACC15L, 0xFFDC2626L, "circle", "", 11, "-3", 13, listOf('L', 'W', 'D', 'L', 'W'), false),
                LeagueStandingRow(12, "FAT", "Al Fateh", "الفتح", 0xFF2563EBL, 0xFF16A34AL, "circle", "", 11, "-5", 11, listOf('L', 'D', 'L', 'W', 'D'), false)
            ),
            headlines = defaultHeadlines
        )

        val map = linkedMapOf(
            "premier_league" to FootballCatalogs.enrichLeagueWithProbabilities(premierLeagueData),
            "la_liga" to FootballCatalogs.enrichLeagueWithProbabilities(laLigaData),
            "serie_a" to FootballCatalogs.enrichLeagueWithProbabilities(serieAData),
            "champions_league" to FootballCatalogs.enrichLeagueWithProbabilities(uclData),
            "saudi_league" to FootballCatalogs.enrichLeagueWithProbabilities(saudiData)
        )
        map.putAll(FootballCatalogs.buildExtraLeaguesBaselines(defaultHeadlines))

        return FootballCenterState(
            leaguesData = map,
            lastUpdatedTime = timeFormat.format(Date()),
            fetchedAtMillis = 0L,
            activeLanguage = "",
            sourceName = "365Scores",
            isLoading = false
        )
    }

    fun getLocalizedStrings(langCode: String): FootballLocalizedStrings {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return when (eff) {
            "ar", "fa", "ur" -> FootballLocalizedStrings(
                footballCenterTitle = "مركز كرة القدم",
                liveBadge = "مباشر",
                ftBadge = "انتهت",
                todaysFixturesTitle = "مباريات اليوم",
                topScorersTitle = "قائمة الهدافين",
                leagueTableTitle = "جدول الترتيب",
                latestHeadlinesTitle = "آخر الأخبار الرياضية",
                colPlayed = "لعب",
                colGoalDiff = "فرق",
                colPoints = "نقاط",
                colForm5 = "آخر 5",
                lastUpdatedPrefix = "آخر تحديث: ",
                customizeLeaguesBtn = "الأندية والدوريات",
                customizeDialogTitle = "تخصيص الدوريات والأندية",
                customizeDialogSubtitle = "اختر حتى 4 دوريات لعرضها في الشريط العلوي",
                resetDefaults = "استعادة الافتراضي",
                doneAction = "تم",
                backToHomeHint = "رجوع للشاشة الرئيسية",
                refreshAction = "تحديث النتائج",
                sourcePrefix = "المصدر: 365Scores",
                winProbabilityTitle = "احتمالات الفوز",
                drawLabel = "تعادل",
                leaguesTabTitle = "الدوريات",
                clubsTabTitle = "الأندية",
                customizeClubsSubtitle = "اختر أنديتك المفضلة لإبراز مبارياتها ومراكزها",
                followedLabel = "مُتابَع"
            )
            "fr" -> FootballLocalizedStrings(
                footballCenterTitle = "Centre Football",
                liveBadge = "EN DIRECT",
                ftBadge = "FIN",
                todaysFixturesTitle = "Matchs du Jour",
                topScorersTitle = "Meilleurs Buteurs",
                leagueTableTitle = "Classement",
                latestHeadlinesTitle = "Dernières Actualités",
                colPlayed = "J",
                colGoalDiff = "DIFF",
                colPoints = "PTS",
                colForm5 = "FORME 5",
                lastUpdatedPrefix = "Mis à jour : ",
                customizeLeaguesBtn = "Clubs & Ligues",
                customizeDialogTitle = "Ligues et Clubs",
                customizeDialogSubtitle = "Choisissez 4 championnats à afficher en haut",
                resetDefaults = "Par défaut",
                doneAction = "Terminé",
                backToHomeHint = "Retour à l'accueil",
                refreshAction = "Actualiser",
                sourcePrefix = "Source : 365Scores",
                winProbabilityTitle = "Probabilités",
                drawLabel = "Nul",
                leaguesTabTitle = "Ligues",
                clubsTabTitle = "Clubs",
                customizeClubsSubtitle = "Suivez vos clubs favoris pour les mettre en avant",
                followedLabel = "Suivi"
            )
            "es" -> FootballLocalizedStrings(
                footballCenterTitle = "Centro de Fútbol",
                liveBadge = "EN VIVO",
                ftBadge = "FIN",
                todaysFixturesTitle = "Partidos de Hoy",
                topScorersTitle = "Goleadores",
                leagueTableTitle = "Clasificación",
                latestHeadlinesTitle = "Últimas Noticias",
                colPlayed = "PJ",
                colGoalDiff = "DG",
                colPoints = "PTS",
                colForm5 = "FORMA 5",
                lastUpdatedPrefix = "Actualizado: ",
                customizeLeaguesBtn = "Clubes y Ligas",
                customizeDialogTitle = "Ligas y Clubes",
                customizeDialogSubtitle = "Seleccione 4 ligas para la barra superior",
                resetDefaults = "Restablecer",
                doneAction = "Listo",
                backToHomeHint = "Volver al inicio",
                refreshAction = "Actualizar",
                sourcePrefix = "Fuente: 365Scores",
                winProbabilityTitle = "Probabilidades",
                drawLabel = "Empate",
                leaguesTabTitle = "Ligas",
                clubsTabTitle = "Clubes",
                customizeClubsSubtitle = "Sigue a tus clubes favoritos para destacarlos",
                followedLabel = "Seguido"
            )
            "tr" -> FootballLocalizedStrings(
                footballCenterTitle = "Futbol Merkezi",
                liveBadge = "CANLI",
                ftBadge = "MS",
                todaysFixturesTitle = "Günün Maçları",
                topScorersTitle = "Gol Krallığı",
                leagueTableTitle = "Puan Durumu",
                latestHeadlinesTitle = "Son Dakika Haberleri",
                colPlayed = "O",
                colGoalDiff = "AV",
                colPoints = "P",
                colForm5 = "SON 5",
                lastUpdatedPrefix = "Son güncelleme: ",
                customizeLeaguesBtn = "Kulüpler & Lig",
                customizeDialogTitle = "Ligler ve Kulüpler",
                customizeDialogSubtitle = "Üst çubukta gösterilecek 4 ligi seçin",
                resetDefaults = "Varsayılan",
                doneAction = "Tamam",
                backToHomeHint = "Ana ekrana dön",
                refreshAction = "Yenile",
                sourcePrefix = "Kaynak: 365Scores",
                winProbabilityTitle = "Kazanma Olasılığı",
                drawLabel = "Beraberlik",
                leaguesTabTitle = "Ligler",
                clubsTabTitle = "Kulüpler",
                customizeClubsSubtitle = "Favori kulüplerinizi takip ederek öne çıkarın",
                followedLabel = "Takip"
            )
            "de" -> FootballLocalizedStrings(
                footballCenterTitle = "Fußball-Zentrum",
                liveBadge = "LIVE",
                ftBadge = "ENDE",
                todaysFixturesTitle = "Heutige Spiele",
                topScorersTitle = "Torjäger",
                leagueTableTitle = "Tabelle",
                latestHeadlinesTitle = "Aktuelle Schlagzeilen",
                colPlayed = "Sp",
                colGoalDiff = "TD",
                colPoints = "Pkt",
                colForm5 = "FORM 5",
                lastUpdatedPrefix = "Aktualisiert: ",
                customizeLeaguesBtn = "Vereine & Ligen",
                customizeDialogTitle = "Ligen & Vereine",
                customizeDialogSubtitle = "Wähle bis zu 4 Ligen für die obere Leiste",
                resetDefaults = "Zurücksetzen",
                doneAction = "Fertig",
                backToHomeHint = "Zurück",
                refreshAction = "Aktualisieren",
                sourcePrefix = "Quelle: 365Scores",
                winProbabilityTitle = "Siegwahrscheinlichkeit",
                drawLabel = "Unentschieden",
                leaguesTabTitle = "Ligen",
                clubsTabTitle = "Vereine",
                customizeClubsSubtitle = "Folge Vereinen, um ihre Spiele hervorzuheben",
                followedLabel = "Gefolgt"
            )
            "it" -> FootballLocalizedStrings(
                footballCenterTitle = "Centro Calcio",
                liveBadge = "LIVE",
                ftBadge = "FIN",
                todaysFixturesTitle = "Partite di oggi",
                topScorersTitle = "Marcatori",
                leagueTableTitle = "Classifica",
                latestHeadlinesTitle = "Ultime notizie",
                colPlayed = "G",
                colGoalDiff = "DR",
                colPoints = "PT",
                colForm5 = "FORMA",
                lastUpdatedPrefix = "Aggiornato: ",
                customizeLeaguesBtn = "Club e Leghe",
                customizeDialogTitle = "Leghe e Club",
                customizeDialogSubtitle = "Seleziona fino a 4 leghe nella barra superiore",
                resetDefaults = "Predefiniti",
                doneAction = "Fatto",
                backToHomeHint = "Home",
                refreshAction = "Aggiorna",
                sourcePrefix = "Fonte: 365Scores",
                winProbabilityTitle = "Probabilità di vittoria",
                drawLabel = "Pareggio",
                leaguesTabTitle = "Leghe",
                clubsTabTitle = "Club",
                customizeClubsSubtitle = "Segui i tuoi club preferiti",
                followedLabel = "Seguito"
            )
            "ru" -> FootballLocalizedStrings(
                footballCenterTitle = "Футбольный центр",
                liveBadge = "LIVE",
                ftBadge = "ЗАВ",
                todaysFixturesTitle = "Матчи сегодня",
                topScorersTitle = "Бомбардиры",
                leagueTableTitle = "Таблица",
                latestHeadlinesTitle = "Последние новости",
                colPlayed = "И",
                colGoalDiff = "РМ",
                colPoints = "О",
                colForm5 = "ФОРМА",
                lastUpdatedPrefix = "Обновлено: ",
                customizeLeaguesBtn = "Клубы и лиги",
                customizeDialogTitle = "Лиги и клубы",
                customizeDialogSubtitle = "Выберите до 4 лиг для верхней панели",
                resetDefaults = "Сбросить",
                doneAction = "Готово",
                backToHomeHint = "На главную",
                refreshAction = "Обновить",
                sourcePrefix = "Источник: 365Scores",
                winProbabilityTitle = "Вероятность победы",
                drawLabel = "Ничья",
                leaguesTabTitle = "Лиги",
                clubsTabTitle = "Клубы",
                customizeClubsSubtitle = "Отслеживайте любимые клубы",
                followedLabel = "В избранном"
            )
            "pt" -> FootballLocalizedStrings(
                footballCenterTitle = "Centro de Futebol",
                liveBadge = "AO VIVO",
                ftBadge = "FIM",
                todaysFixturesTitle = "Jogos de Hoje",
                topScorersTitle = "Artilheiros",
                leagueTableTitle = "Classificação",
                latestHeadlinesTitle = "Últimas Notícias",
                colPlayed = "J",
                colGoalDiff = "SG",
                colPoints = "PTS",
                colForm5 = "FORMA",
                lastUpdatedPrefix = "Atualizado: ",
                customizeLeaguesBtn = "Clubes e Ligas",
                customizeDialogTitle = "Ligas e Clubes",
                customizeDialogSubtitle = "Selecione até 4 ligas para a barra superior",
                resetDefaults = "Padrão",
                doneAction = "Concluído",
                backToHomeHint = "Início",
                refreshAction = "Atualizar",
                sourcePrefix = "Fonte: 365Scores",
                winProbabilityTitle = "Probabilidade de Vitória",
                drawLabel = "Empate",
                leaguesTabTitle = "Ligas",
                clubsTabTitle = "Clubes",
                customizeClubsSubtitle = "Siga clubes para destacar seus jogos",
                followedLabel = "Seguindo"
            )
            "pl" -> FootballLocalizedStrings(
                footballCenterTitle = "Centrum Piłkarskie",
                liveBadge = "NA ŻYWO",
                ftBadge = "KON",
                todaysFixturesTitle = "Dzisiejsze mecze",
                topScorersTitle = "Strzelcy",
                leagueTableTitle = "Tabela",
                latestHeadlinesTitle = "Najnowsze wiadomości",
                colPlayed = "M",
                colGoalDiff = "BG",
                colPoints = "PKT",
                colForm5 = "FORMA",
                lastUpdatedPrefix = "Zaktualizowano: ",
                customizeLeaguesBtn = "Kluby i Ligi",
                customizeDialogTitle = "Ligi i Kluby",
                customizeDialogSubtitle = "Wybierz do 4 lig na górny pasek",
                resetDefaults = "Domyślne",
                doneAction = "Gotowe",
                backToHomeHint = "Powrót",
                refreshAction = "Odśwież",
                sourcePrefix = "Źródło: 365Scores",
                winProbabilityTitle = "Szanse na wygraną",
                drawLabel = "Remis",
                leaguesTabTitle = "Ligi",
                clubsTabTitle = "Kluby",
                customizeClubsSubtitle = "Obserwuj ulubione kluby",
                followedLabel = "Obserw."
            )
            "nl" -> FootballLocalizedStrings(
                footballCenterTitle = "Voetbalcentrum",
                liveBadge = "LIVE",
                ftBadge = "EIND",
                todaysFixturesTitle = "Wedstrijden vandaag",
                topScorersTitle = "Topscorers",
                leagueTableTitle = "Stand",
                latestHeadlinesTitle = "Laatste nieuws",
                colPlayed = "G",
                colGoalDiff = "DS",
                colPoints = "PNT",
                colForm5 = "VORM",
                lastUpdatedPrefix = "Bijgewerkt: ",
                customizeLeaguesBtn = "Clubs & Competities",
                customizeDialogTitle = "Competities & Clubs",
                customizeDialogSubtitle = "Selecteer tot 4 competities",
                resetDefaults = "Standaard",
                doneAction = "Gereed",
                backToHomeHint = "Terug",
                refreshAction = "Vernieuwen",
                sourcePrefix = "Bron: 365Scores",
                winProbabilityTitle = "Winkans",
                drawLabel = "Gelijk",
                leaguesTabTitle = "Competities",
                clubsTabTitle = "Clubs",
                customizeClubsSubtitle = "Volg je favoriete clubs",
                followedLabel = "Gevolgd"
            )
            "el" -> FootballLocalizedStrings(
                footballCenterTitle = "Κέντρο Ποδοσφαίρου",
                liveBadge = "LIVE",
                ftBadge = "ΤΕΛ",
                todaysFixturesTitle = "Σημερινοί αγώνες",
                topScorersTitle = "Σκόρερ",
                leagueTableTitle = "Βαθμολογία",
                latestHeadlinesTitle = "Τελευταία νέα",
                colPlayed = "ΑΓ",
                colGoalDiff = "ΔΤ",
                colPoints = "ΒΑΘ",
                colForm5 = "ΦΟΡΜΑ",
                lastUpdatedPrefix = "Ενημέρωση: ",
                customizeLeaguesBtn = "Ομάδες & Πρωταθλήματα",
                customizeDialogTitle = "Πρωταθλήματα & Ομάδες",
                customizeDialogSubtitle = "Επιλέξτε έως 4 πρωταθλήματα",
                resetDefaults = "Επαναφορά",
                doneAction = "Τέλος",
                backToHomeHint = "Αρχική",
                refreshAction = "Ανανέωση",
                sourcePrefix = "Πηγή: 365Scores",
                winProbabilityTitle = "Πιθανότητα νίκης",
                drawLabel = "Ισοπαλία",
                leaguesTabTitle = "Πρωταθλήματα",
                clubsTabTitle = "Ομάδες",
                customizeClubsSubtitle = "Ακολουθήστε τις αγαπημένες σας ομάδες",
                followedLabel = "Αγαπημένη"
            )
            "id", "in" -> FootballLocalizedStrings(
                footballCenterTitle = "Pusat Sepak Bola",
                liveBadge = "LIVE",
                ftBadge = "FT",
                todaysFixturesTitle = "Jadwal Hari Ini",
                topScorersTitle = "Top Skor",
                leagueTableTitle = "Klasemen",
                latestHeadlinesTitle = "Berita Terbaru",
                colPlayed = "M",
                colGoalDiff = "SG",
                colPoints = "POIN",
                colForm5 = "5 LAGA",
                lastUpdatedPrefix = "Diperbarui: ",
                customizeLeaguesBtn = "Klub & Liga",
                customizeDialogTitle = "Liga & Klub",
                customizeDialogSubtitle = "Pilih hingga 4 liga di bilah atas",
                resetDefaults = "Reset",
                doneAction = "Selesai",
                backToHomeHint = "Beranda",
                refreshAction = "Muat ulang",
                sourcePrefix = "Sumber: 365Scores",
                winProbabilityTitle = "Peluang Menang",
                drawLabel = "Seri",
                leaguesTabTitle = "Liga",
                clubsTabTitle = "Klub",
                customizeClubsSubtitle = "Ikuti klub favorit Anda",
                followedLabel = "Diikuti"
            )
            "vi" -> FootballLocalizedStrings(
                footballCenterTitle = "Trung tâm Bóng đá",
                liveBadge = "TRỰC TIẾP",
                ftBadge = "KT",
                todaysFixturesTitle = "Lịch thi đấu hôm nay",
                topScorersTitle = "Vua phá lưới",
                leagueTableTitle = "Bảng xếp hạng",
                latestHeadlinesTitle = "Tin mới nhất",
                colPlayed = "Tr",
                colGoalDiff = "HS",
                colPoints = "Đ",
                colForm5 = "5 TRẬN",
                lastUpdatedPrefix = "Cập nhật: ",
                customizeLeaguesBtn = "CLB & Giải đấu",
                customizeDialogTitle = "Giải đấu & CLB",
                customizeDialogSubtitle = "Chọn tối đa 4 giải đấu",
                resetDefaults = "Mặc định",
                doneAction = "Xong",
                backToHomeHint = "Trang chủ",
                refreshAction = "Làm mới",
                sourcePrefix = "Nguồn: 365Scores",
                winProbabilityTitle = "Xác suất thắng",
                drawLabel = "Hòa",
                leaguesTabTitle = "Giải đấu",
                clubsTabTitle = "CLB",
                customizeClubsSubtitle = "Theo dõi CLB yêu thích",
                followedLabel = "Đang theo"
            )
            "th" -> FootballLocalizedStrings(
                footballCenterTitle = "ศูนย์ฟุตบอล",
                liveBadge = "สด",
                ftBadge = "จบ",
                todaysFixturesTitle = "โปรแกรมวันนี้",
                topScorersTitle = "ดาวซัลโว",
                leagueTableTitle = "ตารางคะแนน",
                latestHeadlinesTitle = "ข่าวฟุตบอลล่าสุด",
                colPlayed = "แข่ง",
                colGoalDiff = "ได้เสีย",
                colPoints = "แต้ม",
                colForm5 = "5 นัด",
                lastUpdatedPrefix = "อัปเดต: ",
                customizeLeaguesBtn = "สโมสรและลีก",
                customizeDialogTitle = "ลีกและสโมสร",
                customizeDialogSubtitle = "เลือกได้สูงสุด 4 ลีก",
                resetDefaults = "ค่าเริ่มต้น",
                doneAction = "เสร็จสิ้น",
                backToHomeHint = "หน้าหลัก",
                refreshAction = "รีเฟรช",
                sourcePrefix = "ที่มา: 365Scores",
                winProbabilityTitle = "โอกาสชนะ",
                drawLabel = "เสมอ",
                leaguesTabTitle = "ลีก",
                clubsTabTitle = "สโมสร",
                customizeClubsSubtitle = "ติดตามสโมสรโปรดของคุณ",
                followedLabel = "ติดตาม"
            )
            "zh" -> FootballLocalizedStrings(
                footballCenterTitle = "足球中心",
                liveBadge = "直播",
                ftBadge = "完场",
                todaysFixturesTitle = "今日赛程",
                topScorersTitle = "射手榜",
                leagueTableTitle = "积分榜",
                latestHeadlinesTitle = "最新资讯",
                colPlayed = "赛",
                colGoalDiff = "净",
                colPoints = "分",
                colForm5 = "近5场",
                lastUpdatedPrefix = "更新于: ",
                customizeLeaguesBtn = "俱乐部与联赛",
                customizeDialogTitle = "联赛与俱乐部",
                customizeDialogSubtitle = "最多选择4个联赛显示在顶部",
                resetDefaults = "恢复默认",
                doneAction = "完成",
                backToHomeHint = "返回首页",
                refreshAction = "刷新",
                sourcePrefix = "来源: 365Scores",
                winProbabilityTitle = "胜率预测",
                drawLabel = "平局",
                leaguesTabTitle = "联赛",
                clubsTabTitle = "俱乐部",
                customizeClubsSubtitle = "关注喜爱的俱乐部以高亮赛程",
                followedLabel = "已关注"
            )
            "ja" -> FootballLocalizedStrings(
                footballCenterTitle = "サッカーセンター",
                liveBadge = "LIVE",
                ftBadge = "終了",
                todaysFixturesTitle = "本日の試合",
                topScorersTitle = "得点ランキング",
                leagueTableTitle = "順位表",
                latestHeadlinesTitle = "最新ニュース",
                colPlayed = "試",
                colGoalDiff = "得失",
                colPoints = "勝点",
                colForm5 = "直近5",
                lastUpdatedPrefix = "更新: ",
                customizeLeaguesBtn = "クラブ・リーグ",
                customizeDialogTitle = "リーグ・クラブ設定",
                customizeDialogSubtitle = "上部バーに表示するリーグを最大4つ選択",
                resetDefaults = "初期設定",
                doneAction = "完了",
                backToHomeHint = "ホームへ",
                refreshAction = "更新",
                sourcePrefix = "出典: 365Scores",
                winProbabilityTitle = "勝利確率",
                drawLabel = "引分",
                leaguesTabTitle = "リーグ",
                clubsTabTitle = "クラブ",
                customizeClubsSubtitle = "お気に入りクラブをフォロー",
                followedLabel = "フォロー中"
            )
            "ko" -> FootballLocalizedStrings(
                footballCenterTitle = "축구 센터",
                liveBadge = "생중계",
                ftBadge = "종료",
                todaysFixturesTitle = "오늘의 경기",
                topScorersTitle = "득점 순위",
                leagueTableTitle = "리그 순위표",
                latestHeadlinesTitle = "최신 뉴스",
                colPlayed = "경기",
                colGoalDiff = "득실",
                colPoints = "승점",
                colForm5 = "최근5",
                lastUpdatedPrefix = "업데이트: ",
                customizeLeaguesBtn = "클럽 및 리그",
                customizeDialogTitle = "리그 및 클럽",
                customizeDialogSubtitle = "상단 바에 표시할 리그 최대 4개 선택",
                resetDefaults = "초기화",
                doneAction = "완료",
                backToHomeHint = "홈으로",
                refreshAction = "새로고침",
                sourcePrefix = "출처: 365Scores",
                winProbabilityTitle = "승리 확률",
                drawLabel = "무승부",
                leaguesTabTitle = "리그",
                clubsTabTitle = "클럽",
                customizeClubsSubtitle = "좋아하는 클럽 팔로우",
                followedLabel = "팔로우"
            )
            "hi" -> FootballLocalizedStrings(
                footballCenterTitle = "फ़ुटबॉल केंद्र",
                liveBadge = "लाइव",
                ftBadge = "पूर्ण",
                todaysFixturesTitle = "आज के मैच",
                topScorersTitle = "शीर्ष स्कोरर",
                leagueTableTitle = "लीग तालिका",
                latestHeadlinesTitle = "ताज़ा सुर्खियाँ",
                colPlayed = "मैच",
                colGoalDiff = "अंतर",
                colPoints = "अंक",
                colForm5 = "फॉर्म",
                lastUpdatedPrefix = "अपडेट: ",
                customizeLeaguesBtn = "क्लब और लीग",
                customizeDialogTitle = "लीग और क्लब",
                customizeDialogSubtitle = "शीर्ष बार के लिए अधिकतम 4 लीग चुनें",
                resetDefaults = "डिफ़ॉल्ट",
                doneAction = "पूर्ण",
                backToHomeHint = "होम",
                refreshAction = "रिफ्रेश",
                sourcePrefix = "स्रोत: 365Scores",
                winProbabilityTitle = "जीत की संभावना",
                drawLabel = "ड्रॉ",
                leaguesTabTitle = "लीग",
                clubsTabTitle = "क्लब",
                customizeClubsSubtitle = "पसंदीदा क्लबों को फ़ॉलो करें",
                followedLabel = "फ़ॉलोड"
            )
            else -> FootballLocalizedStrings(
                footballCenterTitle = "Football Center",
                liveBadge = "LIVE",
                ftBadge = "FT",
                todaysFixturesTitle = "Today's Fixtures",
                topScorersTitle = "Top Scorers",
                leagueTableTitle = "League Table",
                latestHeadlinesTitle = "Latest Headlines",
                colPlayed = "P",
                colGoalDiff = "GD",
                colPoints = "PTS",
                colForm5 = "FORM 5",
                lastUpdatedPrefix = "Last updated: ",
                customizeLeaguesBtn = "Clubs & Leagues",
                customizeDialogTitle = "Leagues & Clubs",
                customizeDialogSubtitle = "Select up to 4 leagues for the top bar",
                resetDefaults = "Reset Defaults",
                doneAction = "Done",
                backToHomeHint = "Back to Home",
                refreshAction = "Refresh",
                sourcePrefix = "Source: 365Scores",
                winProbabilityTitle = "Win Probability",
                drawLabel = "Draw",
                leaguesTabTitle = "Leagues",
                clubsTabTitle = "Clubs",
                customizeClubsSubtitle = "Follow clubs to highlight their matches & rank",
                followedLabel = "Followed"
            )
        }
    }
}
