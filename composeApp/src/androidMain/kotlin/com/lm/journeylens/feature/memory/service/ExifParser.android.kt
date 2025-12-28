package com.lm.journeylens.feature.memory.service

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.lm.journeylens.feature.memory.model.ExifData
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Android 平台 EXIF 解析器实现
 * 优先读取 EXIF，如果没有 GPS 则回退到 MediaStore
 */
actual class ExifParser(
    private val context: Context
) {
    
    actual suspend fun parseExif(photoUri: String): ExifData {
        return try {
            val uri = Uri.parse(photoUri)
            
            // 先尝试从 EXIF 读取
            val exifData = parseFromExif(uri)
            
            // 如果 EXIF 没有位置，尝试从 MediaStore 读取
            if (!exifData.hasLocation) {
                val mediaStoreData = parseFromMediaStore(uri)
                if (mediaStoreData.hasLocation) {
                    return ExifData(
                        latitude = mediaStoreData.latitude,
                        longitude = mediaStoreData.longitude,
                        timestamp = exifData.timestamp ?: mediaStoreData.timestamp
                    )
                }
            }
            
            exifData
        } catch (e: Exception) {
            e.printStackTrace()
            ExifData(null, null, null)
        }
    }
    
    /**
     * 从 EXIF 解析
     */
    private fun parseFromExif(uri: Uri): ExifData {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ExifData(null, null, null)
            
            val exif = ExifInterface(inputStream)
            
            // 解析 GPS 坐标
            val latLong = FloatArray(2)
            val hasGps = exif.getLatLong(latLong)
            
            // 验证 GPS 数据是否有效（非 0,0）
            val validGps = hasGps && (latLong[0] != 0f || latLong[1] != 0f)
            
            // 解析拍摄时间
            val timestamp = parseDateTime(exif)
            
            inputStream.close()
            
            ExifData(
                latitude = if (validGps) latLong[0].toDouble() else null,
                longitude = if (validGps) latLong[1].toDouble() else null,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExifData(null, null, null)
        }
    }
    
    /**
     * 从 MediaStore 解析（系统数据库）
     */
    private fun parseFromMediaStore(uri: Uri): ExifData {
        return try {
            val projection = arrayOf(
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.DATE_TAKEN
            )
            
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val latIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE)
                    val lngIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE)
                    val dateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                    
                    val latitude = if (latIndex >= 0) cursor.getDouble(latIndex) else 0.0
                    val longitude = if (lngIndex >= 0) cursor.getDouble(lngIndex) else 0.0
                    val dateTaken = if (dateIndex >= 0) cursor.getLong(dateIndex) else 0L
                    
                    // 验证位置是否有效
                    val validLocation = latitude != 0.0 || longitude != 0.0
                    
                    return ExifData(
                        latitude = if (validLocation) latitude else null,
                        longitude = if (validLocation) longitude else null,
                        timestamp = if (dateTaken > 0) dateTaken else null
                    )
                }
            }
            
            ExifData(null, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
            ExifData(null, null, null)
        }
    }
    
    /**
     * 解析 EXIF 日期时间
     */
    private fun parseDateTime(exif: ExifInterface): Long? {
        // 尝试多种日期标签
        val dateString = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED)
            ?: exif.getAttribute(ExifInterface.TAG_DATETIME)
            ?: return null
        
        return try {
            val format = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US)
            format.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
}
