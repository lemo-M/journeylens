package com.lm.journeylens.feature.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lm.journeylens.core.domain.model.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.component.EmojiPickerDialog
import com.lm.journeylens.feature.memory.model.PRESET_EMOJIS
import com.lm.journeylens.feature.memory.service.rememberPhotoPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import com.lm.journeylens.core.util.formatCoordinates

/**
 * 记忆详情/编辑页面
 */
@Composable
fun MemoryDetailScreen(
    memory: Memory,
    onSave: (Memory) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var editedEmoji by remember { mutableStateOf(memory.emoji) }
    var editedNote by remember { mutableStateOf(memory.note ?: "") }
    var editedPhotoUris by remember { mutableStateOf(memory.photoUris) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val launchPicker = rememberPhotoPicker { newUris ->
        editedPhotoUris = editedPhotoUris + newUris
        isEditing = true
    }
    
    if (showEmojiPicker) {
        EmojiPickerDialog(
            currentEmoji = editedEmoji,
            onEmojiSelected = { 
                editedEmoji = it
                isEditing = true
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("删除记忆") },
            text = { Text("确定要删除这个记忆吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("删除", color = JourneyLensColors.ApplePink)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JourneyLensColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "关闭")
            }
            
            Row {
                if (isEditing) {
                    TextButton(
                        onClick = {
                            val updatedMemory = memory.copy(
                                emoji = editedEmoji,
                                note = editedNote.takeIf { it.isNotBlank() },
                                photoUris = editedPhotoUris
                            )
                            onSave(updatedMemory)
                        }
                    ) {
                        Text("保存", color = JourneyLensColors.AppleBlue)
                    }
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = JourneyLensColors.ApplePink
                    )
                }
            }
        }
        
        // 日期和 Emoji
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji 可点击编辑
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JourneyLensColors.AppleBlue.copy(alpha = 0.1f))
                    .clickable { showEmojiPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Text(editedEmoji, style = MaterialTheme.typography.headlineMedium)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
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
                    style = MaterialTheme.typography.titleLarge,
                    color = JourneyLensColors.TextPrimary
                )
                Text(
                    text = formatCoordinates(memory.latitude, memory.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = JourneyLensColors.TextTertiary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 照片
        Text(
            text = "照片 (${editedPhotoUris.size})",
            style = MaterialTheme.typography.titleMedium,
            color = JourneyLensColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(editedPhotoUris) { index, uri ->
                Box(modifier = Modifier.size(160.dp)) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // 删除按钮
                    IconButton(
                        onClick = {
                            editedPhotoUris = editedPhotoUris.filterIndexed { i, _ -> i != index }
                            isEditing = true
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(28.dp)
                            .background(JourneyLensColors.ApplePink, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "删除照片",
                            tint = androidx.compose.ui.graphics.Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            // 添加更多照片
            item {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(JourneyLensColors.SurfaceLight)
                        .border(2.dp, JourneyLensColors.AppleBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { launchPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加照片",
                            tint = JourneyLensColors.AppleBlue,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "添加照片",
                            style = MaterialTheme.typography.labelSmall,
                            color = JourneyLensColors.AppleBlue
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 备注
        Text(
            text = "备注",
            style = MaterialTheme.typography.titleMedium,
            color = JourneyLensColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = editedNote,
            onValueChange = { 
                editedNote = it
                isEditing = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .heightIn(min = 120.dp),
            placeholder = {
                Text(
                    "添加备注...",
                    color = JourneyLensColors.TextTertiary
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JourneyLensColors.AppleBlue,
                unfocusedBorderColor = JourneyLensColors.TextTertiary.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
