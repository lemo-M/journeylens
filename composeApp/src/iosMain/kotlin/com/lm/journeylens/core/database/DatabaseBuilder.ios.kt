package com.lm.journeylens.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

/**
 * iOS 平台的数据库构建器
 * Room 会通过 @ConstructedBy 注解自动处理数据库实例化
 */
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/journeylens.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}
