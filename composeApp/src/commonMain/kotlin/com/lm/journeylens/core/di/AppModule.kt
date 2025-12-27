package com.lm.journeylens.core.di

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lm.journeylens.core.database.AppDatabase
import com.lm.journeylens.core.database.getDatabaseBuilder
import com.lm.journeylens.core.repository.MemoryRepository
import com.lm.journeylens.core.repository.MemoryRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.dsl.module

/**
 * 核心模块 - 数据库和仓库
 */
val coreModule = module {
    // 数据库
    single<AppDatabase> {
        getDatabaseBuilder()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
    
    // DAO
    single { get<AppDatabase>().memoryDao() }
    
    // Repository
    single<MemoryRepository> { MemoryRepositoryImpl(get()) }
}

/**
 * 所有 Koin 模块
 */
val appModules = listOf(coreModule)
