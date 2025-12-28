package com.lm.journeylens.core.util

import kotlin.math.round

/**
 * 格式化经纬度，保留 2 位小数
 */
fun Double.formatCoordinate(): String {
    // KMP 中没有 String.format，使用数学运算处理
    // 另一种方式是使用 BigDecimal，但在 Common 中可能需要额外依赖
    // 简单的位运算处理显示需求
    return ((this * 100).toInt() / 100.0).toString()
}

/**
 * 格式化经纬度对
 */
fun formatCoordinates(lat: Double, lng: Double): String {
    return "📍 ${lat.formatCoordinate()}, ${lng.formatCoordinate()}"
}
