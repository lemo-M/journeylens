package com.lm.journeylens.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
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
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}

/**
 * Room 数据库构造器
 * Room 编译器会自动生成 actual 实现
 */
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
