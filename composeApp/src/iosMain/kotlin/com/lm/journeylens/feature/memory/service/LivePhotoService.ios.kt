package com.lm.journeylens.feature.memory.service

import com.lm.journeylens.feature.memory.model.LivePhotoData
import com.lm.journeylens.feature.memory.model.LivePhotoDetectionResult
import com.lm.journeylens.feature.memory.model.LivePhotoType

/**
 * iOS 实况照片服务实现
 * TODO: 使用 PHAsset 获取 Live Photo 的视频部分
 */
actual class LivePhotoService {
    
    actual suspend fun detectLivePhoto(photoUri: String): LivePhotoDetectionResult {
        // iOS 实现待完成
        // 需要使用 PHPickerViewController + PHAsset 获取 Live Photo
        return LivePhotoDetectionResult(
            type = LivePhotoType.STATIC,
            livePhotoData = LivePhotoData(photoUri = photoUri)
        )
    }
    
    actual suspend fun detectLivePhotos(photoUris: List<String>): List<LivePhotoDetectionResult> {
        return photoUris.map { detectLivePhoto(it) }
    }
}
