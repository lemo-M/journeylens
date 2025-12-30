package com.lm.journeylens.feature.memory.presentation.steps

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.component.EmojiPickerDialog
import com.lm.journeylens.feature.memory.model.PRESET_EMOJIS

/**
 * 步骤 3: 填写详情
 */
@Composable
fun DetailsStep(
    photoUris: List<String>,
    emoji: String,
    note: String?,
    onEmojiChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var showEmojiPicker by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf(note ?: "") }
    
    if (showEmojiPicker) {
        EmojiPickerDialog(
            currentEmoji = emoji,
            onEmojiSelected = onEmojiChange,
            onDismiss = { showEmojiPicker = false }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "完善信息",
                style = MaterialTheme.typography.titleLarge,
                color = JourneyLensColors.TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 照片预览
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(80.dp)
        ) {
            itemsIndexed(photoUris) { _, uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Emoji 选择
        Text(
            text = "选择图标",
            style = MaterialTheme.typography.titleSmall,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 当前选中的 emoji
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JourneyLensColors.AppleBlue.copy(alpha = 0.1f))
                    .border(2.dp, JourneyLensColors.AppleBlue, RoundedCornerShape(12.dp))
                    .clickable { showEmojiPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, style = MaterialTheme.typography.headlineMedium)
            }
            
            // 快速选择
            PRESET_EMOJIS.take(6).forEach { e ->
                if (e != emoji) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(JourneyLensColors.SurfaceLight)
                            .clickable { onEmojiChange(e) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(e, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            // 更多
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(JourneyLensColors.SurfaceLight)
                    .clickable { showEmojiPicker = true },
                contentAlignment = Alignment.Center
            ) {
                Text("...", style = MaterialTheme.typography.titleMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 备注输入
        Text(
            text = "添加备注",
            style = MaterialTheme.typography.titleSmall,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = noteText,
            onValueChange = { 
                noteText = it
                onNoteChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            placeholder = {
                Text(
                    "记录这一刻的心情、故事...",
                    color = JourneyLensColors.TextTertiary
                )
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = JourneyLensColors.AppleBlue,
                unfocusedBorderColor = JourneyLensColors.TextTertiary.copy(alpha = 0.3f)
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 保存按钮
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = JourneyLensColors.AppleBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("保存记忆")
        }
    }
}
