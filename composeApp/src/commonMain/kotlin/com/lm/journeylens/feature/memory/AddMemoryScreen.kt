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
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.component.EmojiPickerDialog
import com.lm.journeylens.feature.memory.component.LocationPickerDialog
import com.lm.journeylens.feature.memory.model.PRESET_EMOJIS
import com.lm.journeylens.feature.memory.service.rememberPhotoPicker
import cafe.adriel.voyager.koin.getScreenModel

/**
 * 添加记忆页面
 * 新流程：选位置 → 选照片 → 填写详情
 */
/**
 * 添加记忆页面
 * 新流程：选位置 → 选照片 → 填写详情
 */
@Composable
fun AddMemoryScreen(screenModel: AddMemoryScreenModel) {
    val uiState by screenModel.uiState.collectAsState()
    
    // 每次显示页面时重新加载草稿
    // 使用 rememberUpdatedState 确保每次 Compose 时都会检查
    LaunchedEffect(screenModel) {
        screenModel.loadDraft()
    }
    
    val currentStep = uiState.step
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JourneyLensColors.Background)
    ) {
        when (currentStep) {
            ImportStep.LOCATION -> LocationStep(
                onUseCurrentLocation = { lat, lng, name ->
                    screenModel.setLocationFromGps(lat, lng, name)
                },
                onSelectFromMap = { lat, lng ->
                    screenModel.setLocationFromMap(lat, lng)
                }
            )
            ImportStep.PHOTOS -> PhotosStep(
                photoUris = uiState.photoUris,
                onAddPhotos = { screenModel.addPhotos(it) },
                onRemovePhoto = { screenModel.removePhoto(it) },
                onConfirm = { screenModel.confirmPhotos() },
                onBack = { screenModel.goBack() }
            )
            ImportStep.DETAILS -> DetailsStep(
                photoUris = uiState.photoUris,
                emoji = uiState.emoji,
                note = uiState.note,
                onEmojiChange = { screenModel.updateEmoji(it) },
                onNoteChange = { screenModel.updateNote(it) },
                onSave = { screenModel.saveMemory() },
                onBack = { screenModel.goBack() }
            )
            ImportStep.SUCCESS -> SuccessStep(
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
 * 步骤 1: 选择位置
 */
@Composable
private fun LocationStep(
    onUseCurrentLocation: (Double, Double, String?) -> Unit,
    onSelectFromMap: (Double, Double) -> Unit
) {
    var showMapPicker by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    
    val locationService: com.lm.journeylens.feature.memory.service.LocationService = org.koin.compose.koinInject()
    val scope = rememberCoroutineScope()
    
    if (showMapPicker) {
        LocationPickerDialog(
            onDismiss = { showMapPicker = false },
            onLocationSelected = { lat, lng ->
                onSelectFromMap(lat, lng)
                showMapPicker = false
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = JourneyLensColors.AppleBlue
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "选择位置",
            style = MaterialTheme.typography.headlineMedium,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "这些照片是在哪里拍的？",
            style = MaterialTheme.typography.bodyMedium,
            color = JourneyLensColors.TextSecondary
        )
        
        // 错误提示
        locationError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = JourneyLensColors.ApplePink
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 使用当前定位
        Button(
            onClick = { 
                isGettingLocation = true
                locationError = null
                scope.launch {
                    val result = locationService.getCurrentLocation()
                    isGettingLocation = false
                    if (result != null) {
                        onUseCurrentLocation(result.latitude, result.longitude, result.address)
                    } else {
                        locationError = "定位失败，请检查定位权限"
                    }
                }
            },
            enabled = !isGettingLocation,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = JourneyLensColors.AppleBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isGettingLocation) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = androidx.compose.ui.graphics.Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("定位中...")
            } else {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("使用当前定位")
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 地图选点
        OutlinedButton(
            onClick = { showMapPicker = true },
            enabled = !isGettingLocation,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = JourneyLensColors.AppleBlue
            )
        ) {
            Icon(Icons.Default.Map, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("在地图上选点")
        }
    }
}

/**
 * 步骤 2: 选择照片
 */
@Composable
private fun PhotosStep(
    photoUris: List<String>,
    onAddPhotos: (List<String>) -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val launchPicker = rememberPhotoPicker(onAddPhotos)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text(
                text = "选择照片",
                style = MaterialTheme.typography.titleLarge,
                color = JourneyLensColors.TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "添加这个地点的照片",
            style = MaterialTheme.typography.bodyMedium,
            color = JourneyLensColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 照片网格
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            // 已选照片
            itemsIndexed(photoUris) { index, uri ->
                PhotoThumbnail(
                    uri = uri,
                    onRemove = { onRemovePhoto(index) }
                )
            }
            
            // 添加按钮
            item {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // 保持正方形
                        .clip(RoundedCornerShape(12.dp))
                        .background(JourneyLensColors.SurfaceLight)
                        .border(
                            2.dp,
                            JourneyLensColors.AppleBlue.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
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
                            "添加",
                            style = MaterialTheme.typography.labelSmall,
                            color = JourneyLensColors.AppleBlue
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 确认按钮
        Button(
            onClick = onConfirm,
            enabled = photoUris.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = JourneyLensColors.AppleBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("下一步 (${photoUris.size} 张照片)")
        }
    }
}

/**
 * 照片缩略图
 */
@Composable
private fun PhotoThumbnail(
    uri: String,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.aspectRatio(1f)
    ) {
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
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .background(
                    JourneyLensColors.ApplePink,
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "删除",
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 步骤 3: 填写详情
 */
@Composable
private fun DetailsStep(
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
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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

/**
 * 成功页面
 */
@Composable
private fun SuccessStep(
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
            text = "保存成功！",
            style = MaterialTheme.typography.headlineLarge,
            color = JourneyLensColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "记忆已添加到你的时间轴",
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
            Text("继续添加")
        }
    }
}
