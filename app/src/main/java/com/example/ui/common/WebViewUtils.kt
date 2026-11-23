package com.example.ui.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import java.io.File
import java.util.Collections
import java.util.WeakHashMap

const val YOUTUBE_TV_USER_AGENT =
    "Mozilla/5.0 (SMART-TV; Linux; Tizen 6.5) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/5.0 Chrome/85.0.4183.93 TV Safari/537.36"

private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
private val destroyedWebViews: MutableSet<WebView> =
    Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))

fun postOnMain(delayMs: Long = 0L, action: () -> Unit) {
    if (delayMs <= 0L) {
        mainHandler.post(action)
    } else {
        mainHandler.postDelayed(action, delayMs)
    }
}

/**
 * Unwraps localized/configuration ContextWrappers back to the hosting Activity context
 * so WebView receives a valid window-backed context.
 */
fun Context.findActivityContext(): Context {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return this
}

/**
 * Grants safe web permissions while blocking RESOURCE_PROTECTED_MEDIA_ID, RESOURCE_AUDIO_CAPTURE,
 * and RESOURCE_VIDEO_CAPTURE probes that trigger MediaDrm / AudioManagerAndroid device selection
 * errors on Android TV devices and emulators.
 */
fun grantSafeWebPermissions(request: PermissionRequest?) {
    if (request == null) return
    val allowed = request.resources
        .filter {
            it != PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID &&
                it != PermissionRequest.RESOURCE_AUDIO_CAPTURE &&
                it != PermissionRequest.RESOURCE_VIDEO_CAPTURE
        }
        .toTypedArray()
    try {
        if (allowed.isNotEmpty()) {
            request.grant(allowed)
        } else {
            request.deny()
        }
    } catch (_: Exception) {}
}

private val webViewCacheLock = Any()
@Volatile
private var webViewCachePrepared = false

/**
 * Validates cacheDir and cleans up any broken non-directory entries along Chromium's cache path
 * strictly before the first WebView initializes so SimpleBackendImpl never races or hits EACCES.
 */
