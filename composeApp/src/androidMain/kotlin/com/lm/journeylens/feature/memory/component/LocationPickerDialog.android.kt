package com.lm.journeylens.feature.memory.component

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.lm.journeylens.core.theme.JourneyLensColors

private const val TAG = "LocationPicker"

/**
 * Android 位置选择对话框
 * 全屏高德地图，点击选择位置
 */
@Composable
actual fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit
) {
    val context = LocalContext.current
    
    // 选中的位置
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    
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
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JourneyLensColors.Background)
        ) {
            // 地图
            AndroidView(
                factory = { mapView },
                update = { view ->
                    view.map?.let { aMap ->
                        // 设置地图类型
                        aMap.mapType = AMap.MAP_TYPE_NORMAL
                        
                        // UI 设置
                        aMap.uiSettings.apply {
                            isZoomControlsEnabled = true
                            isCompassEnabled = true
                            isScaleControlsEnabled = true
                        }
                        
                        // 默认位置（中国中心）
                        aMap.moveCamera(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition(LatLng(35.0, 105.0), 4f, 0f, 0f)
                            )
                        )
                        
                        // 点击地图选择位置
                        aMap.setOnMapClickListener { latLng ->
                            Log.d(TAG, "Map clicked: ${latLng.latitude}, ${latLng.longitude}")
                            selectedLocation = latLng
                            
                            // 清除旧标记，添加新标记
                            aMap.clear()
                            aMap.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("选中位置")
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // 顶部栏
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = JourneyLensColors.GlassBackground,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "取消",
                            tint = JourneyLensColors.TextPrimary
                        )
                    }
                    
                    Text(
                        text = "点击地图选择位置",
                        style = MaterialTheme.typography.titleMedium,
                        color = JourneyLensColors.TextPrimary
                    )
                    
                    IconButton(
                        onClick = {
                            selectedLocation?.let {
                                onLocationSelected(it.latitude, it.longitude)
                                onDismiss()
                            }
                        },
                        enabled = selectedLocation != null
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "确认",
                            tint = if (selectedLocation != null) 
                                JourneyLensColors.AppleBlue 
                            else 
                                JourneyLensColors.TextTertiary
                        )
                    }
                }
            }
            
            // 底部选中位置信息
            selectedLocation?.let { location ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(12.dp),
                    color = JourneyLensColors.GlassBackground,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = JourneyLensColors.AppleBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "已选择位置",
                                style = MaterialTheme.typography.labelMedium,
                                color = JourneyLensColors.TextSecondary
                            )
                            Text(
                                text = "%.6f, %.6f".format(location.latitude, location.longitude),
                                style = MaterialTheme.typography.bodyMedium,
                                color = JourneyLensColors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
