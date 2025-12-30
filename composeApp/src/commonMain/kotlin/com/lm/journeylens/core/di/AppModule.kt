package com.lm.journeylens.core.di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.lm.journeylens.core.database.AppDatabase
import com.lm.journeylens.core.database.getDatabaseBuilder
import com.lm.journeylens.core.repository.MemoryRepository
import com.lm.journeylens.core.repository.MemoryRepositoryImpl
import com.lm.journeylens.feature.memory.AddMemoryScreenModel
import com.lm.journeylens.feature.map.MapScreenModel
import com.lm.journeylens.feature.timeline.TimelineScreenModel
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
            .fallbackToDestructiveMigration(dropAllTables = true)  // 开发阶段使用破坏性迁移
            .build()
    }
    
    // DAO
    single { get<AppDatabase>().memoryDao() }
    
    // Repository
    single<MemoryRepository> { MemoryRepositoryImpl(get()) }
}

/**
 * 功能模块 - 服务和 ScreenModel
 */
val featureModule = module {
    // Global State
    single { com.lm.journeylens.feature.memory.domain.state.GlobalCreationState() }

    // UseCases
    factory { com.lm.journeylens.feature.memory.domain.usecase.GetDraftUseCase(get()) }
    factory { com.lm.journeylens.feature.memory.domain.usecase.SaveDraftUseCase(get()) }
    factory { com.lm.journeylens.feature.memory.domain.usecase.DiscardDraftUseCase(get()) }
    factory { com.lm.journeylens.feature.memory.domain.usecase.CreateMemoryUseCase(get()) }

    // DraftManager - 草稿管理器
    factory { com.lm.journeylens.feature.memory.domain.DraftManager(get(), get(), get()) }

    // Map ScreenModel
    factory { MapScreenModel(get(), get()) }
    
    // AddMemory ScreenModel（通过 DraftManager 简化依赖）
    factory { AddMemoryScreenModel(get(), get(), get()) }
    
    // Timeline ScreenModel
    factory { TimelineScreenModel(get()) }
}

/**
 * 所有 Koin 模块
 */
val appModules = listOf(coreModule, platformModule, featureModule)

