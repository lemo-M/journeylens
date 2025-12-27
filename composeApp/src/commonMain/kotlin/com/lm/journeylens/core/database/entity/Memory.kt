package com.lm.journeylens.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 记忆实体 - 存储用户的记忆点
 * 每个记忆包含位置、时间、照片和可选的备注
 */
@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 位置信息
    val latitude: Double,
    val longitude: Double,
    val locationName: String? = null,  // 可选的地点名称
    
    // 时间信息
    val timestamp: Long,  // 照片拍摄时间 (毫秒)
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    
    // 内容
    val photoUri: String,  // 照片本地路径
    val title: String? = null,  // 可选标题
    val note: String? = null,  // 可选备注
    
    // 元数据
    val mood: String? = null,  // 心情标签
    val isAutoLocated: Boolean = true,  // 是否自动定位（EXIF）
)
