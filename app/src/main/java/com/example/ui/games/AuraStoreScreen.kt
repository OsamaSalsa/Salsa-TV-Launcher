package com.example.ui.games

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.ui.common.AuroraColors
import com.example.ui.common.DimmedAmbientBackground
import com.example.ui.common.DownloadUrlResolver
import com.example.ui.common.TvFocusableCard
import com.example.ui.theme.TvAccentBlue
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvAccentGoldRadiant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AuraStorePage {
    STORE_BROWSER,
    DOWNLOADS_MANAGER
}

data class DownloadedFileInfo(
    val file: File,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isApk: Boolean
)

/**
 * Native Jetpack Compose Salsa Store Screen.
 * Fetches categories, logos, and apps directly from https://auratv.freedev.app/index.html
 * as a pure database and renders a sleek, flat, borderless Android TV UI with a dimmed
 * ambient background and full 21-language + RTL support.
 */
@Composable
fun AuraStoreScreen(
    onBackToHome: () -> Unit,
    appLanguage: String = "en",
    auroraColors: AuroraColors = AuroraColors(),
    wallpaperMode: String = "dynamic",
    customWallpaperUrl: String = "",
    wallpaperVisibility: Int = 100
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentPage by remember { mutableStateOf(AuraStorePage.STORE_BROWSER) }

    // Catalog State
    var categories by remember { mutableStateOf<List<AuraStoreCategory>>(emptyList()) }
    var selectedCategoryName by remember { mutableStateOf("__ALL__") }
    var searchQuery by remember { mutableStateOf("") }
    var isSyncingCatalog by remember { mutableStateOf(true) }

    // Download & Install State
    var isResolvingLink by remember { mutableStateOf(false) }
    var activeDownloadId by remember { mutableStateOf<Long?>(null) }
    var activeDownloadFileName by remember { mutableStateOf("") }
    var activeDownloadTargetFile by remember { mutableStateOf<File?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadedBytes by remember { mutableLongStateOf(0L) }
    var totalBytes by remember { mutableLongStateOf(-1L) }
    var downloadStatusText by remember { mutableStateOf("") }
    var downloadCompleteFile by remember { mutableStateOf<File?>(null) }
    var downloadErrorText by remember { mutableStateOf<String?>(null) }

    // Load local snapshot immediately, then sync live database from https://auratv.freedev.app/index.html
    LaunchedEffect(Unit) {
        val initial = AuraStoreRepository.loadCachedOrBundledDatabase(context)
        if (initial.isNotEmpty()) {
            categories = initial
        }
        isSyncingCatalog = true
        val liveResult = AuraStoreRepository.fetchRemoteDatabase(context)
        val live = liveResult.getOrNull()
        if (live != null && live.isNotEmpty()) {
            categories = live
        }
        isSyncingCatalog = false
    }

    val allApps = remember(categories) {
        categories.flatMap { it.apps }
    }

    val displayedApps = remember(categories, allApps, selectedCategoryName, searchQuery, appLanguage) {
        val baseList = if (selectedCategoryName == "__ALL__") {
            allApps
        } else {
            categories.firstOrNull { it.name == selectedCategoryName }?.apps ?: emptyList()
        }
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            baseList
        } else {
            allApps.filter { app ->
                app.title.contains(query, ignoreCase = true) ||
                    app.description.contains(query, ignoreCase = true) ||
                    app.categoryName.contains(query, ignoreCase = true) ||
                    AuraStoreLocalization.categoryTitle(app.categoryName, appLanguage).contains(query, ignoreCase = true)
            }
        }
    }

    fun startStoreDownload(rawUrl: String, suggestedTitle: String) {
        if (isResolvingLink || activeDownloadId != null) return
        isResolvingLink = true
        downloadErrorText = null
        downloadCompleteFile = null

        val statusConnectingText = context.getString(R.string.aurastore_connecting)
        val statusDownloadingText = context.getString(R.string.aurastore_downloading)

        coroutineScope.launch {
            try {
                val (finalUrl, finalFileName) = withContext(Dispatchers.IO) {
                    var currentUrl = DownloadUrlResolver.normalizeInitialDownloadUrl(rawUrl)
                    var guessedName = if (suggestedTitle.isNotBlank()) {
                        val sanitized = suggestedTitle.replace(Regex("[^a-zA-Z0-9._\\- ]"), "").trim().replace(" ", "_")
                        if (sanitized.lowercase().endsWith(".apk")) sanitized else "$sanitized.apk"
                    } else {
                        URLUtil.guessFileName(currentUrl, null, "application/vnd.android.package-archive")
                    }

                    try {
                        var redirects = 0
                        while (redirects < 5) {
                            val ua = DownloadUrlResolver.resolveUserAgentForUrl(
                                currentUrl,
                                DownloadUrlResolver.DESKTOP_CHROME_USER_AGENT
                            )
                            val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                                instanceFollowRedirects = false
                                connectTimeout = 10000
                                readTimeout = 10000
                                requestMethod = "GET"
                                setRequestProperty("User-Agent", ua)
                            }
                            conn.connect()
                            val code = conn.responseCode
                            if (code in 300..399) {
                                val location = conn.getHeaderField("Location")
                                conn.disconnect()
                                if (!location.isNullOrBlank()) {
                                    currentUrl = URL(URL(currentUrl), location).toString()
                                    currentUrl = DownloadUrlResolver.normalizeInitialDownloadUrl(currentUrl)
                                    redirects++
                                    continue
                                }
                            }
                            val contentDisposition = conn.getHeaderField("Content-Disposition")
                            val contentType = conn.contentType?.substringBefore(";")?.trim() ?: ""
                            if (contentType.contains("text/html", ignoreCase = true)) {
                                val htmlBody = conn.inputStream.bufferedReader().use { it.readText() }
                                conn.disconnect()
                                val htmlRedirect = DownloadUrlResolver.parseHtmlDownloadPage(currentUrl, htmlBody)
                                if (!htmlRedirect.discoveredFileName.isNullOrBlank() && suggestedTitle.isBlank()) {
                                    guessedName = htmlRedirect.discoveredFileName
                                }
                                if (!htmlRedirect.nextUrl.isNullOrBlank()) {
                                    currentUrl = DownloadUrlResolver.normalizeInitialDownloadUrl(htmlRedirect.nextUrl)
                                    redirects++
                                    continue
                                }
                            } else {
                                if (suggestedTitle.isBlank()) {
                                    guessedName = URLUtil.guessFileName(currentUrl, contentDisposition, contentType)
                                }
                                conn.disconnect()
                            }
                            break
                        }
                    } catch (_: Exception) {
                        // Proceed with resolved URL
                    }
                    Pair(currentUrl, guessedName)
                }

                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }

                var targetFile = File(downloadsDir, finalFileName)
                if (targetFile.exists()) {
                    val base = finalFileName.substringBeforeLast(".")
                    val ext = finalFileName.substringAfterLast(".", "")
                    val timestamp = System.currentTimeMillis() % 10000
                    val uniqueName = if (ext.isNotEmpty()) "${base}_$timestamp.$ext" else "${base}_$timestamp"
                    targetFile = File(downloadsDir, uniqueName)
                }

                val request = DownloadManager.Request(Uri.parse(finalUrl)).apply {
                    setTitle(targetFile.name)
                    setDescription(statusDownloadingText)
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setAllowedOverMetered(true)
                    setAllowedOverRoaming(true)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, targetFile.name)
                    addRequestHeader(
                        "User-Agent",
                        DownloadUrlResolver.resolveUserAgentForUrl(
                            finalUrl,
                            DownloadUrlResolver.DESKTOP_CHROME_USER_AGENT
                        )
                    )
                }

                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val id = dm.enqueue(request)
                activeDownloadId = id
                activeDownloadFileName = targetFile.name
                activeDownloadTargetFile = targetFile
                downloadProgress = 0f
                downloadedBytes = 0L
                totalBytes = -1L
                downloadStatusText = statusConnectingText
            } catch (_: Exception) {
                downloadErrorText = context.getString(R.string.aurastore_download_error_generic)
            } finally {
                isResolvingLink = false
            }
        }
    }

    // Poll DownloadManager for active download progress
    LaunchedEffect(activeDownloadId) {
        val id = activeDownloadId ?: return@LaunchedEffect
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        while (isActive && activeDownloadId == id) {
            val query = DownloadManager.Query().setFilterById(id)
            try {
                dm.query(query)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        val localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)

                        val status = if (statusIndex != -1) cursor.getInt(statusIndex) else DownloadManager.STATUS_RUNNING
                        val downloaded = if (downloadedIndex != -1) cursor.getLong(downloadedIndex) else 0L
                        val total = if (totalIndex != -1) cursor.getLong(totalIndex) else -1L

                        downloadedBytes = downloaded
                        totalBytes = total
                        downloadProgress = if (total > 0) (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f) else -1f

                        when (status) {
                            DownloadManager.STATUS_PENDING -> {
                                downloadStatusText = context.getString(R.string.aurastore_connecting)
                            }
                            DownloadManager.STATUS_RUNNING -> {
                                downloadStatusText = context.getString(R.string.aurastore_downloading)
                            }
                            DownloadManager.STATUS_PAUSED -> {
                                downloadStatusText = context.getString(R.string.aurastore_connecting)
                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                var resolvedFile = activeDownloadTargetFile
                                if (localUriIndex != -1) {
                                    val localUriStr = cursor.getString(localUriIndex)
                                    if (!localUriStr.isNullOrBlank()) {
                                        val parsedUri = Uri.parse(localUriStr)
                                        val filePath = parsedUri.path
                                        if (parsedUri.scheme == "file" && !filePath.isNullOrBlank()) {
                                            resolvedFile = File(filePath)
                                        }
                                    }
                                }
                                activeDownloadId = null
                                if (resolvedFile != null && resolvedFile.exists()) {
                                    downloadCompleteFile = resolvedFile
                                    if (resolvedFile.name.lowercase().endsWith(".apk")) {
                                        installApkFile(context, resolvedFile)
                                    }
                                }
                            }
                            DownloadManager.STATUS_FAILED -> {
                                activeDownloadId = null
                                downloadErrorText = context.getString(R.string.aurastore_download_failed)
                            }
                        }
                    } else {
                        activeDownloadId = null
                    }
                }
            } catch (_: Exception) {
                activeDownloadId = null
            }
            delay(350)
        }
    }

    BackHandler {
        when {
            currentPage == AuraStorePage.DOWNLOADS_MANAGER -> {
                currentPage = AuraStorePage.STORE_BROWSER
            }
            searchQuery.isNotEmpty() -> {
                searchQuery = ""
            }
            else -> {
                onBackToHome()
            }
        }
    }

    DimmedAmbientBackground(
        auroraColors = auroraColors,
        wallpaperMode = wallpaperMode,
        customWallpaperUrl = customWallpaperUrl,
        wallpaperVisibility = wallpaperVisibility
    ) {
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
            label = "aura_store_page_transition"
        ) { page ->
            when (page) {
                AuraStorePage.STORE_BROWSER -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Category Sidebar Panel (Sleek Flat Translucent Surface)
                        AuraStoreSidebar(
                            categories = categories,
                            allAppsCount = allApps.size,
                            selectedCategoryName = selectedCategoryName,
                            appLanguage = appLanguage,
                            onSelectCategory = { catName ->
                                selectedCategoryName = catName
                                if (searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                }
                            },
                            onBackToHome = onBackToHome
                        )

                        // Main Store Content Area
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            // Top Bar: Search Input + Sync Button + Downloads Button
                            AuraStoreTopActionBar(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                isSyncingCatalog = isSyncingCatalog,
                                appLanguage = appLanguage,
                                onSyncCatalog = {
                                    if (!isSyncingCatalog) {
                                        isSyncingCatalog = true
                                        coroutineScope.launch {
                                            val updatedResult = AuraStoreRepository.fetchRemoteDatabase(context)
                                            val updated = updatedResult.getOrNull()
                                            if (updated != null && updated.isNotEmpty()) {
                                                categories = updated
                                                val totalSynced = updated.sumOf { it.apps.size }
                                                Toast.makeText(
                                                    context,
                                                    AuraStoreLocalization.syncedToast(totalSynced, appLanguage),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            isSyncingCatalog = false
                                        }
                                    }
                                },
                                onOpenDownloads = {
                                    currentPage = AuraStorePage.DOWNLOADS_MANAGER
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Active Category Banner (Flat Tonal Surface)
                            val activeCategoryObj = remember(categories, selectedCategoryName) {
                                categories.firstOrNull { it.name == selectedCategoryName }
                            }
                            val activeCategoryLogo = remember(activeCategoryObj) {
                                activeCategoryObj?.apps?.firstOrNull()?.logoUrl ?: ""
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF0C1322).copy(alpha = 0.56f))
                                    .padding(horizontal = 18.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (activeCategoryLogo.isNotBlank()) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(activeCategoryLogo)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = activeCategoryObj?.name,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Fit
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(TvAccentGold.copy(alpha = 0.16f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Apps,
                                                contentDescription = null,
                                                tint = TvAccentGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (searchQuery.isNotBlank()) {
                                            AuraStoreLocalization.searchResultsFor(searchQuery, appLanguage)
                                        } else {
                                            AuraStoreLocalization.categoryTitle(selectedCategoryName, appLanguage)
                                        },
                                        color = TvTextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(TvAccentGold.copy(alpha = 0.14f))
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = AuraStoreLocalization.appsCount(displayedApps.size, appLanguage),
                                        color = TvAccentGoldRadiant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Apps Grid
                            if (displayedApps.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(Color(0xFF0C1322).copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = TvTextSecondary.copy(alpha = 0.5f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = AuraStoreLocalization.noMatchingApps(appLanguage),
                                            color = TvTextSecondary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(4),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("aurastore_apps_grid"),
                                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(
                                        items = displayedApps,
                                        key = { app -> app.id }
                                    ) { app ->
                                        AuraStoreAppCard(
                                            app = app,
                                            appLanguage = appLanguage,
                                            onDownloadClick = {
                                                startStoreDownload(app.downloadUrl, app.title)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AuraStorePage.DOWNLOADS_MANAGER -> {
                    DownloadsManagerScreen(
                        onBackToStore = { currentPage = AuraStorePage.STORE_BROWSER },
                        onInstallApk = { apkFile -> installApkFile(context, apkFile) }
                    )
                }
            }
        }

        // Native Download & Installation Dialogs
        AuraStoreDownloadDialogs(
            isResolvingLink = isResolvingLink,
            activeDownloadId = activeDownloadId,
            activeDownloadFileName = activeDownloadFileName,
            downloadStatusText = downloadStatusText,
            downloadProgress = downloadProgress,
            downloadedBytes = downloadedBytes,
            totalBytes = totalBytes,
            downloadCompleteFile = downloadCompleteFile,
            downloadErrorText = downloadErrorText,
            onCancelActiveDownload = {
                val id = activeDownloadId
                if (id != null) {
                    val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    dm.remove(id)
                }
                activeDownloadId = null
            },
            onInstallCompletedFile = { file ->
                downloadCompleteFile = null
                installApkFile(context, file)
            },
            onOpenDownloadsFolder = {
                downloadCompleteFile = null
                currentPage = AuraStorePage.DOWNLOADS_MANAGER
            },
            onDismissComplete = { downloadCompleteFile = null },
            onDismissError = { downloadErrorText = null }
        )
    }
}

@Composable
private fun AuraStoreSidebar(
    categories: List<AuraStoreCategory>,
    allAppsCount: Int,
    selectedCategoryName: String,
    appLanguage: String,
    onSelectCategory: (String) -> Unit,
    onBackToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(265.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF0A101E).copy(alpha = 0.62f))
            .padding(16.dp)
    ) {
        // Store Brand Header + Flat Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TvFocusableCard(
                onClick = onBackToHome,
                shape = CircleShape,
                scaleUpRatio = 1.06f,
                useDefaultBorder = false,
                modifier = Modifier
                    .size(42.dp)
                    .testTag("aurastore_back_home_button")
            ) { isFocused ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            if (isFocused) TvAccentGold else Color(0xFF151F35).copy(alpha = 0.72f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.aurastore_back_to_store),
                        tint = if (isFocused) Color.Black else TvTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Salsa Store",
                    color = TvTextPrimary,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = AuraStoreLocalization.availableApps(allAppsCount, appLanguage),
                    color = TvAccentGold.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subtle Flat Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.06f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // "All Apps" Tab
            item(key = "__ALL__") {
                AuraStoreCategoryTabItem(
                    title = AuraStoreLocalization.categoryTitle("__ALL__", appLanguage),
                    logoUrl = "",
                    appCount = allAppsCount,
                    isSelected = selectedCategoryName == "__ALL__",
                    onClick = { onSelectCategory("__ALL__") }
                )
            }

            // Remote Database Categories
            itemsIndexed(
                items = categories,
                key = { _, cat -> cat.name }
            ) { _, category ->
                AuraStoreCategoryTabItem(
                    title = AuraStoreLocalization.categoryTitle(category.name, appLanguage),
                    logoUrl = category.apps.firstOrNull()?.logoUrl ?: "",
                    appCount = category.apps.size,
                    isSelected = selectedCategoryName == category.name,
                    onClick = { onSelectCategory(category.name) }
                )
            }
        }
    }
}

@Composable
private fun AuraStoreCategoryTabItem(
    title: String,
    logoUrl: String,
    appCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    TvFocusableCard(
        onClick = onClick,
        onFocused = onClick,
        shape = RoundedCornerShape(14.dp),
        scaleUpRatio = 1.02f,
        useDefaultBorder = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) { isFocused ->
        val bgColor by animateColorAsState(
            targetValue = when {
                isFocused -> TvAccentGold
                isSelected -> TvAccentGold.copy(alpha = 0.18f)
                else -> Color.White.copy(alpha = 0.03f)
            },
            animationSpec = tween(150),
            label = "category_tab_bg"
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (logoUrl.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = title,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = title,
                        tint = when {
                            isFocused -> Color.Black
                            isSelected -> TvAccentGold
                            else -> TvTextSecondary
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = title,
                    color = when {
                        isFocused -> Color.Black
                        isSelected -> TvAccentGoldRadiant
                        else -> TvTextPrimary.copy(alpha = 0.9f)
                    },
                    fontSize = 13.sp,
                    fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when {
                            isFocused -> Color.Black.copy(alpha = 0.14f)
                            isSelected -> TvAccentGold.copy(alpha = 0.22f)
                            else -> Color.White.copy(alpha = 0.05f)
                        }
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = appCount.toString(),
                    color = when {
                        isFocused -> Color.Black
                        isSelected -> TvAccentGoldRadiant
                        else -> TvTextSecondary
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AuraStoreTopActionBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSyncingCatalog: Boolean,
    appLanguage: String,
    onSyncCatalog: () -> Unit,
    onOpenDownloads: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchInputFocusRequester = remember { FocusRequester() }
    val searchCardFocusRequester = remember { FocusRequester() }
    var isEditingSearch by remember { mutableStateOf(false) }

    LaunchedEffect(isEditingSearch) {
        if (isEditingSearch) {
            try {
                searchInputFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {}
        } else {
            keyboardController?.hide()
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Input Bar (Requires pressing OK/Enter to open on-screen keyboard; flat surface)
        TvFocusableCard(
            onClick = { isEditingSearch = true },
            focusRequester = searchCardFocusRequester,
            shape = RoundedCornerShape(16.dp),
            scaleUpRatio = 1.01f,
            useDefaultBorder = false,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("aurastore_search_bar")
        ) { isFocused ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isFocused || isEditingSearch) Color(0xFF18233A).copy(alpha = 0.88f)
                        else Color(0xFF0C1322).copy(alpha = 0.56f)
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = if (isFocused || isEditingSearch) TvAccentGold else TvTextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = AuraStoreLocalization.searchPlaceholder(appLanguage),
                            color = TvTextSecondary.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (isEditingSearch) {
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TvTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            cursorBrush = SolidColor(TvAccentGold),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    isEditingSearch = false
                                    keyboardController?.hide()
                                    try { searchCardFocusRequester.requestFocus() } catch (_: Exception) {}
                                },
                                onDone = {
                                    isEditingSearch = false
                                    keyboardController?.hide()
                                    try { searchCardFocusRequester.requestFocus() } catch (_: Exception) {}
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchInputFocusRequester)
                                .onPreviewKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown &&
                                        event.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_BACK
                                    ) {
                                        isEditingSearch = false
                                        keyboardController?.hide()
                                        try { searchCardFocusRequester.requestFocus() } catch (_: Exception) {}
                                        true
                                    } else {
                                        false
                                    }
                                }
                        )
                    } else if (searchQuery.isNotEmpty()) {
                        Text(
                            text = searchQuery,
                            color = TvTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (searchQuery.isNotEmpty()) {
                    TvFocusableCard(
                        onClick = {
                            onSearchQueryChange("")
                            isEditingSearch = false
                            focusManager.clearFocus()
                        },
                        shape = CircleShape,
                        scaleUpRatio = 1.08f,
                        useDefaultBorder = false,
                        modifier = Modifier.size(28.dp)
                    ) { clearFocused ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(
                                    if (clearFocused) TvAccentGold else Color.White.copy(alpha = 0.08f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = if (clearFocused) Color.Black else TvTextPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sync Live Database Button (Flat Surface)
        TvFocusableCard(
            onClick = onSyncCatalog,
            shape = RoundedCornerShape(16.dp),
            scaleUpRatio = 1.04f,
            useDefaultBorder = false,
            modifier = Modifier
                .height(48.dp)
                .testTag("aurastore_sync_button")
        ) { isFocused ->
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isFocused) TvAccentGold else Color(0xFF0C1322).copy(alpha = 0.56f)
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isSyncingCatalog) {
                    CircularProgressIndicator(
                        color = if (isFocused) Color.Black else TvAccentGold,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = if (isFocused) Color.Black else TvAccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = AuraStoreLocalization.syncButton(appLanguage),
                    color = if (isFocused) Color.Black else TvTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Downloads Manager Button (Flat Surface)
        TvFocusableCard(
            onClick = onOpenDownloads,
            shape = RoundedCornerShape(16.dp),
            scaleUpRatio = 1.04f,
            useDefaultBorder = false,
            modifier = Modifier
                .height(48.dp)
                .testTag("aurastore_downloads_button")
        ) { isFocused ->
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isFocused) TvAccentGold else Color(0xFF0C1322).copy(alpha = 0.56f)
                    )
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = stringResource(R.string.aurastore_downloads_btn),
                    tint = if (isFocused) Color.Black else TvAccentGold,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.aurastore_downloads_btn),
                    color = if (isFocused) Color.Black else TvTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AuraStoreAppCard(
    app: AuraStoreAppItem,
    appLanguage: String,
    onDownloadClick: () -> Unit
) {
    val context = LocalContext.current

    TvFocusableCard(
        onClick = onDownloadClick,
        shape = RoundedCornerShape(20.dp),
        scaleUpRatio = 1.035f,
        useDefaultBorder = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
    ) { isFocused ->
        val cardBg by animateColorAsState(
            targetValue = if (isFocused) {
                Color(0xFF19253D).copy(alpha = 0.90f)
            } else {
                Color(0xFF0C1322).copy(alpha = 0.54f)
            },
            animationSpec = tween(160),
            label = "app_card_bg"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // App Logo + Category Tag Row
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = if (isFocused) 0.12f else 0.06f))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(app.logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = app.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = TvAccentGold.copy(alpha = 0.5f),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = app.title.take(2).uppercase(),
                                    color = TvAccentGold,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    )
                }

                // App Name
                Text(
                    text = app.title,
                    color = TvTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                // Description / Localized Tag Badge
                Text(
                    text = AuraStoreLocalization.appTagDescription(
                        rawDesc = app.description,
                        categoryName = app.categoryName,
                        lang = appLanguage
                    ),
                    color = if (isFocused) TvAccentGoldRadiant else TvTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            // Action Pill (Download & Install - Flat Tonal Pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isFocused) TvAccentGold
                        else Color.White.copy(alpha = 0.06f)
                    )
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = if (isFocused) Color.Black else TvAccentGold,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AuraStoreLocalization.downloadAndInstall(appLanguage),
                    color = if (isFocused) Color.Black else TvTextPrimary.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AuraStoreDownloadDialogs(
    isResolvingLink: Boolean,
    activeDownloadId: Long?,
    activeDownloadFileName: String,
    downloadStatusText: String,
    downloadProgress: Float,
    downloadedBytes: Long,
    totalBytes: Long,
    downloadCompleteFile: File?,
    downloadErrorText: String?,
    onCancelActiveDownload: () -> Unit,
    onInstallCompletedFile: (File) -> Unit,
    onOpenDownloadsFolder: () -> Unit,
    onDismissComplete: () -> Unit,
    onDismissError: () -> Unit
) {
    if (isResolvingLink) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF0E1628),
            titleContentColor = TvTextPrimary,
            textContentColor = TvTextSecondary,
            title = {
                Text(
                    text = stringResource(R.string.aurastore_connecting),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    CircularProgressIndicator(
                        color = TvAccentGold,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = stringResource(R.string.aurastore_downloading),
                        color = TvTextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {}
        )
    }

    if (activeDownloadId != null) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFF0E1628),
            titleContentColor = TvTextPrimary,
            textContentColor = TvTextSecondary,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = TvAccentGold
                    )
                    Text(
                        text = stringResource(R.string.aurastore_downloading),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = activeDownloadFileName,
                        color = TvTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = downloadStatusText,
                        color = TvAccentGold,
                        fontSize = 12.sp
                    )

                    if (downloadProgress >= 0f) {
                        LinearProgressIndicator(
                            progress = { downloadProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TvAccentGold,
                            trackColor = Color.White.copy(alpha = 0.12f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(downloadProgress * 100).toInt()}%",
                                color = TvTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${formatFileSize(downloadedBytes)} / ${formatFileSize(totalBytes)}",
                                color = TvTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TvAccentGold,
                            trackColor = Color.White.copy(alpha = 0.12f)
                        )
                        Text(
                            text = formatFileSize(downloadedBytes),
                            color = TvTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = onCancelActiveDownload,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B))
                ) {
                    Text(stringResource(R.string.aurastore_cancel_download))
                }
            }
        )
    }

    val completedFile = downloadCompleteFile
    if (completedFile != null) {
        val isApk = completedFile.name.lowercase().endsWith(".apk")
        AlertDialog(
            onDismissRequest = onDismissComplete,
            containerColor = Color(0xFF0E1628),
            titleContentColor = TvTextPrimary,
            textContentColor = TvTextSecondary,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Text(
                        text = stringResource(R.string.aurastore_download_complete),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = completedFile.name,
                        color = TvTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = formatFileSize(completedFile.length()),
                        color = TvTextSecondary,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isApk) {
                        Button(
                            onClick = { onInstallCompletedFile(completedFile) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TvAccentGold,
                                contentColor = Color.Black
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.aurastore_install),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Button(
                        onClick = onOpenDownloadsFolder,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TvAccentBlue.copy(alpha = 0.25f),
                            contentColor = TvTextPrimary
                        )
                    ) {
                        Text(stringResource(R.string.aurastore_downloads_btn))
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismissComplete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TvTextSecondary)
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    val errorMsg = downloadErrorText
    if (errorMsg != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            containerColor = Color(0xFF0E1628),
            titleContentColor = TvTextPrimary,
            textContentColor = TvTextSecondary,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color(0xFFFF6B6B)
                    )
                    Text(
                        text = stringResource(R.string.aurastore_download_error),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Text(
                    text = errorMsg,
                    color = TvTextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = onDismissError,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TvAccentGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = stringResource(R.string.aurastore_ok),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Composable
private fun DownloadsManagerScreen(
    onBackToStore: () -> Unit,
    onInstallApk: (File) -> Unit
) {
    val context = LocalContext.current
    var files by remember { mutableStateOf<List<DownloadedFileInfo>>(emptyList()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(refreshTrigger) {
        files = withContext(Dispatchers.IO) {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val list = downloadsDir.listFiles()?.filter { it.isFile } ?: emptyList()
            list.sortedByDescending { it.lastModified() }.map { file ->
                DownloadedFileInfo(
                    file = file,
                    name = file.name,
                    sizeBytes = file.length(),
                    lastModified = file.lastModified(),
                    isApk = file.name.lowercase().endsWith(".apk")
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp, vertical = 28.dp)
    ) {
        // Header Row (Flat Borderless Controls)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TvFocusableCard(
                    onClick = onBackToStore,
                    shape = RoundedCornerShape(14.dp),
                    scaleUpRatio = 1.05f,
                    useDefaultBorder = false,
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("aurastore_back_to_store_button")
                ) { isFocused ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isFocused) TvAccentGold else Color(0xFF0C1322).copy(alpha = 0.56f)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.aurastore_back_to_store),
                            tint = if (isFocused) Color.Black else TvTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.aurastore_back_to_store),
                            color = if (isFocused) Color.Black else TvTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.aurastore_manager_title),
                        color = TvTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = stringResource(R.string.aurastore_manager_desc),
                        color = TvTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            if (files.isNotEmpty()) {
                TvFocusableCard(
                    onClick = {
                        files.forEach { runCatching { it.file.delete() } }
                        refreshTrigger++
                        Toast.makeText(
                            context,
                            context.getString(R.string.aurastore_all_cleared),
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(14.dp),
                    scaleUpRatio = 1.05f,
                    useDefaultBorder = false,
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("aurastore_delete_all_button")
                ) { isFocused ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isFocused) Color(0xFFE53935)
                                else Color(0xFFE53935).copy(alpha = 0.16f)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = if (isFocused) Color.White else Color(0xFFFF8A80),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.aurastore_clear_all),
                            color = if (isFocused) Color.White else Color(0xFFFF8A80),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (files.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0C1322).copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = TvTextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = stringResource(R.string.aurastore_no_files),
                        color = TvTextSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(files, key = { _, item -> item.file.absolutePath }) { _, item ->
                    DownloadedFileCard(
                        item = item,
                        onInstall = { onInstallApk(item.file) },
                        onDelete = {
                            runCatching { item.file.delete() }
                            refreshTrigger++
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadedFileCard(
    item: DownloadedFileInfo,
    onInstall: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0C1322).copy(alpha = 0.56f))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (item.isApk) TvAccentGold.copy(alpha = 0.16f)
                        else TvAccentBlue.copy(alpha = 0.16f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isApk) Icons.Default.GetApp else Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = if (item.isApk) TvAccentGold else TvAccentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = TvTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatFileSize(item.sizeBytes)}  •  ${dateFormat.format(Date(item.lastModified))}",
                    color = TvTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (item.isApk) {
                TvFocusableCard(
                    onClick = onInstall,
                    shape = RoundedCornerShape(12.dp),
                    scaleUpRatio = 1.05f,
                    useDefaultBorder = false,
                    modifier = Modifier.height(40.dp)
                ) { isFocused ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isFocused) TvAccentGold else TvAccentGold.copy(alpha = 0.18f)
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GetApp,
                            contentDescription = null,
                            tint = if (isFocused) Color.Black else TvAccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.aurastore_install),
                            color = if (isFocused) Color.Black else TvAccentGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            TvFocusableCard(
                onClick = onDelete,
                shape = RoundedCornerShape(12.dp),
                scaleUpRatio = 1.05f,
                useDefaultBorder = false,
                modifier = Modifier.height(40.dp)
            ) { isFocused ->
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isFocused) Color(0xFFE53935) else Color.White.copy(alpha = 0.06f)
                        )
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.aurastore_delete),
                        tint = if (isFocused) Color.White else Color(0xFFFF8A80),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(R.string.aurastore_delete),
                        color = if (isFocused) Color.White else Color(0xFFFF8A80),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun installApkFile(context: Context, apkFile: File) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.update_permission_hint),
                    Toast.LENGTH_LONG
                ).show()
                val settingsIntent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    } catch (e: Exception) {
        Toast.makeText(
            context,
            context.getString(R.string.aurastore_installer_error, e.localizedMessage ?: ""),
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.1f MB", mb)
        else -> String.format(Locale.US, "%.0f KB", kb)
    }
}
