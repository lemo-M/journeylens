package com.lm.journeylens.feature.memory.service

import androidx.compose.runtime.Composable

/**
 * Photo Picker - expect/actual 模式
 * 提供跨平台的照片选择功能
 */
@Composable
expect fun rememberPhotoPicker(
    onPhotosSelected: (List<String>) -> Unit
): () -> Unit
