package com.lm.journeylens.feature.memory.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lm.journeylens.core.theme.JourneyLensColors

/**
 * iOS 位置选择对话框
 * TODO: 使用 MapKit 实现
 */
@Composable
actual fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = JourneyLensColors.SurfaceLight
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "位置选择",
                    style = MaterialTheme.typography.titleLarge,
                    color = JourneyLensColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "iOS 地图选点功能开发中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneyLensColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JourneyLensColors.AppleBlue
                    )
                ) {
                    Text("关闭")
                }
            }
        }
    }
}
