package com.example.ui.common

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import com.example.R
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.Locale

sealed class WebPortalDownloadState {
    data object Idle : WebPortalDownloadState()
    data class Downloading(
        val fileName: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val percent: Int
    ) : WebPortalDownloadState()

    data class Completed(
        val fileName: String,
        val fileSize: Long,
        val mimeType: String,
        val isApk: Boolean,
        val localFile: File? = null,
        val contentUri: Uri? = null
    ) : WebPortalDownloadState()

    data class Error(val message: String) : WebPortalDownloadState()
}

/**
 * Shared download engine for Custom Web Portals.
 * Supports downloading both Android packages (.apk, .xapk, .apks) and general files
 * (.zip, .rar, .pdf, media, documents, blobs, data URIs) without requiring any
 * persistent downloads bar in the web portal UI.
 */
object WebPortalDownloadManager {

    private const val TAG = "WebPortalDownload"

    const val ANDROID_CHROME_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 13; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"

    private val DIRECT_ARCHIVE_OR_PACKAGE_EXTENSIONS = listOf(
        ".xapk", ".apks", ".aab", ".zip", ".rar", ".7z", ".tar.gz", ".tgz", ".torrent"
    )

    fun isYoutubeHost(url: String): Boolean {
        val host = try {
            url.toUri().host?.lowercase(Locale.US) ?: ""
        } catch (_: Exception) {
            ""
        }
        return host == "youtube.com" ||
                host.endsWith(".youtube.com") ||
                host == "youtu.be" ||
                host.endsWith(".youtu.be")
    }

    /**
     * Returns true when [rawUrl] points directly to an APK, package archive, or
     * cloud file share link that should be intercepted immediately by the native downloader.
     * General media/documents remain browsable and are captured by WebView's DownloadListener
     * whenever the site initiates a download.
     */
    fun isDirectDownloadableUrl(rawUrl: String): Boolean {
        if (DownloadUrlResolver.shouldInterceptAsDownload(rawUrl)) return true
        val unwrapped = DownloadUrlResolver.unwrapIntentUrl(rawUrl)
        val path = try {
            unwrapped.toUri().path?.lowercase(Locale.US) ?: unwrapped.substringBefore("?").lowercase(Locale.US)
        } catch (_: Exception) {
            unwrapped.substringBefore("?").lowercase(Locale.US)
        }
        return DIRECT_ARCHIVE_OR_PACKAGE_EXTENSIONS.any { ext -> path.endsWith(ext) }
    }

    fun isApkFile(fileName: String, mimeType: String?): Boolean {
        val cleanMime = mimeType?.substringBefore(";")?.trim()?.lowercase(Locale.US) ?: ""
        return fileName.endsWith(".apk", ignoreCase = true) ||
                cleanMime == "application/vnd.android.package-archive" ||
                cleanMime == "application/x-apk"
    }

    fun sanitizeGeneralFileName(name: String, fallbackExt: String? = null): String {
        val clean = name
            .replace(Regex("""[\\/:*?"<>|\r\n\t]"""), "_")
            .trim()
            .trim('.')
        if (clean.isEmpty()) {
            val ext = fallbackExt?.trimStart('.')?.takeIf { it.isNotBlank() } ?: "bin"
            return "download_${System.currentTimeMillis()}.$ext"
        }
        return clean
    }