fun ensureWebViewCacheDirs(context: Context) {
    if (webViewCachePrepared) return
    synchronized(webViewCacheLock) {
        if (webViewCachePrepared) return
        try {
            val cacheRoot = context.cacheDir ?: return
            if (!cacheRoot.exists()) {
                cacheRoot.mkdirs()
            }
            cacheRoot.setReadable(true, true)
            cacheRoot.setWritable(true, true)
            cacheRoot.setExecutable(true, true)

            val webViewRoot = File(cacheRoot, "WebView")
            if (webViewRoot.exists() && !webViewRoot.isDirectory) {
                webViewRoot.delete()
            }
            // If an interrupted run left an unreadable/unwritable Code Cache leaf, reset it cleanly before Chromium starts
            val codeCacheJs = File(cacheRoot, "WebView/Default/HTTP Cache/Code Cache/js")
            if (codeCacheJs.exists() && (!codeCacheJs.isDirectory || !codeCacheJs.canWrite() || !codeCacheJs.canRead())) {
                codeCacheJs.deleteRecursively()
            }
            val codeCacheWasm = File(cacheRoot, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (codeCacheWasm.exists() && (!codeCacheWasm.isDirectory || !codeCacheWasm.canWrite() || !codeCacheWasm.canRead())) {
                codeCacheWasm.deleteRecursively()
            }
            webViewCachePrepared = true
        } catch (_: Exception) {}
    }
}

fun isWebViewDestroyed(webView: WebView?): Boolean =
    webView == null || destroyedWebViews.contains(webView)

/**
 * Checks whether the underlying Android kernel exposes a DRM/Mesa GPU rendernode
 * (`/dev/dri/renderD128` or `/dev/dri/card0`). Cloud emulators and certain TV containers
 * without a DRM render node log `E/platform: Failed to open rendernode: No such file or directory`
 * whenever Chromium or HWUI tries to allocate a hardware-accelerated GL/WebGL render node.
 */
val hasHardwareRenderNode: Boolean by lazy {
    try {
        File("/dev/dri/renderD128").exists() || File("/dev/dri/card0").exists()
    } catch (_: Exception) {
        false
    }
}

/**
 * Protects active WebView renderers from OS LMK kills (code -1) and GPU rendernode crashes
 * on emulators/TV boxes without /dev/dri/renderD128 or hardware MediaDrm factories.
 */
fun hardenWebView(webView: WebView) {
    val app = webView.context.applicationContext as? com.example.AuraTvApp
    if (app != null) {
        // Free non-essential launcher image cache bitmaps before Chromium allocates renderer RAM
        app.trimNonEssentialMemoryForWebView()
        // Defer keep-alive anchor creation past initial SurfaceSyncGroup layout so it never delays
        // first-frame rendering, while still keeping RenderProcessHost refCount >= 1 on all devices.
        postOnMain(1500L) {
            try {
                app.ensureWebViewKeepAliveAnchor()
            } catch (_: Exception) {}
        }
    }
    try {
        webView.setLayerType(View.LAYER_TYPE_NONE, null)
    } catch (_: Exception) {}
    try {
        // Ensure prior global pauseTimers() calls from another lifecycle cannot leave new WebViews frozen
        webView.resumeTimers()
        webView.onResume()
    } catch (_: Exception) {}
    try {
        webView.setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)
    } catch (_: Exception) {}
    try {
        webView.settings.safeBrowsingEnabled = false
    } catch (_: Exception) {}
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            webView.webViewRenderProcessClient = object : android.webkit.WebViewRenderProcessClient() {
                override fun onRenderProcessUnresponsive(
                    view: WebView,
                    renderer: android.webkit.WebViewRenderProcess?
                ) {
                    // Allow renderer a grace window to finish heavy JS/video work instead of failing
                }

                override fun onRenderProcessResponsive(
                    view: WebView,
                    renderer: android.webkit.WebViewRenderProcess?
                ) {
                    // Renderer recovered normally
                }
            }
        } catch (_: Exception) {}
    }
    try {
        webView.settings.offscreenPreRaster = false
    } catch (_: Exception) {}
    try {
        webView.settings.setGeolocationEnabled(false)
    } catch (_: Exception) {}
    try {
        // Inject stability shims at document-start BEFORE YouTube TV / SPA <head> scripts probe
        // WebGL, MediaDrm, ServiceWorker, or high-RAM VP9/AV1 1080p software codecs.
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            WebViewCompat.addDocumentStartJavaScript(
                webView,
                buildWebViewStabilityShimScript(disableWebGl = false),
                setOf("*")
            )
        }
    } catch (_: Exception) {}
}

/**
 * Pre-seeds YouTube's SOCS consent cookie before loading any YouTube URL so EU/UK regions
 * do not block WebView playback with the "Before you continue to YouTube" GDPR modal.
 */
fun ensureYouTubeConsentCookies(webView: WebView) {
    try {
        val cookieManager = android.webkit.CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        val socsCookie = "SOCS=CAESEwgDEgk0ODE3Nzk3MjQaAmVuIAEaBgiA_LyaBg; Path=/; Secure; SameSite=Lax"
        val consentCookie = "CONSENT=YES+cb.20210328-17-p0.en+FX+999; Path=/; Secure; SameSite=Lax"
        val domains = listOf(
            "https://www.youtube.com",
            "https://m.youtube.com",
            "https://www.youtube-nocookie.com",
            "https://consent.youtube.com",
            "https://www.google.com"
        )
        for (domain in domains) {
            cookieManager.setCookie(domain, socsCookie)
            cookieManager.setCookie(domain, consentCookie)
        }
        cookieManager.flush()
    } catch (_: Exception) {}
}

/**
 * Injects a defensive JS shim into WebView pages to:
 * 1. Stub navigator.requestMediaKeySystemAccess so Chromium never invokes native MediaDrm / DrmHalHidl.
 * 2. Stub navigator.serviceWorker.register so sites do not spawn background ServiceWorker processes.
 * 3. Filter out unsupported AV1 / HEVC / 10-bit HDR video codecs in MediaSource & mediaCapabilities
 *    so YouTube TV always selects standard H.264 (avc1) with full video + audio.
 * 4. Disable WebGL/WebGPU when the device lacks a hardware DRM rendernode (/dev/dri/renderD128),
 *    or force low-power WebGL attributes on physical hardware.
 */
