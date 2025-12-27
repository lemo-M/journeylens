package com.lm.journeylens.feature.map.component

import android.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.lm.journeylens.core.database.entity.Memory
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.CircleManager
import org.maplibre.android.plugins.annotation.CircleOptions

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
    
    // 使用简约白色地图样式 (OpenStreetMap Light)
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
                mapLibreMap.getStyle { style ->
                    // 添加记忆点标记
                    addMemoryMarkers(mapView, style, memories, onMemoryClick)
                }
            }
        },
        modifier = modifier
    )
}

/**
 * 添加记忆点标记
 */
private fun addMemoryMarkers(
    mapView: MapView,
    style: Style,
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit
) {
    // 简化实现：使用基础的 marker
    // 完整实现需要使用 SymbolLayer 或 CircleLayer
    
    // 更新相机位置到第一个记忆点（如果有）
    memories.firstOrNull()?.let { memory ->
        mapView.getMapAsync { map ->
            map.cameraPosition = CameraPosition.Builder()
                .target(LatLng(memory.latitude, memory.longitude))
                .zoom(12.0)
                .build()
        }
    }
}
