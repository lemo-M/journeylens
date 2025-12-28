package com.lm.journeylens.feature.memory.service

import com.lm.journeylens.feature.memory.model.LivePhotoData
import com.lm.journeylens.feature.memory.model.LivePhotoDetectionResult
import com.lm.journeylens.feature.memory.model.LivePhotoType

/**
 * 实况照片服务接口 - expect/actual 模式
 * 提供跨平台的实况照片检测和处理
 */
expect class LivePhotoService {
    /**
     * 检测照片是否为实况照片，并返回关联的视频路径
     * @param photoUri 静态照片的 URI
     * @return 检测结果，包含类型和关联数据
     */
    suspend fun detectLivePhoto(photoUri: String): LivePhotoDetectionResult
    
    /**
     * 批量检测多张照片
     */
    suspend fun detectLivePhotos(photoUris: List<String>): List<LivePhotoDetectionResult>
}