fun buildWebViewStabilityShimScript(disableWebGl: Boolean = false): String {
    val shouldDisableWebGl = disableWebGl || !hasHardwareRenderNode
    val webGlPatch = if (shouldDisableWebGl) {
        """
        if (typeof HTMLCanvasElement !== 'undefined') {
            var origGetCtx = HTMLCanvasElement.prototype.getContext;
            HTMLCanvasElement.prototype.getContext = function(type, attrs) {
                if (type === 'webgl' || type === 'experimental-webgl' || type === 'webgl2' || type === 'webgpu') {
                    return null;
                }
                return origGetCtx.call(this, type, attrs);
            };
        }
        try {
            if (typeof window.OffscreenCanvas !== 'undefined') {
                var origOffCtx = window.OffscreenCanvas.prototype.getContext;
                window.OffscreenCanvas.prototype.getContext = function(type, attrs) {
                    if (type === 'webgl' || type === 'experimental-webgl' || type === 'webgl2' || type === 'webgpu') {
                        return null;
                    }
                    return origOffCtx.call(this, type, attrs);
                };
            }
        } catch (e) {}
        """.trimIndent()
    } else {
        """
        if (typeof HTMLCanvasElement !== 'undefined') {
            var origGetCtx = HTMLCanvasElement.prototype.getContext;
            HTMLCanvasElement.prototype.getContext = function(type, attrs) {
                if (type === 'webgl' || type === 'experimental-webgl' || type === 'webgl2') {
                    attrs = attrs || {};
                    attrs.failIfMajorPerformanceCaveat = false;
                    attrs.antialias = false;
                    attrs.powerPreference = 'low-power';
                    attrs.preserveDrawingBuffer = false;
                    attrs.desynchronized = false;
                }
                return origGetCtx.call(this, type, attrs);
            };
        }
        """.trimIndent()
    }

    val codecAndMediaPatch = if (!hasHardwareRenderNode) {
        """
            function isUnsupportedVideoCodec(typeStr) {
                if (!typeStr || typeof typeStr !== 'string') return false;
                var lower = typeStr.toLowerCase();
                if (lower.indexOf('av01') !== -1 ||
                    lower.indexOf('hev1') !== -1 ||
                    lower.indexOf('hvc1') !== -1 ||
                    lower.indexOf('dvh1') !== -1 ||
                    lower.indexOf('dvhe') !== -1 ||
                    lower.indexOf('vp09') !== -1 ||
                    lower.indexOf('vp9') !== -1 ||
                    lower.indexOf('vp08') !== -1 ||
                    lower.indexOf('vp8') !== -1) {
                    return true;
                }
                var wMatch = lower.match(/width=(\d+)/);
                if (wMatch && parseInt(wMatch[1], 10) > 1280) return true;
                var hMatch = lower.match(/height=(\d+)/);
                if (hMatch && parseInt(hMatch[1], 10) > 720) return true;
                return false;
            }
            try {
                if (typeof HTMLMediaElement !== 'undefined' && HTMLMediaElement.prototype && typeof HTMLMediaElement.prototype.canPlayType === 'function') {
                    var origCanPlayType = HTMLMediaElement.prototype.canPlayType;
                    HTMLMediaElement.prototype.canPlayType = function(type) {
                        if (isUnsupportedVideoCodec(type)) return '';
                        return origCanPlayType.call(this, type);
                    };
                }
            } catch (e) {}
            try {
                if (typeof window.MediaSource !== 'undefined' && typeof window.MediaSource.isTypeSupported === 'function') {
                    var origIsTypeSupported = window.MediaSource.isTypeSupported.bind(window.MediaSource);
                    window.MediaSource.isTypeSupported = function(mimeType) {
                        if (isUnsupportedVideoCodec(mimeType)) return false;
                        return origIsTypeSupported(mimeType);
                    };
                }
            } catch (e) {}
            try {
                if (typeof navigator !== 'undefined' && navigator.mediaCapabilities && typeof navigator.mediaCapabilities.decodingInfo === 'function') {
                    var origDecodingInfo = navigator.mediaCapabilities.decodingInfo.bind(navigator.mediaCapabilities);
                    navigator.mediaCapabilities.decodingInfo = function(config) {
                        try {
                            if (config && config.video) {
                                var v = config.video;
                                if (isUnsupportedVideoCodec(v.contentType) ||
                                    (v.width && v.width > 1280) ||
                                    (v.height && v.height > 720) ||
                                    (v.framerate && v.framerate > 31)) {
                                    return Promise.resolve({ supported: false, smooth: false, powerEfficient: false });
                                }
                            }
                        } catch (innerErr) {}
                        return origDecodingInfo(config);
                    };
                }
            } catch (e) {}
        """.trimIndent()
    } else {
        ""
    }

    return """
        (function() {
            if (window.__auraStabilityShimApplied) return;
            window.__auraStabilityShimApplied = true;
            $codecAndMediaPatch
            try {
                if (typeof navigator !== 'undefined') {
                    try {
                        Object.defineProperty(navigator, 'gpu', {
                            get: function() { return undefined; },
                            configurable: true
                        });
                    } catch (e) {}
                    if (${!hasHardwareRenderNode}) {
                        navigator.requestMediaKeySystemAccess = function() {
                            return Promise.reject(new DOMException('EME disabled on this device', 'NotSupportedError'));
                        };
                    }
                    if (navigator.serviceWorker) {
                        navigator.serviceWorker.register = function() {
                            return Promise.reject(new DOMException('ServiceWorker disabled', 'SecurityError'));
                        };
                    }
                    if (navigator.mediaDevices) {
                        navigator.mediaDevices.getUserMedia = function() {
                            return Promise.reject(new DOMException('Media capture disabled on TV', 'NotAllowedError'));
                        };
                        navigator.mediaDevices.enumerateDevices = function() {
                            return Promise.resolve([]);
                        };
                        if (typeof navigator.mediaDevices.selectAudioOutput === 'function') {
                            navigator.mediaDevices.selectAudioOutput = function() {
                                return Promise.reject(new DOMException('Audio output selection disabled on TV', 'NotSupportedError'));
                            };
                        }
                    }
                }
            } catch (e) {}
            try {
                if (typeof HTMLMediaElement !== 'undefined' && HTMLMediaElement.prototype) {
                    HTMLMediaElement.prototype.setSinkId = function() {
                        return Promise.resolve();
                    };
                }
                if (typeof AudioContext !== 'undefined' && AudioContext.prototype) {
                    AudioContext.prototype.setSinkId = function() {
                        return Promise.resolve();
                    };
                }
            } catch (e) {}
            try {
                $webGlPatch
            } catch (e) {}
        })();
    """.trimIndent()
}

