package com.lm.journeylens.core.theme

import androidx.compose.ui.graphics.Color

/**
 * JourneyLens 苹果风配色方案
 * 白色主题 + 液态玻璃效果
 */
object JourneyLensColors {
    // 主色调
    val AppleBlue = Color(0xFF007AFF)
    val AppleTeal = Color(0xFF30B0C7)
    val ApplePink = Color(0xFFFF2D55)
    val AppleOrange = Color(0xFFFF9500)
    val AppleGreen = Color(0xFF34C759)
    val ApplePurple = Color(0xFFAF52DE)

    // 背景色
    val Background = Color(0xFFFFFFFF)
    val SurfaceLight = Color(0xFFF2F2F7)

    // 毛玻璃效果
    val GlassBackground = Color(0xB3FFFFFF)  // 70% 白色
    val GlassBackgroundLight = Color(0x80FFFFFF)  // 50% 白色

    // 文字颜色
    val TextPrimary = Color(0xFF1C1C1E)
    val TextSecondary = Color(0xFF8E8E93)
    val TextTertiary = Color(0xFFC7C7CC)

    // 分隔线
    val Separator = Color(0x33000000)  // 20% 黑色

    // 阴影
    val Shadow = Color(0x0A000000)  // 4% 黑色
}
