package com.lm.journeylens.feature.memory.service

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.lm.journeylens.feature.memory.model.ExifData
import java.text.SimpleDateFormat
import java.util.Locale

private lateinit var appContext: Context

/**
 * 初始化 EXIF 解析器上下文
 */
fun initExifParser(context: Context) {
    appContext = context.applicationContext
}

/**
 * Android 平台 EXIF 解析器实现
 */
actual class ExifParser {
    
    actual suspend fun parseExif(photoUri: String): ExifData {
        return try {
            val uri = Uri.parse(photoUri)
            val inputStream = appContext.contentResolver.openInputStream(uri)
                ?: return ExifData(null, null, null)
            
            val exif = ExifInterface(inputStream)
            
            // 解析 GPS 坐标
            val latLong = FloatArray(2)
            val hasGps = exif.getLatLong(latLong)
            
            // 解析拍摄时间
            val timestamp = parseDateTime(exif)
            
            inputStream.close()
            
            ExifData(
                latitude = if (hasGps) latLong[0].toDouble() else null,
                longitude = if (hasGps) latLong[1].toDouble() else null,
                timestamp = timestamp
            )
        } catch (e: Exception) {
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