fun injectWebViewStabilityShim(webView: WebView?, disableWebGl: Boolean = false) {
    if (webView == null || isWebViewDestroyed(webView)) return
    val script = buildWebViewStabilityShimScript(disableWebGl = disableWebGl)
    try {
        webView.evaluateJavascript(script, null)
    } catch (_: Exception) {}
}

/**
 * Syncs WebView pause/resume state with the hosting Lifecycle so backgrounded renderers
 * suspend timers and active media codecs cleanly instead of being killed by LMK.
 */
@Composable
fun BindWebViewLifecycle(webView: WebView?) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, webView) {
        if (webView == null) return@DisposableEffect onDispose {}
        var pausedGlobally = false
        val observer = LifecycleEventObserver { _, event ->
            if (isWebViewDestroyed(webView)) return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    pausedGlobally = true
                    try {
                        webView.evaluateJavascript(
                            "try{document.querySelectorAll('video').forEach(function(v){if(!v.paused){v.dataset.auraPaused='1';v.pause();}});}catch(e){}",
                            null
                        )
                    } catch (_: Exception) {}
                    try { webView.onPause() } catch (_: Exception) {}
                    try { webView.pauseTimers() } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_STOP -> {
                    try {
                        webView.evaluateJavascript(
                            "try{document.querySelectorAll('video').forEach(function(v){if(!v.paused){v.dataset.auraPaused='1';v.pause();}});}catch(e){}",
                            null
                        )
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_RESUME -> {
                    pausedGlobally = false
                    try { webView.resumeTimers() } catch (_: Exception) {}
                    try { webView.onResume() } catch (_: Exception) {}
                    try {
                        webView.evaluateJavascript(
                            "try{document.querySelectorAll('video').forEach(function(v){if(v.dataset.auraPaused==='1'){delete v.dataset.auraPaused;v.play().catch(function(){});}});}catch(e){}",
                            null
                        )
                    } catch (_: Exception) {}
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            if (pausedGlobally) {
                try { webView.resumeTimers() } catch (_: Exception) {}
            }
        }
    }
}

private val rendererGoneWebViews: MutableSet<WebView> =
    Collections.synchronizedSet(Collections.newSetFromMap(WeakHashMap()))

/**
 * WebViewClient used for parked/unloading WebViews so even an unexpected LMK kill of a backgrounded
 * parked renderer returns true cleanly instead of throwing an unhandled render process exception.
 */
private class SafeParkedWebViewClient : WebViewClient() {
    override fun onRenderProcessGone(
        view: WebView?,
        detail: android.webkit.RenderProcessGoneDetail?
    ): Boolean {
        if (view != null) {
            safeDestroyWebView(view, rendererGone = true)
        }
        return true
    }
}

fun createSafeParkedWebViewClient(): WebViewClient = SafeParkedWebViewClient()

fun destroyWebViewImmediately(webView: WebView?) {
    if (webView == null) return
    destroyedWebViews.add(webView)
    try {
        (webView.parent as? ViewGroup)?.removeView(webView)
    } catch (_: Exception) {}
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            webView.webViewRenderProcessClient = null
        } catch (_: Exception) {}
    }
    try {
        webView.stopLoading()
    } catch (_: Exception) {}
    try {
        webView.webChromeClient = null
        webView.webViewClient = SafeParkedWebViewClient()
    } catch (_: Exception) {}
    try {
        webView.onPause()
    } catch (_: Exception) {}
    try {
        webView.removeAllViews()
    } catch (_: Exception) {}
    try {
        webView.destroy()
    } catch (_: Exception) {}
}

