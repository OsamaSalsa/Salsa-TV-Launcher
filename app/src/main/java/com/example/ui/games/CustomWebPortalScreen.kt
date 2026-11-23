package com.example.ui.games

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.ui.common.BindWebViewLifecycle
import com.example.ui.common.DownloadUrlResolver
import com.example.ui.common.TvFocusableCard
import com.example.ui.common.WebPortalDownloadDialog
import com.example.ui.common.WebPortalDownloadManager
import com.example.ui.common.WebPortalDownloadState
import com.example.ui.common.ensureYouTubeConsentCookies
import com.example.ui.common.findActivityContext
import com.example.ui.common.grantSafeWebPermissions
import com.example.ui.common.hardenWebView
import com.example.ui.common.injectWebViewStabilityShim
import com.example.ui.common.isWebViewDestroyed
import com.example.ui.common.postOnMain
import com.example.ui.common.safeDestroyWebView
import com.example.ui.downloader.VirtualMousePointer
import com.example.ui.theme.TvAccentGold
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.ui.common.YOUTUBE_TV_USER_AGENT
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
class CustomBookmarkWebView(
    context: Context,
    initialUrl: String = "",
    var onBackAtRoot: (() -> Unit)? = null,
    var onToggleMouseMode: (() -> Unit)? = null,
    var onRendererCrashed: (() -> Unit)? = null,
    var onRequestDownload: ((url: String, userAgent: String?, contentDisposition: String?, mimeType: String?, referer: String?) -> Unit)? = null,
    var onBlobDownloaded: ((dataUri: String, fileName: String?, mimeType: String?) -> Unit)? = null
) : WebView(context) {

    private val isYoutubeHost = WebPortalDownloadManager.isYoutubeHost(initialUrl)

    var onLoadingStateChanged: ((Boolean) -> Unit)? = null
    var onErrorChanged: ((String?) -> Unit)? = null
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null
    private var fullscreenContainer: FrameLayout? = null

    private inner class AuraBlobDownloadBridge {
        @JavascriptInterface
        fun onBlobReady(dataUri: String?, fileName: String?, mimeType: String?) {
            if (dataUri.isNullOrBlank()) return
            postOnMain {
                onBlobDownloaded?.invoke(dataUri, fileName, mimeType)
            }
        }
    }

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

        val isYoutube = WebPortalDownloadManager.isYoutubeHost(initialUrl)
        val resolvedUserAgent = if (isYoutube) {
            YOUTUBE_TV_USER_AGENT
        } else {
            WebPortalDownloadManager.ANDROID_CHROME_USER_AGENT
        }

        if (isYoutube) {
            setBackgroundColor(android.graphics.Color.BLACK)
            keepScreenOn = true
        }

        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            userAgentString = resolvedUserAgent
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(!isYoutube)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = !isYoutube
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            safeBrowsingEnabled = false
        }

        if (!isYoutube) {
            addJavascriptInterface(AuraBlobDownloadBridge(), "AuraPortalDownloadBridge")
        }
        hardenWebView(this)
        if (isYoutube) {
            ensureYouTubeConsentCookies(this)
        } else {
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                setAcceptThirdPartyCookies(this@CustomBookmarkWebView, true)
            }
        }

        setDownloadListener { dUrl, userAgent, contentDisposition, mimeType, _ ->
            if (dUrl.isNullOrBlank()) return@setDownloadListener
            val effectiveUa = userAgent?.takeIf { it.isNotBlank() } ?: settings.userAgentString
            val refererUrl = this@CustomBookmarkWebView.url
            if (dUrl.startsWith("blob:", ignoreCase = true)) {
                val suggestedName = WebPortalDownloadManager.extractGeneralFileName(
                    url = dUrl,
                    contentDisposition = contentDisposition,
                    mimeType = mimeType
                )
                val jsUrl = JSONObject.quote(dUrl)
                val jsName = JSONObject.quote(suggestedName)
                val jsMime = JSONObject.quote(mimeType ?: "")
                evaluateJavascript(
                    """
                    (function() {
                        fetch($jsUrl).then(function(r) { return r.blob(); }).then(function(blob) {
                            var reader = new FileReader();
                            reader.onloadend = function() {
                                if (window.AuraPortalDownloadBridge && reader.result) {
                                    window.AuraPortalDownloadBridge.onBlobReady(reader.result, $jsName, blob.type || $jsMime);
                                }
                            };
                            reader.readAsDataURL(blob);
                        }).catch(function() {});
                    })();
                    """.trimIndent(),
                    null
                )
            } else {
                onRequestDownload?.invoke(dUrl, effectiveUa, contentDisposition, mimeType, refererUrl)
            }
        }

        webViewClient = object : WebViewClient() {
            override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                (context.applicationContext as? com.example.AuraTvApp)?.trimNonEssentialMemoryForWebView()
                safeDestroyWebView(view, rendererGone = true)
                postOnMain(500L) { onRendererCrashed?.invoke() }
                return true
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val rawUrl = request?.url?.toString() ?: return false
                if (isYoutube) {
                    return !(rawUrl.contains("youtube.com", ignoreCase = true) ||
                        rawUrl.contains("youtu.be", ignoreCase = true) ||
                        rawUrl.contains("google.", ignoreCase = true) ||
                        rawUrl.contains("googlevideo.com", ignoreCase = true) ||
                        rawUrl.contains("ytimg.com", ignoreCase = true) ||
                        rawUrl.contains("gstatic.com", ignoreCase = true))
                }
                if (WebPortalDownloadManager.isDirectDownloadableUrl(rawUrl)) {
                    val effectiveUa = view?.settings?.userAgentString
                    val refererUrl = view?.url
                    onRequestDownload?.invoke(
                        DownloadUrlResolver.normalizeInitialDownloadUrl(rawUrl),
                        effectiveUa,
                        null,
                        if (rawUrl.contains(".apk", ignoreCase = true)) "application/vnd.android.package-archive" else null,
                        refererUrl
                    )
                    return true
                }
                if (rawUrl.startsWith("intent://", ignoreCase = true)) {
                    val unwrapped = DownloadUrlResolver.unwrapIntentUrl(rawUrl)
                    if (unwrapped != rawUrl &&
                        (unwrapped.startsWith("http://", ignoreCase = true) ||
                            unwrapped.startsWith("https://", ignoreCase = true))
                    ) {
                        view?.loadUrl(unwrapped)
                        return true
                    }
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                injectWebViewStabilityShim(view, disableWebGl = false)
                if (!isYoutube) {
                    DownloadUrlResolver.injectPopupAndBlankTargetShim(view)
                }
                onErrorChanged?.invoke(null)
                onLoadingStateChanged?.invoke(true)
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                injectWebViewStabilityShim(view, disableWebGl = false)
                if (!isYoutube) {
                    DownloadUrlResolver.injectPopupAndBlankTargetShim(view)
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectWebViewStabilityShim(view, disableWebGl = false)
                if (!isYoutube) {
                    DownloadUrlResolver.injectPopupAndBlankTargetShim(view)
                }
                onLoadingStateChanged?.invoke(false)
                if (isYoutube) {
                    view?.requestFocus()
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    val failingUrl = request.url?.toString().orEmpty()
                    if (failingUrl.isNotBlank() && WebPortalDownloadManager.isDirectDownloadableUrl(failingUrl)) {
                        onLoadingStateChanged?.invoke(false)
                        onErrorChanged?.invoke(null)
                        onRequestDownload?.invoke(
                            DownloadUrlResolver.normalizeInitialDownloadUrl(failingUrl),
                            view?.settings?.userAgentString,
                            null,
                            if (failingUrl.contains(".apk", ignoreCase = true)) "application/vnd.android.package-archive" else null,
                            view?.url
                        )
                        return
                    }
                    onLoadingStateChanged?.invoke(false)
                    onErrorChanged?.invoke(error?.description?.toString() ?: "Page load error")
                }
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                grantSafeWebPermissions(request)
            }

            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                fullscreenContainer = FrameLayout(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.BLACK)
                    addView(view)
                }
                (parent as? ViewGroup)?.addView(fullscreenContainer)
                visibility = View.INVISIBLE
            }

            override fun onHideCustomView() {
                if (customView == null) return
                fullscreenContainer?.let { container ->
                    container.removeAllViews()
                    (container.parent as? ViewGroup)?.removeView(container)
                }
                fullscreenContainer = null
                customView = null
                customViewCallback?.onCustomViewHidden()
                visibility = View.VISIBLE
                requestFocus()
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_SETTINGS -> {
                    if (!isYoutubeHost && onToggleMouseMode != null) {
                        onToggleMouseMode?.invoke()
                        return true
                    }
                }
                KeyEvent.KEYCODE_BACK -> {
                    if (customView != null) {
                        webChromeClient?.onHideCustomView()
                        return true
                    }
                    if (canGoBack()) {
                        goBack()
                        return true
                    } else {
                        onBackAtRoot?.invoke()
                        return true
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onDetachedFromWindow() {
        fullscreenContainer?.let { container ->
            container.removeAllViews()
            (container.parent as? ViewGroup)?.removeView(container)
        }
        fullscreenContainer = null
        customView = null
        customViewCallback = null
        super.onDetachedFromWindow()
    }
}

@Composable
fun CustomWebPortalScreen(
    title: String,
    url: String,
    onBackToHome: () -> Unit
) {
    val normalizedUrl = remember(url) {
        val trimmed = url.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var crashCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var webViewRef by remember { mutableStateOf<CustomBookmarkWebView?>(null) }

    var downloadState by remember { mutableStateOf<WebPortalDownloadState>(WebPortalDownloadState.Idle) }
    var downloadJob by remember { mutableStateOf<Job?>(null) }

    fun startPortalDownload(
        downloadUrl: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?,
        referer: String?
    ) {
        downloadJob?.cancel()
        downloadJob = scope.launch {
            WebPortalDownloadManager.downloadUrl(
                context = context,
                rawUrl = downloadUrl,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType,
                referer = referer ?: normalizedUrl,
                onStateChanged = { state -> downloadState = state },
                onFallbackToWebView = { fallbackUrl ->
                    webViewRef?.let { wv ->
                        if (!isWebViewDestroyed(wv) && wv.url != fallbackUrl) {
                            wv.loadUrl(fallbackUrl)
                        }
                    }
                }
            )
        }
    }

    fun startBlobDownload(dataUri: String, fileName: String?, mimeType: String?) {
        downloadJob?.cancel()
        downloadJob = scope.launch {
            WebPortalDownloadManager.saveDataUri(
                context = context,
                dataUri = dataUri,
                contentDisposition = null,
                mimeType = mimeType,
                suggestedFileName = fileName,
                onStateChanged = { state -> downloadState = state }
            )
        }
    }

    val isYouTubePortal = remember(normalizedUrl) {
        WebPortalDownloadManager.isYoutubeHost(normalizedUrl)
    }
    var isMouseModeEnabled by remember(isYouTubePortal) { mutableStateOf(!isYouTubePortal) }
    var screenWidthPx by remember { mutableFloatStateOf(1920f) }
    var screenHeightPx by remember { mutableFloatStateOf(1080f) }
    var cursorX by remember { mutableFloatStateOf(960f) }
    var cursorY by remember { mutableFloatStateOf(540f) }
    var isCursorClicking by remember { mutableStateOf(false) }

    val cursorStep = 65f
    val scrollStep = 220

    fun dispatchClickAtCursor() {
        val webView = webViewRef ?: return
        if (isWebViewDestroyed(webView)) return
        isCursorClicking = true
        val downTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, cursorX, cursorY, 0)
        val upEvent = MotionEvent.obtain(downTime, downTime + 45, MotionEvent.ACTION_UP, cursorX, cursorY, 0)
        webView.dispatchTouchEvent(downEvent)
        webView.dispatchTouchEvent(upEvent)
        downEvent.recycle()
        upEvent.recycle()
        postOnMain(120L) { isCursorClicking = false }
    }

    BackHandler {
        val webView = webViewRef
        if (webView != null && webView.canGoBack()) {
            webView.goBack()
        } else {
            onBackToHome()
        }
    }

    if (crashCount > 1) {
        LaunchedEffect(Unit) { onBackToHome() }
        return
    }

    val portalFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isMouseModeEnabled, isYouTubePortal, webViewRef) {
        if (!isYouTubePortal && isMouseModeEnabled) {
            try { portalFocusRequester.requestFocus() } catch (_: Exception) {}
        } else {
            try { webViewRef?.requestFocus() } catch (_: Exception) {}
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .then(
                if (!isYouTubePortal) {
                    Modifier
                        .focusRequester(portalFocusRequester)
                        .focusable(enabled = isMouseModeEnabled)
                        .onKeyEvent { keyEvent ->
                            val native = keyEvent.nativeKeyEvent
                            if (native.action == KeyEvent.ACTION_DOWN) {
                                if (native.keyCode == KeyEvent.KEYCODE_MENU || native.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                                    isMouseModeEnabled = !isMouseModeEnabled
                                    true
                                } else if (isMouseModeEnabled) {
                                    when (native.keyCode) {
                                        KeyEvent.KEYCODE_DPAD_LEFT -> {
                                            cursorX = (cursorX - cursorStep).coerceAtLeast(10f)
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                            cursorX = (cursorX + cursorStep).coerceAtMost(screenWidthPx - 10f)
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_UP -> {
                                            if (cursorY <= 120f) webViewRef?.scrollBy(0, -scrollStep)
                                            else cursorY = (cursorY - cursorStep).coerceAtLeast(10f)
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_DOWN -> {
                                            if (cursorY >= screenHeightPx - 140f) webViewRef?.scrollBy(0, scrollStep)
                                            else cursorY = (cursorY + cursorStep).coerceAtMost(screenHeightPx - 10f)
                                            true
                                        }
                                        KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                            dispatchClickAtCursor()
                                            true
                                        }
                                        else -> false
                                    }
                                } else false
                            } else false
                        }
                } else {
                    Modifier
                }
            )
    ) {
        val density = LocalDensity.current
        screenWidthPx = with(density) { maxWidth.toPx() }
        screenHeightPx = with(density) { maxHeight.toPx() }

        key(crashCount, normalizedUrl) {
            AndroidView(
                factory = { ctx ->
                    CustomBookmarkWebView(
                        context = ctx.findActivityContext(),
                        initialUrl = normalizedUrl,
                        onBackAtRoot = { onBackToHome() },
                        onToggleMouseMode = {
                            if (!isYouTubePortal) {
                                isMouseModeEnabled = !isMouseModeEnabled
                            }
                        },
                        onRendererCrashed = { crashCount++ },
                        onRequestDownload = { dUrl, ua, disp, mime, ref ->
                            startPortalDownload(dUrl, ua, disp, mime, ref)
                        },
                        onBlobDownloaded = { dataUri, fileName, mime ->
                            startBlobDownload(dataUri, fileName, mime)
                        }
                    ).apply {
                        onLoadingStateChanged = { isLoading = it }
                        onErrorChanged = { loadError = it }
                        if (crashCount > 0) {
                            try { clearCache(false) } catch (_: Exception) {}
                        }
                        loadUrl(normalizedUrl)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webViewRef = webView
                    val directDpadMode = isYouTubePortal || !isMouseModeEnabled
                    webView.isFocusable = directDpadMode
                    webView.isFocusableInTouchMode = directDpadMode
                    if (isYouTubePortal && !webView.hasFocus()) {
                        webView.requestFocus()
                    }
                },
                onRelease = { webView ->
                    if (webViewRef === webView) webViewRef = null
                    safeDestroyWebView(webView)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!isYouTubePortal && isMouseModeEnabled) {
            VirtualMousePointer(
                x = cursorX,
                y = cursorY,
                isClicking = isCursorClicking
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                color = TvAccentGold,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(42.dp)
                    .align(Alignment.Center)
            )
        } else if (loadError != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0B1120).copy(alpha = 0.94f))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = loadError ?: "",
                    color = TvTextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                TvFocusableCard(
                    onClick = { webViewRef?.loadUrl(normalizedUrl) },
                    shape = RoundedCornerShape(12.dp),
                    cardBackgroundColor = TvAccentGold.copy(alpha = 0.18f),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.action_retry),
                        color = TvTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }

    WebPortalDownloadDialog(
        state = downloadState,
        onCancelDownload = {
            downloadJob?.cancel()
            downloadState = WebPortalDownloadState.Idle
        },
        onOpenOrInstall = { completed ->
            WebPortalDownloadManager.openOrInstallCompleted(context, completed)
        },
        onDeleteFile = { completed ->
            val deleted = WebPortalDownloadManager.deleteCompleted(context, completed)
            if (deleted) {
                Toast.makeText(
                    context,
                    context.getString(R.string.mini_downloader_file_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }
            downloadState = WebPortalDownloadState.Idle
        },
        onDismiss = {
            downloadState = WebPortalDownloadState.Idle
        }
    )

    BindWebViewLifecycle(webViewRef)

    DisposableEffect(Unit) {
        onDispose {
            downloadJob?.cancel()
            safeDestroyWebView(webViewRef)
            webViewRef = null
        }
    }
}
