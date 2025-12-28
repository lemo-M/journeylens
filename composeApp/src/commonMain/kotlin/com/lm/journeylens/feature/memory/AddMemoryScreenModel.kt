package com.lm.journeylens.feature.memory

import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import com.lm.journeylens.feature.memory.model.ExifData
import com.lm.journeylens.feature.memory.model.PendingImport
import com.lm.journeylens.feature.memory.model.PhotoImportResult
import com.lm.journeylens.feature.memory.service.ExifParser
import com.lm.journeylens.feature.memory.service.LivePhotoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 添加记忆页面的 ViewModel
 * 支持普通照片和实况照片导入
 */
class AddMemoryScreenModel(
    private val memoryRepository: MemoryRepository,
    private val exifParser: ExifParser,
    private val livePhotoService: LivePhotoService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // UI 状态
    private val _uiState = MutableStateFlow(AddMemoryUiState())
    val uiState: StateFlow<AddMemoryUiState> = _uiState.asStateFlow()
    
    /**
     * 处理选中的照片
     */
    fun processSelectedPhotos(photoUris: List<String>) {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val pendingImports = mutableListOf<PendingImport>()
            
            // 检测实况照片并解析 EXIF
            val livePhotoResults = livePhotoService.detectLivePhotos(photoUris)
            
            for (result in livePhotoResults) {
                val photoUri = result.livePhotoData.photoUri
                val videoUri = result.livePhotoData.videoUri
                
                // 解析 EXIF
                val exifData = exifParser.parseExif(photoUri)
                val importResult = processExifData(photoUri, exifData, videoUri)
                
                // 转换为待审核项
                pendingImports.add(resultToPendingImport(importResult))
            }
            
            // 智能位置匹配：为无GPS的照片推测位置
            matchLocationsForMissingGps(pendingImports)
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                pendingImports = pendingImports,
                step = ImportStep.REVIEW
            )
        }
    }
    
    /**
     * 处理 EXIF 数据，返回导入结果
     */
    private fun processExifData(
        uri: String, 
        exifData: ExifData, 
        videoUri: String?
    ): PhotoImportResult {
        return when {
            exifData.hasLocation && exifData.timestamp != null -> {
                PhotoImportResult.AutoLocated(
                    photoUri = uri,
                    latitude = exifData.latitude!!,
                    longitude = exifData.longitude!!,
                    timestamp = exifData.timestamp,
                    videoUri = videoUri
                )
            }
            exifData.timestamp != null -> {
                PhotoImportResult.NeedsManualLocation(
                    photoUri = uri,
                    timestamp = exifData.timestamp,
                    videoUri = videoUri
                )
            }
            else -> {
                PhotoImportResult.NoMetadata(
                    photoUri = uri,
                    videoUri = videoUri
                )
            }
        }
    }
    
    /**
     * 将导入结果转换为待审核项
     */
    private fun resultToPendingImport(result: PhotoImportResult): PendingImport {
        return when (result) {
            is PhotoImportResult.AutoLocated -> PendingImport(
                photoUri = result.photoUri,
                videoUri = result.videoUri,
                latitude = result.latitude,
                longitude = result.longitude,
                timestamp = result.timestamp,
                isAutoLocated = true
            )
            is PhotoImportResult.NeedsManualLocation -> PendingImport(
                photoUri = result.photoUri,
                videoUri = result.videoUri,
                latitude = result.suggestedLatitude,
                longitude = result.suggestedLongitude,
                timestamp = result.timestamp,
                isAutoLocated = false,
                isSuggested = result.suggestedLatitude != null
            )
            is PhotoImportResult.NoMetadata -> PendingImport(
                photoUri = result.photoUri,
                videoUri = result.videoUri,
                latitude = null,
                longitude = null,
                timestamp = null,
                isAutoLocated = false
            )
        }
    }
    
    /**
     * 智能位置匹配：为无GPS的照片根据拍摄时间推测位置
     */
    private fun matchLocationsForMissingGps(imports: MutableList<PendingImport>) {
        // 找出有位置的照片
        val locatedPhotos = imports.filter { it.latitude != null && it.longitude != null }
        if (locatedPhotos.isEmpty()) return
        
        // 为没有位置的照片推测
        imports.forEachIndexed { index, import ->
            if (import.latitude == null && import.timestamp != null) {
                // 找时间最接近的有位置的照片
                val nearest = locatedPhotos.minByOrNull { 
                    kotlin.math.abs((it.timestamp ?: 0) - import.timestamp)
                }
                
                if (nearest != null) {
                    // 如果时间差在1小时内，使用其位置作为推测
                    val timeDiff = kotlin.math.abs((nearest.timestamp ?: 0) - import.timestamp)
                    if (timeDiff <= 3600000) { // 1 hour
                        imports[index] = import.copy(
                            latitude = nearest.latitude,
                            longitude = nearest.longitude,
                            isSuggested = true
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 更新待审核项的位置（手动选点）
     */
    fun updatePendingLocation(index: Int, latitude: Double, longitude: Double) {
        val currentList = _uiState.value.pendingImports.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = currentList[index].copy(
                latitude = latitude,
                longitude = longitude,
                isConfirmed = true
            )
            _uiState.value = _uiState.value.copy(pendingImports = currentList)
        }
    }
    
    /**
     * 更新待审核项的 emoji
     */
    fun updatePendingEmoji(index: Int, emoji: String) {
        val currentList = _uiState.value.pendingImports.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = currentList[index].copy(emoji = emoji)
            _uiState.value = _uiState.value.copy(pendingImports = currentList)
        }
    }
    
    /**
     * 确认导入所有照片
     */
    fun confirmImport() {
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val memories = _uiState.value.pendingImports
                .filter { it.latitude != null && it.longitude != null }
                .map { pending ->
                    Memory(
                        latitude = pending.latitude!!,
                        longitude = pending.longitude!!,
                        timestamp = pending.timestamp ?: System.currentTimeMillis(),
                        photoUri = pending.photoUri,
                        videoUri = pending.videoUri,
                        emoji = pending.emoji,
                        isAutoLocated = pending.isAutoLocated,
                        isLivePhoto = pending.isLivePhoto
                    )
                }
            
            memoryRepository.insertAll(memories)
            
            _uiState.value = AddMemoryUiState(
                step = ImportStep.SUCCESS,
                importedCount = memories.size
            )
        }
    }
    
    /**
     * 重置状态
     */
    fun reset() {
        _uiState.value = AddMemoryUiState()
    }
}

/**
 * 导入步骤
 */
enum class ImportStep {
    SELECT,   // 选择照片
    REVIEW,   // 审核确认
    SUCCESS   // 导入成功
}

/**
 * UI 状态
 */
data class AddMemoryUiState(
    val step: ImportStep = ImportStep.SELECT,
    val isLoading: Boolean = false,
    val pendingImports: List<PendingImport> = emptyList(),
    val importedCount: Int = 0
)
