package com.lm.journeylens.feature.memory.service

/**
 * iOS 定位服务 - 占位实现
 */
actual class LocationService {
    actual suspend fun getCurrentLocation(): LocationResult? {
        // TODO: 使用 CoreLocation 实现
        return null
    }
}
