package com.lm.journeylens.feature.memory.model

/**
 * 实况照片数据模型
 * 支持静态图片 + 可选的视频部分
 */
data class LivePhotoData(
    val photoUri: String,          // 静态图片 URI
    val videoUri: String? = null,  // 关联视频 URI (可选)
    val isLivePhoto: Boolean = videoUri != null
)

/**
 * 实况照片类型
 */
enum class LivePhotoType {
    STATIC,           // 普通照片（无视频）
    XIAOMI_SEPARATE,  // 小米：分离的 JPG + MP4
    GOOGLE_EMBEDDED,  // Google Pixel：嵌入式 Motion Photo (待实现)
    SAMSUNG_XMP,      // 三星：XMP 嵌入 (待实现)
    IOS_LIVE_PHOTO    // iOS：HEIC + MOV (待实现)
}

/**
 * 实况照片检测结果
 */
data class LivePhotoDetectionResult(
    val type: LivePhotoType,
    val livePhotoData: LivePhotoData
)
