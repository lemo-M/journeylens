package com.lm.journeylens.feature.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.theme.JourneyLensColors

/**
 * 添加记忆页面 - 导入照片/创建记忆
 * TODO: 实现照片选择器和 EXIF 解析
 */
@Composable
fun AddMemoryScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JourneyLensColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📸",
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "添加新记忆",
                style = MaterialTheme.typography.headlineLarge,
                color = JourneyLensColors.TextPrimary
            )
            Text(
                text = "导入照片，锚定你的足迹",
                style = MaterialTheme.typography.bodyMedium,
                color = JourneyLensColors.TextSecondary
            )
        }
    }
}
