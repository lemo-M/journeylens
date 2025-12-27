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
    return Room.databaseBuilder(
        context = appContext,
        klass = AppDatabase::class.java,
        name = "journeylens.db"
    )
}
