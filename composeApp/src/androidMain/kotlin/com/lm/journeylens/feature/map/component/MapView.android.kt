package com.lm.journeylens.feature.map.component

import android.util.Log
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
    
    // 同步初始化 MapLibre（在创建 MapView 之前）
    remember {
        MapLibre.getInstance(context)
        true
    }
    
    // 使用 MapLibre 内置的 Demo 样式（更稳定）
    // 或者使用 OSM Raster 样式
    val styleJson = """
    {
        "version": 8,
        "name": "JourneyLens Light",
        "sources": {
            "osm": {
                "type": "raster",
                "tiles": [
                    "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                ],
                "tileSize": 256,
                "attribution": "© OpenStreetMap contributors"
            }
        },
        "layers": [
            {
                "id": "osm-tiles",
                "type": "raster",
                "source": "osm",
                "minzoom": 0,
                "maxzoom": 19
            }
        ]
    }
    """.trimIndent()
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                getMapAsync { mapLibreMap ->
                    // 使用内联 JSON 样式（避免网络请求）
                    mapLibreMap.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                        Log.d("MapView", "Style loaded successfully")
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
