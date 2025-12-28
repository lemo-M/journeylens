package com.lm.journeylens.feature.memory.service

import com.lm.journeylens.feature.memory.model.ExifData

/**
 * EXIF 解析器接口 - expect/actual 实现
 * Android: 接收 Context 参数
 * iOS: 无参数
 */
expect class ExifParser {
    /**
     * 从照片 URI 解析 EXIF 数据
     */
    suspend fun parseExif(photoUri: String): ExifData
}
