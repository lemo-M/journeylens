package com.lm.journeylens.feature.map.component

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.lm.journeylens.core.database.entity.Memory

private const val TAG = "AMapView"

/**
 * Android 高德地图实现
 */
@Composable
actual fun MapView(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    
    // 记住 MapView 实例
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }
    
    // 管理生命周期
    DisposableEffect(mapView) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDestroy()
        }
    }
    
    AndroidView(
        factory = { mapView },
        update = { view ->
            view.map?.let { aMap ->
                // 设置地图类型为标准地图
                aMap.mapType = AMap.MAP_TYPE_NORMAL
                
                // 设置 UI 控件
                aMap.uiSettings.apply {
                    isZoomControlsEnabled = false  // 隐藏缩放按钮
                    isCompassEnabled = false       // 隐藏指南针
                    isScaleControlsEnabled = true  // 显示比例尺
                }
                
                // 清除旧标记
                aMap.clear()
                
                // 添加记忆点标记
                memories.forEach { memory ->
                    val position = LatLng(memory.latitude, memory.longitude)
                    val marker = aMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title("${memory.emoji} ${memory.locationName ?: ""}")
                            .snippet(memory.note ?: "")
                    )
                    marker?.`object` = memory
                }
                
                // 设置标记点击事件
                aMap.setOnMarkerClickListener { marker ->
                    val memory = marker.`object` as? Memory
                    memory?.let { onMemoryClick(it) }
                    true
                }
                
                // 移动相机到第一个记忆点或默认位置
                val targetPosition = memories.firstOrNull()?.let {
                    LatLng(it.latitude, it.longitude)
                } ?: LatLng(35.0, 105.0)  // 默认中国中心
                
                val zoom = if (memories.isNotEmpty()) 12f else 4f
                
                aMap.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(targetPosition, zoom, 0f, 0f)
                    )
                )
                
                Log.d(TAG, "Map updated with ${memories.size} memories")
            }
        },
        modifier = modifier
    )
}
