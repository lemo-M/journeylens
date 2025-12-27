package com.lm.journeylens.feature.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.timeline.component.SpiralTimeline
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

/**
 * 时间轴页面 - 螺旋时间轴
 */
@Composable
fun TimelineScreen() {
    val screenModel: TimelineScreenModel = koinInject()
    val uiState by screenModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JourneyLensColors.Background)
    ) {
        when {
            uiState.isLoading -> {
                // 加载中
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = JourneyLensColors.AppleBlue)
                }
            }
            uiState.memories.isEmpty() -> {
                // 空状态
                EmptyTimelineContent()
            }
            else -> {
                // 螺旋时间轴
                SpiralTimeline(
                    memories = uiState.memories,
                    onMemoryClick = { memory -> screenModel.selectMemory(memory) },
                    modifier = Modifier.fillMaxSize()
                )
                
                // 提示文字
                Text(
                    text = "双指缩放 · 拖动浏览",
                    style = MaterialTheme.typography.labelSmall,
                    color = JourneyLensColors.TextTertiary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                )
                
                // 记忆数量
                Text(
                    text = "${uiState.memories.size} 条记忆",
                    style = MaterialTheme.typography.labelMedium,
                    color = JourneyLensColors.TextSecondary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }
        }
        
        // 选中记忆的详情卡片
        AnimatedVisibility(
            visible = uiState.selectedMemory != null,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            uiState.selectedMemory?.let { memory ->
                MemoryDetailCard(
                    memory = memory,
                    onDismiss = { screenModel.clearSelection() }
                )
            }
        }
    }
}

/**
 * 空状态内容
 */
@Composable
private fun EmptyTimelineContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌀",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "时间轴空空如也",
            style = MaterialTheme.typography.headlineLarge,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "添加第一条记忆，开始你的时间之旅",
            style = MaterialTheme.typography.bodyMedium,
            color = JourneyLensColors.TextSecondary
        )
    }
}

/**
 * 记忆详情卡片
 */
@Composable
private fun MemoryDetailCard(
    memory: Memory,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = JourneyLensColors.GlassBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 顶部栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 时间
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
                
                // 关闭按钮
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = JourneyLensColors.TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 照片占位
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JourneyLensColors.SurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Text("📷", style = MaterialTheme.typography.displayMedium)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 位置
            if (memory.locationName != null) {
                Text(
                    text = "📍 ${memory.locationName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneyLensColors.TextSecondary
                )
            } else {
                Text(
                    text = "📍 %.4f, %.4f".format(memory.latitude, memory.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyLensColors.TextTertiary
                )
            }
            
            // 备注
            if (memory.note != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = memory.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneyLensColors.TextPrimary
                )
            }
        }
    }
}
