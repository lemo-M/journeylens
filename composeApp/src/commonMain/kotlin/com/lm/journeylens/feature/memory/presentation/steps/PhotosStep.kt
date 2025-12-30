package com.lm.journeylens.feature.memory.presentation.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.feature.memory.service.rememberPhotoPicker

/**
 * 步骤 2: 选择照片
 */
@Composable
fun PhotosStep(
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
        
        // 照片网格 - 添加顶部 padding 防止删除按钮被截断
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 8.dp), // 给顶部留出空间
            modifier = Modifier.weight(1f)
        ) {
            // 已选照片
            itemsIndexed(photoUris) { index, uri ->
                PhotoThumbnail(
                    uri = uri,
                    onRemove = { onRemovePhoto(index) }
                )
            }
            
            // 添加按钮 - 始终显示，满 20 张时变灰禁用
            item {
                val isEnabled = photoUris.size < 20
                Box(
                    modifier = Modifier
                        .aspectRatio(1f) // 保持正方形
                        .padding(top = 6.dp, end = 6.dp) // 与照片缩略图保持一致的 padding
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isEnabled) JourneyLensColors.SurfaceLight 
                            else JourneyLensColors.SurfaceLight.copy(alpha = 0.5f)
                        )
                        .border(
                            2.dp,
                            if (isEnabled) JourneyLensColors.AppleBlue.copy(alpha = 0.5f)
                            else JourneyLensColors.TextTertiary.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .then(
                            if (isEnabled) Modifier.clickable { launchPicker() }
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "添加照片",
                            tint = if (isEnabled) JourneyLensColors.AppleBlue 
                                   else JourneyLensColors.TextTertiary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (isEnabled) "添加" else "已满",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isEnabled) JourneyLensColors.AppleBlue 
                                    else JourneyLensColors.TextTertiary
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
        modifier = Modifier
            .aspectRatio(1f)
            .padding(top = 6.dp, end = 6.dp) // 给删除按钮留出空间
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        
        // 删除按钮 - 更小更精致
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 6.dp, y = (-6).dp) // 偏移到图片角落外
                .size(20.dp)
                .clip(CircleShape)
                .background(JourneyLensColors.TextSecondary.copy(alpha = 0.8f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "删除",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}
