package com.lm.journeylens.feature.map.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lm.journeylens.core.domain.model.Memory

/**
 * 地图组件 - expect/actual 模式
 * Android 使用 MapLibre
 * iOS 使用 MapKit（待实现）
 */
import com.lm.journeylens.feature.map.MapCameraPosition

@Composable
expect fun MapView(
    memories: List<Memory>,
    onMemoryClick: (List<Memory>) -> Unit,
    modifier: Modifier = Modifier,
    cameraControl: MapCameraControl? = null,
    cameraPosition: MapCameraPosition? = null,
    onCameraPositionChange: ((MapCameraPosition) -> Unit)? = null
)
