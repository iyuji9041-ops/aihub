package com.foss.aihub.utils

import android.content.Context
import android.webkit.WebSettings

const val USER_AGENT_DESKTOP =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0.0 Safari/537.36"
const val USER_AGENT_MOBILE =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.7632.121 Mobile Safari/537.36"
const val GITHUB_USER_NAME = "SilentCoderHere"
const val GITHUB_REPO_NAME = "aihub"
const val SUPPORT_EMAIL = "silentcoder@tutamail.com"
const val CLOUD_BASE_URL = "https://silentcoderhere.github.io/aihub-config-data/"
const val AI_SERVICES_FILE = "ais.json"
const val DOMAINS_FILE = "domains.txt"

private const val FALLBACK_MAJOR_VERSION = "131"
private const val FALLBACK_FULL_VERSION = "131.0.6778.135"

fun extractChromiumVersion(context: Context): Pair<String, String> {
    val defaultUa = try {
        WebSettings.getDefaultUserAgent(context)
    } catch (_: Exception) {
        ""
    }
    val match = Regex("Chrome/((\\d+)(?:\\.[0-9]+)+)").find(defaultUa)
    return if (match != null) {
        val fullVersion = match.groupValues[1]
        val majorVersion = match.groupValues[2]
        Pair(majorVersion, fullVersion)
    } else {
        Pair(FALLBACK_MAJOR_VERSION, FALLBACK_FULL_VERSION)
    }
}

fun getCleanUserAgent(context: Context, isDesktop: Boolean): String {
    val (_, fullVersion) = extractChromiumVersion(context)
    if (isDesktop) {
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$fullVersion Safari/537.36"
    }

    val defaultUa = try {
        WebSettings.getDefaultUserAgent(context)
    } catch (_: Exception) {
        ""
    }

    return if (defaultUa.isNotBlank()) {
        defaultUa
            .replace("; wv", "")
            .replace(";wv", "")
            .replace(Regex(";\\s*wv\\b"), "")
            .replace(Regex("\\bwv\\b"), "")
            .replace(Regex("Version/\\d+\\.\\d+\\s*"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    } else {
        "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$fullVersion Mobile Safari/537.36"
    }
}
