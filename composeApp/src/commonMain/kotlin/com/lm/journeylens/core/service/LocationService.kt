package com.lm.journeylens.core.service

/**
 * 定位结果
 */
data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)

/**
 * 定位服务接口
 * 抽象具体平台的定位实现
 */
interface LocationService {
    /**
     * 获取当前位置（一次性）
     */
    suspend fun getCurrentLocation(): LocationResult?
}