    /**
     * Extracts an accurate filename while preserving the real file extension for both
     * APKs and general files (PDF, ZIP, MP4, MP3, M3U, images, documents, etc.).
     */
    fun extractGeneralFileName(
        url: String,
        contentDisposition: String?,
        mimeType: String?,
        discoveredName: String? = null
    ): String {
        val cleanMime = mimeType?.substringBefore(";")?.trim()?.lowercase(Locale.US)
        val mimeExt = when (cleanMime) {
            "application/vnd.android.package-archive", "application/x-apk" -> "apk"
            null, "", "application/octet-stream", "binary/octet-stream" -> null
            else -> MimeTypeMap.getSingleton().getExtensionFromMimeType(cleanMime)
        }

        // 1. Content-Disposition header (RFC 5987 filename* first, then filename=)
        if (!contentDisposition.isNullOrBlank()) {
            val utf8Match = Regex("""filename\*\s*=\s*(?:UTF-8|utf-8)''([^;]+)""", RegexOption.IGNORE_CASE)
                .find(contentDisposition)
            val stdMatch = Regex("""filename\s*=\s*["']?([^"';]+)["']?""", RegexOption.IGNORE_CASE)
                .find(contentDisposition)
            val rawHeaderName = utf8Match?.groupValues?.get(1) ?: stdMatch?.groupValues?.get(1)
            if (!rawHeaderName.isNullOrBlank()) {
                val decoded = try {
                    URLDecoder.decode(rawHeaderName.trim(), "UTF-8")
                } catch (_: Exception) {
                    Uri.decode(rawHeaderName.trim())
                }
                val sanitized = sanitizeGeneralFileName(decoded, mimeExt)
                return ensureExtension(sanitized, mimeExt, cleanMime)
            }
        }

        // 2. Discovered name from HTML intermediate page (e.g. Google Drive warning page)
        if (!discoveredName.isNullOrBlank()) {
            val sanitized = sanitizeGeneralFileName(Uri.decode(discoveredName.trim()), mimeExt)
            return ensureExtension(sanitized, mimeExt, cleanMime)
        }

        // 3. Direct APK pattern in URL
        val apkMatcher = Regex("""/([a-zA-Z0-9_\-\.]+?)(?:-apk|\.apk)(?:\.[0-9]+)?(?:/|\?|#|$)""", RegexOption.IGNORE_CASE)
            .find(url)
        if (apkMatcher != null) {
            val baseName = Uri.decode(apkMatcher.groupValues[1].trim())
            if (baseName.isNotEmpty()) {
                return sanitizeGeneralFileName("$baseName.apk", "apk")
            }
        }

        // 4. Google Drive fallback name
        val driveId = DownloadUrlResolver.extractGoogleDriveFileId(url)
        if (!driveId.isNullOrBlank()) {
            val ext = mimeExt ?: "bin"
            return sanitizeGeneralFileName("gdrive_${driveId.take(10)}.$ext", ext)
        }

        // 5. Standard Android URLUtil guess
        val guessed = try {
            URLUtil.guessFileName(url, contentDisposition, cleanMime)
        } catch (_: Exception) {
            "download_${System.currentTimeMillis()}"
        }
        val sanitized = sanitizeGeneralFileName(guessed, mimeExt)
        return ensureExtension(sanitized, mimeExt, cleanMime)
    }

    private fun ensureExtension(fileName: String, mimeExt: String?, cleanMime: String?): String {
        if (cleanMime == "application/vnd.android.package-archive" || cleanMime == "application/x-apk") {
            return when {
                fileName.endsWith(".apk", ignoreCase = true) -> fileName
                fileName.endsWith(".bin", ignoreCase = true) -> fileName.dropLast(4) + ".apk"
                else -> "$fileName.apk"
            }
        }
        val hasExtension = fileName.contains('.') && fileName.substringAfterLast('.').length in 1..6
        if (!hasExtension && !mimeExt.isNullOrBlank()) {
            return "$fileName.$mimeExt"
        }
        if (fileName.endsWith(".bin", ignoreCase = true) && !mimeExt.isNullOrBlank() && mimeExt != "bin") {
            return fileName.dropLast(4) + ".$mimeExt"
        }
        return fileName
    }

    fun resolveMimeType(fileName: String, serverMimeType: String?): String {
        val cleanServer = serverMimeType?.substringBefore(";")?.trim()?.lowercase(Locale.US)
        if (isApkFile(fileName, cleanServer)) {
            return "application/vnd.android.package-archive"
        }
        if (!cleanServer.isNullOrBlank() &&
            cleanServer != "application/octet-stream" &&
            cleanServer != "binary/octet-stream"
        ) {
            return cleanServer
        }
        val ext = fileName.substringAfterLast('.', "").lowercase(Locale.US)
        if (ext.isNotEmpty()) {
            val mapped = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            if (!mapped.isNullOrBlank()) return mapped
        }
        return "application/octet-stream"
    }