/**
 * Safely and idempotently tears down a WebView instance.
 * Ensures a lightweight Application-context anchor keeps Chromium's RenderProcessHost alive during
 * the session without holding any static reference to an Activity-backed UI WebView.
 */
fun safeDestroyWebView(webView: WebView?, rendererGone: Boolean = false) {
    if (webView == null) return
    val app = webView.context.applicationContext as? com.example.AuraTvApp

    if (rendererGone) {
        rendererGoneWebViews.add(webView)
        destroyedWebViews.add(webView)
        val anchor = app?.webViewKeepAliveAnchor
        if (anchor != null) {
            app.webViewKeepAliveAnchor = null
            if (anchor !== webView) {
                rendererGoneWebViews.add(anchor)
                destroyedWebViews.add(anchor)
                destroyWebViewImmediately(anchor)
            }
        }
        destroyWebViewImmediately(webView)
        return
    }

    if (!destroyedWebViews.add(webView)) return

    // Keep Chromium RenderProcessHost refCount >= 1 via an Application-scoped blank anchor
    // so destroying this Activity-backed UI WebView never leaks MainActivity or triggers LMK kill -1.
    app?.ensureWebViewKeepAliveAnchor()

    try {
        (webView.parent as? ViewGroup)?.removeView(webView)
    } catch (_: Exception) {}

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        try {
            webView.webViewRenderProcessClient = null
        } catch (_: Exception) {}
    }

    // Release active media decoders, page intervals, and WebGL buffers cleanly before unloading
    try {
        webView.evaluateJavascript(
            """
            (function() {
                try {
                    if (window.__ambientCleanTimer) {
                        clearInterval(window.__ambientCleanTimer);
                        window.__ambientCleanTimer = null;
                    }
                    var media = document.querySelectorAll('video, audio');
                    for (var i = 0; i < media.length; i++) {
                        try {
                            media[i].pause();
                            media[i].removeAttribute('src');
                            media[i].load();
                        } catch (e) {}
                    }
                } catch (e) {}
            })();
            """.trimIndent(),
            null
        )
    } catch (_: Exception) {}

    try {
        webView.stopLoading()
    } catch (_: Exception) {}
    try {
        webView.webChromeClient = null
        webView.webViewClient = SafeParkedWebViewClient()
    } catch (_: Exception) {}
    try {
        webView.loadUrl("about:blank")
    } catch (_: Exception) {}
    try {
        webView.clearHistory()
    } catch (_: Exception) {}

    // Destroy the unloaded UI WebView cleanly once about:blank finishes releasing page resources.
    postOnMain(900L) {
        if (!rendererGoneWebViews.contains(webView)) {
            try { webView.onPause() } catch (_: Exception) {}
            try { webView.removeAllViews() } catch (_: Exception) {}
            try { webView.destroy() } catch (_: Exception) {}
        }
    }
}
