package com.lm.journeylens.core.service

/**
 * iOS 定位服务 - 占位实现
 */
class IosLocationService : LocationService {
    override suspend fun getCurrentLocation(): LocationResult? {
        // TODO: 使用 CoreLocation 实现
        return null
    }
}
