package com.lm.journeylens.feature.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.model.PendingImport
import com.lm.journeylens.feature.memory.service.rememberPhotoPicker
import org.koin.compose.koinInject

/**
 * 添加记忆页面
 */
@Composable
fun AddMemoryScreen() {
    val screenModel: AddMemoryScreenModel = koinInject()
    val uiState by screenModel.uiState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JourneyLensColors.Background)
    ) {
        when (uiState.step) {
            ImportStep.SELECT -> SelectPhotosContent(
                onPhotosSelected = { uris -> screenModel.processSelectedPhotos(uris) }
            )
            ImportStep.REVIEW -> ReviewContent(
                pendingImports = uiState.pendingImports,
                isLoading = uiState.isLoading,
                onLocationUpdate = { index, lat, lng -> 
                    screenModel.updatePendingLocation(index, lat, lng) 
                },
                onEmojiUpdate = { index, emoji ->
                    screenModel.updatePendingEmoji(index, emoji)
                },
                onConfirm = { screenModel.confirmImport() }
            )
            ImportStep.SUCCESS -> SuccessContent(
                count = uiState.importedCount,
                onDone = { screenModel.reset() }
            )
        }
        
        // Loading 遮罩
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(JourneyLensColors.Background.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = JourneyLensColors.AppleBlue)
            }
        }
    }
}

/**
 * 选择照片内容
 */
@Composable
private fun SelectPhotosContent(
    onPhotosSelected: (List<String>) -> Unit
) {
    // 使用 Photo Picker
    val launchPicker = rememberPhotoPicker(onPhotosSelected)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = JourneyLensColors.AppleBlue
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "添加新记忆",
            style = MaterialTheme.typography.headlineLarge,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "选择照片，我们会自动读取位置和时间",
            style = MaterialTheme.typography.bodyMedium,
            color = JourneyLensColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { launchPicker() },
            colors = ButtonDefaults.buttonColors(
                containerColor = JourneyLensColors.AppleBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("选择照片")
        }
    }
}

/**
 * 审核确认内容
 */
@Composable
private fun ReviewContent(
    pendingImports: List<PendingImport>,
    isLoading: Boolean,
    onLocationUpdate: (Int, Double, Double) -> Unit,
    onEmojiUpdate: (Int, String) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "确认导入",
            style = MaterialTheme.typography.headlineLarge,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "请确认位置并选择标记图标",
            style = MaterialTheme.typography.bodyMedium,
            color = JourneyLensColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(pendingImports) { index, item ->
                PendingImportCard(
                    item = item,
                    onLocationUpdate = { lat, lng -> onLocationUpdate(index, lat, lng) },
                    onEmojiUpdate = { emoji -> onEmojiUpdate(index, emoji) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 确认按钮
        val validCount = pendingImports.count { it.latitude != null && it.longitude != null }
        Button(
            onClick = onConfirm,
            enabled = validCount > 0 && !isLoading,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = JourneyLensColors.AppleBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("导入 $validCount 条记忆")
        }
    }
}

/**
 * 待导入项卡片
 */
@Composable
private fun PendingImportCard(
    item: PendingImport,
    onLocationUpdate: (Double, Double) -> Unit,
    onEmojiUpdate: (String) -> Unit
) {
    // 控制地图选点对话框显示
    var showLocationPicker by remember { mutableStateOf(false) }
    // 控制 emoji 选择器对话框显示
    var showEmojiPicker by remember { mutableStateOf(false) }
    
    // 显示地图选点对话框
    if (showLocationPicker) {
        com.lm.journeylens.feature.memory.component.LocationPickerDialog(
            onDismiss = { showLocationPicker = false },
            onLocationSelected = { lat, lng ->
                onLocationUpdate(lat, lng)
                showLocationPicker = false
            }
        )
    }
    
    // 显示 emoji 选择器对话框
    if (showEmojiPicker) {
        com.lm.journeylens.feature.memory.component.EmojiPickerDialog(
            currentEmoji = item.emoji,
            onEmojiSelected = { emoji ->
                onEmojiUpdate(emoji)
            },
            onDismiss = { showEmojiPicker = false }
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = JourneyLensColors.SurfaceLight
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji 标记（可点击更换）
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JourneyLensColors.AppleBlue.copy(alpha = 0.1f))
                        .clickable { showEmojiPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 信息
                Column(modifier = Modifier.weight(1f)) {
                    // 状态
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when {
                            item.isAutoLocated -> {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = JourneyLensColors.AppleGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "自动定位",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JourneyLensColors.AppleGreen
                                )
                            }
                            item.isSuggested -> {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = JourneyLensColors.AppleOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "推测位置",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JourneyLensColors.AppleOrange
                                )
                            }
                            item.latitude == null -> {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = JourneyLensColors.ApplePink,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "需要手动选点",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JourneyLensColors.ApplePink
                                )
                            }
                        }
                    }
                    
                    // 坐标
                    if (item.latitude != null && item.longitude != null) {
                        Text(
                            text = "%.4f, %.4f".format(item.latitude, item.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = JourneyLensColors.TextSecondary
                        )
                    }
                }
                
                // 编辑按钮
                if (item.latitude == null || item.isSuggested) {
                    TextButton(onClick = { showLocationPicker = true }) {
                        Text("选点", color = JourneyLensColors.AppleBlue)
                    }
                }
            }
            
            // 点击更换 emoji 提示
            Text(
                text = "点击图标可更换标记",
                style = MaterialTheme.typography.labelSmall,
                color = JourneyLensColors.TextTertiary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * 成功内容
 */
@Composable
private fun SuccessContent(
    count: Int,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✨",
            style = MaterialTheme.typography.displayLarge
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "导入成功！",
            style = MaterialTheme.typography.headlineLarge,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "已添加 $count 条新记忆",
            style = MaterialTheme.typography.bodyMedium,
            color = JourneyLensColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onDone,
            colors = ButtonDefaults.buttonColors(
                containerColor = JourneyLensColors.AppleBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("完成")
        }
    }
}
