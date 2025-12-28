package com.lm.journeylens.feature.memory.service

/**
 * 定位结果
 */
data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

/**
 * 定位服务 - expect 声明
 */
expect class LocationService {
    /**
     * 获取当前位置（一次性）
     */
    suspend fun getCurrentLocation(): LocationResult?
}
