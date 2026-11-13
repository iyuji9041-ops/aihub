package com.foss.aihub.models

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.foss.aihub.utils.generateAccentColorFromName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class RawAiService(
    val name: String,
    val website: String,
    val pricing: String,
    val privacy: String,
    val login_required: Boolean,
    val best_for: List<String>
)

@Serializable
data class CustomAiService(
    val name: String,
    val url: String,
    val category: String = "Custom"
)

@Serializable
data class PromptItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String
)

data class AiService(
    val name: String,
    val url: String,
    val category: String,
    val pricing: String,
    val privacy: String,
    val loginRequired: Boolean,
    val bestFor: List<String>,
    val accentColor: Color
)


suspend fun loadServices(context: Context): List<AiService> = withContext(Dispatchers.IO) {
    val file = File(context.filesDir, "ais.json")
    val baseServices: List<AiService> = if (file.exists()) {
        try {
            val jsonString = file.readText()
            val rawMap = Json.decodeFromString<Map<String, List<RawAiService>>>(jsonString)
            rawMap.flatMap { (categoryName, rawServices) ->
                rawServices.map { raw ->
                    val resolvedUrl = when {
                        raw.name.equals("Grok", ignoreCase = true) || raw.website.trimEnd('/') == "https://x.ai" || raw.website.trimEnd('/') == "http://x.ai" -> "https://grok.com"
                        raw.name.equals("Groq", ignoreCase = true) || raw.website.contains("groq.com") -> "https://chat.groq.com"
                        else -> raw.website
                    }
                    AiService(
                        name = raw.name,
                        url = resolvedUrl,
                        category = categoryName,
                        pricing = raw.pricing,
                        privacy = raw.privacy,
                        loginRequired = raw.login_required,
                        bestFor = raw.best_for,
                        accentColor = generateAccentColorFromName(raw.name)
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    } else {
        emptyList()
    }

    val customServices = try {
        val settingsManager = com.foss.aihub.utils.SettingsManager(context)
        settingsManager.loadCustomServices().map { custom ->
            val resolvedUrl = when {
                custom.name.equals("Grok", ignoreCase = true) || custom.url.trimEnd('/') == "https://x.ai" -> "https://grok.com"
                custom.name.equals("Groq", ignoreCase = true) -> "https://chat.groq.com"
                else -> custom.url
            }
            AiService(
                name = custom.name,
                url = resolvedUrl,
                category = custom.category.ifBlank { "Custom" },
                pricing = "Custom",
                privacy = "Custom",
                loginRequired = false,
                bestFor = listOf("Custom AI"),
                accentColor = generateAccentColorFromName(custom.name)
            )
        }
    } catch (_: Exception) {
        emptyList()
    }

    (baseServices + customServices).distinctBy { it.name.trim().lowercase() }
}