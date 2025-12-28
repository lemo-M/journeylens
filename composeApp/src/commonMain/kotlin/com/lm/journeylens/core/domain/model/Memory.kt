package com.lm.journeylens.core.domain.model

import kotlinx.datetime.Clock

/**
 * 记忆模型 (Domain)
 * 纯 Kotlin 类，不包含 Android/Room 依赖
 */
data class Memory(
    val id: Long = 0,
    
    // 位置信息
    val latitude: Double,
    val longitude: Double,
    val locationName: String? = null,
    
    // 时间信息
    val timestamp: Long,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    
    // 照片组
    val photoUris: List<String> = emptyList(),
    
    // 标记
    val emoji: String = "📍",
    
    // 描述
    val note: String? = null,
    
    // 元数据
    val isAutoLocated: Boolean = true,
) {
    /**
     * 获取主照片 URI（第一张）
     */
    val primaryPhotoUri: String?
        get() = photoUris.firstOrNull()
    
    /**
     * 照片数量
     */
    val photoCount: Int
        get() = photoUris.size
}
