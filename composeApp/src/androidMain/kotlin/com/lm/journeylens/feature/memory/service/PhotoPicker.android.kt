package com.lm.journeylens.feature.memory.service

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Android Photo Picker 实现
 * 使用现代 Photo Picker API (Android 13+) 或回退到旧版选择器
 */
@Composable
actual fun rememberPhotoPicker(
    onPhotosSelected: (List<String>) -> Unit
): () -> Unit {
    val context = LocalContext.current
    
    // 使用 PickMultipleVisualMedia 支持多选照片
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20)
    ) { uris: List<Uri> ->
        // 获取持久权限，确保重启后还能访问这些 URI
        val persistedUris = uris.mapNotNull { uri ->
            try {
                // 尝试获取持久读取权限
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                uri.toString()
            } catch (e: SecurityException) {
                // Photo Picker 的 URI 可能不支持持久权限
                // 这种情况下需要复制文件到应用私有目录
                copyToAppStorage(context, uri)
            }
        }
        
        if (persistedUris.isNotEmpty()) {
            onPhotosSelected(persistedUris)
        }
    }
    
    return {
        launcher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
}

/**
 * 将图片复制到应用私有目录
 */
private fun copyToAppStorage(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "photo_${System.currentTimeMillis()}_${uri.lastPathSegment?.takeLast(8) ?: "img"}.jpg"
        val outputFile = java.io.File(context.filesDir, "photos").apply { mkdirs() }
            .resolve(fileName)
        
        outputStream(outputFile).use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        
        // 返回文件 URI
        outputFile.toURI().toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun outputStream(file: java.io.File): java.io.OutputStream {
    return java.io.FileOutputStream(file)
}