    /**
     * Downloads a file or APK from [rawUrl] (supporting http/https redirects, cookies,
     * HTML confirmation pages like Google Drive / MediaFire, and data: URIs).
     */
    suspend fun downloadUrl(
        context: Context,
        rawUrl: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        referer: String?,
        onStateChanged: (WebPortalDownloadState) -> Unit,
        onFallbackToWebView: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (rawUrl.startsWith("data:", ignoreCase = true)) {
            saveDataUri(context, rawUrl, contentDisposition, mimeType, onStateChanged)
            return@withContext
        }

        var currentUrl = DownloadUrlResolver.normalizeInitialDownloadUrl(rawUrl)
        var currentReferer = referer?.takeIf { it.isNotBlank() } ?: currentUrl
        var discoveredHtmlFileName: String? = null
        val sessionCookies = linkedMapOf<String, String>()
        val baseUserAgent = userAgent?.takeIf { it.isNotBlank() } ?: ANDROID_CHROME_USER_AGENT

        withContext(Dispatchers.Main) {
            onStateChanged(
                WebPortalDownloadState.Downloading(
                    fileName = context.getString(R.string.mini_downloader_connecting),
                    downloadedBytes = 0L,
                    totalBytes = -1L,
                    percent = -1
                )
            )
        }

        var activeConn: HttpURLConnection? = null
        try {
            var redirects = 0
            val maxRedirects = 14

            while (redirects < maxRedirects) {
                currentCoroutineContext().ensureActive()
                val urlObj = URL(currentUrl)
                val effectiveUa = DownloadUrlResolver.resolveUserAgentForUrl(currentUrl, baseUserAgent)
                val conn = (urlObj.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 20_000
                    readTimeout = 30_000
                    setRequestProperty("User-Agent", effectiveUa)
                    setRequestProperty(
                        "Accept",
                        "text/html,application/xhtml+xml,application/xml;q=0.9,application/vnd.android.package-archive,application/octet-stream,*/*;q=0.8"
                    )
                    setRequestProperty("Accept-Language", "en-US,en;q=0.9,ar;q=0.8")
                    setRequestProperty("Referer", currentReferer)
                    setRequestProperty("Connection", "keep-alive")

                    val webViewCookies = try {
                        CookieManager.getInstance().getCookie(currentUrl)
                    } catch (_: Exception) {
                        null
                    }
                    val mergedCookies = buildString {
                        if (!webViewCookies.isNullOrBlank()) {
                            append(webViewCookies.trim().trimEnd(';'))
                        }
                        if (sessionCookies.isNotEmpty()) {
                            if (isNotEmpty()) append("; ")
                            append(sessionCookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
                        }
                    }
                    if (mergedCookies.isNotBlank()) {
                        setRequestProperty("Cookie", mergedCookies)
                    }
                }

                val code = conn.responseCode

                conn.headerFields["Set-Cookie"]?.forEach { cookieHeader ->
                    try {
                        CookieManager.getInstance().setCookie(currentUrl, cookieHeader)
                    } catch (_: Exception) {}
                    val pair = cookieHeader.substringBefore(";").trim()
                    val k = pair.substringBefore("=", "").trim()
                    val v = pair.substringAfter("=", "").trim()
                    if (k.isNotEmpty()) {
                        sessionCookies[k] = v
                    }
                }

                if (code in 300..399) {
                    val newLoc = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (newLoc.isNullOrBlank()) break
                    currentReferer = currentUrl
                    val resolvedLoc = if (newLoc.startsWith("http://", ignoreCase = true) ||
                        newLoc.startsWith("https://", ignoreCase = true)
                    ) {
                        newLoc
                    } else {
                        URL(urlObj, newLoc).toString()
                    }
                    currentUrl = DownloadUrlResolver.unwrapIntentUrl(resolvedLoc)
                    redirects++
                } else if (code in 200..299) {
                    val serverContentType = conn.contentType
                    val finalDisposition = conn.getHeaderField("Content-Disposition")
                    val isExplicitAttachment = finalDisposition?.contains("attachment", ignoreCase = true) == true
                    val isHtml = serverContentType?.startsWith("text/html", ignoreCase = true) == true

                    if (isHtml && !isExplicitAttachment && !isApkFile(currentUrl, serverContentType)) {
                        val htmlBody = try {
                            conn.inputStream.bufferedReader().use { it.readText() }
                        } catch (_: Exception) {
                            ""
                        }
                        conn.disconnect()

                        val htmlResult = DownloadUrlResolver.parseHtmlDownloadPage(currentUrl, htmlBody)
                        if (!htmlResult.discoveredFileName.isNullOrBlank()) {
                            discoveredHtmlFileName = htmlResult.discoveredFileName
                        }
                        val nextUrl = htmlResult.nextUrl
                        if (!nextUrl.isNullOrBlank() && nextUrl != currentUrl) {
                            currentReferer = currentUrl
                            currentUrl = nextUrl
                            redirects++
                            continue
                        } else {
                            withContext(Dispatchers.Main) {
                                onStateChanged(WebPortalDownloadState.Idle)
                                onFallbackToWebView(currentUrl)
                            }
                            return@withContext
                        }
                    } else {
                        activeConn = conn
                        break
                    }
                } else {
                    conn.disconnect()
                    withContext(Dispatchers.Main) {
                        onStateChanged(WebPortalDownloadState.Error("HTTP $code"))
                    }
                    return@withContext
                }
            }

            val conn = activeConn ?: throw IllegalStateException("Could not resolve download stream")
            val serverContentType = conn.contentType ?: mimeType
            val finalDisposition = conn.getHeaderField("Content-Disposition") ?: contentDisposition
            val fileName = extractGeneralFileName(
                url = currentUrl,
                contentDisposition = finalDisposition,
                mimeType = serverContentType,
                discoveredName = discoveredHtmlFileName
            )
            val resolvedMime = resolveMimeType(fileName, serverContentType)
            val isApk = isApkFile(fileName, resolvedMime)
            val contentLength = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                conn.contentLengthLong
            } else {
                conn.contentLength.toLong()
            }

            withContext(Dispatchers.Main) {
                onStateChanged(
                    WebPortalDownloadState.Downloading(
                        fileName = fileName,
                        downloadedBytes = 0L,
                        totalBytes = contentLength,
                        percent = if (contentLength > 0) 0 else -1
                    )
                )
            }

            val completed = conn.inputStream.use { input ->
                streamToStorage(
                    context = context,
                    inputStream = input,
                    fileName = fileName,
                    mimeType = resolvedMime,
                    isApk = isApk,
                    totalBytes = contentLength,
                    sourceUrl = currentUrl,
                    onProgress = { downloaded, percent ->
                        onStateChanged(
                            WebPortalDownloadState.Downloading(
                                fileName = fileName,
                                downloadedBytes = downloaded,
                                totalBytes = contentLength,
                                percent = percent
                            )
                        )
                    }
                )
            }
            conn.disconnect()
            activeConn = null

            withContext(Dispatchers.Main) {
                onStateChanged(completed)
                if (completed.isApk) {
                    openOrInstallCompleted(context, completed)
                }
            }
        } catch (ce: CancellationException) {
            try { activeConn?.disconnect() } catch (_: Exception) {}
            throw ce
        } catch (e: Exception) {
            try { activeConn?.disconnect() } catch (_: Exception) {}
            Log.w(TAG, "Download failed for $rawUrl", e)
            withContext(Dispatchers.Main) {
                onStateChanged(
                    WebPortalDownloadState.Error(
                        e.localizedMessage ?: context.getString(R.string.mini_downloader_download_error)
                    )
                )
            }
        }
    }

