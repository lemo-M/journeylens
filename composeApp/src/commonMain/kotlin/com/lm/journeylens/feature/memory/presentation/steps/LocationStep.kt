package com.lm.journeylens.feature.memory.presentation.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.component.LocationPickerDialog
import com.lm.journeylens.core.service.LocationService
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * 步骤 1: 选择位置
 */
@Composable
fun LocationStep(
    onUseCurrentLocation: (Double, Double, String?) -> Unit,
    onSelectFromMap: (Double, Double) -> Unit
) {
    var showMapPicker by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    
    val locationService: LocationService = koinInject()
    val scope = rememberCoroutineScope()
    
    if (showMapPicker) {
        LocationPickerDialog(
            onDismiss = { showMapPicker = false },
            onLocationSelected = { lat, lng ->
                onSelectFromMap(lat, lng)
                showMapPicker = false
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = JourneyLensColors.AppleBlue
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "选择位置",
            style = MaterialTheme.typography.headlineMedium,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "这些照片是在哪里拍的？",
            style = MaterialTheme.typography.bodyMedium,
            color = JourneyLensColors.TextSecondary
        )
        
        // 错误提示
        locationError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = JourneyLensColors.ApplePink
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 使用当前定位
        Button(
            onClick = { 
                isGettingLocation = true
                locationError = null
                scope.launch {
                    val result = locationService.getCurrentLocation()
                    isGettingLocation = false
                    if (result != null) {
                        onUseCurrentLocation(result.latitude, result.longitude, result.address)
                    } else {
                        locationError = "定位失败，请检查定位权限"
                    }
                }
            },
            enabled = !isGettingLocation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = JourneyLensColors.AppleBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isGettingLocation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("定位中...")
            } else {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("使用当前定位")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 地图选点
        OutlinedButton(
            onClick = { showMapPicker = true },
            enabled = !isGettingLocation,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = JourneyLensColors.AppleBlue
            )
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("在地图上选点")
        }
    }
}
