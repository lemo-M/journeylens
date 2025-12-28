package com.lm.journeylens.feature.memory.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.model.PRESET_EMOJIS

/**
 * Emoji 选择器对话框
 */
@Composable
fun EmojiPickerDialog(
    currentEmoji: String,
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = JourneyLensColors.SurfaceLight,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "选择标记图标",
                    style = MaterialTheme.typography.titleMedium,
                    color = JourneyLensColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PRESET_EMOJIS) { emoji ->
                        EmojiItem(
                            emoji = emoji,
                            isSelected = emoji == currentEmoji,
                            onClick = {
                                onEmojiSelected(emoji)
                                onDismiss()
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消", color = JourneyLensColors.TextSecondary)
                    }
                }
            }
        }
    }
}

/**
 * 单个 Emoji 项
 */
@Composable
private fun EmojiItem(
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) JourneyLensColors.AppleBlue.copy(alpha = 0.2f)
                else JourneyLensColors.Background
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) JourneyLensColors.AppleBlue else JourneyLensColors.Background,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 24.sp
        )
    }
}

/**
 * 内联 Emoji 选择器（水平滚动）
 */
@Composable
fun InlineEmojiPicker(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PRESET_EMOJIS.take(8).forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (emoji == selectedEmoji) 
                            JourneyLensColors.AppleBlue.copy(alpha = 0.2f)
                        else 
                            JourneyLensColors.Background
                    )
                    .border(
                        width = if (emoji == selectedEmoji) 1.5.dp else 0.dp,
                        color = if (emoji == selectedEmoji) JourneyLensColors.AppleBlue else JourneyLensColors.Background,
                        shape = CircleShape
                    )
                    .clickable { onEmojiSelected(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 18.sp
                )
            }
        }
    }
}
