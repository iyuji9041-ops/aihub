package com.foss.aihub.utils

import android.content.Context
import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.head
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object CloudDataHandler {
    private val client = HttpClient(OkHttp) {
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000L
            connectTimeoutMillis = 10_000L
            socketTimeoutMillis = 15_000L
        }
    }

    private const val DEFAULT_AIS_JSON = """
{
  "General": [
    {
      "name": "ChatGPT",
      "website": "https://chatgpt.com",
      "pricing": "Freemium",
      "privacy": "Proprietary",
      "login_required": false,
      "best_for": ["Reasoning", "Writing", "Daily Tasks"]
    },
    {
      "name": "Claude",
      "website": "https://claude.ai",
      "pricing": "Freemium",
      "privacy": "Proprietary",
      "login_required": true,
      "best_for": ["Long context", "Coding", "Writing"]
    },
    {
      "name": "Gemini",
      "website": "https://gemini.google.com",
      "pricing": "Freemium",
      "privacy": "Proprietary",
      "login_required": true,
      "best_for": ["Multimodal", "Google Integration", "Fast search"]
    },
    {
      "name": "Grok",
      "website": "https://x.ai",
      "pricing": "Freemium",
      "privacy": "Proprietary",
      "login_required": true,
      "best_for": ["Real-time X data", "Uncensored humor"]
    },
    {
      "name": "DeepSeek",
      "website": "https://chat.deepseek.com",
      "pricing": "Free",
      "privacy": "Proprietary",
      "login_required": false,
      "best_for": ["R1 Deep Reasoning", "Math", "Coding"]
    },
    {
      "name": "Duck.ai",
      "website": "https://duckduckgo.com/?q=DuckDuckGo+AI+Chat&ia=chat",
      "pricing": "Free",
      "privacy": "Private",
      "login_required": false,
      "best_for": ["Anonymous", "No Login", "Privacy"]
    }
  ],
  "Research": [
    {
      "name": "Perplexity",
      "website": "https://www.perplexity.ai",
      "pricing": "Freemium",
      "privacy": "Proprietary",
      "login_required": false,
      "best_for": ["Cited Web Search", "Research", "Synthesis"]
    },
    {
      "name": "Qwen",
      "website": "https://chat.qwenlm.ai",
      "pricing": "Free",
      "privacy": "Proprietary",
      "login_required": false,
      "best_for": ["Open Weights", "Multilingual", "Math"]
    }
  ],
  "Coding & Dev": [
    {
      "name": "Google AI Studio",
      "website": "https://aistudio.google.com",
      "pricing": "Free",
      "privacy": "Developer",
      "login_required": true,
      "best_for": ["Gemini 1.5 Pro 2M Tokens", "Prototyping", "System Prompts"]
    }
  ],
  "Audio & Voice": [
    {
      "name": "ElevenLabs",
      "website": "https://elevenlabs.io/app/speech-synthesis/text-to-speech",
      "pricing": "Freemium",
      "privacy": "Proprietary",
      "login_required": false,
      "best_for": ["Realistic Voice Cloning", "TTS", "Speech Synthesis"]
    }
  ]
}
"""

    private const val DEFAULT_DOMAINS_TXT = """chatgpt.com
openai.com
claude.ai
anthropic.com
gemini.google.com
google.com
accounts.google.com
x.ai
grok.com
deepseek.com
perplexity.ai
duckduckgo.com
elevenlabs.io
aistudio.google.com
qwenlm.ai
challenges.cloudflare.com
auth0.com
"""

    suspend fun getEtag(url: String): String? {
        return try {
            val response = client.head(url)
            response.headers["ETag"]
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getResponse(url: String, etag: String?): Pair<String, String?>? {
        val newEtag = getEtag(url)
        if (etag != null && newEtag == etag) {
            return null
        }

        val response = client.get(url)

        return when (response.status.value) {
            200 -> {
                val body = response.body<String>()
                body to newEtag
            }

            else -> throw ResponseException(response, "HTTP ${response.status}")
        }
    }

    fun ensureFallbackData(context: Context) {
        val aisFile = File(context.filesDir, AI_SERVICES_FILE)
        if (!aisFile.exists() || aisFile.length() == 0L) {
            try {
                aisFile.writeText(DEFAULT_AIS_JSON.trim())
            } catch (e: Exception) {
                Log.e("CloudDataHandler", "Failed to write fallback ais.json", e)
            }
        }

        val domainsFile = File(context.filesDir, DOMAINS_FILE)
        if (!domainsFile.exists() || domainsFile.length() == 0L) {
            try {
                domainsFile.writeText(DEFAULT_DOMAINS_TXT.trim())
            } catch (e: Exception) {
                Log.e("CloudDataHandler", "Failed to write fallback domains.txt", e)
            }
        }
    }

    suspend fun updateDomains(context: Context): Boolean {
        return try {
            val settingsManager = SettingsManager(context)
            val etag = updateData(context, DOMAINS_FILE, settingsManager.getDomainsEtag())
            if (etag != null) {
                settingsManager.saveDomainsEtag(etag)
                true
            } else {
                ensureFallbackData(context)
                false
            }
        } catch (e: Exception) {
            Log.w("CloudDataHandler", "Remote domains update failed, using fallback: ${e.message}")
            ensureFallbackData(context)
            false
        }
    }

    suspend fun updateAiServices(context: Context): Boolean {
        return try {
            val settingsManager = SettingsManager(context)
            val etag = updateData(context, AI_SERVICES_FILE, settingsManager.getAiServicesEtag())
            if (etag != null) {
                settingsManager.saveAiServicesEtag(etag)
                true
            } else {
                ensureFallbackData(context)
                false
            }
        } catch (e: Exception) {
            Log.w("CloudDataHandler", "Remote AI services update failed, using fallback: ${e.message}")
            ensureFallbackData(context)
            false
        }
    }

    suspend fun updateData(context: Context, fileName: String, etag: String?): String? {
        return withContext(Dispatchers.IO) {
            try {
                val response = getResponse(url = CLOUD_BASE_URL + fileName, etag = etag)
                if (response != null) {
                    val (body, newEtag) = response
                    context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                        outputStream.write(body.toByteArray())
                    }
                    return@withContext newEtag
                }
            } catch (e: Exception) {
                Log.w("CloudDataHandler", "Error fetching $fileName from cloud: ${e.message}")
            }
            return@withContext null
        }
    }
}
