package com.lm.journeylens.feature.map.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.domain.model.Memory
import com.lm.journeylens.core.theme.JourneyLensColors

import com.lm.journeylens.feature.map.MapCameraPosition

/**
 * iOS 地图实现 - 占位符
 * TODO: 使用 MapKit UIKitView 实现
 */
@Composable
actual fun MapView(
    memories: List<Memory>,
    onMemoryClick: (List<Memory>) -> Unit,
    modifier: Modifier,
    cameraControl: MapCameraControl?,
    cameraPosition: MapCameraPosition?,
    onCameraPositionChange: ((MapCameraPosition) -> Unit)?
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
