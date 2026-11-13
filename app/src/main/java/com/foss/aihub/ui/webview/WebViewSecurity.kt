package com.foss.aihub.ui.webview

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.foss.aihub.utils.SettingsManager

object WebViewSecurity {
    private lateinit var settings: SettingsManager
    private lateinit var domains: HashSet<String>

    private val ALLOWED_PREFIXES = arrayOf(
        "blob:",
        "about:blank",
        "about:srcdoc",
        "data:",
        "file:",
        "content:"
    )

    private val AUTH_AND_CHALLENGE_HOSTS = listOf(
        "challenges.cloudflare.com",
        "cloudflare.com",
        "accounts.google.com",
        "google.com",
        "gstatic.com",
        "googleapis.com",
        "auth0.com",
        "openai.com",
        "chatgpt.com",
        "claude.ai",
        "anthropic.com",
        "x.ai",
        "grok.com",
        "perplexity.ai",
        "deepseek.com",
        "hcaptcha.com",
        "arkoselabs.com"
    )

    fun init(context: Context, domains: HashSet<String>) {
        if (!::settings.isInitialized) {
            settings = SettingsManager(context.applicationContext)
            this.domains = domains
        }
    }

    fun allowConnectivityForService(url: String): Boolean {
        if (!settings.settingsFlow.value.blockAdsAndTrackers) return true
        if (url.isBlank()) return false

        if (ALLOWED_PREFIXES.any(url::startsWith)) {
            return true
        }

        val uri = url.toUri()
        val scheme = uri.scheme ?: ""
        val host = uri.host ?: ""

        if (host.isEmpty()) return false

        if (scheme != "https") return false

        val lowerHost = host.lowercase()
        if (AUTH_AND_CHALLENGE_HOSTS.any { allowedHost ->
                lowerHost == allowedHost || lowerHost.endsWith(".$allowedHost")
            }) {
            return true
        }

        if (domains.contains(host)) {
            return false
        }

        return true
    }
}
