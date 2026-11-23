package com.example.data.football

import com.example.util.LocaleHelper
import kotlin.math.exp
import kotlin.math.roundToInt

data class MatchProbabilities(
    val homeWinPct: Int,
    val drawPct: Int,
    val awayWinPct: Int,
    val isFromLiveOdds: Boolean = false
)

data class FootballClubCatalogItem(
    val id: String,
    val scores365CompId: Int,
    val code: String,
    val nameEn: String,
    val nameAr: String,
    val leagueId: String,
    val leagueLabelEn: String,
    val leagueLabelAr: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val crestStyle: String = "shield",
    val matchAliases: List<String> = emptyList()
) {
    val logoUrl: String
        get() = if (scores365CompId > 0) FootballRepository.build365CompetitorLogoUrl(scores365CompId) else ""

    fun getLocalizedName(langCode: String): String {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) nameAr else nameEn
    }

    fun getLocalizedLeague(langCode: String): String {
        val eff = LocaleHelper.getEffectiveLanguage(langCode)
        return if (eff in setOf("ar", "fa", "ur")) leagueLabelAr else leagueLabelEn
    }

    fun matchesTeam(codeCandidate: String, nameEnCandidate: String, nameArCandidate: String, localizedCandidate: String): Boolean {
        if (codeCandidate.equals(code, ignoreCase = true)) return true
        val needles = listOf(nameEn, nameAr) + matchAliases
        val haystacks = listOf(nameEnCandidate, nameArCandidate, localizedCandidate)
            .filter { it.isNotBlank() }
        for (h in haystacks) {
            val hNorm = h.trim().lowercase()
            for (n in needles) {
                val nNorm = n.trim().lowercase()
                if (nNorm.length >= 3 && (hNorm.contains(nNorm) || nNorm.contains(hNorm))) {
                    return true
                }
            }
        }
        return false
    }
}

object FootballCatalogs {

    val ALL_LEAGUES: List<FootballLeagueCatalogItem> = listOf(
        FootballLeagueCatalogItem("premier_league", 7, "PREMIER LEAGUE", "الدوري الإنجليزي", "England", "إنجلترا"),
        FootballLeagueCatalogItem("la_liga", 11, "LA LIGA", "الدوري الإسباني", "Spain", "إسبانيا"),
        FootballLeagueCatalogItem("serie_a", 17, "SERIE A", "الدوري الإيطالي", "Italy", "إيطاليا"),
        FootballLeagueCatalogItem("champions_league", 572, "CHAMPIONS LEAGUE", "دوري أبطال أوروبا", "Europe", "أوروبا"),
        FootballLeagueCatalogItem("bundesliga", 25, "BUNDESLIGA", "الدوري الألماني", "Germany", "ألمانيا"),
        FootballLeagueCatalogItem("ligue_1", 35, "LIGUE 1", "الدوري الفرنسي", "France", "فرنسا"),
        FootballLeagueCatalogItem("saudi_league", 649, "ROSHN SAUDI LEAGUE", "دوري روشن السعودي", "Saudi Arabia", "السعودية"),
        FootballLeagueCatalogItem("egyptian_league", 552, "EGYPTIAN LEAGUE", "الدوري المصري", "Egypt", "مصر"),
        FootballLeagueCatalogItem("afc_champions", 670, "AFC CHAMPIONS ELITE", "دوري أبطال آسيا للنخبة", "Asia", "آسيا"),
        FootballLeagueCatalogItem("caf_champions", 624, "CAF CHAMPIONS LEAGUE", "دوري أبطال أفريقيا", "Africa", "أفريقيا"),
        FootballLeagueCatalogItem("morocco_botola", 557, "BOTOLA PRO", "الدوري المغربي", "Morocco", "المغرب"),
        FootballLeagueCatalogItem("uae_pro_league", 677, "UAE PRO LEAGUE", "الدوري الإماراتي", "UAE", "الإمارات"),
        FootballLeagueCatalogItem("qatar_stars", 408, "QATAR STARS LEAGUE", "دوري نجوم قطر", "Qatar", "قطر"),
        FootballLeagueCatalogItem("algeria_ligue_1", 558, "ALGERIAN LIGUE 1", "الدوري الجزائري", "Algeria", "الجزائر"),
        FootballLeagueCatalogItem("tunisia_ligue_1", 5558, "TUNISIAN LIGUE 1", "الدوري التونسي", "Tunisia", "تونس"),
        FootballLeagueCatalogItem("iraq_stars", 6822, "IRAQ STARS LEAGUE", "دوري نجوم العراق", "Iraq", "العراق"),
        FootballLeagueCatalogItem("jordan_pro", 565, "JORDAN PRO LEAGUE", "الدوري الأردني", "Jordan", "الأردن"),
        FootballLeagueCatalogItem("turkish_super_lig", 19, "SÜPER LİG", "الدوري التركي", "Turkey", "تركيا"),
        FootballLeagueCatalogItem("eredivisie", 57, "EREDIVISIE", "الدوري الهولندي", "Netherlands", "هولندا"),
        FootballLeagueCatalogItem("liga_portugal", 73, "LIGA PORTUGAL", "الدوري البرتغالي", "Portugal", "البرتغال"),
        FootballLeagueCatalogItem("europa_league", 573, "EUROPA LEAGUE", "الدوري الأوروبي", "Europe", "أوروبا"),
        FootballLeagueCatalogItem("conference_league", 712, "CONFERENCE LEAGUE", "دوري المؤتمر الأوروبي", "Europe", "أوروبا"),
        FootballLeagueCatalogItem("world_cup", 608, "INTERNATIONAL", "المنتخبات العالمية", "Global", "عالمي")
    )

