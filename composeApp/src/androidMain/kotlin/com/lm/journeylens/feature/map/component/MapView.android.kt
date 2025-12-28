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
import com.amap.api.maps.model.MyLocationStyle
import com.lm.journeylens.core.database.entity.Memory

private const val TAG = "AMapView"

/**
 * Android 高德地图实现
 */
@Composable
actual fun MapView(
    memories: List<Memory>,
    onMemoryClick: (List<Memory>) -> Unit,
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
                    isZoomControlsEnabled = true   // 显示缩放按钮
                    isCompassEnabled = true        // 显示指南针
                    isScaleControlsEnabled = true  // 显示比例尺
                    isMyLocationButtonEnabled = true  // 显示定位按钮
                }
                
                // 设置定位样式
                val myLocationStyle = MyLocationStyle().apply {
                    // 定位蓝点展现模式 - 只定位一次
                    myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
                }
                aMap.myLocationStyle = myLocationStyle
                aMap.isMyLocationEnabled = true  // 启用定位
                
                // 清除旧标记
                aMap.clear()
                
                // 按位置分组记忆点（处理重叠问题）
                val groupedMemories = memories.groupBy { 
                    // 使用 4 位小数精度分组（约 10米范围）
                    String.format("%.4f,%.4f", it.latitude, it.longitude)
                }
                
                // 添加记忆点标记
                groupedMemories.forEach { (_, memoriesAtLocation) ->
                    val firstMemory = memoriesAtLocation.first()
                    val position = LatLng(firstMemory.latitude, firstMemory.longitude)
                    val count = memoriesAtLocation.size
                    
                    // 标题显示数量（如果有多个）
                    val title = if (count > 1) {
                        "${firstMemory.emoji} 等 $count 条记忆"
                    } else {
                        "${firstMemory.emoji} ${firstMemory.locationName ?: ""}"
                    }
                    
                    val marker = aMap.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(title)
                            .snippet(if (count > 1) "点击查看详情" else (firstMemory.note ?: ""))
                    )
                    // 存储该位置的所有记忆（用于后续展示列表）
                    marker?.`object` = memoriesAtLocation
                }
                
                // 设置标记点击事件
                aMap.setOnMarkerClickListener { marker ->
                    @Suppress("UNCHECKED_CAST")
                    val memoriesAtLocation = marker.`object` as? List<Memory>
                    // 返回所有记忆
                    memoriesAtLocation?.let { onMemoryClick(it) }
                    true
                }
                
                // 移动相机到第一个记忆点或默认位置
                if (memories.isNotEmpty()) {
                    val targetPosition = LatLng(memories.first().latitude, memories.first().longitude)
                     aMap.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition(targetPosition, 12f, 0f, 0f)
                        )
                    )
                }
                
                Log.d(TAG, "Map updated with ${memories.size} memories in ${groupedMemories.size} locations")
            }
        },
        modifier = modifier
    )
}
