package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkGoColorScheme = darkColorScheme(
    primary = DarkGoPrimary,
    onPrimary = DarkGoBackground,
    primaryContainer = DarkGoSurfaceVariant,
    onPrimaryContainer = DarkGoPrimary,
    secondary = DarkGoSecondary,
    onSecondary = DarkGoTextPrimary,
    secondaryContainer = DarkGoSurfaceElevated,
    onSecondaryContainer = DarkGoTextPrimary,
    tertiary = DarkGoTertiary,
    background = DarkGoBackground,
    onBackground = DarkGoTextPrimary,
    surface = DarkGoSurface,
    onSurface = DarkGoTextPrimary,
    surfaceVariant = DarkGoSurfaceVariant,
    onSurfaceVariant = DarkGoTextSecondary,
    outline = DarkGoBorder,
    error = DarkGoError,
    onError = DarkGoBackground
)

@Composable
fun DarkGoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkGoColorScheme,
        typography = Typography,
        content = content
    )
}