    val ALL_CLUBS: List<FootballClubCatalogItem> = listOf(
        // Spain
        FootballClubCatalogItem("real_madrid", 131, "RMA", "Real Madrid", "ريال مدريد", "la_liga", "La Liga", "الدوري الإسباني", 0xFF1D3557L, 0xFFD4AF37L, "circle", listOf("Madrid", "الريال")),
        FootballClubCatalogItem("barcelona", 132, "BAR", "Barcelona", "برشلونة", "la_liga", "La Liga", "الدوري الإسباني", 0xFFA50044L, 0xFF004D98L, "shield", listOf("Barça", "البارسا")),
        FootballClubCatalogItem("atletico", 130, "ATM", "Atlético Madrid", "أتلتيكو مدريد", "la_liga", "La Liga", "الدوري الإسباني", 0xFFCB3524L, 0xFF272E61L, "shield", listOf("Atletico", "أتلتيكو")),
        FootballClubCatalogItem("athletic_club", 129, "ATH", "Athletic Club", "أتلتيك بلباو", "la_liga", "La Liga", "الدوري الإسباني", 0xFFEE2523L, 0xFFFFFFFFL, "shield", listOf("Bilbao", "بلباو")),
        // England
        FootballClubCatalogItem("liverpool", 108, "LIV", "Liverpool", "ليفربول", "premier_league", "Premier League", "الدوري الإنجليزي", 0xFFC8102EL, 0xFF00B2A9L, "shield", listOf("الريدز")),
        FootballClubCatalogItem("arsenal", 104, "ARS", "Arsenal", "أرسنال", "premier_league", "Premier League", "الدوري الإنجليزي", 0xFFEF0107L, 0xFFD4AF37L, "shield", listOf("آرسنال")),
        FootballClubCatalogItem("man_city", 110, "MCI", "Manchester City", "مانشستر سيتي", "premier_league", "Premier League", "الدوري الإنجليزي", 0xFF6CABDDL, 0xFFFFFFFFL, "circle", listOf("Man City", "السيتي")),
        FootballClubCatalogItem("chelsea", 106, "CHE", "Chelsea", "تشيلسي", "premier_league", "Premier League", "الدوري الإنجليزي", 0xFF034694L, 0xFFFFFFFFL, "circle", listOf("البلوز")),
        FootballClubCatalogItem("man_utd", 105, "MUN", "Manchester Utd", "مانشستر يونايتد", "premier_league", "Premier League", "الدوري الإنجليزي", 0xFFDA291CL, 0xFFFBE122L, "shield", listOf("Man United", "Man Utd", "اليونايتد")),
        FootballClubCatalogItem("tottenham", 114, "TOT", "Tottenham", "توتنهام", "premier_league", "Premier League", "الدوري الإنجليزي", 0xFF132257L, 0xFFFFFFFFL, "shield", listOf("Spurs")),
        FootballClubCatalogItem("newcastle", 112, "NEW", "Newcastle", "نيوكاسل", "premier_league", "Premier League", "الدوري الإنجليزي", 0xFF241F20L, 0xFFFFFFFFL, "shield"),
        FootballClubCatalogItem("aston_villa", 117, "AST", "Aston Villa", "أستون فيلا", "premier_league", "Premier League", "الدوري الإنجليزي", 0xFF670E36L, 0xFF95BFE5L, "shield"),
        // Italy
        FootballClubCatalogItem("inter_milan", 219, "INT", "Inter Milan", "إنتر ميلان", "serie_a", "Serie A", "الدوري الإيطالي", 0xFF010E80L, 0xFFD4AF37L, "circle", listOf("Inter", "الإنتر")),
        FootballClubCatalogItem("ac_milan", 226, "MIL", "AC Milan", "ميلان", "serie_a", "Serie A", "الدوري الإيطالي", 0xFFFB090BL, 0xFF000000L, "circle", listOf("Milan", "إي سي ميلان")),
        FootballClubCatalogItem("juventus", 220, "JUV", "Juventus", "يوفنتوس", "serie_a", "Serie A", "الدوري الإيطالي", 0xFF1E293BL, 0xFFFFFFFFL, "shield", listOf("اليوفي")),
        FootballClubCatalogItem("napoli", 227, "NAP", "Napoli", "نابولي", "serie_a", "Serie A", "الدوري الإيطالي", 0xFF12A0D7L, 0xFFFFFFFFL, "circle"),
        FootballClubCatalogItem("as_roma", 228, "ROM", "AS Roma", "روما", "serie_a", "Serie A", "الدوري الإيطالي", 0xFF8E1F2FL, 0xFFF0BC42L, "shield", listOf("Roma")),
        // Germany & France
        FootballClubCatalogItem("bayern", 335, "BAY", "Bayern Munich", "بايرن ميونخ", "bundesliga", "Bundesliga", "الدوري الألماني", 0xFFDC052DL, 0xFF0066B2L, "circle", listOf("Bayern", "البايرن")),
        FootballClubCatalogItem("dortmund", 337, "BVB", "Borussia Dortmund", "بوروسيا دورتموند", "bundesliga", "Bundesliga", "الدوري الألماني", 0xFFFDE100L, 0xFF000000L, "circle", listOf("Dortmund", "دورتموند")),
        FootballClubCatalogItem("leverkusen", 334, "LEV", "Bayer Leverkusen", "باير ليفركوزن", "bundesliga", "Bundesliga", "الدوري الألماني", 0xFFE32221L, 0xFF000000L, "circle", listOf("Leverkusen", "ليفركوزن")),
        FootballClubCatalogItem("psg", 498, "PSG", "Paris SG", "باريس سان جيرمان", "ligue_1", "Ligue 1", "الدوري الفرنسي", 0xFF004170L, 0xFFDA291CL, "circle", listOf("Paris", "باريس")),
        FootballClubCatalogItem("marseille", 495, "OM", "Marseille", "مارسيليا", "ligue_1", "Ligue 1", "الدوري الفرنسي", 0xFF2FAEE0L, 0xFFFFFFFFL, "shield", listOf("أولمبيك مارسيليا")),
        // Saudi Arabia
        FootballClubCatalogItem("al_hilal", 8111, "HIL", "Al Hilal", "الهلال", "saudi_league", "Saudi League", "دوري روشن", 0xFF0054A6L, 0xFFFFFFFFL, "shield", listOf("Hilal", "الهلال السعودي")),
        FootballClubCatalogItem("al_nassr", 8112, "NAS", "Al Nassr", "النصر", "saudi_league", "Saudi League", "دوري روشن", 0xFFFDD000L, 0xFF004B87L, "circle", listOf("Nassr", "النصر السعودي")),
        FootballClubCatalogItem("al_ittihad", 8114, "ITT", "Al Ittihad", "الاتحاد", "saudi_league", "Saudi League", "دوري روشن", 0xFFF5C400L, 0xFF111111L, "shield", listOf("Ittihad", "اتحاد جدة")),
        FootballClubCatalogItem("al_ahli_ksa", 8110, "AHL", "Al Ahli Saudi", "الأهلي السعودي", "saudi_league", "Saudi League", "دوري روشن", 0xFF007A3DL, 0xFFFFFFFFL, "shield", listOf("Al Ahli", "أهلي جدة")),
        FootballClubCatalogItem("al_shabab", 8115, "SHB", "Al Shabab", "الشباب", "saudi_league", "Saudi League", "دوري روشن", 0xFF1F2937L, 0xFFF97316L, "circle"),
        // Egypt
        FootballClubCatalogItem("al_ahly_eg", 7561, "ALY", "Al Ahly SC", "الأهلي المصري", "egyptian_league", "Egyptian League", "الدوري المصري", 0xFFC8102EL, 0xFFFFFFFFL, "shield", listOf("Al Ahly", "الأهلي")),
        FootballClubCatalogItem("zamalek", 7562, "ZAM", "Zamalek SC", "الزمالك", "egyptian_league", "Egyptian League", "الدوري المصري", 0xFFFFFFFFL, 0xFFDC2626L, "shield", listOf("Zamalek")),
        FootballClubCatalogItem("pyramids", 52281, "PYR", "Pyramids FC", "بيراميدز", "egyptian_league", "Egyptian League", "الدوري المصري", 0xFF1E3A8AL, 0xFF38BDF8L, "circle", listOf("Pyramids")),
        // North Africa & Levant & Gulf
        FootballClubCatalogItem("wydad", 7551, "WAC", "Wydad AC", "الوداد الرياضي", "morocco_botola", "Botola Pro", "الدوري المغربي", 0xFFDC2626L, 0xFFFFFFFFL, "shield", listOf("Wydad", "الوداد")),
        FootballClubCatalogItem("raja", 7552, "RCA", "Raja Casablanca", "الرجاء الرياضي", "morocco_botola", "Botola Pro", "الدوري المغربي", 0xFF15803DL, 0xFFFFFFFFL, "shield", listOf("Raja", "الرجاء")),
        FootballClubCatalogItem("esperance", 7559, "EST", "Espérance Tunis", "الترجي التونسي", "tunisia_ligue_1", "Tunisian Ligue 1", "الدوري التونسي", 0xFFDC2626L, 0xFFFACC15L, "shield", listOf("Esperance", "الترجي")),
        FootballClubCatalogItem("mc_alger", 7544, "MCA", "MC Alger", "مولودية الجزائر", "algeria_ligue_1", "Algerian Ligue 1", "الدوري الجزائري", 0xFF15803DL, 0xFFDC2626L, "shield", listOf("المولودية")),
        FootballClubCatalogItem("al_ain", 8151, "AIN", "Al Ain FC", "العين الإماراتي", "uae_pro_league", "UAE Pro League", "الدوري الإماراتي", 0xFF6B21A8L, 0xFFFFFFFFL, "shield", listOf("Al Ain", "العين")),
        FootballClubCatalogItem("al_sadd", 8161, "SAD", "Al Sadd SC", "السد القطري", "qatar_stars", "Qatar Stars", "دوري نجوم قطر", 0xFF1E293BL, 0xFFFFFFFFL, "shield", listOf("Al Sadd", "السد")),
        FootballClubCatalogItem("al_shorta", 0, "SHO", "Al Shorta", "الشرطة العراقي", "iraq_stars", "Iraq Stars", "الدوري العراقي", 0xFF15803DL, 0xFFFFFFFFL, "shield", listOf("الشرطة")),
        FootballClubCatalogItem("al_faisaly", 0, "FAI", "Al Faisaly", "الفيصلي الأردني", "jordan_pro", "Jordan Pro", "الدوري الأردني", 0xFF1D4ED8L, 0xFFFFFFFFL, "shield", listOf("الفيصلي")),
        FootballClubCatalogItem("al_wehdat", 0, "WEH", "Al Wehdat", "الوحدات", "jordan_pro", "Jordan Pro", "الدوري الأردني", 0xFF15803DL, 0xFFDC2626L, "shield"),
        // Turkey, Netherlands & Portugal
        FootballClubCatalogItem("galatasaray", 547, "GAL", "Galatasaray", "غلطة سراي", "turkish_super_lig", "Süper Lig", "الدوري التركي", 0xFFA90432L, 0xFFFDB912L, "shield"),
        FootballClubCatalogItem("fenerbahce", 546, "FEN", "Fenerbahçe", "فنربخشة", "turkish_super_lig", "Süper Lig", "الدوري التركي", 0xFF002D72L, 0xFFFFED00L, "shield", listOf("Fenerbahce")),
        FootballClubCatalogItem("ajax", 412, "AJX", "Ajax", "أياكس", "eredivisie", "Eredivisie", "الدوري الهولندي", 0xFFD2122EL, 0xFFFFFFFFL, "circle"),
        FootballClubCatalogItem("benfica", 448, "BEN", "Benfica", "بنفيكا", "liga_portugal", "Liga Portugal", "الدوري البرتغالي", 0xFFE30613L, 0xFFFFFFFFL, "shield")
    )

    fun isClubFollowed(
        favoriteClubIds: Set<String>,
        teamCode: String,
        teamNameEn: String,
        teamNameAr: String,
        localizedName: String = ""
    ): Boolean {
        if (favoriteClubIds.isEmpty()) return false
        for (club in ALL_CLUBS) {
            if (club.id in favoriteClubIds &&
                club.matchesTeam(teamCode, teamNameEn, teamNameAr, localizedName)
            ) {
                return true
            }
        }
        return false
    }

    fun isMatchFollowed(favoriteClubIds: Set<String>, match: FootballMatchItem): Boolean {
        if (favoriteClubIds.isEmpty()) return false
        return isClubFollowed(
            favoriteClubIds,
            match.homeCode,
            match.homeNameEn,
            match.homeNameAr,
            match.homeNameLocalized
        ) || isClubFollowed(
            favoriteClubIds,
            match.awayCode,
            match.awayNameEn,
            match.awayNameAr,
            match.awayNameLocalized
        )
    }

