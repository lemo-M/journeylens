package com.lm.journeylens.feature.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.MemoryDetailScreen
import com.lm.journeylens.feature.timeline.component.SpiralTimeline
import kotlinx.coroutines.launch
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
    val repository: MemoryRepository = koinInject()
    val uiState by screenModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    // 控制详情编辑对话框
    var showDetailDialog by remember { mutableStateOf(false) }
    var editingMemory by remember { mutableStateOf<Memory?>(null) }
    
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
                    onDismiss = { screenModel.clearSelection() },
                    onEdit = {
                        editingMemory = memory
                        showDetailDialog = true
                        screenModel.clearSelection()
                    }
                )
            }
        }
    }
    
    // 编辑对话框
    if (showDetailDialog && editingMemory != null) {
        Dialog(
            onDismissRequest = { 
                showDetailDialog = false 
                editingMemory = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            MemoryDetailScreen(
                memory = editingMemory!!,
                onSave = { updatedMemory ->
                    scope.launch {
                        repository.update(updatedMemory)
                        showDetailDialog = false
                        editingMemory = null
                    }
                },
                onDelete = {
                    scope.launch {
                        repository.delete(editingMemory!!)
                        showDetailDialog = false
                        editingMemory = null
                    }
                },
                onDismiss = {
                    showDetailDialog = false
                    editingMemory = null
                }
            )
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
    onDismiss: () -> Unit,
    onEdit: () -> Unit
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
            
            // 备注
            memory.note?.let { note ->
                if (note.isNotBlank()) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JourneyLensColors.TextSecondary,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            // 位置 + 照片数量 + 编辑提示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📍 %.4f, %.4f".format(memory.latitude, memory.longitude),
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
