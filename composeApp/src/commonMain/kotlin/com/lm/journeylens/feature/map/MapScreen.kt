package com.lm.journeylens.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.theme.JourneyLensColors

/**
 * 地图页面 - 战争迷雾主视图
 * TODO: 集成 MapLibre 地图
 */
@Composable
fun MapScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JourneyLensColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "🗺️",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "战争迷雾地图",
                style = MaterialTheme.typography.headlineLarge,
                color = JourneyLensColors.TextPrimary
            )
            Text(
                text = "即将点亮你的世界",
                style = MaterialTheme.typography.bodyMedium,
                color = JourneyLensColors.TextSecondary
            )
        }
    }
}