    /**
     * Handles `data:` or JS-extracted `blob:` base64 payloads.
     */
    suspend fun saveDataUri(
        context: Context,
        dataUri: String,
        contentDisposition: String?,
        mimeType: String?,
        onStateChanged: (WebPortalDownloadState) -> Unit,
        suggestedFileName: String? = null
    ) = withContext(Dispatchers.IO) {
        try {
            val commaIdx = dataUri.indexOf(',')
            if (commaIdx <= 0) {
                withContext(Dispatchers.Main) {
                    onStateChanged(WebPortalDownloadState.Error("Invalid data payload"))
                }
                return@withContext
            }
            val header = dataUri.substring(5, commaIdx)
            val payload = dataUri.substring(commaIdx + 1)
            val headerMime = header.substringBefore(";").takeIf { it.isNotBlank() } ?: mimeType
            val isBase64 = header.contains(";base64", ignoreCase = true)
            val bytes = if (isBase64) {
                Base64.decode(payload, Base64.DEFAULT)
            } else {
                URLDecoder.decode(payload, "UTF-8").toByteArray(Charsets.UTF_8)
            }

            val fileName = extractGeneralFileName(
                url = "https://download.local/${suggestedFileName ?: ""}",
                contentDisposition = contentDisposition,
                mimeType = headerMime,
                discoveredName = suggestedFileName
            )
            val resolvedMime = resolveMimeType(fileName, headerMime)
            val isApk = isApkFile(fileName, resolvedMime)

            withContext(Dispatchers.Main) {
                onStateChanged(
                    WebPortalDownloadState.Downloading(
                        fileName = fileName,
                        downloadedBytes = 0L,
                        totalBytes = bytes.size.toLong(),
                        percent = 0
                    )
                )
            }

            val completed = bytes.inputStream().use { input ->
                streamToStorage(
                    context = context,
                    inputStream = input,
                    fileName = fileName,
                    mimeType = resolvedMime,
                    isApk = isApk,
                    totalBytes = bytes.size.toLong(),
                    sourceUrl = null,
                    onProgress = { downloaded, percent ->
                        onStateChanged(
                            WebPortalDownloadState.Downloading(
                                fileName = fileName,
                                downloadedBytes = downloaded,
                                totalBytes = bytes.size.toLong(),
                                percent = percent
                            )
                        )
                    }
                )
            }

            withContext(Dispatchers.Main) {
                onStateChanged(completed)
                if (completed.isApk) {
                    openOrInstallCompleted(context, completed)
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "Failed saving data/blob download", e)
            withContext(Dispatchers.Main) {
                onStateChanged(
                    WebPortalDownloadState.Error(
                        e.localizedMessage ?: context.getString(R.string.mini_downloader_download_error)
                    )
                )
            }
        }
    }

    private suspend fun streamToStorage(
        context: Context,
        inputStream: InputStream,
        fileName: String,
        mimeType: String,
        isApk: Boolean,
        totalBytes: Long,
        sourceUrl: String?,
        onProgress: suspend (downloadedBytes: Long, percent: Int) -> Unit
    ): WebPortalDownloadState.Completed {
        // Non-APK general files on Android 10+ stream directly into MediaStore.Downloads
        // (zero storage permissions needed, visible in device Downloads immediately).
        if (!isApk && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val insertedUri = try {
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            } catch (e: Exception) {
                Log.w(TAG, "MediaStore insert failed, falling back to app Downloads dir", e)
                null
            }

            if (insertedUri != null) {
                try {
                    val written = resolver.openOutputStream(insertedUri)?.use { output ->
                        copyWithProgress(inputStream, output, totalBytes, onProgress)
                    } ?: throw IllegalStateException("Cannot open MediaStore output stream")

                    val doneValues = ContentValues().apply {
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }
                    resolver.update(insertedUri, doneValues, null, null)

                    return WebPortalDownloadState.Completed(
                        fileName = fileName,
                        fileSize = written,
                        mimeType = mimeType,
                        isApk = false,
                        localFile = null,
                        contentUri = insertedUri
                    )
                } catch (t: Throwable) {
                    try {
                        resolver.delete(insertedUri, null, null)
                    } catch (_: Exception) {}
                    throw t
                }
            }
        }

        // APKs (all Android versions) & pre-Android 10 fallback save to File storage
        val targetDir = if (!isApk && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (publicDir != null && (publicDir.exists() || publicDir.mkdirs()) && publicDir.canWrite()) {
                publicDir
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            }
        } else {
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
        }
        if (!targetDir.exists()) targetDir.mkdirs()

        val finalFile = uniqueTargetFile(targetDir, fileName, overwriteExistingApk = isApk)
        val tempFile = File(targetDir, ".${finalFile.name}.part")
        if (tempFile.exists()) tempFile.delete()

        try {
            val written = FileOutputStream(tempFile).use { output ->
                copyWithProgress(inputStream, output, totalBytes, onProgress)
            }
            if (finalFile.exists()) finalFile.delete()
            if (!tempFile.renameTo(finalFile)) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }

            if (isApk && !sourceUrl.isNullOrBlank()) {
                try {
                    val prefs = context.getSharedPreferences("mini_downloader_history", Context.MODE_PRIVATE)
                    prefs.edit { putString("url_${finalFile.name}", sourceUrl) }
                } catch (_: Exception) {}
            }

            return WebPortalDownloadState.Completed(
                fileName = finalFile.name,
                fileSize = finalFile.length().takeIf { it > 0L } ?: written,
                mimeType = mimeType,
                isApk = isApk,
                localFile = finalFile,
                contentUri = null
            )
        } catch (t: Throwable) {
            try {
                if (tempFile.exists()) tempFile.delete()
            } catch (_: Exception) {}
            throw t
        }
    }

