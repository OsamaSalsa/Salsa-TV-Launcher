package com.example.ui.games

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

const val AURA_STORE_DB_URL = "https://auratv.freedev.app/index.html"
const val AURA_STORE_URL = AURA_STORE_DB_URL
const val AURA_STORE_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
private const val CACHE_FILE_NAME = "aura_store_db_cache.txt"
private const val FALLBACK_ASSET_NAME = "aura_store_fallback.txt"

data class AuraStoreAppItem(
    val id: String,
    val title: String,
    val logoUrl: String,
    val description: String,
    val downloadUrl: String,
    val categoryName: String
)

data class AuraStoreCategory(
    val name: String,
    val apps: List<AuraStoreAppItem>
)

object AuraStoreRepository {

    /**
     * Parses the raw line-based database format from https://auratv.freedev.app/index.html
     * Format:
     * # Category Name
     * App Title
     * Logo: https://...
     * Short Description / Tag
     * https://download.url/...
     */
    fun parseRawDataset(rawText: String): List<AuraStoreCategory> {
        val lines = rawText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val categories = mutableListOf<Pair<String, MutableList<AuraStoreAppItem>>>()
        var currentCategoryName: String? = null
        var currentApps: MutableList<AuraStoreAppItem>? = null

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.startsWith("#")) {
                val catName = line.removePrefix("#").trim()
                if (catName.isNotEmpty()) {
                    currentCategoryName = catName
                    currentApps = mutableListOf()
                    categories.add(catName to currentApps)
                }
                i++
            } else if (currentCategoryName != null && currentApps != null) {
                val title = line
                var logo = ""
                var desc = ""
                var url = ""
                i++

                if (i < lines.size && lines[i].lowercase().startsWith("logo")) {
                    logo = lines[i].replace(Regex("""^logo\s*:\s*""", RegexOption.IGNORE_CASE), "").trim()
                    i++
                }

                if (i < lines.size &&
                    !lines[i].startsWith("http://", ignoreCase = true) &&
                    !lines[i].startsWith("https://", ignoreCase = true) &&
                    !lines[i].startsWith("#")
                ) {
                    desc = lines[i].trim()
                    i++
                }

                if (i < lines.size &&
                    (lines[i].startsWith("http://", ignoreCase = true) ||
                            lines[i].startsWith("https://", ignoreCase = true))
                ) {
                    url = lines[i].trim()
                    i++
                }

                if (url.isNotEmpty()) {
                    val itemId = "${currentCategoryName}_${title}_${currentApps.size}"
                    currentApps.add(
                        AuraStoreAppItem(
                            id = itemId,
                            title = title,
                            logoUrl = logo,
                            description = desc.ifBlank { currentCategoryName },
                            downloadUrl = url,
                            categoryName = currentCategoryName
                        )
                    )
                }
            } else {
                i++
            }
        }

        return categories
            .filter { it.second.isNotEmpty() }
            .map { (name, apps) -> AuraStoreCategory(name = name, apps = apps.toList()) }
    }

    /**
     * Extracts `const rawData = `...`;` from the index.html document,
     * or returns the body directly if it is already raw category text starting with `#`.
     */
    fun extractRawDataFromHtml(html: String): String? {
        val trimmed = html.trim()
        if (trimmed.isEmpty()) return null
        if (trimmed.startsWith("#")) return trimmed

        val rawDataRegex = Regex(
            """(?:const|let|var)\s+rawData\s*=\s*`([\s\S]*?)`\s*;""",
            RegexOption.IGNORE_CASE
        )
        val match = rawDataRegex.find(html)
        if (match != null) {
            val extracted = match.groupValues[1].trim()
            if (extracted.isNotEmpty()) return extracted
        }
        return null
    }

    /**
     * Solves InfinityFree's slowAES cookie challenge (`__test=<hex>`) when fetching
     * https://auratv.freedev.app/index.html directly via HttpURLConnection.
     */
    fun solveAesChallengeCookie(challengeHtml: String): Pair<String, String>? {
        if (!challengeHtml.contains("slowAES.decrypt", ignoreCase = true)) return null
        return try {
            val numsMatch = Regex(
                """a\s*=\s*toNumbers\(["']([0-9a-fA-F]{32})["']\)\s*,\s*b\s*=\s*toNumbers\(["']([0-9a-fA-F]{32})["']\)\s*,\s*c\s*=\s*toNumbers\(["']([0-9a-fA-F]{32})["']\)"""
            ).find(challengeHtml) ?: return null

            val keyBytes = hexToBytes(numsMatch.groupValues[1])
            val ivBytes = hexToBytes(numsMatch.groupValues[2])
            val cipherBytes = hexToBytes(numsMatch.groupValues[3])

            val cipher = Cipher.getInstance("AES/CBC/NoPadding")
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(keyBytes, "AES"),
                IvParameterSpec(ivBytes)
            )
            val decrypted = cipher.doFinal(cipherBytes)
            val cookieValue = bytesToHex(decrypted)

            val nextUrlMatch = Regex("""location\.href\s*=\s*["']([^"']+)["']""")
                .find(challengeHtml)
            val targetUrl = nextUrlMatch?.groupValues?.get(1)?.trim()
                ?.takeIf { it.startsWith("http", ignoreCase = true) }
                ?: "$AURA_STORE_DB_URL?i=1"

            "__test=$cookieValue" to targetUrl
        } catch (e: Exception) {
            android.util.Log.w("AuraStoreRepository", "Failed to solve AES challenge", e)
            null
        }
    }

    private fun hexToBytes(hex: String): ByteArray {
        val clean = hex.trim()
        val len = clean.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(clean[i], 16) shl 4) +
                    Character.digit(clean[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val sb = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val v = b.toInt() and 0xFF
            sb.append(hexChars[v ushr 4])
            sb.append(hexChars[v and 0x0F])
        }
        return sb.toString()
    }

    /**
     * Loads the local cached database or the bundled asset fallback immediately (0ms UI start).
     */
    fun loadCachedOrBundledDatabase(context: Context): List<AuraStoreCategory> {
        return try {
            val cacheFile = File(context.filesDir, CACHE_FILE_NAME)
            if (cacheFile.exists() && cacheFile.length() > 128L) {
                val cachedText = cacheFile.readText(Charsets.UTF_8)
                val parsed = parseRawDataset(cachedText)
                if (parsed.isNotEmpty()) {
                    return parsed
                }
            }
            loadBundledFallback(context)
        } catch (e: Exception) {
            android.util.Log.w("AuraStoreRepository", "Error loading cached database, falling back to asset", e)
            loadBundledFallback(context)
        }
    }

    private fun loadBundledFallback(context: Context): List<AuraStoreCategory> {
        return try {
            val rawText = context.assets.open(FALLBACK_ASSET_NAME)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
            parseRawDataset(rawText)
        } catch (e: Exception) {
            android.util.Log.w("AuraStoreRepository", "Failed to read bundled fallback asset", e)
            emptyList()
        }
    }

    /**
     * Connects to https://auratv.freedev.app/index.html, extracts the raw database of categories,
     * app names, logos, descriptions, and download URLs, caches it locally, and returns the parsed list.
     */
    suspend fun fetchRemoteDatabase(context: Context): Result<List<AuraStoreCategory>> =
        withContext(Dispatchers.IO) {
            try {
                val firstHtml = executeGet(AURA_STORE_DB_URL, cookie = null)
                val finalHtml = if (firstHtml.contains("slowAES.decrypt", ignoreCase = true)) {
                    val solved = solveAesChallengeCookie(firstHtml)
                    if (solved != null) {
                        executeGet(solved.second, cookie = solved.first)
                    } else {
                        firstHtml
                    }
                } else {
                    firstHtml
                }

                val rawData = extractRawDataFromHtml(finalHtml)
                    ?: throw IllegalStateException("Could not extract rawData from remote database")

                val parsedCategories = parseRawDataset(rawData)
                if (parsedCategories.isEmpty()) {
                    throw IllegalStateException("Remote database parsed 0 categories")
                }

                // Persist to local cache for instant load on next launch
                try {
                    File(context.filesDir, CACHE_FILE_NAME).writeText(rawData, Charsets.UTF_8)
                } catch (_: Exception) {}

                Result.success(parsedCategories)
            } catch (e: Exception) {
                android.util.Log.w("AuraStoreRepository", "Remote database sync failed", e)
                Result.failure(e)
            }
        }

    private fun executeGet(urlStr: String, cookie: String?): String {
        var currentUrl = urlStr
        var redirects = 0
        while (redirects < 6) {
            val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                connectTimeout = 15000
                readTimeout = 20000
                setRequestProperty("User-Agent", AURA_STORE_USER_AGENT)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                setRequestProperty("Referer", AURA_STORE_URL)
                if (!cookie.isNullOrBlank()) {
                    setRequestProperty("Cookie", cookie)
                }
            }
            val code = conn.responseCode
            if (code in 300..399) {
                val loc = conn.getHeaderField("Location")
                conn.disconnect()
                if (loc.isNullOrBlank()) break
                currentUrl = if (loc.startsWith("http", ignoreCase = true)) {
                    loc
                } else {
                    URL(URL(currentUrl), loc).toString()
                }
                redirects++
            } else {
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                conn.disconnect()
                return body
            }
        }
        return ""
    }
}
