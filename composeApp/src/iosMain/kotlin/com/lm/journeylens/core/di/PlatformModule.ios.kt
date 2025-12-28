package com.lm.journeylens.core.di

import com.lm.journeylens.feature.memory.service.ExifParser
import com.lm.journeylens.feature.memory.service.LivePhotoService
import org.koin.dsl.module

/**
 * iOS 平台特定的 Koin 模块
 */
actual val platformModule = module {
    // EXIF 解析器 - iOS 实现
    factory { ExifParser() }
    
    // 实况照片服务 - iOS 实现
    factory { LivePhotoService() }
    
    // 草稿服务 - iOS 实现
    factory<com.lm.journeylens.feature.memory.service.DraftService> { com.lm.journeylens.feature.memory.service.IosDraftService() }

    // 定位服务 - iOS 实现
    factory<com.lm.journeylens.core.service.LocationService> { com.lm.journeylens.core.service.IosLocationService() }
}
