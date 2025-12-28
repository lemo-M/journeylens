package com.lm.journeylens.feature.map.component

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
import com.lm.journeylens.core.domain.model.Memory
import com.lm.journeylens.feature.map.MapCameraPosition
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
    cameraControl: MapCameraControl?,
    cameraPosition: MapCameraPosition?,
    onCameraPositionChange: ((MapCameraPosition) -> Unit)?
) {
    val context = LocalContext.current
    
    // 记住 MapView 实例
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }
    
    // 获取 AMap 对象
    var aMap: AMap? by remember { mutableStateOf(null) }
    
    // 标记是否已恢复过状态，防止重组时重复通过 cameraPosition 移动相机
    var isMapRestored by remember { mutableStateOf(false) }
    
    // 监听相机控制事件 (My Location)
    LaunchedEffect(cameraControl, aMap) {
        val map = aMap ?: return@LaunchedEffect
        cameraControl?.events?.collectLatest { event ->
            when (event) {
                is MapCameraControl.CameraEvent.MoveToCurrentLocation -> {
                    map.myLocation?.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        // 使用 500ms 动画时长使移动更流畅
                        map.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                            500, // 动画持续时间 (毫秒)
                            null // 动画回调
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
                // 设置地图类型
                map.mapType = AMap.MAP_TYPE_NORMAL
                
                // 设置相机监听 (用于保存状态)
                map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
                    override fun onCameraChange(position: CameraPosition?) {}
                    
                    override fun onCameraChangeFinish(position: CameraPosition?) {
                        position?.let {
                            onCameraPositionChange?.invoke(
                                MapCameraPosition(
                                    latitude = it.target.latitude,
                                    longitude = it.target.longitude,
                                    zoom = it.zoom
                                )
                            )
                        }
                    }
                })
                
                // 设置 UI 控件
                map.uiSettings.apply {
                    isZoomControlsEnabled = false
                    isCompassEnabled = true
                    isScaleControlsEnabled = true
                    isMyLocationButtonEnabled = false
                }
                
                // 设置定位样式
                val myLocationStyle = MyLocationStyle().apply {
                    myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
                    interval(2000)
                }
                map.myLocationStyle = myLocationStyle
                map.isMyLocationEnabled = true
                
                // 恢复之前的相机位置 (仅一次)
                if (!isMapRestored && cameraPosition != null) {
                    val target = LatLng(cameraPosition.latitude, cameraPosition.longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition(target, cameraPosition.zoom, 0f, 0f)
                        )
                    )
                    isMapRestored = true
                }
                
                // 清除旧标记
                map.clear()
                
                // 按位置分组记忆点
                val groupedMemories = memories.groupBy { 
                    String.format("%.4f,%.4f", it.latitude, it.longitude)
                }
                
                // 添加记忆点标记
                groupedMemories.forEach { (_, memoriesAtLocation) ->
                    val firstMemory = memoriesAtLocation.first()
                    val position = LatLng(firstMemory.latitude, firstMemory.longitude)
                    val count = memoriesAtLocation.size
                    
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
                    marker?.`object` = memoriesAtLocation
                }
                
                // 设置标记点击事件
                map.setOnMarkerClickListener { marker ->
                    @Suppress("UNCHECKED_CAST")
                    val memoriesAtLocation = marker.`object` as? List<Memory>
                    memoriesAtLocation?.let { onMemoryClick(it) }
                    true
                }
                
                // 自动聚焦逻辑：如果还没恢复过状态，且有记忆点
                if (!isMapRestored && cameraPosition == null && memories.isNotEmpty() && map.cameraPosition.zoom < 5) {
                    val targetPosition = LatLng(memories.first().latitude, memories.first().longitude)
                     map.moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition(targetPosition, 10f, 0f, 0f)
                        )
                    )
                    isMapRestored = true
                }
                
                // Log.d(TAG, "Map updated...")
            }
        },
        modifier = modifier
    )
}
