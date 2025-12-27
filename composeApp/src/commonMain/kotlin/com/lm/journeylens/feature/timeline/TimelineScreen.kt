package com.lm.journeylens.feature.timeline

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
 * 时间轴页面 - 螺旋时间轴
 * TODO: 实现阿基米德螺旋可视化
 */
@Composable
fun TimelineScreen() {
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
                text = "🌀",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "螺旋时间轴",
                style = MaterialTheme.typography.headlineLarge,
                color = JourneyLensColors.TextPrimary
            )
            Text(
                text = "从中心向外，时间在流淌",
                style = MaterialTheme.typography.bodyMedium,
                color = JourneyLensColors.TextSecondary
            )
        }
    }
}
