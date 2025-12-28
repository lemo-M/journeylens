package com.lm.journeylens.feature.memory.service

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.lm.journeylens.feature.memory.model.LivePhotoData
import com.lm.journeylens.feature.memory.model.LivePhotoDetectionResult
import com.lm.journeylens.feature.memory.model.LivePhotoType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android 实况照片服务实现
 * 当前支持：小米分离式 (JPG + MP4)
 * 预留接口：Google Motion Photo, 三星 XMP
 */
actual class LivePhotoService(
    private val context: Context
) {
    
    actual suspend fun detectLivePhoto(photoUri: String): LivePhotoDetectionResult {
        return withContext(Dispatchers.IO) {
            // 尝试各种检测策略
            detectXiaomiLivePhoto(photoUri)
                ?: detectGoogleMotionPhoto(photoUri)
                ?: detectSamsungMotionPhoto(photoUri)
                ?: LivePhotoDetectionResult(
                    type = LivePhotoType.STATIC,
                    livePhotoData = LivePhotoData(photoUri = photoUri)
                )
        }
    }
    
    actual suspend fun detectLivePhotos(photoUris: List<String>): List<LivePhotoDetectionResult> {
        return photoUris.map { detectLivePhoto(it) }
    }
    
    /**
     * 检测小米实况照片
     * 小米的实况照片是分离的 JPG + MP4 文件
     * 关联方式：相同的文件名（不同扩展名）或通过拍摄时间关联
     */
    private fun detectXiaomiLivePhoto(photoUri: String): LivePhotoDetectionResult? {
        try {
            val uri = Uri.parse(photoUri)
            
            // 获取照片的 display name 和 date taken
            val photoInfo = getPhotoInfo(uri) ?: return null
            
            // 查找关联的视频文件
            val videoUri = findAssociatedVideo(photoInfo)
            
            if (videoUri != null) {
                return LivePhotoDetectionResult(
                    type = LivePhotoType.XIAOMI_SEPARATE,
                    livePhotoData = LivePhotoData(
                        photoUri = photoUri,
                        videoUri = videoUri
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    /**
     * 获取照片信息
     */
    private fun getPhotoInfo(photoUri: Uri): PhotoInfo? {
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATA  // 文件路径
        )
        
        context.contentResolver.query(photoUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayName = cursor.getString(
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                )
                val dateTaken = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                )
                val filePath = cursor.getString(
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                )
                
                return PhotoInfo(
                    displayName = displayName,
                    dateTaken = dateTaken,
                    filePath = filePath,
                    baseName = displayName.substringBeforeLast(".")
                )
            }
        }
        return null
    }
    
    /**
     * 查找关联的视频文件
     * 策略1: 相同文件名，不同扩展名 (JPG -> MP4)
     * 策略2: 拍摄时间在 1 秒内的视频
     */
    private fun findAssociatedVideo(photoInfo: PhotoInfo): String? {
        // 策略1: 查找同名 MP4 文件
        val sameNameVideo = findVideoByName(photoInfo.baseName)
        if (sameNameVideo != null) return sameNameVideo
        
        // 策略2: 查找拍摄时间接近的视频（1秒内）
        return findVideoByTime(photoInfo.dateTaken, 1000)
    }
    
    /**
     * 通过文件名查找视频
     */
    private fun findVideoByName(baseName: String): String? {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME
        )
        
        // 查找以相同 baseName 开头的视频
        val selection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("$baseName%")
        
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                )
                return ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()
            }
        }
        return null
    }
    
    /**
     * 通过拍摄时间查找视频
     */
    private fun findVideoByTime(photoTime: Long, toleranceMs: Long): String? {
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATE_TAKEN
        )
        
        val selection = "${MediaStore.Video.Media.DATE_TAKEN} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(
            (photoTime - toleranceMs).toString(),
            (photoTime + toleranceMs).toString()
        )
        
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Video.Media.DATE_TAKEN} ASC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                )
                return ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                ).toString()
            }
        }
        return null
    }
    
    /**
     * 检测 Google Pixel Motion Photo
     * TODO: 实现从 JPEG 末尾提取嵌入视频
     */
    private fun detectGoogleMotionPhoto(photoUri: String): LivePhotoDetectionResult? {
        // 预留接口 - 需要解析 JPEG 二进制格式
        // Motion Photo 视频嵌入在 JPEG 文件末尾
        return null
    }
    
    /**
     * 检测三星 Motion Photo
     * TODO: 实现 XMP 元数据解析
     */
    private fun detectSamsungMotionPhoto(photoUri: String): LivePhotoDetectionResult? {
        // 预留接口 - 需要解析 XMP 元数据
        return null
    }
    
    /**
     * 照片信息数据类
     */
    private data class PhotoInfo(
        val displayName: String,
        val dateTaken: Long,
        val filePath: String,
        val baseName: String
    )
}
