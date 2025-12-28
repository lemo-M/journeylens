package com.lm.journeylens.feature.map.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lm.journeylens.core.domain.model.Memory
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.core.util.formatCoordinates
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 地图记忆详情卡片
 */
@Composable
fun MapMemoryDetailCard(
    memory: Memory,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            // 使用更不透明的背景
            containerColor = JourneyLensColors.SurfaceLight.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                // 设置最小高度，防止滑动时因内容高度不一导致跳动
                .heightIn(min = 320.dp, max = 400.dp) // 增加最大高度限制
        ) {
            // 顶部栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji + 时间
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = memory.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val dateTime = remember(memory.timestamp) {
                        try {
                            val instant = Instant.fromEpochMilliseconds(memory.timestamp)
                            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                            "${localDateTime.year}年${localDateTime.monthNumber}月${localDateTime.dayOfMonth}日"
                        } catch (e: Exception) {
                            "未知时间"
                        }
                    }
                    Text(
                        text = dateTime,
                        style = MaterialTheme.typography.titleMedium,
                        color = JourneyLensColors.TextPrimary
                    )
                }
                
                Row {
                    // 编辑按钮
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = JourneyLensColors.AppleBlue
                        )
                    }
                    // 关闭按钮
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = JourneyLensColors.TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 照片（使用 Coil）
            if (memory.photoUris.isNotEmpty()) {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(memory.photoUris.size) { index ->
                        AsyncImage(
                            model = memory.photoUris[index],
                            contentDescription = null,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(JourneyLensColors.SurfaceLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", style = MaterialTheme.typography.displayMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 备注区域 (使用 Weight 让其占据固定空间，或者用 Spacer 撑满)
            // 支持垂直滑动，移除行数限制
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                 memory.note?.let { note ->
                    if (note.isNotBlank()) {
                         Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = JourneyLensColors.TextSecondary,
                            // maxLines = 3, // 移除行数限制
                            // overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    } else {
                         // 即使为空也占位，或者显示默认文案
                         Text(
                            text = "没有备注",
                            style = MaterialTheme.typography.bodyMedium,
                            color = JourneyLensColors.TextTertiary.copy(alpha = 0.5f)
                         )
                    }
                } ?: run {
                     Text(
                        text = "没有备注",
                        style = MaterialTheme.typography.bodyMedium,
                         color = JourneyLensColors.TextTertiary.copy(alpha = 0.5f)
                    )
                }
            }
           
            Spacer(modifier = Modifier.height(8.dp))
            
            // 位置 + 照片数量
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatCoordinates(memory.latitude, memory.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyLensColors.TextTertiary
                )
                Text(
                    text = "${memory.photoCount} 张照片",
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyLensColors.TextTertiary
                )
            }
        }
    }
}
