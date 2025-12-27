package com.lm.journeylens.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lm.journeylens.core.database.dao.MemoryDao
import com.lm.journeylens.core.database.entity.Memory

/**
 * JourneyLens 应用数据库
 */
@Database(
    entities = [Memory::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}
