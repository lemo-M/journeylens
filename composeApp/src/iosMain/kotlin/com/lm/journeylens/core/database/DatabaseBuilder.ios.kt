package com.lm.journeylens.core.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

/**
 * iOS 平台的数据库构建器
 */
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/Documents/journeylens.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}
