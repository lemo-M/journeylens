package com.lm.journeylens.feature.memory.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lm.journeylens.core.theme.JourneyLensColors

/**
 * 位置选择对话框 - expect/actual 模式
 * 显示全屏地图，点击选择位置
 */
@Composable
expect fun LocationPickerDialog(
    onDismiss: () -> Unit,
    onLocationSelected: (latitude: Double, longitude: Double) -> Unit
)
