package com.picdraw.quickbar.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF1B5E8C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCFE5F5),
    onPrimaryContainer = Color(0xFF00344F),
    secondary = Color(0xFF4F616E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41484D),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF95CCF0),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004B6C),
    onPrimaryContainer = Color(0xFFCFE5F5),
    secondary = Color(0xFFB6C9D8),
    surfaceVariant = Color(0xFF41484D),
    onSurfaceVariant = Color(0xFFC1C7CE),
)

@Composable
fun QuickBarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
