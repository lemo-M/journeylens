package com.lm.journeylens.feature.memory.service

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Android Photo Picker 实现
 * 使用现代 Photo Picker API (Android 13+) 或回退到旧版选择器
 */
@Composable
actual fun rememberPhotoPicker(
    onPhotosSelected: (List<String>) -> Unit
): () -> Unit {
    // 使用 PickMultipleVisualMedia 支持多选照片
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris: List<Uri> ->
        // 将 Uri 转换为 String
        val uriStrings = uris.map { it.toString() }
        if (uriStrings.isNotEmpty()) {
            onPhotosSelected(uriStrings)
        }
    }
    
    return {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}
