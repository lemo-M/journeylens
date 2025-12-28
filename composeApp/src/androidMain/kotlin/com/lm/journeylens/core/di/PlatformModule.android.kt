package com.lm.journeylens.core.di

import com.lm.journeylens.feature.memory.service.ExifParser
import com.lm.journeylens.feature.memory.service.LivePhotoService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android 平台特定的 Koin 模块
 */
actual val platformModule = module {
    // EXIF 解析器 - 需要 Android Context
    factory { ExifParser(androidContext()) }
    
    // 实况照片服务 - 需要 Android Context
    factory { LivePhotoService(androidContext()) }
}
