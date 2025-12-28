package com.lm.journeylens.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 照片 URI 列表的 TypeConverter
 */
class PhotoUrisConverter {
    private val json = Json { ignoreUnknownKeys = true }
    
    @TypeConverter
    fun fromString(value: String): List<String> {
        return try {
            json.decodeFromString<List<String>>(value)
        } catch (e: Exception) {
            // 兼容旧数据（单个 URI）
            if (value.isNotEmpty()) listOf(value) else emptyList()
        }
    }
    
    @TypeConverter
    fun toString(list: List<String>): String {
        return json.encodeToString(list)
    }
}

/**
 * 记忆实体 - 存储用户的记忆点
 * 一个记忆点可以包含多张照片
 */
@Entity(tableName = "memories")
@TypeConverters(PhotoUrisConverter::class)
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 位置信息
    val latitude: Double,
    val longitude: Double,
    val locationName: String? = null,
    
    // 时间信息
    val timestamp: Long,  // 主要时间戳
    val createdAt: Long = System.currentTimeMillis(),
    
    // 照片组（多张照片 URI）
    val photoUris: List<String> = emptyList(),
    
    // 标记
    val emoji: String = "📍",
    
    // 描述（可长文）
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
