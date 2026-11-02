package com.example.attendance.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = White,
    primaryContainer = NavySecondary,
    onPrimaryContainer = GoldLight,
    secondary = GoldAccent,
    onSecondary = NavyPrimary,
    secondaryContainer = GoldLight,
    onSecondaryContainer = NavyPrimary,
    background = CreamBackground,
    onBackground = InkText,
    surface = PaperSurface,
    onSurface = InkText,
    surfaceVariant = LineBorder,
    onSurfaceVariant = MutedText,
    outline = LineBorder,
    error = StatusRed,
    onError = White
)

private val DarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = NavyPrimary,
    primaryContainer = NavySecondary,
    onPrimaryContainer = GoldLight,
    secondary = GoldLight,
    onSecondary = NavyPrimary,
    background = NavyPrimary,
    onBackground = PaperSurface,
    surface = NavySecondary,
    onSurface = PaperSurface,
    surfaceVariant = LineBorder,
    onSurfaceVariant = GoldLight,
    outline = GoldAccent,
    error = StatusRed,
    onError = White
)

@Composable
fun AttendanceTheme(
    darkTheme: Boolean = false, // Keep default classic corporate look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
