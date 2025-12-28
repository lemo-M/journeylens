package com.lm.journeylens.feature.memory.service

import androidx.compose.runtime.Composable

/**
 * iOS Photo Picker 实现 - 占位符
 * TODO: 使用 PHPickerViewController 实现
 */
@Composable
actual fun rememberPhotoPicker(
    onPhotosSelected: (List<String>) -> Unit
): () -> Unit {
    // iOS 实现待完成
    return {
        // 暂时不执行任何操作
    }
}
