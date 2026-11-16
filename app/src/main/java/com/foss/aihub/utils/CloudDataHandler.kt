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
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

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
  "version": 1,
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
      "website": "https://grok.com",
      "pricing": "Freemium",
      "privacy": "Proprietary",
      "login_required": true,
      "best_for": ["Real-time X data", "Uncensored humor"]
    },
    {
      "name": "Groq",
      "website": "https://chat.groq.com",
      "pricing": "Free",
      "privacy": "Proprietary",
      "login_required": false,
      "best_for": ["Ultra-fast inference", "Llama 3", "Coding"]
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

    private const val DEFAULT_DOMAINS_TXT = """# version: 1
chatgpt.com
openai.com
claude.ai
anthropic.com
gemini.google.com
google.com
accounts.google.com
x.ai
grok.com
groq.com
deepseek.com
perplexity.ai
duckduckgo.com
elevenlabs.io
aistudio.google.com
qwenlm.ai
challenges.cloudflare.com
auth0.com
"""

    fun verifySignature(data: ByteArray, signatureBase64: String): Boolean {
        return try {
            val keyBytes = Base64.getDecoder().decode(CONFIG_PUBLIC_KEY_BASE64.trim())
            val keySpec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("EC")
            val publicKey = keyFactory.generatePublic(keySpec)

            val sigBytes = Base64.getDecoder().decode(signatureBase64.trim())
            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(data)
            verifier.verify(sigBytes)
        } catch (e: Exception) {
            Log.w("CloudDataHandler", "Signature verification failed: ${e.message}")
            false
        }
    }

    private fun extractVersion(fileName: String, content: String): Long? {
        if (fileName.endsWith(".json")) {
            try {
                val obj = org.json.JSONObject(content)
                if (obj.has("version")) return obj.getLong("version")
            } catch (_: Exception) {}
        }
        for (line in content.lineSequence().take(5)) {
            val trimmed = line.trim()
            if (trimmed.startsWith("# version:", ignoreCase = true)) {
                return trimmed.substringAfter(":").trim().toLongOrNull()
            }
            if (trimmed.startsWith("# version=", ignoreCase = true)) {
                return trimmed.substringAfter("=").trim().toLongOrNull()
            }
        }
        return null
    }

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

    suspend fun updateDomains(context: Context, force: Boolean = false): Boolean {
        return try {
            val settingsManager = SettingsManager(context)
            val currentEtag = if (force) null else settingsManager.getDomainsEtag()
            val etag = updateData(context, DOMAINS_FILE, currentEtag)
            if (etag != null) {
                settingsManager.saveDomainsEtag(etag)
                true
            } else {
                ensureFallbackData(context)
                File(context.filesDir, DOMAINS_FILE).exists()
            }
        } catch (e: Exception) {
            Log.w("CloudDataHandler", "Remote domains update failed, using fallback: ${e.message}")
            ensureFallbackData(context)
            false
        }
    }

    suspend fun updateAiServices(context: Context, force: Boolean = false): Boolean {
        return try {
            val settingsManager = SettingsManager(context)
            val currentEtag = if (force) null else settingsManager.getAiServicesEtag()
            val etag = updateData(context, AI_SERVICES_FILE, currentEtag)
            if (etag != null) {
                settingsManager.saveAiServicesEtag(etag)
                true
            } else {
                ensureFallbackData(context)
                File(context.filesDir, AI_SERVICES_FILE).exists()
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
                val fileUrl = CLOUD_BASE_URL + fileName
                val response = getResponse(url = fileUrl, etag = etag) ?: return@withContext null
                val (body, newEtag) = response

                // Fetch signature file "<file>.sig"
                val sigUrl = "$fileUrl.sig"
                val sigResponse = try {
                    val res = client.get(sigUrl)
                    if (res.status.value == 200) res.body<String>() else null
                } catch (_: Exception) {
                    null
                }

                if (sigResponse.isNullOrBlank()) {
                    Log.w("CloudDataHandler", "Missing or empty signature for $fileName")
                    return@withContext null
                }

                // Verify signature
                val dataBytes = body.toByteArray(Charsets.UTF_8)
                if (!verifySignature(dataBytes, sigResponse)) {
                    Log.w("CloudDataHandler", "Signature invalid for $fileName")
                    return@withContext null
                }

                // Verify monotonically increasing version
                val newVersion = extractVersion(fileName, body)
                val pref = context.getSharedPreferences("aihub_config_versions", Context.MODE_PRIVATE)
                val currentVersion = pref.getLong("version_$fileName", 0L)
                if (newVersion != null && newVersion < currentVersion) {
                    Log.w("CloudDataHandler", "Rejecting $fileName: lower version $newVersion < $currentVersion")
                    return@withContext null
                }

                // Write file to internal storage
                context.openFileOutput(fileName, Context.MODE_PRIVATE).use { outputStream ->
                    outputStream.write(dataBytes)
                }

                if (newVersion != null) {
                    pref.edit().putLong("version_$fileName", newVersion).apply()
                }

                return@withContext newEtag
            } catch (e: Exception) {
                Log.w("CloudDataHandler", "Cloud config update failed for $fileName: ${e.message}")
                return@withContext null
            }
        }
    }
}
