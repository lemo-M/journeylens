package com.lm.journeylens.feature.map.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.theme.JourneyLensColors

/**
 * iOS 地图实现 - 占位符
 * TODO: 使用 MapKit UIKitView 实现
 */
@Composable
actual fun MapView(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit,
    modifier: Modifier
) {
    // iOS 暂时显示占位符
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JourneyLensColors.SurfaceLight),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🗺️",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "iOS 地图即将推出",
                style = MaterialTheme.typography.bodyMedium,
                color = JourneyLensColors.TextSecondary
            )
        }
    }
}
