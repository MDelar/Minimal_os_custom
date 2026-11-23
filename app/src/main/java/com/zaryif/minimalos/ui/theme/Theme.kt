package com.zaryif.minimalos.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MinimalOSTheme(
    themeId: String = "AMOLED_RED",
    content: @Composable () -> Unit
) {
    // Update active theme state
    currentActiveTheme = MinimalThemeRegistry.getTheme(themeId)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Match the window background to the theme so app-switch animations
            // don't show a black frame behind non-black themes. The app is edge to
            // edge, so this also shows behind the transparent system bars.
            window.setBackgroundDrawable(ColorDrawable(currentActiveTheme.background.toArgb()))
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    val colorScheme = darkColorScheme(
        primary = currentActiveTheme.accent,
        onPrimary = currentActiveTheme.textPrimary,
        secondary = currentActiveTheme.border,
        onSecondary = currentActiveTheme.textPrimary,
        tertiary = currentActiveTheme.textSecondary,
        background = currentActiveTheme.background,
        onBackground = currentActiveTheme.textPrimary,
        surface = currentActiveTheme.card,
        onSurface = currentActiveTheme.textPrimary,
        surfaceVariant = currentActiveTheme.border,
        onSurfaceVariant = currentActiveTheme.textSecondary,
        outline = currentActiveTheme.border,
        error = currentActiveTheme.accent,
        onError = currentActiveTheme.textPrimary,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MinimalTypography,
        content = content
    )
}
