package com.lm.journeylens.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lm.journeylens.core.database.dao.MemoryDao
import com.lm.journeylens.core.database.entity.Memory

/**
 * JourneyLens 应用数据库
 * version 3: 添加 emoji 和 label 字段
 */
@Database(
    entities = [Memory::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}
