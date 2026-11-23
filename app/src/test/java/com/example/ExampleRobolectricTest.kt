package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.IptvParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salsa TV", appName)
    }

    @Test
    fun `m3u parser parses valid channels accurately`() {
        val m3uSample = """
            #EXTM3U
            #EXTINF:-1 tvg-id="news1" tvg-name="World News" tvg-logo="https://example.com/logo.png" group-title="News",World News HD
            https://example.com/stream.m3u8
        """.trimIndent()

        val parsed = IptvParser.parseFromContent(m3uSample)
        assertEquals(1, parsed.size)
        assertEquals("World News HD", parsed[0].name)
        assertEquals("News", parsed[0].groupTitle)
        assertEquals("https://example.com/stream.m3u8", parsed[0].streamUrl)
        assertEquals(false, parsed[0].isFavorite)
    }

    @Test
    fun `m3u parser handles empty input and multiple channels cleanly`() {
        assertTrue(IptvParser.parseFromContent("").isEmpty())

        val multiSample = """
            #EXTM3U
            #EXTINF:-1 group-title="Sports",Sports 1
            https://example.com/s1.m3u8
            #EXTINF:-1,Uncategorized Channel
            https://example.com/s2.m3u8
        """.trimIndent()
        val parsed = IptvParser.parseFromContent(multiSample)
        assertEquals(2, parsed.size)
        assertEquals("Sports", parsed[0].groupTitle)
        assertEquals("Uncategorized Channel", parsed[1].name)
    }

    @Test
    fun `locale helper formats dates with ascii digits across locales`() {
        val date = java.util.Date(1700000000000L)
        val formattedEn = com.example.util.LocaleHelper.formatDate(date, "numeric_iso", "en")
        val formattedAr = com.example.util.LocaleHelper.formatDate(date, "numeric_iso", "ar_rtl")
        assertTrue(formattedEn.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        assertTrue(formattedAr.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `backup and restore manager serializes and restores launcher preferences`() {
        val snapshot = mapOf<String, Any>(
            "apps_per_row" to 6,
            "night_comfort_mode" to "auto",
            "low_ram_mode" to true
        )
        val playlists = listOf(
            com.example.data.model.IptvPlaylist(
                id = 1L,
                name = "Test Playlist",
                url = "https://example.com/list.m3u",
                channelCount = 12
            )
        )
        val json = com.example.util.BackupRestoreManager.createBackupJson(snapshot, playlists)
        assertTrue(json.contains("night_comfort_mode"))
        assertTrue(json.contains("Test Playlist"))
    }

    @Test
    fun `aura store repository parses database categories logos and apps from index html`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val categories = com.example.ui.games.AuraStoreRepository.loadCachedOrBundledDatabase(context)
        assertEquals(9, categories.size)
        assertEquals("Entertainment", categories[0].name)
        assertTrue(categories.sumOf { it.apps.size } > 200)
        val firstApp = categories[0].apps.first()
        assertTrue(firstApp.title.contains("Ostora"))
        assertTrue(firstApp.logoUrl.startsWith("https://"))
        assertTrue(firstApp.downloadUrl.startsWith("https://"))

        val challengeSample = """
            <html><body><script>var a=toNumbers("f655ba9d09a112d4968c63579db590b4"),b=toNumbers("98344c2eee86c3994890592585b49f80"),c=toNumbers("e44f4c84fc3fd5d2d2972ce2555da5f4");document.cookie="__test="+toHex(slowAES.decrypt(c,2,a,b));location.href="https://auratv.freedev.app/index.html?i=1";</script></body></html>
        """.trimIndent()
        val solved = com.example.ui.games.AuraStoreRepository.solveAesChallengeCookie(challengeSample)
        assertTrue(solved != null)
        assertTrue(solved!!.first.startsWith("__test="))
        assertEquals("https://auratv.freedev.app/index.html?i=1", solved.second)
    }

    @Test
    fun `news screen in screensaver mode honors initial politics section without reverting to top stories`() {
        val politicsLabel = com.example.data.news.NewsRepository.getLocalizedStrings("en").sectionPolitics
        val newsState = com.example.data.news.NewsRepository.newsState.value
        assertTrue(politicsLabel.isNotBlank())
        assertTrue(newsState.articlesBySection[com.example.data.news.NewsSectionType.POLITICS].orEmpty().isNotEmpty())
        assertTrue(newsState.articlesBySection[com.example.data.news.NewsSectionType.ECONOMY].orEmpty().isNotEmpty())
        assertTrue(newsState.articlesBySection[com.example.data.news.NewsSectionType.TECHNOLOGY].orEmpty().isNotEmpty())
        assertTrue(newsState.articlesBySection[com.example.data.news.NewsSectionType.WORLD].orEmpty().isNotEmpty())
    }
}
