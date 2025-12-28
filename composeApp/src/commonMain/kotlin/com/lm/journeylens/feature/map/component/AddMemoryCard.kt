package com.lm.journeylens.feature.map.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.theme.JourneyLensColors

/**
 * "在此处添加记忆" 卡片
 */
@Composable
fun AddMemoryCard(
    locationName: String,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
     Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = JourneyLensColors.SurfaceLight.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    // 与 MapMemoryDetailCard 保持一致的高度逻辑
                    .heightIn(min = 320.dp, max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(JourneyLensColors.AppleBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = JourneyLensColors.AppleBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "在此处添加记忆",
                    style = MaterialTheme.typography.titleLarge,
                    color = JourneyLensColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "复用 $locationName 的位置信息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneyLensColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JourneyLensColors.AppleBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("去添加")
                }
            }
            
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = JourneyLensColors.TextSecondary
                )
            }
        }
    }
}