    private fun uniqueTargetFile(dir: File, fileName: String, overwriteExistingApk: Boolean): File {
        val candidate = File(dir, fileName)
        if (overwriteExistingApk || !candidate.exists()) return candidate
        val dot = fileName.lastIndexOf('.')
        val base = if (dot > 0) fileName.substring(0, dot) else fileName
        val ext = if (dot > 0) fileName.substring(dot) else ""
        for (i in 1..99) {
            val next = File(dir, "${base}_($i)$ext")
            if (!next.exists()) return next
        }
        return File(dir, "${base}_${System.currentTimeMillis()}$ext")
    }

    private suspend fun copyWithProgress(
        input: InputStream,
        output: OutputStream,
        totalBytes: Long,
        onProgress: suspend (downloadedBytes: Long, percent: Int) -> Unit
    ): Long {
        val buffer = ByteArray(64 * 1024)
        var downloaded = 0L
        var lastEmitTime = 0L
        var lastEmitPercent = -2
        var bytesRead: Int

        while (input.read(buffer).also { bytesRead = it } != -1) {
            currentCoroutineContext().ensureActive()
            output.write(buffer, 0, bytesRead)
            downloaded += bytesRead
            val percent = if (totalBytes > 0L) {
                ((downloaded * 100L) / totalBytes).toInt().coerceIn(0, 100)
            } else {
                -1
            }
            val now = System.currentTimeMillis()
            if (now - lastEmitTime >= 100L || (percent >= 0 && percent != lastEmitPercent)) {
                lastEmitTime = now
                lastEmitPercent = percent
                withContext(Dispatchers.Main) {
                    onProgress(downloaded, percent)
                }
            }
        }
        output.flush()
        return downloaded
    }

