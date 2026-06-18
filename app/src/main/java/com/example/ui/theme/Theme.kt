package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightCosmicColorScheme = lightColorScheme(
    primary = NeonViolet,
    secondary = NeonCyan,
    tertiary = HotPink,
    background = DeepSableSpace,
    surface = DarkSurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color(0xFF21005D),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderAmbient,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondary
)

private val DarkCosmicColorScheme = darkColorScheme(
    primary = NeonViolet,
    secondary = NeonCyan,
    tertiary = HotPink,
    background = DeepSableSpace,
    surface = DarkSurfaceCard,
    onPrimary = Color.White,
    onSecondary = Color(0xFF21005D),
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderAmbient,
    surfaceVariant = DarkSurfaceCard,
    onSurfaceVariant = TextSecondary
)

// We enforce the Vibrant Palette theme as the default joyful deck setup.
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to false for the bright Vibrant Palette theme
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve our beautiful custom M3 palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkCosmicColorScheme else LightCosmicColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
