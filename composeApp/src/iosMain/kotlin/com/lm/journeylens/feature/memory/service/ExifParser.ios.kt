package com.lm.journeylens.feature.memory.service

import com.lm.journeylens.feature.memory.model.ExifData

/**
 * iOS 平台 EXIF 解析器实现
 * TODO: 使用 ImageIO/Photos framework 实现
 */
actual class ExifParser {
    
    actual suspend fun parseExif(photoUri: String): ExifData {
        // iOS 实现待完成
        // 需要使用 CGImageSource 和 kCGImagePropertyExifDictionary
        return ExifData(null, null, null)
    }
}