    /**
     * Computes realistic Home / Draw / Away win probabilities (summing to 100%)
     * based on league table position, home advantage, current scoreline, and match progress.
     */
    fun computeMatchProbabilities(
        match: FootballMatchItem,
        standings: List<LeagueStandingRow>
    ): MatchProbabilities {
        if (match.probabilities != null && match.probabilities.isFromLiveOdds) {
            return match.probabilities
        }

        if (match.statusType == MatchStatusType.FINISHED && match.homeScore != null && match.awayScore != null) {
            return when {
                match.homeScore > match.awayScore -> MatchProbabilities(100, 0, 0, isFromLiveOdds = false)
                match.awayScore > match.homeScore -> MatchProbabilities(0, 0, 100, isFromLiveOdds = false)
                else -> MatchProbabilities(0, 100, 0, isFromLiveOdds = false)
            }
        }

        val homeRow = standings.find {
            it.teamCode.equals(match.homeCode, ignoreCase = true) ||
                it.teamNameEn.equals(match.homeNameEn, ignoreCase = true) ||
                it.teamNameAr == match.homeNameAr
        }
        val awayRow = standings.find {
            it.teamCode.equals(match.awayCode, ignoreCase = true) ||
                it.teamNameEn.equals(match.awayNameEn, ignoreCase = true) ||
                it.teamNameAr == match.awayNameAr
        }

        val totalTeams = standings.size.coerceAtLeast(10)
        val homeRank = homeRow?.rank ?: (totalTeams / 2)
        val awayRank = awayRow?.rank ?: (totalTeams / 2)
        val rankEdge = (awayRank - homeRank).toDouble() / totalTeams.toDouble()

        // Pre-match strength rating with natural home advantage (+0.28)
        var strengthDelta = 0.28 + rankEdge * 1.15

        // Incorporate live score & elapsed minute for LIVE fixtures
        val progress = match.progressRatio.coerceIn(0f, 0.98f).toDouble()
        if (match.statusType == MatchStatusType.LIVE && match.homeScore != null && match.awayScore != null) {
            val goalDiff = (match.homeScore - match.awayScore).toDouble()
            val timeWeight = 0.65 + progress * 1.75
            strengthDelta += goalDiff * timeWeight
        }

        val homeRaw = exp(strengthDelta * 0.95)
        val awayRaw = exp(-strengthDelta * 0.95)
        val drawFactor = when {
            match.statusType == MatchStatusType.LIVE && match.homeScore == match.awayScore ->
                0.78 + progress * 0.70
            match.statusType == MatchStatusType.LIVE ->
                (0.68 - progress * 0.42).coerceAtLeast(0.18)
            else ->
                0.76 - kotlin.math.abs(rankEdge) * 0.24
        }.coerceIn(0.15, 1.5)

        val total = homeRaw + awayRaw + drawFactor
        var homePct = ((homeRaw / total) * 100.0).roundToInt().coerceIn(4, 92)
        var awayPct = ((awayRaw / total) * 100.0).roundToInt().coerceIn(4, 92)
        var drawPct = (100 - homePct - awayPct).coerceAtLeast(3)

        // Normalize strictly to 100
        val sum = homePct + drawPct + awayPct
        if (sum != 100) {
            val diff = 100 - sum
            if (homePct >= awayPct) homePct = (homePct + diff).coerceIn(3, 94)
            else awayPct = (awayPct + diff).coerceIn(3, 94)
            drawPct = (100 - homePct - awayPct).coerceAtLeast(2)
        }

        return MatchProbabilities(
            homeWinPct = homePct,
            drawPct = drawPct,
            awayWinPct = awayPct,
            isFromLiveOdds = false
        )
    }

    fun enrichLeagueWithProbabilities(data: LeagueDashboardData): LeagueDashboardData {
        val enrichedFixtures = data.fixtures.map { m ->
            if (m.probabilities != null) m
            else m.copy(probabilities = computeMatchProbabilities(m, data.standings))
        }
        val enrichedFeatured = enrichedFixtures.find { it.id == data.featuredMatch.id }
            ?: data.featuredMatch.let { m ->
                if (m.probabilities != null) m
                else m.copy(probabilities = computeMatchProbabilities(m, data.standings))
            }
        return data.copy(
            featuredMatch = enrichedFeatured,
            fixtures = enrichedFixtures
        )
    }

    private fun logo(id: Int): String = if (id > 0) FootballRepository.build365CompetitorLogoUrl(id) else ""

