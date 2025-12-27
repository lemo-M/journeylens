package com.lm.journeylens.feature.map.component

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.lm.journeylens.core.database.entity.Memory
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView

/**
 * Android MapLibre 地图实现
 */
@Composable
actual fun MapView(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    
    // 初始化 MapLibre
    LaunchedEffect(Unit) {
        MapLibre.getInstance(context)
    }
    
    // 使用简约白色地图样式 (Stadia Maps Alidade Smooth - 免费)
    val styleUrl = "https://tiles.stadiamaps.com/styles/alidade_smooth.json"
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                getMapAsync { mapLibreMap ->
                    // 设置地图样式
                    mapLibreMap.setStyle(styleUrl) { style ->
                        // 设置初始相机位置（中国中心）
                        mapLibreMap.cameraPosition = CameraPosition.Builder()
                            .target(LatLng(35.0, 105.0))
                            .zoom(4.0)
                            .build()
                    }
                }
            }
        },
        update = { mapView ->
            mapView.getMapAsync { mapLibreMap ->
                // 如果有记忆点，移动相机到第一个
                memories.firstOrNull()?.let { memory ->
                    mapLibreMap.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(memory.latitude, memory.longitude))
                        .zoom(12.0)
                        .build()
                }
            }
        },
        modifier = modifier
    )
}
