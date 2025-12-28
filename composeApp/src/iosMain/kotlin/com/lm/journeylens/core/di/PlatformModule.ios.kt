package com.lm.journeylens.core.di

import com.lm.journeylens.feature.memory.service.ExifParser
import org.koin.dsl.module

/**
 * iOS 平台特定的 Koin 模块
 */
actual val platformModule = module {
    // EXIF 解析器 - iOS 实现
    factory { ExifParser() }
}