    fun buildExtraLeaguesBaselines(defaultHeadlines: List<FootballHeadlineItem>): Map<String, LeagueDashboardData> {
        val map = linkedMapOf<String, LeagueDashboardData>()

        // BUNDESLIGA
        val bunFeatured = FootballMatchItem(
            id = "bun_bay_bvb", homeCode = "BAY", homeNameEn = "BAYERN", homeNameAr = "بايرن ميونخ",
            homePrimaryColor = 0xFFDC052DL, homeSecondaryColor = 0xFF0066B2L, homeCrestStyle = "circle", homeLogoUrl = logo(335),
            awayCode = "BVB", awayNameEn = "DORTMUND", awayNameAr = "دورتموند",
            awayPrimaryColor = 0xFFFDE100L, awaySecondaryColor = 0xFF000000L, awayCrestStyle = "circle", awayLogoUrl = logo(337),
            homeScore = 2, awayScore = 1, statusType = MatchStatusType.LIVE, statusBadgeText = "64'", progressRatio = 64f / 90f,
            goalEvents = listOf(
                FootballGoalEvent("Kane", "هاري كين", "19'", true),
                FootballGoalEvent("Guirassy", "غيراسي", "42'", false),
                FootballGoalEvent("Musiala", "موسيالا", "59'", true)
            )
        )
        map["bundesliga"] = enrichLeagueWithProbabilities(
            LeagueDashboardData(
                leagueId = "bundesliga",
                featuredMatch = bunFeatured,
                fixtures = listOf(
                    bunFeatured,
                    FootballMatchItem("bun_lev_rbl", "LEV", "LEVERKUSEN", "ليفركوزن", 0xFFE32221L, 0xFF000000L, "circle", logo(334), "RBL", "RB LEIPZIG", "لايبزيغ", 0xFFDD0741L, 0xFFFFFFFFL, "shield", logo(7124), 2, 0, MatchStatusType.FINISHED, "FT", 1f),
                    FootballMatchItem("bun_stu_fra", "STU", "STUTTGART", "شتوتغارت", 0xFFE32219L, 0xFFFFFFFFL, "shield", logo(346), "FRA", "FRANKFURT", "فرانكفورت", 0xFFE1000FL, 0xFF000000L, "circle", logo(349), null, null, MatchStatusType.UPCOMING, "17:30", 0f),
                    FootballMatchItem("bun_fre_gla", "FRE", "FREIBURG", "فرايبورغ", 0xFF111111L, 0xFFDC2626L, "shield", logo(345), "BMG", "GLADBACH", "غلادباخ", 0xFF15803DL, 0xFFFFFFFFL, "shield", logo(336), null, null, MatchStatusType.UPCOMING, "19:30", 0f),
                    FootballMatchItem("bun_wol_wer", "WOB", "WOLFSBURG", "فولفسبورغ", 0xFF65B32EL, 0xFFFFFFFFL, "circle", logo(353), "SVW", "WERDER", "فيردر بريمن", 0xFF1D9053L, 0xFFFFFFFFL, "shield", logo(351), null, null, MatchStatusType.UPCOMING, "20:30", 0f),
                    FootballMatchItem("bun_uni_mai", "FCU", "UNION BERLIN", "يونيون برلين", 0xFFEB1923L, 0xFFFACC15L, "shield", logo(361), "M05", "MAINZ", "ماينز", 0xFFC3141EL, 0xFFFFFFFFL, "circle", logo(340), null, null, MatchStatusType.UPCOMING, "21:30", 0f)
                ),
                topScorers = listOf(
                    TopScorerItem(1, "Harry Kane", "هاري كين", "BAY", 14, 14),
                    TopScorerItem(2, "Omar Marmoush", "عمر مرموش", "FRA", 11, 14),
                    TopScorerItem(3, "Guirassy", "غيراسي", "BVB", 9, 14),
                    TopScorerItem(4, "Wirtz", "فلوريان فيرتز", "LEV", 8, 14)
                ),
                standings = listOf(
                    LeagueStandingRow(1, "BAY", "Bayern Munich", "بايرن ميونخ", 0xFFDC052DL, 0xFF0066B2L, "circle", logo(335), 12, "+29", 30, listOf('W', 'W', 'W', 'W', 'D'), true),
                    LeagueStandingRow(2, "LEV", "Leverkusen", "باير ليفركوزن", 0xFFE32221L, 0xFF000000L, "circle", logo(334), 12, "+15", 24, listOf('W', 'D', 'W', 'W', 'D'), true),
                    LeagueStandingRow(3, "FRA", "Eintracht Frankfurt", "آينتراخت فرانكفورت", 0xFFE1000FL, 0xFF000000L, "circle", logo(349), 12, "+13", 23, listOf('W', 'W', 'D', 'W', 'W'), true),
                    LeagueStandingRow(4, "RBL", "RB Leipzig", "لايبزيغ", 0xFFDD0741L, 0xFFFFFFFFL, "shield", logo(7124), 12, "+9", 21, listOf('W', 'L', 'D', 'W', 'L'), true),
                    LeagueStandingRow(5, "BVB", "Dortmund", "بوروسيا دورتموند", 0xFFFDE100L, 0xFF000000L, "circle", logo(337), 12, "+7", 20, listOf('W', 'L', 'W', 'D', 'W'), false),
                    LeagueStandingRow(6, "FRE", "SC Freiburg", "فرايبورغ", 0xFF111111L, 0xFFDC2626L, "shield", logo(345), 12, "+4", 19, listOf('D', 'W', 'L', 'W', 'D'), false),
                    LeagueStandingRow(7, "STU", "VfB Stuttgart", "شتوتغارت", 0xFFE32219L, 0xFFFFFFFFL, "shield", logo(346), 12, "+3", 18, listOf('W', 'D', 'L', 'W', 'W'), false),
                    LeagueStandingRow(8, "BMG", "M'gladbach", "بوروسيا مونشنغلادباخ", 0xFF15803DL, 0xFFFFFFFFL, "shield", logo(336), 12, "+1", 17, listOf('W', 'D', 'W', 'L', 'W'), false),
                    LeagueStandingRow(9, "WOB", "Wolfsburg", "فولفسبورغ", 0xFF65B32EL, 0xFFFFFFFFL, "circle", logo(353), 12, "+2", 16, listOf('W', 'W', 'D', 'L', 'D'), false),
                    LeagueStandingRow(10, "SVW", "Werder Bremen", "فيردر بريمن", 0xFF1D9053L, 0xFFFFFFFFL, "shield", logo(351), 12, "-1", 16, listOf('D', 'W', 'L', 'W', 'D'), false),
                    LeagueStandingRow(11, "FCU", "Union Berlin", "يونيون برلين", 0xFFEB1923L, 0xFFFACC15L, "shield", logo(361), 12, "-2", 15, listOf('L', 'D', 'W', 'L', 'D'), false),
                    LeagueStandingRow(12, "M05", "Mainz 05", "ماينز", 0xFFC3141EL, 0xFFFFFFFFL, "circle", logo(340), 12, "-1", 14, listOf('W', 'D', 'L', 'D', 'W'), false)
                ),
                headlines = defaultHeadlines
            )
        )

        // LIGUE 1
        val ligFeatured = FootballMatchItem(
            id = "fra_psg_om", homeCode = "PSG", homeNameEn = "PARIS SG", homeNameAr = "باريس سان جيرمان",
            homePrimaryColor = 0xFF004170L, homeSecondaryColor = 0xFFDA291CL, homeCrestStyle = "circle", homeLogoUrl = logo(498),
            awayCode = "OM", awayNameEn = "MARSEILLE", awayNameAr = "مارسيليا",
            awayPrimaryColor = 0xFF2FAEE0L, awaySecondaryColor = 0xFFFFFFFFL, awayCrestStyle = "shield", awayLogoUrl = logo(495),
            homeScore = 2, awayScore = 0, statusType = MatchStatusType.LIVE, statusBadgeText = "58'", progressRatio = 58f / 90f,
            goalEvents = listOf(
                FootballGoalEvent("Dembélé", "ديمبيلي", "16'", true),
                FootballGoalEvent("Barcola", "باركولا", "49'", true)
            )
        )
        map["ligue_1"] = enrichLeagueWithProbabilities(
            LeagueDashboardData(
                leagueId = "ligue_1",
                featuredMatch = ligFeatured,
                fixtures = listOf(
                    ligFeatured,
                    FootballMatchItem("fra_mon_lil", "ASM", "MONACO", "موناكو", 0xFFE51B22L, 0xFFFFFFFFL, "shield", logo(496), "LIL", "LILLE", "ليل", 0xFFE01E13L, 0xFF1D2C59L, "shield", logo(492), 1, 1, MatchStatusType.FINISHED, "FT", 1f),
                    FootballMatchItem("fra_lyo_nic", "OL", "LYON", "ليون", 0xFF14387FL, 0xFFDA291CL, "shield", logo(494), "OGC", "NICE", "نيس", 0xFFCC0000L, 0xFF000000L, "shield", logo(497), null, null, MatchStatusType.UPCOMING, "18:00", 0f),
                    FootballMatchItem("fra_len_ren", "RCL", "LENS", "لانس", 0xFFE30613L, 0xFFFFED00L, "shield", logo(491), "SR", "RENNES", "رين", 0xFFE41E26L, 0xFF000000L, "circle", logo(500), null, null, MatchStatusType.UPCOMING, "20:00", 0f),
                    FootballMatchItem("fra_bre_str", "SB29", "BREST", "بريست", 0xFFDF0024L, 0xFFFFFFFFL, "shield", logo(487), "STR", "STRASBOURG", "ستراسبورغ", 0xFF009EE0L, 0xFFFFFFFFL, "circle", logo(503), null, null, MatchStatusType.UPCOMING, "22:00", 0f)
                ),
                topScorers = listOf(
                    TopScorerItem(1, "Barcola", "باركولا", "PSG", 10, 10),
                    TopScorerItem(2, "Greenwood", "غرينوود", "OM", 8, 10),
                    TopScorerItem(3, "Jonathan David", "جوناثان ديفيد", "LIL", 8, 10),
                    TopScorerItem(4, "Dembélé", "ديمبيلي", "PSG", 7, 10)
                ),
                standings = listOf(
                    LeagueStandingRow(1, "PSG", "Paris SG", "باريس سان جيرمان", 0xFF004170L, 0xFFDA291CL, "circle", logo(498), 12, "+25", 32, listOf('W', 'W', 'W', 'W', 'D'), true),
                    LeagueStandingRow(2, "ASM", "AS Monaco", "موناكو", 0xFFE51B22L, 0xFFFFFFFFL, "shield", logo(496), 12, "+12", 26, listOf('W', 'L', 'W', 'W', 'W'), true),
                    LeagueStandingRow(3, "OM", "Marseille", "مارسيليا", 0xFF2FAEE0L, 0xFFFFFFFFL, "shield", logo(495), 12, "+11", 23, listOf('W', 'W', 'L', 'W', 'D'), true),
                    LeagueStandingRow(4, "LIL", "Lille", "ليل", 0xFFE01E13L, 0xFF1D2C59L, "shield", logo(492), 12, "+8", 22, listOf('D', 'W', 'D', 'W', 'W'), true),
                    LeagueStandingRow(5, "OL", "Lyon", "أولمبيك ليون", 0xFF14387FL, 0xFFDA291CL, "shield", logo(494), 12, "+6", 20, listOf('W', 'D', 'W', 'W', 'L'), false),
                    LeagueStandingRow(6, "OGC", "OGC Nice", "نيس", 0xFFCC0000L, 0xFF000000L, "shield", logo(497), 12, "+7", 19, listOf('W', 'W', 'D', 'L', 'W'), false),
                    LeagueStandingRow(7, "RCL", "RC Lens", "لانس", 0xFFE30613L, 0xFFFFED00L, "shield", logo(491), 12, "+3", 18, listOf('L', 'W', 'D', 'W', 'D'), false),
                    LeagueStandingRow(8, "SR", "Rennes", "رين", 0xFFE41E26L, 0xFF000000L, "circle", logo(500), 12, "+1", 16, listOf('W', 'L', 'D', 'W', 'L'), false),
                    LeagueStandingRow(9, "SB29", "Brest", "بريست", 0xFFDF0024L, 0xFFFFFFFFL, "shield", logo(487), 12, "-2", 15, listOf('L', 'W', 'L', 'W', 'D'), false),
                    LeagueStandingRow(10, "STR", "Strasbourg", "ستراسبورغ", 0xFF009EE0L, 0xFFFFFFFFL, "circle", logo(503), 12, "-1", 14, listOf('D', 'W', 'L', 'D', 'W'), false)
                ),
                headlines = defaultHeadlines
            )
        )

        // EGYPTIAN PREMIER LEAGUE
        val egyFeatured = FootballMatchItem(
            id = "egy_ahl_zam", homeCode = "ALY", homeNameEn = "AL AHLY", homeNameAr = "الأهلي",
            homePrimaryColor = 0xFFC8102EL, homeSecondaryColor = 0xFFFFFFFFL, homeCrestStyle = "shield", homeLogoUrl = logo(7561),
            awayCode = "ZAM", awayNameEn = "ZAMALEK", awayNameAr = "الزمالك",
            awayPrimaryColor = 0xFF1E293BL, awaySecondaryColor = 0xFFDC2626L, awayCrestStyle = "shield", awayLogoUrl = logo(7562),
            homeScore = 2, awayScore = 1, statusType = MatchStatusType.LIVE, statusBadgeText = "72'", progressRatio = 72f / 90f,
            goalEvents = listOf(
                FootballGoalEvent("Wessam Abou Ali", "وسام أبو علي", "24'", true),
                FootballGoalEvent("Zizo", "زيزو", "48'", false),
                FootballGoalEvent("Emam Ashour", "إمام عاشور", "66'", true)
            )
        )
        map["egyptian_league"] = enrichLeagueWithProbabilities(
            LeagueDashboardData(
                leagueId = "egyptian_league",
                featuredMatch = egyFeatured,
                fixtures = listOf(
                    egyFeatured,
                    FootballMatchItem("egy_pyr_mas", "PYR", "PYRAMIDS", "بيراميدز", 0xFF1E3A8AL, 0xFF38BDF8L, "circle", logo(52281), "MAS", "AL MASRY", "المصري", 0xFF15803DL, 0xFFFFFFFFL, "shield", "", 2, 0, MatchStatusType.FINISHED, "FT", 1f),
                    FootballMatchItem("egy_ism_iti", "ISM", "ISMAILY", "الإسماعيلي", 0xFFFACC15L, 0xFF1D4ED8L, "shield", "", "ITI", "AL ITTIHAD", "الاتحاد السكندري", 0xFF15803DL, 0xFFFFFFFFL, "circle", "", null, null, MatchStatusType.UPCOMING, "18:00", 0f),
                    FootballMatchItem("egy_cer_fut", "CER", "CERAMICA", "سيراميكا كليوباترا", 0xFF991B1BL, 0xFFFACC15L, "shield", "", "MOD", "MODERN SPORT", "مودرن سبورت", 0xFFDC2626L, 0xFF111111L, "circle", "", null, null, MatchStatusType.UPCOMING, "20:00", 0f),
                    FootballMatchItem("egy_enp_smo", "ENP", "ENPPI", "إنبي", 0xFF1D4ED8L, 0xFFFFFFFFL, "circle", "", "SMO", "SMOUHA", "سموحة", 0xFF0284C7L, 0xFFFFFFFFL, "shield", "", null, null, MatchStatusType.UPCOMING, "21:30", 0f)
                ),
                topScorers = listOf(
                    TopScorerItem(1, "Wessam Abou Ali", "وسام أبو علي", "ALY", 9, 9),
                    TopScorerItem(2, "Fiston Mayele", "فيستون ماييلي", "PYR", 8, 9),
                    TopScorerItem(3, "Zizo", "أحمد سيد زيزو", "ZAM", 7, 9),
                    TopScorerItem(4, "Salah Mohsen", "صلاح محسن", "MAS", 6, 9)
                ),
                standings = listOf(
                    LeagueStandingRow(1, "ALY", "Al Ahly", "الأهلي", 0xFFC8102EL, 0xFFFFFFFFL, "shield", logo(7561), 10, "+16", 26, listOf('W', 'W', 'D', 'W', 'W'), true),
                    LeagueStandingRow(2, "PYR", "Pyramids FC", "بيراميدز", 0xFF1E3A8AL, 0xFF38BDF8L, "circle", logo(52281), 10, "+14", 24, listOf('W', 'W', 'W', 'D', 'W'), true),
                    LeagueStandingRow(3, "ZAM", "Zamalek", "الزمالك", 0xFF1E293BL, 0xFFDC2626L, "shield", logo(7562), 10, "+10", 21, listOf('W', 'D', 'W', 'L', 'W'), true),
                    LeagueStandingRow(4, "MAS", "Al Masry", "المصري", 0xFF15803DL, 0xFFFFFFFFL, "shield", "", 10, "+6", 19, listOf('W', 'D', 'W', 'W', 'L'), true),
                    LeagueStandingRow(5, "CER", "Ceramica", "سيراميكا كليوباترا", 0xFF991B1BL, 0xFFFACC15L, "shield", "", 10, "+4", 17, listOf('D', 'W', 'L', 'W', 'D'), false),
                    LeagueStandingRow(6, "ITI", "Al Ittihad Alex", "الاتحاد السكندري", 0xFF15803DL, 0xFFFFFFFFL, "circle", "", 10, "+2", 15, listOf('W', 'L', 'D', 'W', 'D'), false),
                    LeagueStandingRow(7, "MOD", "Modern Sport", "مودرن سبورت", 0xFFDC2626L, 0xFF111111L, "circle", "", 10, "+1", 14, listOf('D', 'D', 'W', 'L', 'W'), false),
                    LeagueStandingRow(8, "SMO", "Smouha", "سموحة", 0xFF0284C7L, 0xFFFFFFFFL, "shield", "", 10, "0", 13, listOf('L', 'W', 'D', 'D', 'W'), false),
                    LeagueStandingRow(9, "ISM", "Ismaily", "الإسماعيلي", 0xFFFACC15L, 0xFF1D4ED8L, "shield", "", 10, "-2", 12, listOf('D', 'L', 'W', 'D', 'L'), false),
                    LeagueStandingRow(10, "ENP", "ENPPI", "إنبي", 0xFF1D4ED8L, 0xFFFFFFFFL, "circle", "", 10, "-3", 11, listOf('L', 'D', 'L', 'W', 'D'), false)
                ),
                headlines = defaultHeadlines
            )
        )

        // AFC CHAMPIONS LEAGUE ELITE
        map["afc_champions"] = buildCustomLeagueBaseline(
            leagueId = "afc_champions",
            clubs = listOf(
                ClubSeed("HIL", "Al Hilal", "الهلال", 0xFF0054A6L, 0xFFFFFFFFL, 8111, 28, "+18"),
                ClubSeed("AHL", "Al Ahli", "الأهلي السعودي", 0xFF007A3DL, 0xFFFFFFFFL, 8110, 26, "+15"),
                ClubSeed("NAS", "Al Nassr", "النصر", 0xFFFDD000L, 0xFF004B87L, 8112, 23, "+11"),
                ClubSeed("SAD", "Al Sadd", "السد", 0xFF1E293BL, 0xFFFFFFFFL, 8161, 20, "+8"),
                ClubSeed("AIN", "Al Ain", "العين", 0xFF6B21A8L, 0xFFFFFFFFL, 8151, 18, "+6"),
                ClubSeed("WAS", "Al Wasl", "الوصل", 0xFFFACC15L, 0xFF111111L, 0, 17, "+4"),
                ClubSeed("RAY", "Al Rayyan", "الريان", 0xFFDC2626L, 0xFF111111L, 0, 15, "+2"),
                ClubSeed("PER", "Persepolis", "برسبوليس", 0xFFDC2626L, 0xFFFFFFFFL, 0, 14, "+1"),
                ClubSeed("VIS", "Vissel Kobe", "فيسيل كوبي", 0xFF991B1BL, 0xFFFFFFFFL, 0, 14, "+3"),
                ClubSeed("YOK", "Yokohama FM", "يوكوهاما", 0xFF1D4ED8L, 0xFFDC2626L, 0, 13, "0")
            ),
            scorers = listOf(
                TopScorerItem(1, "Salem Al Dawsari", "سالم الدوسري", "HIL", 7, 7),
                TopScorerItem(2, "Ronaldo", "كريستيانو رونالدو", "NAS", 6, 7),
                TopScorerItem(3, "Riyad Mahrez", "رياض محرز", "AHL", 5, 7),
                TopScorerItem(4, "Akram Afif", "أكرم عفيف", "SAD", 5, 7)
            ),
            headlines = defaultHeadlines
        )

        // CAF CHAMPIONS LEAGUE
        map["caf_champions"] = buildCustomLeagueBaseline(
            leagueId = "caf_champions",
            clubs = listOf(
                ClubSeed("ALY", "Al Ahly", "الأهلي", 0xFFC8102EL, 0xFFFFFFFFL, 7561, 16, "+11"),
                ClubSeed("EST", "Espérance", "الترجي", 0xFFDC2626L, 0xFFFACC15L, 7559, 14, "+8"),
                ClubSeed("PYR", "Pyramids", "بيراميدز", 0xFF1E3A8AL, 0xFF38BDF8L, 52281, 13, "+7"),
                ClubSeed("FAR", "AS FAR", "الجيش الملكي", 0xFF111111L, 0xFFDC2626L, 0, 12, "+6"),
                ClubSeed("RCA", "Raja AC", "الرجاء", 0xFF15803DL, 0xFFFFFFFFL, 7552, 11, "+4"),
                ClubSeed("MCA", "MC Alger", "مولودية الجزائر", 0xFF15803DL, 0xFFDC2626L, 7544, 10, "+3"),
                ClubSeed("SUN", "Sundowns", "صن داونز", 0xFFFACC15L, 0xFF15803DL, 0, 10, "+4"),
                ClubSeed("ORL", "Orlando Pirates", "أورلاندو بايرتس", 0xFF111111L, 0xFFFFFFFFL, 0, 9, "+2"),
                ClubSeed("CRB", "CR Belouizdad", "شباب بلوزداد", 0xFFDC2626L, 0xFFFFFFFFL, 0, 8, "+1"),
                ClubSeed("HIL", "Al Hilal Omdurman", "الهلال السوداني", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 8, "0")
            ),
            scorers = listOf(
                TopScorerItem(1, "Emam Ashour", "إمام عاشور", "ALY", 5, 5),
                TopScorerItem(2, "Youcef Belaïli", "يوسف بلايلي", "EST", 5, 5),
                TopScorerItem(3, "Fiston Mayele", "ماييلي", "PYR", 4, 5),
                TopScorerItem(4, "Amine Zouhzouh", "أمين زحزوح", "FAR", 4, 5)
            ),
            headlines = defaultHeadlines
        )

        // MOROCCO BOTOLA PRO
        map["morocco_botola"] = buildCustomLeagueBaseline(
            leagueId = "morocco_botola",
            clubs = listOf(
                ClubSeed("RSB", "RS Berkane", "نهضة بركان", 0xFFF97316L, 0xFF111111L, 0, 27, "+14"),
                ClubSeed("FAR", "AS FAR", "الجيش الملكي", 0xFF111111L, 0xFFDC2626L, 0, 24, "+11"),
                ClubSeed("WAC", "Wydad AC", "الوداد الرياضي", 0xFFDC2626L, 0xFFFFFFFFL, 7551, 22, "+8"),
                ClubSeed("RCA", "Raja AC", "الرجاء الرياضي", 0xFF15803DL, 0xFFFFFFFFL, 7552, 21, "+7"),
                ClubSeed("FUS", "FUS Rabat", "الفتح الرباطي", 0xFFDC2626L, 0xFFFFFFFFL, 0, 19, "+5"),
                ClubSeed("MAS", "MAS Fez", "المغرب الفاسي", 0xFFFACC15L, 0xFF111111L, 0, 18, "+3"),
                ClubSeed("IRT", "IR Tanger", "اتحاد طنجة", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 16, "+1"),
                ClubSeed("OCS", "OC Safi", "أولمبيك آسفي", 0xFF1E3A8AL, 0xFFDC2626L, 0, 15, "0"),
                ClubSeed("HUS", "Hassania Agadir", "حسنية أكادير", 0xFFDC2626L, 0xFFFFFFFFL, 0, 14, "-1"),
                ClubSeed("DHJ", "DH El Jadida", "الدفاع الجديدي", 0xFF15803DL, 0xFFFFFFFFL, 0, 13, "-2")
            ),
            scorers = listOf(
                TopScorerItem(1, "Youssef El Fahli", "يوسف الفحلي", "FAR", 8, 8),
                TopScorerItem(2, "Oussama Lamlioui", "أسامة المليوي", "RSB", 7, 8),
                TopScorerItem(3, "Rayhi", "محمد الرايحي", "WAC", 6, 8),
                TopScorerItem(4, "Adam Ennafati", "آدم النفاتي", "RCA", 6, 8)
            ),
            headlines = defaultHeadlines
        )

        // UAE PRO LEAGUE
        map["uae_pro_league"] = buildCustomLeagueBaseline(
            leagueId = "uae_pro_league",
            clubs = listOf(
                ClubSeed("SHA", "Shabab Al Ahli", "شباب الأهلي", 0xFFDC2626L, 0xFF15803DL, 0, 26, "+15"),
                ClubSeed("SHJ", "Sharjah FC", "الشارقة", 0xFFDC2626L, 0xFFFFFFFFL, 0, 24, "+13"),
                ClubSeed("AIN", "Al Ain", "العين", 0xFF6B21A8L, 0xFFFFFFFFL, 8151, 21, "+10"),
                ClubSeed("WAS", "Al Wasl", "الوصل", 0xFFFACC15L, 0xFF111111L, 0, 19, "+7"),
                ClubSeed("WAH", "Al Wahda", "الوحدة", 0xFF7F1D1DL, 0xFFFFFFFFL, 0, 18, "+5"),
                ClubSeed("JAZ", "Al Jazira", "الجزيرة", 0xFF111111L, 0xFFFFFFFFL, 0, 17, "+3"),
                ClubSeed("NAS", "Al Nasr", "النصر الإماراتي", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 16, "+2"),
                ClubSeed("AJM", "Ajman", "عجمان", 0xFFF97316L, 0xFFFFFFFFL, 0, 13, "-2"),
                ClubSeed("KHO", "Khor Fakkan", "خورفكان", 0xFF15803DL, 0xFFFFFFFFL, 0, 11, "-4"),
                ClubSeed("BAN", "Baniyas", "بني ياس", 0xFF0284C7L, 0xFFFFFFFFL, 0, 10, "-5")
            ),
            scorers = listOf(
                TopScorerItem(1, "Kodjo Laba", "لابا كودجو", "AIN", 11, 11),
                TopScorerItem(2, "Sardar Azmoun", "سردار أزمون", "SHA", 9, 11),
                TopScorerItem(3, "Omar Khribin", "عمر خربين", "WAH", 8, 11),
                TopScorerItem(4, "Caio Lucas", "كايو لوكاس", "SHJ", 7, 11)
            ),
            headlines = defaultHeadlines
        )

        // QATAR STARS LEAGUE
        map["qatar_stars"] = buildCustomLeagueBaseline(
            leagueId = "qatar_stars",
            clubs = listOf(
                ClubSeed("DUH", "Al Duhail", "الدحيل", 0xFFDC2626L, 0xFFFFFFFFL, 0, 25, "+16"),
                ClubSeed("SAD", "Al Sadd", "السد", 0xFF1E293BL, 0xFFFFFFFFL, 8161, 24, "+14"),
                ClubSeed("GHA", "Al Gharafa", "الغرافة", 0xFFFACC15L, 0xFF1D4ED8L, 0, 21, "+8"),
                ClubSeed("WAK", "Al Wakrah", "الوكرة", 0xFF0284C7L, 0xFFFFFFFFL, 0, 19, "+5"),
                ClubSeed("RAY", "Al Rayyan", "الريان", 0xFFDC2626L, 0xFF111111L, 0, 17, "+3"),
                ClubSeed("AHL", "Al Ahli Doha", "الأهلي القطري", 0xFF15803DL, 0xFFFFFFFFL, 0, 16, "+2"),
                ClubSeed("ARA", "Al Arabi", "العربي القطري", 0xFF991B1BL, 0xFFFFFFFFL, 0, 14, "0"),
                ClubSeed("SHA", "Al Shamal", "الشمال", 0xFFDC2626L, 0xFFFFFFFFL, 0, 13, "-1"),
                ClubSeed("UMM", "Umm Salal", "أم صلال", 0xFFF97316L, 0xFFFFFFFFL, 0, 11, "-4"),
                ClubSeed("QAT", "Qatar SC", "قطر", 0xFFFACC15L, 0xFF111111L, 0, 10, "-5")
            ),
            scorers = listOf(
                TopScorerItem(1, "Baghdad Bounedjah", "بغداد بونجاح", "SHA", 10, 10),
                TopScorerItem(2, "Akram Afif", "أكرم عفيف", "SAD", 9, 10),
                TopScorerItem(3, "Michael Olunga", "أولونغا", "DUH", 8, 10),
                TopScorerItem(4, "Joselu", "خوسيلو", "GHA", 7, 10)
            ),
            headlines = defaultHeadlines
        )

        // ALGERIAN LIGUE 1
        map["algeria_ligue_1"] = buildCustomLeagueBaseline(
            leagueId = "algeria_ligue_1",
            clubs = listOf(
                ClubSeed("MCA", "MC Alger", "مولودية الجزائر", 0xFF15803DL, 0xFFDC2626L, 7544, 24, "+11"),
                ClubSeed("JSK", "JS Kabylie", "شبيبة القبائل", 0xFFFACC15L, 0xFF15803DL, 0, 21, "+8"),
                ClubSeed("CRB", "CR Belouizdad", "شباب بلوزداد", 0xFFDC2626L, 0xFFFFFFFFL, 0, 20, "+7"),
                ClubSeed("USM", "USM Alger", "اتحاد العاصمة", 0xFFDC2626L, 0xFF111111L, 0, 19, "+6"),
                ClubSeed("CSC", "CS Constantine", "شباب قسنطينة", 0xFF15803DL, 0xFF111111L, 0, 18, "+4"),
                ClubSeed("ESS", "ES Sétif", "وفاق سطيف", 0xFF111111L, 0xFFFFFFFFL, 0, 17, "+3"),
                ClubSeed("PAC", "Paradou AC", "أتلتيك بارادو", 0xFF1D4ED8L, 0xFFFACC15L, 0, 15, "+1"),
                ClubSeed("MCO", "MC Oran", "مولودية وهران", 0xFFDC2626L, 0xFFFFFFFFL, 0, 14, "0"),
                ClubSeed("ASO", "ASO Chlef", "أولمبي الشلف", 0xFFDC2626L, 0xFFFFFFFFL, 0, 13, "-2"),
                ClubSeed("JSS", "JS Saoura", "شبيبة الساورة", 0xFFFACC15L, 0xFF15803DL, 0, 12, "-3")
            ),
            scorers = listOf(
                TopScorerItem(1, "Adil Boulbina", "عادل بولبينة", "PAC", 8, 8),
                TopScorerItem(2, "Andy Delort", "أندي ديلور", "MCA", 7, 8),
                TopScorerItem(3, "Aymen Mahious", "أيمن محيوص", "CRB", 6, 8),
                TopScorerItem(4, "Berkane", "بركان", "JSK", 6, 8)
            ),
            headlines = defaultHeadlines
        )

        // TUNISIAN LIGUE 1
        map["tunisia_ligue_1"] = buildCustomLeagueBaseline(
            leagueId = "tunisia_ligue_1",
            clubs = listOf(
                ClubSeed("EST", "Espérance", "الترجي الرياضي", 0xFFDC2626L, 0xFFFACC15L, 7559, 25, "+13"),
                ClubSeed("CA", "Club Africain", "النادي الإفريقي", 0xFFDC2626L, 0xFFFFFFFFL, 0, 23, "+10"),
                ClubSeed("USM", "US Monastir", "الاتحاد المنستيري", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 22, "+9"),
                ClubSeed("ESS", "Étoile du Sahel", "النجم الساحلي", 0xFFDC2626L, 0xFFFFFFFFL, 0, 20, "+6"),
                ClubSeed("ST", "Stade Tunisien", "الملعب التونسي", 0xFF15803DL, 0xFFDC2626L, 0, 19, "+5"),
                ClubSeed("CSS", "CS Sfaxien", "النادي الصفاقسي", 0xFF111111L, 0xFFFFFFFFL, 0, 17, "+3"),
                ClubSeed("ESZ", "ES Zarzis", "الترجي الجرجيسي", 0xFFDC2626L, 0xFFFACC15L, 0, 16, "+2"),
                ClubSeed("OB", "O. Béja", "الأولمبي الباجي", 0xFFDC2626L, 0xFFFFFFFFL, 0, 14, "0"),
                ClubSeed("CAB", "CA Bizertin", "النادي البنزرتي", 0xFFFACC15L, 0xFF111111L, 0, 12, "-2"),
                ClubSeed("USM2", "US Ben Guerdane", "اتحاد بن قردان", 0xFFFACC15L, 0xFF111111L, 0, 11, "-4")
            ),
            scorers = listOf(
                TopScorerItem(1, "Youcef Belaïli", "يوسف بلايلي", "EST", 8, 8),
                TopScorerItem(2, "Hazem Mastouri", "حازم المستوري", "USM", 7, 8),
                TopScorerItem(3, "Firas Chaouat", "فراس شواط", "ESS", 7, 8),
                TopScorerItem(4, "Hamdi Labidi", "حمدي العبيدي", "CA", 6, 8)
            ),
            headlines = defaultHeadlines
        )

        // IRAQ STARS LEAGUE
        map["iraq_stars"] = buildCustomLeagueBaseline(
            leagueId = "iraq_stars",
            clubs = listOf(
                ClubSeed("SHO", "Al Shorta", "الشرطة", 0xFF15803DL, 0xFFFFFFFFL, 0, 26, "+14"),
                ClubSeed("ZAW", "Al Zawraa", "الزوراء", 0xFF1E293BL, 0xFFFFFFFFL, 0, 24, "+11"),
                ClubSeed("QAW", "Al Quwa Al Jawiya", "القوة الجوية", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 22, "+9"),
                ClubSeed("TAL", "Al Talaba", "الطلبة", 0xFF0284C7L, 0xFFFFFFFFL, 0, 20, "+6"),
                ClubSeed("ZAK", "Zakho SC", "زاخو", 0xFFDC2626L, 0xFFFFFFFFL, 0, 19, "+5"),
                ClubSeed("DUH", "Duhok SC", "دهوك", 0xFFFACC15L, 0xFF1D4ED8L, 0, 18, "+4"),
                ClubSeed("ERB", "Erbil SC", "أربيل", 0xFFFACC15L, 0xFF111111L, 0, 16, "+2"),
                ClubSeed("NAJ", "Al Najaf", "النجف", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 15, "0"),
                ClubSeed("MIN", "Al Minaa", "الميناء", 0xFF0284C7L, 0xFFFFFFFFL, 0, 13, "-2"),
                ClubSeed("NAF", "Al Naft", "النفط", 0xFF15803DL, 0xFFFACC15L, 0, 12, "-3")
            ),
            scorers = listOf(
                TopScorerItem(1, "Mohanad Ali", "مهند علي", "SHO", 9, 9),
                TopScorerItem(2, "Alaa Abbas", "علاء عباس", "ZAW", 7, 9),
                TopScorerItem(3, "Gustavo Henrique", "غوستافو", "ZAK", 7, 9),
                TopScorerItem(4, "Aymen Hussein", "أيمن حسين", "QAW", 6, 9)
            ),
            headlines = defaultHeadlines
        )

        // JORDAN PRO LEAGUE
        map["jordan_pro"] = buildCustomLeagueBaseline(
            leagueId = "jordan_pro",
            clubs = listOf(
                ClubSeed("HUS", "Al Hussein Irbid", "الحسين إربد", 0xFFFACC15L, 0xFF111111L, 0, 27, "+15"),
                ClubSeed("WEH", "Al Wehdat", "الوحدات", 0xFF15803DL, 0xFFDC2626L, 0, 24, "+12"),
                ClubSeed("FAI", "Al Faisaly", "الفيصلي", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 21, "+8"),
                ClubSeed("RAM", "Al Ramtha", "الرمثا", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 19, "+5"),
                ClubSeed("AHJ", "Al Ahli Amman", "الأهلي الأردني", 0xFF1E293BL, 0xFFFFFFFFL, 0, 17, "+3"),
                ClubSeed("SAL", "Al Salt", "السلط", 0xFFDC2626L, 0xFFFFFFFFL, 0, 16, "+1"),
                ClubSeed("SHB", "Shabab Al Ordon", "شباب الأردن", 0xFFDC2626L, 0xFF111111L, 0, 14, "-1"),
                ClubSeed("JAZ", "Al Jazeera", "الجزيرة الأردني", 0xFFDC2626L, 0xFFFFFFFFL, 0, 13, "-2"),
                ClubSeed("MAAN", "Ma'an SC", "معان", 0xFF0284C7L, 0xFFFFFFFFL, 0, 11, "-4"),
                ClubSeed("AQA", "Shabab Al Aqaba", "شباب العقبة", 0xFF1D4ED8L, 0xFFFACC15L, 0, 10, "-6")
            ),
            scorers = listOf(
                TopScorerItem(1, "Mahmoud Al Mardi", "محمود المرضي", "HUS", 8, 8),
                TopScorerItem(2, "Ousmane Gueye", "أوسمان غي", "WEH", 7, 8),
                TopScorerItem(3, "Yazan Al Naimat", "يزن النعيمات", "FAI", 6, 8),
                TopScorerItem(4, "Hamza Al Dardour", "حمزة الدردور", "RAM", 6, 8)
            ),
            headlines = defaultHeadlines
        )

        // TURKISH SUPER LIG
        map["turkish_super_lig"] = buildCustomLeagueBaseline(
            leagueId = "turkish_super_lig",
            clubs = listOf(
                ClubSeed("GAL", "Galatasaray", "غلطة سراي", 0xFFA90432L, 0xFFFDB912L, 547, 31, "+21"),
                ClubSeed("FEN", "Fenerbahçe", "فنربخشة", 0xFF002D72L, 0xFFFFED00L, 546, 28, "+18"),
                ClubSeed("SAM", "Samsunspor", "سامسون سبور", 0xFFDC2626L, 0xFFFFFFFFL, 0, 23, "+9"),
                ClubSeed("BJK", "Beşiktaş", "بشكتاش", 0xFF111111L, 0xFFFFFFFFL, 545, 22, "+8"),
                ClubSeed("EYU", "Eyüpspor", "أيوب سبور", 0xFF6B21A8L, 0xFFFACC15L, 0, 20, "+5"),
                ClubSeed("GOZ", "Göztepe", "غوزتيبي", 0xFFDC2626L, 0xFFFACC15L, 0, 19, "+4"),
                ClubSeed("BAS", "Başakşehir", "باشاك شهير", 0xFFF97316L, 0xFF1E3A8AL, 0, 17, "+2"),
                ClubSeed("TS", "Trabzonspor", "طرابزون سبور", 0xFF7F1D1DL, 0xFF38BDF8L, 548, 16, "+1"),
                ClubSeed("KAS", "Kasımpaşa", "قاسم باشا", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 14, "-1"),
                ClubSeed("ANT", "Antalyaspor", "أنطاليا سبور", 0xFFDC2626L, 0xFFFFFFFFL, 0, 13, "-3")
            ),
            scorers = listOf(
                TopScorerItem(1, "Victor Osimhen", "فيكتور أوسيمين", "GAL", 11, 11),
                TopScorerItem(2, "Ciro Immobile", "تشيرو إيموبيلي", "BJK", 9, 11),
                TopScorerItem(3, "Edin Džeko", "إدين دجيكو", "FEN", 8, 11),
                TopScorerItem(4, "Youssef En-Nesyri", "يوسف النصيري", "FEN", 7, 11)
            ),
            headlines = defaultHeadlines
        )

        // EREDIVISIE
        map["eredivisie"] = buildCustomLeagueBaseline(
            leagueId = "eredivisie",
            clubs = listOf(
                ClubSeed("PSV", "PSV Eindhoven", "آيندهوفن", 0xFFDC2626L, 0xFFFFFFFFL, 415, 33, "+28"),
                ClubSeed("AJX", "Ajax", "أياكس", 0xFFD2122EL, 0xFFFFFFFFL, 412, 29, "+18"),
                ClubSeed("UTR", "FC Utrecht", "أوتريخت", 0xFFDC2626L, 0xFFFFFFFFL, 0, 26, "+11"),
                ClubSeed("FEY", "Feyenoord", "فينورد", 0xFFDC2626L, 0xFF111111L, 413, 25, "+14"),
                ClubSeed("TWE", "FC Twente", "تفينتي", 0xFFDC2626L, 0xFFFFFFFFL, 0, 22, "+8"),
                ClubSeed("AZ", "AZ Alkmaar", "ألكمار", 0xFFDC2626L, 0xFFFFFFFFL, 411, 20, "+6"),
                ClubSeed("GAE", "Go Ahead Eagles", "غو أهيد إيغلز", 0xFFDC2626L, 0xFFFACC15L, 0, 18, "+2"),
                ClubSeed("NEC", "NEC Nijmegen", "نيميخن", 0xFF15803DL, 0xFFDC2626L, 0, 16, "+1"),
                ClubSeed("FOR", "Fortuna Sittard", "فورتونا سيتارد", 0xFFFACC15L, 0xFF15803DL, 0, 15, "-1"),
                ClubSeed("HEE", "Heerenveen", "هيرنفين", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 14, "-3")
            ),
            scorers = listOf(
                TopScorerItem(1, "Sem Steijn", "سيم ستاين", "TWE", 10, 10),
                TopScorerItem(2, "Ricardo Pepi", "ريكاردو بيبي", "PSV", 9, 10),
                TopScorerItem(3, "Luuk de Jong", "لوك دي يونغ", "PSV", 8, 10),
                TopScorerItem(4, "Brian Brobbey", "بروبي", "AJX", 7, 10)
            ),
            headlines = defaultHeadlines
        )

        // LIGA PORTUGAL
        map["liga_portugal"] = buildCustomLeagueBaseline(
            leagueId = "liga_portugal",
            clubs = listOf(
                ClubSeed("SCP", "Sporting CP", "سبورتينغ لشبونة", 0xFF15803DL, 0xFFFFFFFFL, 450, 33, "+29"),
                ClubSeed("POR", "FC Porto", "بورتو", 0xFF1D4ED8L, 0xFFFFFFFFL, 449, 27, "+19"),
                ClubSeed("BEN", "Benfica", "بنفيكا", 0xFFE30613L, 0xFFFFFFFFL, 448, 25, "+17"),
                ClubSeed("BRA", "SC Braga", "سبورتينغ براغا", 0xFFDC2626L, 0xFFFFFFFFL, 451, 22, "+10"),
                ClubSeed("SAN", "Santa Clara", "سانتا كلارا", 0xFFDC2626L, 0xFFFFFFFFL, 0, 21, "+6"),
                ClubSeed("VIT", "Vitória SC", "فيتوريا غيماريش", 0xFF111111L, 0xFFFFFFFFL, 0, 19, "+5"),
                ClubSeed("FAM", "Famalicão", "فاماليكاو", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 17, "+2"),
                ClubSeed("MOR", "Moreirense", "موريرينسي", 0xFF15803DL, 0xFFFFFFFFL, 0, 16, "+1"),
                ClubSeed("CP", "Casa Pia", "كاسا بيا", 0xFF111111L, 0xFFFFFFFFL, 0, 14, "-1"),
                ClubSeed("RIO", "Rio Ave", "ريو آفي", 0xFF15803DL, 0xFFFFFFFFL, 0, 13, "-3")
            ),
            scorers = listOf(
                TopScorerItem(1, "Viktor Gyökeres", "فيكتور غيوكيريس", "SCP", 16, 16),
                TopScorerItem(2, "Samu Aghehowa", "سامو أوموروديون", "POR", 11, 16),
                TopScorerItem(3, "Pavlidis", "بافليديس", "BEN", 8, 16),
                TopScorerItem(4, "Di María", "دي ماريا", "BEN", 7, 16)
            ),
            headlines = defaultHeadlines
        )

        // EUROPA LEAGUE
        map["europa_league"] = buildCustomLeagueBaseline(
            leagueId = "europa_league",
            clubs = listOf(
                ClubSeed("LAZ", "Lazio", "لاتسيو", 0xFF87D8F7L, 0xFFFFFFFFL, 225, 13, "+10"),
                ClubSeed("ATH", "Athletic Club", "أتلتيك بلباو", 0xFFEE2523L, 0xFFFFFFFFL, 129, 13, "+8"),
                ClubSeed("FRA", "Frankfurt", "فرانكفورت", 0xFFE1000FL, 0xFF000000L, 349, 13, "+7"),
                ClubSeed("GAL", "Galatasaray", "غلطة سراي", 0xFFA90432L, 0xFFFDB912L, 547, 11, "+6"),
                ClubSeed("AJX", "Ajax", "أياكس", 0xFFD2122EL, 0xFFFFFFFFL, 412, 10, "+7"),
                ClubSeed("OL", "Lyon", "ليون", 0xFF14387FL, 0xFFDA291CL, 494, 10, "+5"),
                ClubSeed("TOT", "Tottenham", "توتنهام", 0xFF132257L, 0xFFFFFFFFL, 114, 10, "+4"),
                ClubSeed("MUN", "Man United", "مانشستر يونايتد", 0xFFDA291CL, 0xFFFBE122L, 105, 9, "+3"),
                ClubSeed("ROM", "AS Roma", "روما", 0xFF8E1F2FL, 0xFFF0BC42L, 228, 8, "+1"),
                ClubSeed("POR", "FC Porto", "بورتو", 0xFF1D4ED8L, 0xFFFFFFFFL, 449, 7, "0")
            ),
            scorers = listOf(
                TopScorerItem(1, "Ayoub El Kaabi", "أيوب الكعبي", "OLY", 5, 5),
                TopScorerItem(2, "Rasmus Højlund", "هويلوند", "MUN", 5, 5),
                TopScorerItem(3, "Yunus Akgün", "يونس أكغون", "GAL", 4, 5),
                TopScorerItem(4, "Omar Marmoush", "عمر مرموش", "FRA", 4, 5)
            ),
            headlines = defaultHeadlines
        )

        // CONFERENCE LEAGUE
        map["conference_league"] = buildCustomLeagueBaseline(
            leagueId = "conference_league",
            clubs = listOf(
                ClubSeed("CHE", "Chelsea", "تشيلسي", 0xFF034694L, 0xFFFFFFFFL, 106, 15, "+16"),
                ClubSeed("FIO", "Fiorentina", "فيورنتينا", 0xFF482E92L, 0xFFFFFFFFL, 217, 12, "+9"),
                ClubSeed("LEG", "Legia Warsaw", "ليغيا وارسو", 0xFF15803DL, 0xFFFFFFFFL, 0, 12, "+8"),
                ClubSeed("VIT", "Vitória SC", "غيماريش", 0xFF111111L, 0xFFFFFFFFL, 0, 11, "+6"),
                ClubSeed("BET", "Real Betis", "ريال بيتيس", 0xFF0BB363L, 0xFFFFFFFFL, 135, 10, "+4"),
                ClubSeed("RAP", "Rapid Wien", "رابيد فيينا", 0xFF15803DL, 0xFFFFFFFFL, 0, 10, "+4"),
                ClubSeed("HEI", "Heidenheim", "هايدنهايم", 0xFF1D4ED8L, 0xFFDC2626L, 0, 9, "+2"),
                ClubSeed("COP", "FC Copenhagen", "كوبنهاغن", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 8, "+1"),
                ClubSeed("PAN", "Panathinaikos", "باناثينايكوس", 0xFF15803DL, 0xFFFFFFFFL, 0, 7, "0"),
                ClubSeed("GEN", "KAA Gent", "غينت", 0xFF1D4ED8L, 0xFFFFFFFFL, 0, 7, "-1")
            ),
            scorers = listOf(
                TopScorerItem(1, "Christopher Nkunku", "نكونكو", "CHE", 5, 5),
                TopScorerItem(2, "Marc Guiu", "مارك غويو", "CHE", 4, 5),
                TopScorerItem(3, "Moise Kean", "مويس كين", "FIO", 4, 5),
                TopScorerItem(4, "Abde Ezzalzouli", "عبد الصمد الزلزولي", "BET", 3, 5)
            ),
            headlines = defaultHeadlines
        )

        // INTERNATIONAL / NATIONAL TEAMS
        map["world_cup"] = buildCustomLeagueBaseline(
            leagueId = "world_cup",
            clubs = listOf(
                ClubSeed("ARG", "Argentina", "الأرجنتين", 0xFF38BDF8L, 0xFFFFFFFFL, 0, 25, "+14"),
                ClubSeed("ESP", "Spain", "إسبانيا", 0xFFDC2626L, 0xFFFACC15L, 0, 24, "+15"),
                ClubSeed("FRA", "France", "فرنسا", 0xFF1D4ED8L, 0xFFDC2626L, 0, 22, "+11"),
                ClubSeed("ENG", "England", "إنجلترا", 0xFF1E3A8AL, 0xFFDC2626L, 0, 21, "+10"),
                ClubSeed("MAR", "Morocco", "المغرب", 0xFFDC2626L, 0xFF15803DL, 0, 21, "+12"),
                ClubSeed("BRA", "Brazil", "البرازيل", 0xFFFACC15L, 0xFF15803DL, 0, 19, "+7"),
                ClubSeed("POR", "Portugal", "البرتغال", 0xFFDC2626L, 0xFF15803DL, 0, 19, "+9"),
                ClubSeed("GER", "Germany", "ألمانيا", 0xFF111111L, 0xFFFACC15L, 0, 18, "+8"),
                ClubSeed("EGY", "Egypt", "مصر", 0xFFDC2626L, 0xFF111111L, 0, 17, "+6"),
                ClubSeed("KSA", "Saudi Arabia", "السعودية", 0xFF15803DL, 0xFFFFFFFFL, 0, 16, "+4")
            ),
            scorers = listOf(
                TopScorerItem(1, "Lionel Messi", "ليونيل ميسي", "ARG", 6, 6),
                TopScorerItem(2, "Cristiano Ronaldo", "كريستيانو رونالدو", "POR", 5, 6),
                TopScorerItem(3, "Brahim Díaz", "إبراهيم دياز", "MAR", 5, 6),
                TopScorerItem(4, "Mohamed Salah", "محمد صلاح", "EGY", 4, 6)
            ),
            headlines = defaultHeadlines
        )

        return map
    }

