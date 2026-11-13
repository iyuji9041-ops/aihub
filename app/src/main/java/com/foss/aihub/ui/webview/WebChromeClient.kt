package com.foss.aihub.ui.webview

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.foss.aihub.MainActivity
import com.foss.aihub.utils.USER_AGENT_FIREFOX_ANDROID
import com.foss.aihub.utils.getCleanUserAgent

open class CustomWebChromeClient(
    private val context: MainActivity,
    private val onProgressUpdate: (Int) -> Unit,
    private val onJsAlertRequest: (String?, JsResult?) -> Unit,
    private val onJsPromptRequest: (String?, JsResult?) -> Unit,
    private val onJsConfirmRequest: (String?, JsResult?) -> Unit,
    private val onJsBeforeUnloadRequest: (String?, JsResult?) -> Unit,
    private val mainWebView: WebView? = null
) : WebChromeClient() {
    private val activeWebViews = mutableListOf<WebView>()

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressUpdate(newProgress)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        context.launchFileChooser(filePathCallback)
        return true
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        val resources = request.resources
        Log.d("AI_HUB", "WebView requesting permission for: ${resources.joinToString()}")
        context.runOnUiThread {
            context.requestWebViewPermissions(request)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateWindow(
        view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?
    ): Boolean {
        Log.d(
            "AI_HUB", "onCreateWindow called - isDialog: $isDialog, isUserGesture: $isUserGesture"
        )

        val transport = resultMsg?.obj as? WebView.WebViewTransport ?: return false

        val dialog = Dialog(context, android.R.style.Theme_DeviceDefault_NoActionBar)
        val popupWebView = WebView(context)

        popupWebView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        updateWebViewSettings(popupWebView, context.settingsManager.settingsFlow.value, reload = false)
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(popupWebView, true)
        }

        popupWebView.webChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(window: WebView?) {
                super.onCloseWindow(window)
                CookieManager.getInstance().flush()
                try {
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e("AI_HUB", "Error dismissing OAuth dialog: ${e.message}")
                }
                window?.destroy()
                activeWebViews.remove(window)
            }
        }

        popupWebView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?, request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: ""
                if (url.contains("accounts.google.com")) {
                    view?.settings?.userAgentString = USER_AGENT_FIREFOX_ANDROID
                } else {
                    view?.settings?.userAgentString = getCleanUserAgent(context, context.settingsManager.settingsFlow.value.desktopView)
                }
                return if (url.startsWith("https://") || url.startsWith("http://")) {
                    false
                } else {
                    super.shouldOverrideUrlLoading(view, request)
                }
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (url?.contains("accounts.google.com") == true) {
                    view?.settings?.userAgentString = USER_AGENT_FIREFOX_ANDROID
                } else if (url != null) {
                    view?.settings?.userAgentString = getCleanUserAgent(context, context.settingsManager.settingsFlow.value.desktopView)
                }
            }
        }

        activeWebViews.add(popupWebView)

        dialog.setContentView(popupWebView)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                if (popupWebView.canGoBack()) {
                    popupWebView.goBack()
                } else {
                    dialog.dismiss()
                }
                true
            } else {
                false
            }
        }
        dialog.setOnDismissListener {
            CookieManager.getInstance().flush()
            if (activeWebViews.contains(popupWebView)) {
                activeWebViews.remove(popupWebView)
                popupWebView.destroy()
            }
        }

        dialog.show()

        transport.webView = popupWebView
        resultMsg.sendToTarget()
        return true
    }

    override fun onCloseWindow(window: WebView?) {
        super.onCloseWindow(window)
        window?.destroy()
        activeWebViews.remove(window)
    }

    override fun onJsAlert(
        view: WebView?, url: String?, message: String?, result: JsResult?
    ): Boolean {
        context.runOnUiThread {
            onJsAlertRequest(message, result)
        }
        return true
    }

    override fun onJsPrompt(
        view: WebView?,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult?
    ): Boolean {
        context.runOnUiThread {
            onJsPromptRequest(message, result)
        }
        return true
    }

    override fun onJsConfirm(
        view: WebView?, url: String?, message: String?, result: JsResult?
    ): Boolean {
        context.runOnUiThread {
            onJsConfirmRequest(message, result)
        }
        return true
    }

    override fun onJsBeforeUnload(
        view: WebView?, url: String?, message: String?, result: JsResult?
    ): Boolean {
        context.runOnUiThread {
            onJsBeforeUnloadRequest(message, result)
        }
        return true
    }
}