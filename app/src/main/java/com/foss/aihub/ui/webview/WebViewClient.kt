package com.foss.aihub.ui.webview

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.foss.aihub.MainActivity
import com.foss.aihub.models.AiService
import com.foss.aihub.utils.USER_AGENT_FIREFOX_ANDROID
import com.foss.aihub.utils.getCleanUserAgent
import com.foss.aihub.utils.readAssetsFile
import java.io.ByteArrayInputStream

class CustomWebViewClient(
    val context: MainActivity,
    private val onProgressUpdate: (Int) -> Unit,
    private val onLoadingStateChange: (Boolean) -> Unit,
    private val service: AiService,
    private val onError: (Int, String) -> Unit
) : WebViewClient() {
    private var hasErrorOccurred = false

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        hasErrorOccurred = false
        onLoadingStateChange(true)
        onProgressUpdate(0)
        if (url?.contains("accounts.google.com") == true) {
            view?.settings?.userAgentString = USER_AGENT_FIREFOX_ANDROID
        } else if (url != null) {
            view?.settings?.userAgentString = getCleanUserAgent(context, context.settingsManager.settingsFlow.value.desktopView)
        }
        injectMediaGuard(view)
        Log.d("AI_HUB", "Page started: ${service.name} - $url")
    }

    private fun isAuthOrChallengeUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val uri = try { Uri.parse(url) } catch (_: Exception) { return false }
        val host = uri.host?.lowercase() ?: return false
        val authChallengeDomains = listOf(
            "accounts.google.com",
            "challenges.cloudflare.com",
            "auth0.com",
            "accounts.x.ai"
        )
        return authChallengeDomains.any { domain ->
            host == domain || host.endsWith(".$domain")
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (!hasErrorOccurred) {
            onProgressUpdate(100)
            onLoadingStateChange(false)

            if (view != null && !isAuthOrChallengeUrl(url)) {
                val isDesktop = context.settingsManager.settingsFlow.value.desktopView
                val viewportJs = if (isDesktop) {
                    """
                    (function() {
                        try {
                            let meta = document.querySelector('meta[name="viewport"]');
                            if (!meta) {
                                meta = document.createElement('meta');
                                meta.name = 'viewport';
                                document.head.appendChild(meta);
                            }
                            meta.content = 'width=1280, initial-scale=0.35, minimum-scale=0.25, maximum-scale=5.0, user-scalable=yes';
                            window.dispatchEvent(new Event('resize'));
                        } catch(e) {}
                    })();
                    """.trimIndent()
                } else {
                    """
                    (function() {
                        try {
                            let meta = document.querySelector('meta[name="viewport"]');
                            if (meta) {
                                meta.content = 'width=device-width, initial-scale=1.0';
                                window.dispatchEvent(new Event('resize'));
                            }
                        } catch(e) {}
                    })();
                    """.trimIndent()
                }
                view.evaluateJavascript(viewportJs, null)

                injectBlobInterceptor(view)
                injectShareInterceptor(view)
                injectCustomJs(view)
                injectCustomCss(view)
                injectMediaGuard(view)
            }

            CookieManager.getInstance().flush()

            Log.d("AI_HUB", "Page finished: ${service.name} - $url")
        }
    }

    override fun onReceivedError(
        view: WebView?, request: WebResourceRequest?, error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            if (request.url?.toString() == "about:blank") return
            hasErrorOccurred = true
            onProgressUpdate(0)
            onLoadingStateChange(false)
            val errorCode = error?.errorCode ?: return
            val errorDescription = error.description?.toString() ?: "Unknown error"
            onError(errorCode, errorDescription)
            Log.e("WEBVIEW", "❌ Error loading ${service.name}: $errorCode - $errorDescription")
        }
    }

    override fun onReceivedHttpError(
        view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)
        // Ignoring server error
        if (errorResponse?.statusCode in 500..599) return
        if (request?.isForMainFrame == true) {
            if (request.url?.toString() == "about:blank") return
            hasErrorOccurred = true
            onProgressUpdate(0)
            onLoadingStateChange(false)
            val statusCode = errorResponse?.statusCode ?: return
            onError(errorResponse.statusCode, "HTTP Error $statusCode")
            Log.e("WEBVIEW", "❌ HTTP Error loading ${service.name}: $statusCode")
        }
    }

    override fun shouldInterceptRequest(
        view: WebView?, request: WebResourceRequest?
    ): WebResourceResponse? {
        val url = request?.url?.toString() ?: return null
        if (!WebViewSecurity.allowConnectivityForService(url)) {
            return WebResourceResponse(
                "text/html",
                "UTF-8",
                403,
                "Forbidden",
                emptyMap(),
                ByteArrayInputStream(ByteArray(0))
            )
        }

        try {
            if (request.isForMainFrame == false) {
                val settings = context.settingsManager.settingsFlow.value
                val serviceHost = try { Uri.parse(service.url).host?.lowercase() } catch (_: Exception) { null }

                if (serviceHost != null) {
                    val isAudioMuted = settings.audioMutedHosts.any { h ->
                        val hl = h.trim().lowercase()
                        serviceHost == hl || serviceHost.endsWith(".$hl")
                    }
                    if (isAudioMuted) {
                        val path = request.url?.path?.lowercase() ?: ""
                        val audioExtensions = listOf(".mp3", ".aac", ".ogg", ".oga", ".wav", ".m4a", ".opus", ".flac")
                        if (audioExtensions.any { path.endsWith(it) }) {
                            return WebResourceResponse(
                                "audio/mpeg",
                                "UTF-8",
                                204,
                                "No Content",
                                emptyMap(),
                                ByteArrayInputStream(ByteArray(0))
                            )
                        }
                    }

                    val isVideoBlocked = settings.videoBlockedHosts.any { h ->
                        val hl = h.trim().lowercase()
                        serviceHost == hl || serviceHost.endsWith(".$hl")
                    }
                    if (isVideoBlocked) {
                        val path = request.url?.path?.lowercase() ?: ""
                        val videoExtensions = listOf(".mp4", ".webm", ".mov", ".m3u8", ".mpd", ".m4s")
                        if (videoExtensions.any { path.endsWith(it) }) {
                            return WebResourceResponse(
                                "video/mp4",
                                "UTF-8",
                                204,
                                "No Content",
                                emptyMap(),
                                ByteArrayInputStream(ByteArray(0))
                            )
                        }
                    }
                }

                if (settings.saveDataOnMobile) {
                    val connectivityManager =
                        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                    if (connectivityManager?.isActiveNetworkMetered == true) {
                        val path = request.url?.path?.lowercase() ?: ""
                        val blockedExtensions = listOf(".mp4", ".webm", ".mov", ".m3u8", ".mpd", ".m4s")
                        if (blockedExtensions.any { path.endsWith(it) }) {
                            return WebResourceResponse(
                                "video/mp4",
                                "UTF-8",
                                204,
                                "No Content",
                                emptyMap(),
                                ByteArrayInputStream(ByteArray(0))
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Any error lets the request pass
        }

        return null
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?, request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false
        if (url.contains("accounts.google.com")) {
            view?.settings?.userAgentString = USER_AGENT_FIREFOX_ANDROID
        } else {
            view?.settings?.userAgentString = getCleanUserAgent(context, context.settingsManager.settingsFlow.value.desktopView)
        }
        return !WebViewSecurity.allowConnectivityForService(url)
    }

    private fun injectBlobInterceptor(view: WebView) {
        val script = context.readAssetsFile("blobDownloadInterceptor.txt").trimIndent()
        view.evaluateJavascript(script) { result ->
            Log.d("WebView", "Blob interceptor injection result: $result")
        }
    }

    private fun injectShareInterceptor(view: WebView) {
        val script = context.readAssetsFile("webSharePolyfill.txt").trimIndent()
        view.evaluateJavascript(script) { result ->
            Log.d("WebView", "Share injection result: $result")
        }
    }

    private fun injectCustomJs(view: WebView) {
        val script = context.settingsManager.settingsFlow.value.customJs.trimIndent()
        if (script.isNotEmpty()) {
            view.evaluateJavascript(script) { result ->
                Log.d("WebView", "Custom js injection result: $result")
            }
        }
    }

    private fun injectCustomCss(view: WebView) {
        val css = context.settingsManager.settingsFlow.value.customCss.trimIndent()
        if (css.isNotEmpty()) {
            val escapedCss = css.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")
            val script = context.readAssetsFile("customCss.txt").replace("{{CSS}}", escapedCss)
            view.evaluateJavascript(script, null)
        }
    }

    private fun injectMediaGuard(view: WebView?) {
        if (view == null) return
        try {
            val settings = context.settingsManager.settingsFlow.value
            val serviceHost = try { Uri.parse(service.url).host?.lowercase() } catch (_: Exception) { null }
            val isAudioMuted = serviceHost != null && settings.audioMutedHosts.any { h ->
                val hl = h.trim().lowercase()
                serviceHost == hl || serviceHost.endsWith(".$hl")
            }
            val isVideoBlocked = serviceHost != null && settings.videoBlockedHosts.any { h ->
                val hl = h.trim().lowercase()
                serviceHost == hl || serviceHost.endsWith(".$hl")
            }
            val js = """
                (function(){
                    window.__aihubBlockVideo = $isVideoBlocked;
                    window.__aihubMuteAudio = $isAudioMuted;
                    if(window.__aihubMediaGuard) return;
                    window.__aihubMediaGuard=true;
                    function muteOn(){return window.__aihubMuteAudio===true;}
                    function vidOff(){return window.__aihubBlockVideo===true;}
                    function apply(m){ try{ if(muteOn()){ m.muted=true; m.volume=0; } }catch(e){} }
                    document.addEventListener('play',function(e){ var t=e.target; if(!t||!t.tagName) return;
                      if(t.tagName==='VIDEO' && vidOff()){ t.pause(); return; }
                      apply(t); },true);
                    document.addEventListener('volumechange',function(e){ apply(e.target); },true);
                    var op=HTMLMediaElement.prototype.play;
                    HTMLMediaElement.prototype.play=function(){
                      if(vidOff() && this instanceof HTMLVideoElement) return Promise.reject(new DOMException('Blocked','NotAllowedError'));
                      apply(this); return op.apply(this,arguments); };
                    try{ var AC=window.AudioContext||window.webkitAudioContext; if(AC){ var orr=AC.prototype.resume; AC.prototype.resume=function(){ if(muteOn()) return Promise.resolve(); return orr.apply(this,arguments); }; } }catch(e){}
                    try{ var sp=window.speechSynthesis&&window.speechSynthesis.speak; if(sp){ window.speechSynthesis.speak=function(u){ if(muteOn()) return; return sp.call(window.speechSynthesis,u); }; } }catch(e){}
                })();
            """.trimIndent()
            view.evaluateJavascript(js, null)
        } catch (_: Exception) {}
    }
}