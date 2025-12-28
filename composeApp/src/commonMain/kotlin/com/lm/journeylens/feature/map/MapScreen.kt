package com.lm.journeylens.feature.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.map.component.MapCameraControl
import com.lm.journeylens.feature.map.component.MapView
import com.lm.journeylens.feature.memory.MemoryDetailScreen
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

import com.lm.journeylens.feature.memory.service.DraftService
import com.lm.journeylens.feature.memory.AddMemoryUiState
import com.lm.journeylens.feature.memory.ImportStep
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.lm.journeylens.navigation.AddTab

import cafe.adriel.voyager.koin.getScreenModel

/**
 * 地图页面 - 战争迷雾探索地图
 */
@Composable
fun MapScreen(
    screenModel: MapScreenModel
) {
    val repository: MemoryRepository = koinInject()
    val draftService: DraftService = koinInject()
    val uiState by screenModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    
    val tabNavigator = LocalTabNavigator.current
    
    // 相机控制器
    val cameraControl = remember { MapCameraControl() }
    
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
            else -> {
                // 地图
                MapView(
                    memories = uiState.memories,
                    onMemoryClick = { memories -> screenModel.selectMemories(memories) },
                    cameraControl = cameraControl,
                    cameraPosition = uiState.cameraPosition,
                    onCameraPositionChange = { pos -> 
                        screenModel.updateCameraPosition(pos.latitude, pos.longitude, pos.zoom)
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // 定位按钮 (自定义 FAB)
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            cameraControl.moveToCurrentLocation()
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = if (uiState.selectedMemories.isEmpty()) 32.dp else 400.dp) // 避开详情卡片
                        .padding(bottom = 48.dp), // 额外留出底部导航栏高度
                    containerColor = JourneyLensColors.Background,
                    contentColor = JourneyLensColors.AppleBlue,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "定位")
                }
                
                // 顶部信息栏
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    // 使用稍微不透明一点的背景，防止看不清
                    color = JourneyLensColors.SurfaceLight.copy(alpha = 0.95f),
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
                    // 如果没有记忆，尝试定位到当前位置
                    LaunchedEffect(Unit) {
                        cameraControl.moveToCurrentLocation()
                    }
                    
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
        
        // 选中记忆的详情卡片 (支持左右滑动)
        AnimatedVisibility(
            visible = uiState.selectedMemories.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val selectedMemories = uiState.selectedMemories
            // 增加一页用于"添加新记忆"
            val pageCount = selectedMemories.size + 1
            
            if (selectedMemories.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { pageCount })
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 页码指示器 (如果有多页) - 加背景胶囊
                    if (pageCount > 1) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.3f), // 半透明黑色背景，增强对比度
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(pageCount) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) 
                                        Color.White else Color.White.copy(alpha = 0.5f)
                                    Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(6.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = if (pageCount > 1) 16.dp else 0.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        if (page < selectedMemories.size) {
                            MapMemoryDetailCard(
                                memory = selectedMemories[page],
                                onDismiss = { screenModel.clearSelection() },
                                onEdit = {
                                    editingMemory = selectedMemories[page]
                                    showDetailDialog = true
                                    screenModel.clearSelection()
                                }
                            )
                        } else {
                            // 最后一页："添加新记忆"卡片
                            val currentLocMemory = selectedMemories.firstOrNull()
                            AddMemoryCard(
                                locationName = currentLocMemory?.locationName ?: "此处",
                                onAdd = {
                                    currentLocMemory?.let { m ->
                                        // 1. 保存包含位置信息的 Draft
                                        val draft = AddMemoryUiState(
                                            step = ImportStep.PHOTOS, // 直接跳到选照片
                                            latitude = m.latitude,
                                            longitude = m.longitude,
                                            locationName = m.locationName,
                                            // address = m.address // Memory 实体没有 address 字段，忽略
                                        )
                                        scope.launch {
                                            draftService.saveDraft(draft)
                                            // 2. 跳转到 AddTab
                                            tabNavigator.current = AddTab
                                        }
                                    }
                                },
                                onDismiss = { screenModel.clearSelection() }
                            )
                        }
                    }
                    
                    // 底部留白，为了不被 NavigationBar 遮挡
                    Spacer(modifier = Modifier.height(16.dp))
                }
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
 * "在此处添加记忆" 卡片
 */
@Composable
private fun AddMemoryCard(
    locationName: String,
    onAdd: () -> Unit,
    onDismiss: () -> Unit
) {
     Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = JourneyLensColors.SurfaceLight.copy(alpha = 0.98f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    // 与 MapMemoryDetailCard 保持一致的高度逻辑
                    .heightIn(min = 320.dp, max = 400.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(JourneyLensColors.AppleBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = JourneyLensColors.AppleBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "在此处添加记忆",
                    style = MaterialTheme.typography.titleLarge,
                    color = JourneyLensColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "复用 $locationName 的位置信息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JourneyLensColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JourneyLensColors.AppleBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("去添加")
                }
            }
            
            // 关闭按钮
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    tint = JourneyLensColors.TextSecondary
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
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
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
