package com.lm.journeylens.core.database

import androidx.room.RoomDatabase

/**
 * 数据库构建器 - expect/actual 模式
 * 每个平台需要提供自己的 RoomDatabase.Builder 实现
 */
expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
