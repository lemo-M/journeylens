package com.lm.journeylens.feature.memory

import androidx.compose.foundation.background
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
    // TODO: 集成 Android Photo Picker
    // 目前显示占位 UI
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
            onClick = {
                // TODO: 启动 Photo Picker
                // 临时用于测试的假数据
            },
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
            text = "请确认以下照片的位置信息",
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
                    onLocationUpdate = { lat, lng -> onLocationUpdate(index, lat, lng) }
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
    onLocationUpdate: (Double, Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = JourneyLensColors.SurfaceLight
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 照片缩略图占位
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JourneyLensColors.TextTertiary),
                contentAlignment = Alignment.Center
            ) {
                Text("📷", style = MaterialTheme.typography.headlineMedium)
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
                TextButton(onClick = {
                    // TODO: 打开地图选点
                }) {
                    Text("选点", color = JourneyLensColors.AppleBlue)
                }
            }
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
