package com.lm.journeylens.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lm.journeylens.core.database.dao.MemoryDao
import com.lm.journeylens.core.database.entity.Memory

/**
 * JourneyLens 应用数据库
 * version 2: 添加 videoUri 和 isLivePhoto 字段
 */
@Database(
    entities = [Memory::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}