    private data class ClubSeed(
        val code: String,
        val nameEn: String,
        val nameAr: String,
        val primaryColor: Long,
        val secondaryColor: Long,
        val scores365Id: Int,
        val points: Int,
        val goalDiff: String
    )

    private fun buildCustomLeagueBaseline(
        leagueId: String,
        clubs: List<ClubSeed>,
        scorers: List<TopScorerItem>,
        headlines: List<FootballHeadlineItem>
    ): LeagueDashboardData {
        val standings = clubs.mapIndexed { idx, c ->
            val form = when {
                idx == 0 -> listOf('W', 'W', 'W', 'D', 'W')
                idx <= 2 -> listOf('W', 'W', 'D', 'W', 'W')
                idx <= 5 -> listOf('W', 'D', 'W', 'L', 'W')
                idx <= 7 -> listOf('D', 'W', 'L', 'D', 'W')
                else -> listOf('L', 'D', 'W', 'L', 'D')
            }
            LeagueStandingRow(
                rank = idx + 1,
                teamCode = c.code,
                teamNameEn = c.nameEn,
                teamNameAr = c.nameAr,
                primaryColor = c.primaryColor,
                secondaryColor = c.secondaryColor,
                crestStyle = if (idx % 2 == 0) "shield" else "circle",
                logoUrl = logo(c.scores365Id),
                played = 11,
                goalDiff = c.goalDiff,
                points = c.points,
                form5 = form,
                isTopQualification = idx < 4
            )
        }

        val c0 = clubs[0]
        val c1 = clubs[1]
        val c2 = clubs[2]
        val c3 = clubs[3]
        val c4 = clubs[4]
        val c5 = clubs[5]
        val c6 = clubs.getOrElse(6) { c0 }
        val c7 = clubs.getOrElse(7) { c1 }
        val c8 = clubs.getOrElse(8) { c2 }
        val c9 = clubs.getOrElse(9) { c3 }

        val featured = FootballMatchItem(
            id = "${leagueId}_f1",
            homeCode = c0.code, homeNameEn = c0.nameEn.uppercase(), homeNameAr = c0.nameAr,
            homePrimaryColor = c0.primaryColor, homeSecondaryColor = c0.secondaryColor, homeCrestStyle = "shield", homeLogoUrl = logo(c0.scores365Id),
            awayCode = c1.code, awayNameEn = c1.nameEn.uppercase(), awayNameAr = c1.nameAr,
            awayPrimaryColor = c1.primaryColor, awaySecondaryColor = c1.secondaryColor, awayCrestStyle = "circle", awayLogoUrl = logo(c1.scores365Id),
            homeScore = 2, awayScore = 1, statusType = MatchStatusType.LIVE, statusBadgeText = "65'", progressRatio = 65f / 90f,
            goalEvents = listOf(
                FootballGoalEvent(scorers.getOrNull(0)?.playerNameEn ?: "Forward", scorers.getOrNull(0)?.playerNameAr ?: "مهاجم", "21'", true),
                FootballGoalEvent(scorers.getOrNull(1)?.playerNameEn ?: "Striker", scorers.getOrNull(1)?.playerNameAr ?: "هداف", "44'", false),
                FootballGoalEvent(scorers.getOrNull(2)?.playerNameEn ?: "Midfielder", scorers.getOrNull(2)?.playerNameAr ?: "وسط", "58'", true)
            )
        )

        val fixtures = listOf(
            featured,
            FootballMatchItem("${leagueId}_f2", c2.code, c2.nameEn.uppercase(), c2.nameAr, c2.primaryColor, c2.secondaryColor, "shield", logo(c2.scores365Id), c3.code, c3.nameEn.uppercase(), c3.nameAr, c3.primaryColor, c3.secondaryColor, "circle", logo(c3.scores365Id), 1, 0, MatchStatusType.FINISHED, "FT", 1f),
            FootballMatchItem("${leagueId}_f3", c4.code, c4.nameEn.uppercase(), c4.nameAr, c4.primaryColor, c4.secondaryColor, "circle", logo(c4.scores365Id), c5.code, c5.nameEn.uppercase(), c5.nameAr, c5.primaryColor, c5.secondaryColor, "shield", logo(c5.scores365Id), null, null, MatchStatusType.UPCOMING, "18:00", 0f),
            FootballMatchItem("${leagueId}_f4", c6.code, c6.nameEn.uppercase(), c6.nameAr, c6.primaryColor, c6.secondaryColor, "shield", logo(c6.scores365Id), c7.code, c7.nameEn.uppercase(), c7.nameAr, c7.primaryColor, c7.secondaryColor, "circle", logo(c7.scores365Id), null, null, MatchStatusType.UPCOMING, "20:00", 0f),
            FootballMatchItem("${leagueId}_f5", c8.code, c8.nameEn.uppercase(), c8.nameAr, c8.primaryColor, c8.secondaryColor, "circle", logo(c8.scores365Id), c9.code, c9.nameEn.uppercase(), c9.nameAr, c9.primaryColor, c9.secondaryColor, "shield", logo(c9.scores365Id), null, null, MatchStatusType.UPCOMING, "21:45", 0f)
        )

        return enrichLeagueWithProbabilities(
            LeagueDashboardData(
                leagueId = leagueId,
                featuredMatch = featured,
                fixtures = fixtures,
                topScorers = scorers,
                standings = standings,
                headlines = headlines
            )
        )
    }
}
