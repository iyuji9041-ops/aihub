package com.foss.aihub.ui.components

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.foss.aihub.utils.SettingsManager

val AmoledColorScheme = darkColorScheme(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceContainer = Color(0xFF111215),
    surfaceContainerHigh = Color(0xFF18191E),
    surfaceContainerHighest = Color(0xFF22242B),
    surfaceContainerLow = Color(0xFF0A0A0C),
    onBackground = Color(0xFFE4E4E6),
    onSurface = Color(0xFFE4E4E6),
    onSurfaceVariant = Color(0xFF9E9EA4),
    outline = Color(0xFF383A42),
    outlineVariant = Color(0xFF23252C),
    primary = Color(0xFF9DD2FF),
    onPrimary = Color(0xFF003355),
    primaryContainer = Color(0xFF004B75),
    onPrimaryContainer = Color(0xFFCCE5FF)
)

val ObsidianColorScheme = darkColorScheme(
    background = Color(0xFF0B0D13),
    surface = Color(0xFF0B0D13),
    surfaceContainer = Color(0xFF151924),
    surfaceContainerHigh = Color(0xFF1D2230),
    surfaceContainerHighest = Color(0xFF262C3E),
    surfaceContainerLow = Color(0xFF0E1119),
    onBackground = Color(0xFFE2E4EC),
    onSurface = Color(0xFFE2E4EC),
    onSurfaceVariant = Color(0xFFA0A6B5),
    outline = Color(0xFF3B4356),
    outlineVariant = Color(0xFF232A3B),
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF083060),
    primaryContainer = Color(0xFF234476),
    onPrimaryContainer = Color(0xFFD3E3FD)
)

val ClaudeColorScheme = darkColorScheme(
    background = Color(0xFF191816),
    surface = Color(0xFF191816),
    surfaceContainer = Color(0xFF24221F),
    surfaceContainerHigh = Color(0xFF2C2925),
    surfaceContainerHighest = Color(0xFF37332E),
    surfaceContainerLow = Color(0xFF1E1C1A),
    onBackground = Color(0xFFECE5DD),
    onSurface = Color(0xFFECE5DD),
    onSurfaceVariant = Color(0xFFABA196),
    outline = Color(0xFF4A443D),
    outlineVariant = Color(0xFF332E29),
    primary = Color(0xFFE5A97C),
    onPrimary = Color(0xFF462105),
    primaryContainer = Color(0xFF62320E),
    onPrimaryContainer = Color(0xFFFFDBC8)
)

val ChatGptColorScheme = darkColorScheme(
    background = Color(0xFF171717),
    surface = Color(0xFF171717),
    surfaceContainer = Color(0xFF212121),
    surfaceContainerHigh = Color(0xFF2A2A2A),
    surfaceContainerHighest = Color(0xFF363636),
    surfaceContainerLow = Color(0xFF1C1C1C),
    onBackground = Color(0xFFECECEC),
    onSurface = Color(0xFFECECEC),
    onSurfaceVariant = Color(0xFFA3A3A3),
    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF2E2E2E),
    primary = Color(0xFF74D4B4),
    onPrimary = Color(0xFF003829),
    primaryContainer = Color(0xFF00513C),
    onPrimaryContainer = Color(0xFF90F1CF)
)

val EmeraldColorScheme = darkColorScheme(
    background = Color(0xFF081412),
    surface = Color(0xFF081412),
    surfaceContainer = Color(0xFF102420),
    surfaceContainerHigh = Color(0xFF16302B),
    surfaceContainerHighest = Color(0xFF1E4039),
    surfaceContainerLow = Color(0xFF0C1C19),
    onBackground = Color(0xFFE0EFEA),
    onSurface = Color(0xFFE0EFEA),
    onSurfaceVariant = Color(0xFF94ABA4),
    outline = Color(0xFF35524B),
    outlineVariant = Color(0xFF1E3631),
    primary = Color(0xFF50E389),
    onPrimary = Color(0xFF00391A),
    primaryContainer = Color(0xFF005228),
    onPrimaryContainer = Color(0xFF6FFFB4)
)

fun Modifier.frostedBorder(
    shape: Shape = RoundedCornerShape(22.dp),
    borderColor: Color? = null
): Modifier = this.border(
    BorderStroke(0.8.dp, borderColor ?: Color(0xFFFFFFFF).copy(alpha = 0.08f)),
    shape = shape
)

fun Modifier.frostedBorder(
    radius: androidx.compose.ui.unit.Dp,
    borderColor: Color? = null
): Modifier = frostedBorder(RoundedCornerShape(radius), borderColor)

@Composable
fun AiHubTheme(
    context: Context, settingsManager: SettingsManager, content: @Composable () -> Unit
) {
    val settings by settingsManager.settingsFlow.collectAsState()

    val isDark = when (settings.theme) {
        "amoled", "obsidian", "claude", "chatgpt", "emerald", "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = when (settings.theme) {
        "amoled" -> AmoledColorScheme
        "obsidian" -> ObsidianColorScheme
        "claude" -> ClaudeColorScheme
        "chatgpt" -> ChatGptColorScheme
        "emerald" -> EmeraldColorScheme
        "dark" -> darkColorScheme()
        "light" -> lightColorScheme()
        else -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isDark) darkColorScheme() else lightColorScheme()
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(activity.window, view)
            val shouldUseLightIcons = !isDark

            if (controller.isAppearanceLightStatusBars != shouldUseLightIcons) {
                controller.isAppearanceLightStatusBars = shouldUseLightIcons
            }
            if (controller.isAppearanceLightNavigationBars != shouldUseLightIcons) {
                controller.isAppearanceLightNavigationBars = shouldUseLightIcons
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme, content = content
    )
}
