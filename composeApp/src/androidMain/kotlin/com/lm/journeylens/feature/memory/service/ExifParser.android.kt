package com.lm.journeylens.feature.memory.service

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.lm.journeylens.feature.memory.model.ExifData
import java.text.SimpleDateFormat
import java.util.Locale

private const val TAG = "ExifParser"

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
            Log.d(TAG, "Parsing photo: $photoUri")
            
            // 先尝试从 EXIF 读取
            val exifData = parseFromExif(uri)
            Log.d(TAG, "EXIF result: lat=${exifData.latitude}, lng=${exifData.longitude}, time=${exifData.timestamp}")
            
            // 如果 EXIF 没有位置，尝试从 MediaStore 读取
            if (!exifData.hasLocation) {
                Log.d(TAG, "No EXIF location, trying MediaStore...")
                
                // 尝试多种方式获取 MediaStore ID
                val mediaStoreData = parseFromMediaStoreByUri(uri)
                    ?: parseFromMediaStoreById(uri)
                
                if (mediaStoreData != null && mediaStoreData.hasLocation) {
                    Log.d(TAG, "MediaStore result: lat=${mediaStoreData.latitude}, lng=${mediaStoreData.longitude}")
                    return ExifData(
                        latitude = mediaStoreData.latitude,
                        longitude = mediaStoreData.longitude,
                        timestamp = exifData.timestamp ?: mediaStoreData.timestamp
                    )
                } else {
                    Log.d(TAG, "MediaStore also has no location")
                }
            }
            
            exifData
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing: ${e.message}", e)
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

            // 解析 GPS 坐标
            val latLong = exif.latLong
            
            // 验证 GPS 数据是否有效（非 0,0）
            val validGps = latLong != null && (latLong[0] != 0.0 || latLong[1] != 0.0)
            
            // 解析拍摄时间
            val timestamp = parseDateTime(exif)
            
            inputStream.close()
            
            ExifData(
                latitude = if (validGps) latLong!![0] else null,
                longitude = if (validGps) latLong!![1] else null,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            Log.e(TAG, "EXIF parse error: ${e.message}")
            ExifData(null, null, null)
        }
    }
    
    /**
     * 直接使用 URI 查询 MediaStore
     */
    private fun parseFromMediaStoreByUri(uri: Uri): ExifData? {
        // MediaStore.Images.Media.LATITUDE/LONGITUDE 在 API 29+ 已废弃
        // 但为了兼容旧版本，这里保留并镇压警告，现代设备应主要依赖 Exif 解析
        @Suppress("DEPRECATION")
        return try {
            val projection = arrayOf(
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media._ID
            )
            
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val latIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE)
                    val lngIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE)
                    val dateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                    
                    var latitude = 0.0
                    var longitude = 0.0
                    var dateTaken = 0L
                    
                    if (latIndex >= 0 && !cursor.isNull(latIndex)) {
                        latitude = cursor.getDouble(latIndex)
                    }
                    if (lngIndex >= 0 && !cursor.isNull(lngIndex)) {
                        longitude = cursor.getDouble(lngIndex)
                    }
                    if (dateIndex >= 0 && !cursor.isNull(dateIndex)) {
                        dateTaken = cursor.getLong(dateIndex)
                    }
                    
                    Log.d(TAG, "MediaStore by URI: lat=$latitude, lng=$longitude, date=$dateTaken")
                    
                    // 验证位置是否有效
                    val validLocation = latitude != 0.0 || longitude != 0.0
                    
                    return ExifData(
                        latitude = if (validLocation) latitude else null,
                        longitude = if (validLocation) longitude else null,
                        timestamp = if (dateTaken > 0) dateTaken else null
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore by URI error: ${e.message}")
            null
        }
    }
    
    /**
     * 通过从 URI 提取 ID 查询 MediaStore
     * 适用于 Photo Picker 返回的 picker:// 或 content://media/ URI
     */
    private fun parseFromMediaStoreById(uri: Uri): ExifData? {
        return try {
            // 尝试从 URI 路径中提取 ID
            val id = ContentUris.parseId(uri)
            if (id <= 0) return null
            
            Log.d(TAG, "Extracted media ID: $id")
            
            // 构建 MediaStore URI
            val mediaUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            
            return parseFromMediaStoreByUri(mediaUri)
        } catch (e: Exception) {
            Log.e(TAG, "MediaStore by ID error: ${e.message}")
            null
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
