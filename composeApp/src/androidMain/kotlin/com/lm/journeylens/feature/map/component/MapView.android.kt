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
import kotlinx.coroutines.flow.collectLatest

private const val TAG = "AMapView"

/**
 * Android 高德地图实现
 */
@Composable
actual fun MapView(
    memories: List<Memory>,
    onMemoryClick: (List<Memory>) -> Unit,
    modifier: Modifier,
    cameraControl: MapCameraControl?
) {
    val context = LocalContext.current
    
    // 记住 MapView 实例
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }
    
    // 获取 AMap 对象 (异步获取可能还在初始化，但在 Compose 中通常这样直接访问属性不太安全，最好用 callback)
    // 不过高德地图的 map 属性在 onCreate 之后通常可用。稳妥起见我们用 remember
    var aMap: AMap? by remember { mutableStateOf(null) }
    
    // 监听相机控制事件
    LaunchedEffect(cameraControl, aMap) {
        val map = aMap ?: return@LaunchedEffect
        cameraControl?.events?.collectLatest { event ->
            when (event) {
                is MapCameraControl.CameraEvent.MoveToCurrentLocation -> {
                    map.myLocation?.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        // 缩放并移动到当前位置 (缩放级别 15 - 街道级)
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                        )
                    }
                }
            }
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
        factory = { 
            mapView.apply {
                // 初始化 AMap
                if (map != null && aMap == null) {
                    aMap = map
                }
            }
        },
        update = { view ->
            // 确保 aMap 引用是最新的
            if (aMap == null) aMap = view.map
            
            aMap?.let { map ->
                // 设置地图类型为标准地图
                map.mapType = AMap.MAP_TYPE_NORMAL
                
                // 设置 UI 控件
                map.uiSettings.apply {
                    isZoomControlsEnabled = false   // 隐藏默认缩放按钮
                    isCompassEnabled = true        // 显示指南针
                    isScaleControlsEnabled = true  // 显示比例尺
                    isMyLocationButtonEnabled = false  // 隐藏默认定位按钮 (我们将使用自定义按钮)
                }
                
                // 设置定位样式
                val myLocationStyle = MyLocationStyle().apply {
                    // 定位蓝点展现模式 - 连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
                    // 这里我们只需要显示蓝点，控制逻辑由自定义按钮处理
                    myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                    interval(2000) // 定位间隔
                }
                map.myLocationStyle = myLocationStyle
                map.isMyLocationEnabled = true  // 启用定位图层
                
                // 清除旧标记
                map.clear()
                
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
                    
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(title)
                            .snippet(if (count > 1) "点击查看详情" else (firstMemory.note ?: ""))
                    )
                    // 存储该位置的所有记忆（用于后续展示列表）
                    marker?.`object` = memoriesAtLocation
                }
                
                // 设置标记点击事件
                map.setOnMarkerClickListener { marker ->
                    @Suppress("UNCHECKED_CAST")
                    val memoriesAtLocation = marker.`object` as? List<Memory>
                    // 返回所有记忆
                    memoriesAtLocation?.let { onMemoryClick(it) }
                    true // 消费事件
                }
                
                // 首次加载且有数据时，移动相机到数据点
                // 注意：这里需要避免每次重组都移动相机，可能会打断用户操作。
                // 简单的做法是只在第一次非空时移动，或者不自动移动让用户自己探索。
                // 目前逻辑暂且保留，或考虑优化为只在 memories 变化很大时移动。
                // 为防止频繁移动，暂时不做全自动移动，除非完全重置。
                 if (memories.isNotEmpty() && map.cameraPosition.zoom < 5) {
                    val targetPosition = LatLng(memories.first().latitude, memories.first().longitude)
                     map.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition(targetPosition, 10f, 0f, 0f)
                        )
                    )
                }
                
                Log.d(TAG, "Map updated with ${memories.size} memories in ${groupedMemories.size} locations")
            }
        },
        modifier = modifier
    )
}
