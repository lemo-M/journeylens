package com.lm.journeylens.core.service

import android.content.Context
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 高德定位服务 - Android 实现
 */
class AndroidLocationService(private val context: Context) : LocationService {
    
    /**
     * 获取当前位置（一次性）
     */
    override suspend fun getCurrentLocation(): LocationResult? {
        return suspendCancellableCoroutine { continuation ->
            var locationClient: AMapLocationClient? = null
            
            try {
                // 设置隐私合规
                AMapLocationClient.updatePrivacyShow(context, true, true)
                AMapLocationClient.updatePrivacyAgree(context, true)
                
                locationClient = AMapLocationClient(context)
                
                // 配置定位参数
                val option = AMapLocationClientOption().apply {
                    // 高精度模式
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    // 只定位一次
                    isOnceLocation = true
                    // 30 秒超时
                    httpTimeOut = 30000
                    // 返回地址信息
                    isNeedAddress = true
                }
                locationClient.setLocationOption(option)
                
                // 设置监听器
                val listener = AMapLocationListener { location: AMapLocation? ->
                    if (location != null && location.errorCode == 0) {
                        // 定位成功
                        val result = LocationResult(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = location.address
                        )
                        continuation.resume(result)
                    } else {
                        // 定位失败
                        android.util.Log.e("LocationService", "定位失败: errCode=${location?.errorCode} errInfo=${location?.errorInfo}")
                        continuation.resume(null)
                    }
                    locationClient?.stopLocation()
                    locationClient?.onDestroy()
                }
                locationClient.setLocationListener(listener)
                
                // 开始定位
                locationClient.startLocation()
                
                // 取消时清理
                continuation.invokeOnCancellation {
                    locationClient.stopLocation()
                    locationClient.onDestroy()
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                locationClient?.onDestroy()
                continuation.resume(null)
            }
        }
    }
}
