package com.lm.journeylens.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lm.journeylens.core.database.dao.MemoryDao
import com.lm.journeylens.core.database.entity.MemoryEntity
import com.lm.journeylens.core.database.entity.PhotoUrisConverter

/**
 * JourneyLens 应用数据库
 * version 4: 重构为一个地点多张照片
 */
@Database(
    entities = [MemoryEntity::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(PhotoUrisConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}
