package com.example.userapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    onPrimary = Color(0xFF003548),
    primaryContainer = Color(0xFF004D65),
    secondary = Color(0xFF81D4FA),
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF132232),
    surfaceVariant = Color(0xFF1E3448),
    onBackground = Color(0xFFE1F5FE),
    onSurface = Color(0xFFE1F5FE),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF0277BD),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1F5FE),
    secondary = Color(0xFF0288D1),
    background = Color(0xFFF5F9FF),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8F4FD),
    onBackground = Color(0xFF0D1B2A),
    onSurface = Color(0xFF0D1B2A),
)

@Composable
fun UserAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
