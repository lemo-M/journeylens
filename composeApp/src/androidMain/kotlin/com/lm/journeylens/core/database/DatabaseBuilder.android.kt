package com.lm.journeylens.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private lateinit var appContext: Context

/**
 * 初始化数据库上下文（需要在 Application 中调用）
 */
fun initDatabase(context: Context) {
    appContext = context.applicationContext
}

/**
 * Android 平台的数据库构建器
 */
actual fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    // 使用应用内部数据目录的完整路径
    val dbFile = appContext.getDatabasePath("journeylens.db")
    
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
