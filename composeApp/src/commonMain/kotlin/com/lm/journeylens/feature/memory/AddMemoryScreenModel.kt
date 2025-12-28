package com.lm.journeylens.feature.memory

import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 添加记忆页面的 ViewModel
 * 新流程：选位置 → 选照片 → 填写详情
 */
class AddMemoryScreenModel(
    private val memoryRepository: MemoryRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // UI 状态
    private val _uiState = MutableStateFlow(AddMemoryUiState())
    val uiState: StateFlow<AddMemoryUiState> = _uiState.asStateFlow()
    
    /**
     * 步骤 1: 设置位置（当前定位）
     */
    fun setLocationFromGps(latitude: Double, longitude: Double, locationName: String? = null) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            isAutoLocated = true,
            step = ImportStep.PHOTOS
        )
    }
    
    /**
     * 步骤 1: 设置位置（地图选点）
     */
    fun setLocationFromMap(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            isAutoLocated = false,
            step = ImportStep.PHOTOS
        )
    }
    
    /**
     * 步骤 2: 添加照片
     */
    fun addPhotos(photoUris: List<String>) {
        val currentPhotos = _uiState.value.photoUris.toMutableList()
        currentPhotos.addAll(photoUris)
        _uiState.value = _uiState.value.copy(photoUris = currentPhotos)
    }
    
    /**
     * 步骤 2: 移除照片
     */
    fun removePhoto(index: Int) {
        val currentPhotos = _uiState.value.photoUris.toMutableList()
        if (index in currentPhotos.indices) {
            currentPhotos.removeAt(index)
            _uiState.value = _uiState.value.copy(photoUris = currentPhotos)
        }
    }
    
    /**
     * 步骤 2: 确认照片，进入详情步骤
     */
    fun confirmPhotos() {
        if (_uiState.value.photoUris.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(step = ImportStep.DETAILS)
        }
    }
    
    /**
     * 步骤 3: 更新 emoji
     */
    fun updateEmoji(emoji: String) {
        _uiState.value = _uiState.value.copy(emoji = emoji)
    }
    
    /**
     * 步骤 3: 更新备注
     */
    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }
    
    /**
     * 保存记忆
     */
    fun saveMemory() {
        val state = _uiState.value
        if (state.latitude == null || state.longitude == null || state.photoUris.isEmpty()) {
            return
        }
        
        scope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val memory = Memory(
                latitude = state.latitude,
                longitude = state.longitude,
                locationName = state.locationName,
                timestamp = System.currentTimeMillis(),
                photoUris = state.photoUris,
                emoji = state.emoji,
                note = state.note?.takeIf { it.isNotBlank() },
                isAutoLocated = state.isAutoLocated
            )
            
            memoryRepository.insert(memory)
            
            _uiState.value = AddMemoryUiState(step = ImportStep.SUCCESS)
        }
    }
    
    /**
     * 返回上一步
     */
    fun goBack() {
        val currentStep = _uiState.value.step
        val previousStep = when (currentStep) {
            ImportStep.PHOTOS -> ImportStep.LOCATION
            ImportStep.DETAILS -> ImportStep.PHOTOS
            else -> currentStep
        }
        _uiState.value = _uiState.value.copy(step = previousStep)
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
    LOCATION,  // 选择位置
    PHOTOS,    // 选择照片
    DETAILS,   // 填写详情
    SUCCESS    // 完成
}

/**
 * UI 状态
 */
data class AddMemoryUiState(
    val step: ImportStep = ImportStep.LOCATION,
    val isLoading: Boolean = false,
    
    // 位置
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val isAutoLocated: Boolean = false,
    
    // 照片
    val photoUris: List<String> = emptyList(),
    
    // 详情
    val emoji: String = "📍",
    val note: String? = null
)