    fun openOrInstallCompleted(context: Context, completed: WebPortalDownloadState.Completed) {
        try {
            if (completed.isApk) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.web_portal_allow_unknown_apps),
                        Toast.LENGTH_LONG
                    ).show()
                    try {
                        val settingsIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                            data = "package:${context.packageName}".toUri()
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(settingsIntent)
                    } catch (e: Exception) {
                        Log.w(TAG, "Unable to open unknown sources settings", e)
                    }
                    return
                }
                val apkFile = completed.localFile ?: return
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(installIntent)
            } else {
                val targetUri = completed.contentUri ?: completed.localFile?.let { file ->
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                } ?: return

                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(targetUri, completed.mimeType)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(viewIntent)
            }
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                context,
                context.getString(R.string.web_portal_no_viewer_app),
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to open/install downloaded file", e)
            Toast.makeText(
                context,
                context.getString(R.string.mini_downloader_error_installer).format(e.localizedMessage ?: ""),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun deleteCompleted(context: Context, completed: WebPortalDownloadState.Completed): Boolean {
        var deleted = false
        try {
            if (completed.contentUri != null) {
                deleted = context.contentResolver.delete(completed.contentUri, null, null) > 0
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed deleting MediaStore item", e)
        }
        try {
            val file = completed.localFile
            if (file != null && file.exists()) {
                deleted = file.delete() || deleted
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed deleting local file", e)
        }
        return deleted
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024L -> "$bytes B"
            bytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", bytes / 1024f)
            else -> String.format(Locale.US, "%.1f MB", bytes / (1024f * 1024f))
        }
    }
}

/**
 * Transient download progress / completion / error dialog shown ONLY while a download is
 * actively running or just finished inside a Custom Web Portal.
 */
@Composable
fun WebPortalDownloadDialog(
    state: WebPortalDownloadState,
    onCancelDownload: () -> Unit,
    onOpenOrInstall: (WebPortalDownloadState.Completed) -> Unit,
    onDeleteFile: (WebPortalDownloadState.Completed) -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        WebPortalDownloadState.Idle -> Unit

        is WebPortalDownloadState.Downloading -> {
            Dialog(onDismissRequest = { /* Keep visible while actively downloading */ }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .width(440.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = TvAccentGold,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.mini_downloader_downloading),
                            color = TvTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.fileName,
                            color = TvAccentGold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        if (state.percent >= 0 && state.totalBytes > 0L) {
                            LinearProgressIndicator(
                                progress = { state.percent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = TvAccentGold,
                                trackColor = Color.White.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val downloadedMb = state.downloadedBytes / (1024f * 1024f)
                            val totalMb = state.totalBytes / (1024f * 1024f)
                            Text(
                                text = stringResource(R.string.mini_downloader_status_format)
                                    .format(state.percent, downloadedMb, totalMb),
                                color = TvTextSecondary,
                                fontSize = 13.sp
                            )
                        } else {
                            CircularProgressIndicator(
                                color = TvAccentGold,
                                strokeWidth = 3.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = WebPortalDownloadManager.formatFileSize(state.downloadedBytes),
                                color = TvTextSecondary,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        TvFocusableCard(
                            onClick = onCancelDownload,
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth(0.6f)
                                .testTag("web_portal_download_cancel")
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFocused) Color(0xFFEF4444) else Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.action_cancel),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        is WebPortalDownloadState.Completed -> {
            Dialog(onDismissRequest = onDismiss) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 18.dp,
                    modifier = Modifier
                        .width(460.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = stringResource(R.string.mini_downloader_delete_prompt_title),
                            color = TvTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${state.fileName} (${WebPortalDownloadManager.formatFileSize(state.fileSize)})",
                            color = TvAccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (state.isApk) {
                                stringResource(R.string.mini_downloader_delete_prompt_desc)
                            } else {
                                stringResource(R.string.web_portal_download_saved_desc)
                            },
                            color = TvTextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(22.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TvFocusableCard(
                                onClick = { onOpenOrInstall(state) },
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(48.dp)
                                    .testTag("web_portal_download_open_or_install")
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isFocused) TvAccentGold else Color(0xFF10B981),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (state.isApk) {
                                            stringResource(R.string.mini_downloader_install_now)
                                        } else {
                                            stringResource(R.string.web_portal_open_file)
                                        },
                                        color = if (isFocused) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            TvFocusableCard(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("web_portal_download_keep")
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.12f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.mini_downloader_keep_file),
                                        color = if (isFocused) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            TvFocusableCard(
                                onClick = { onDeleteFile(state) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("web_portal_download_delete")
                            ) { isFocused ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            if (isFocused) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.35f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = stringResource(R.string.mini_downloader_delete_file),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        is WebPortalDownloadState.Error -> {
            Dialog(onDismissRequest = onDismiss) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 16.dp,
                    modifier = Modifier
                        .width(420.dp)
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.mini_downloader_download_error),
                            color = TvTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            color = TvTextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        TvFocusableCard(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(48.dp)
                                .testTag("web_portal_download_error_ok")
                        ) { isFocused ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        if (isFocused) TvAccentGold else Color.White.copy(alpha = 0.14f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.screensaver_yt_close_btn),
                                    color = if (isFocused) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
