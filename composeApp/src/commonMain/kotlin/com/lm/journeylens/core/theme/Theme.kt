package com.lm.journeylens.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * JourneyLens 液态玻璃主题
 * 苹果风白色调 + 毛玻璃效果
 */

private val LightColorScheme = lightColorScheme(
    primary = JourneyLensColors.AppleBlue,
    onPrimary = JourneyLensColors.Background,
    primaryContainer = JourneyLensColors.AppleBlue.copy(alpha = 0.1f),
    onPrimaryContainer = JourneyLensColors.AppleBlue,

    secondary = JourneyLensColors.AppleTeal,
    onSecondary = JourneyLensColors.Background,
    secondaryContainer = JourneyLensColors.AppleTeal.copy(alpha = 0.1f),
    onSecondaryContainer = JourneyLensColors.AppleTeal,

    tertiary = JourneyLensColors.ApplePink,
    onTertiary = JourneyLensColors.Background,

    background = JourneyLensColors.Background,
    onBackground = JourneyLensColors.TextPrimary,

    surface = JourneyLensColors.SurfaceLight,
    onSurface = JourneyLensColors.TextPrimary,
    surfaceVariant = JourneyLensColors.GlassBackground,
    onSurfaceVariant = JourneyLensColors.TextSecondary,

    outline = JourneyLensColors.Separator,
    outlineVariant = JourneyLensColors.TextTertiary,
)

@Composable
fun JourneyLensTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = JourneyLensTypography,
        content = content
    )
}
