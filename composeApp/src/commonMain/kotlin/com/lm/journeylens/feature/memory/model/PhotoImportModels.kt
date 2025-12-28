package com.lm.journeylens.feature.memory.model

/**
 * EXIF 数据模型 - 从照片中提取的元数据
 */
data class ExifData(
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long?,  // 拍摄时间（毫秒）
    val hasLocation: Boolean = latitude != null && longitude != null
)

/**
 * 照片导入结果
 */
sealed class PhotoImportResult {
    /**
     * 自动定位成功 - EXIF 包含有效的 GPS
     */
    data class AutoLocated(
        val photoUri: String,
        val latitude: Double,
        val longitude: Double,
        val timestamp: Long,
        val videoUri: String? = null  // 实况照片视频
    ) : PhotoImportResult()
    
    /**
     * 需要手动定位 - EXIF 无 GPS 但有时间
     */
    data class NeedsManualLocation(
        val photoUri: String,
        val timestamp: Long?,
        val suggestedLatitude: Double? = null,  // 根据时间推测的位置
        val suggestedLongitude: Double? = null,
        val videoUri: String? = null  // 实况照片视频
    ) : PhotoImportResult()
    
    /**
     * 完全无元数据
     */
    data class NoMetadata(
        val photoUri: String,
        val videoUri: String? = null  // 实况照片视频
    ) : PhotoImportResult()
}

/**
 * 预设 emoji 列表
 */
val PRESET_EMOJIS = listOf(
    "📍", "🏠", "🏢", "🎉", "🍜", "☕", "🎬", "🎵",
    "✈️", "🚗", "🚇", "🏖️", "⛰️", "🌳", "🌸", "🌙",
    "❤️", "⭐", "🔥", "💡", "📚", "🎮", "🏃", "🛒"
)

/**
 * 待审核的导入项
 */
data class PendingImport(
    val photoUri: String,
    val videoUri: String? = null,  // 实况照片视频 URI
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long?,
    val isAutoLocated: Boolean,
    val isSuggested: Boolean = false,  // 是否是时间推测的位置
    val isLivePhoto: Boolean = videoUri != null,  // 是否为实况照片
    val emoji: String = "📍",  // 自定义 emoji
    var isConfirmed: Boolean = false
)
