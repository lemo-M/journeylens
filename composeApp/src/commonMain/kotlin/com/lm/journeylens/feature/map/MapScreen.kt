package com.lm.journeylens.feature.map

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
import com.lm.journeylens.feature.map.component.MapView
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

/**
 * 地图页面 - 战争迷雾探索地图
 */
@Composable
fun MapScreen() {
    val screenModel: MapScreenModel = koinInject()
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
            else -> {
                // 地图
                MapView(
                    memories = uiState.memories,
                    onMemoryClick = { memory -> screenModel.selectMemory(memory) },
                    modifier = Modifier.fillMaxSize()
                )
                
                // 顶部信息栏
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = JourneyLensColors.GlassBackground,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📍",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${uiState.memories.size} 个记忆点",
                            style = MaterialTheme.typography.bodyMedium,
                            color = JourneyLensColors.TextPrimary
                        )
                    }
                }
                
                // 空状态提示
                if (uiState.memories.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = JourneyLensColors.GlassBackground,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🗺️",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "开始探索吧",
                                style = MaterialTheme.typography.titleMedium,
                                color = JourneyLensColors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "添加第一张照片，解锁地图区域",
                                style = MaterialTheme.typography.bodySmall,
                                color = JourneyLensColors.TextSecondary
                            )
                        }
                    }
                }
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
                MapMemoryDetailCard(
                    memory = memory,
                    onDismiss = { screenModel.clearSelection() }
                )
            }
        }
    }
}

/**
 * 地图记忆详情卡片
 */
@Composable
private fun MapMemoryDetailCard(
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
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JourneyLensColors.SurfaceLight),
                contentAlignment = Alignment.Center
            ) {
                Text("📷", style = MaterialTheme.typography.displayMedium)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 位置
            Text(
                text = "📍 %.4f, %.4f".format(memory.latitude, memory.longitude),
                style = MaterialTheme.typography.bodySmall,
                color = JourneyLensColors.TextTertiary
            )
        }
    }
}
