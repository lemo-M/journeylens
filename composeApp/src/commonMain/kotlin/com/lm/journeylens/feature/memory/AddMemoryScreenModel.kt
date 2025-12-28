package com.lm.journeylens.feature.memory

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import com.lm.journeylens.feature.memory.service.DraftService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

/**
 * 添加记忆页面的 ViewModel
 * 新流程：选位置 → 选照片 → 填写详情
 */
class AddMemoryScreenModel(
    private val memoryRepository: MemoryRepository,
    private val draftService: DraftService
) : ScreenModel {
    
    // UI 状态
    private val _uiState = MutableStateFlow(AddMemoryUiState())
    val uiState: StateFlow<AddMemoryUiState> = _uiState.asStateFlow()
    
    init {
        loadDraft()
    }
    
    fun loadDraft() {
        screenModelScope.launch {
            val draft = draftService.loadDraft()
            if (draft != null) {
                _uiState.value = draft
            } else {
                // 如果没有草稿，初始化默认状态
                _uiState.value = AddMemoryUiState()
            }
        }
    }
    
    /**
     * 更新状态并自动保存草稿
     */
    private fun updateState(update: (AddMemoryUiState) -> AddMemoryUiState) {
        val newState = update(_uiState.value)
        _uiState.value = newState
        
        // 自动保存草稿 (除了成功状态)
        if (newState.step != ImportStep.SUCCESS) {
            screenModelScope.launch {
                draftService.saveDraft(newState)
            }
        }
    }
    
    /**
     * 步骤 1: 设置位置（当前定位）
     */
    fun setLocationFromGps(latitude: Double, longitude: Double, locationName: String? = null) {
        updateState { state ->
            state.copy(
                latitude = latitude,
                longitude = longitude,
                locationName = locationName,
                isAutoLocated = true,
                step = ImportStep.PHOTOS
            )
        }
    }
    
    /**
     * 步骤 1: 设置位置（地图选点）
     */
    fun setLocationFromMap(latitude: Double, longitude: Double) {
        updateState { state ->
            state.copy(
                latitude = latitude,
                longitude = longitude,
                isAutoLocated = false,
                step = ImportStep.PHOTOS
            )
        }
    }
    
    /**
     * 步骤 2: 添加照片
     */
    fun addPhotos(photoUris: List<String>) {
        updateState { state ->
            val currentPhotos = state.photoUris.toMutableList()
            currentPhotos.addAll(photoUris)
            state.copy(photoUris = currentPhotos)
        }
    }
    
    /**
     * 步骤 2: 移除照片
     */
    fun removePhoto(index: Int) {
        updateState { state ->
            val currentPhotos = state.photoUris.toMutableList()
            if (index in currentPhotos.indices) {
                currentPhotos.removeAt(index)
                state.copy(photoUris = currentPhotos)
            } else {
                state
            }
        }
    }
    
    /**
     * 步骤 2: 确认照片，进入详情步骤
     */
    fun confirmPhotos() {
        updateState { state ->
            if (state.photoUris.isNotEmpty()) {
                state.copy(step = ImportStep.DETAILS)
            } else {
                state
            }
        }
    }
    
    /**
     * 步骤 3: 更新 emoji
     */
    fun updateEmoji(emoji: String) {
        updateState { it.copy(emoji = emoji) }
    }
    
    /**
     * 步骤 3: 更新备注
     */
    fun updateNote(note: String) {
        updateState { it.copy(note = note) }
    }
    
    /**
     * 保存记忆
     */
    fun saveMemory() {
        val state = _uiState.value
        if (state.latitude == null || state.longitude == null || state.photoUris.isEmpty()) {
            return
        }
        
        screenModelScope.launch {
            updateState { it.copy(isLoading = true) }
            
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
            
            // 成功后清除草稿
            draftService.clearDraft()
            
            updateState { AddMemoryUiState(step = ImportStep.SUCCESS) }
        }
    }
    
    /**
     * 返回上一步
     */
    fun goBack() {
        updateState { state ->
            val currentStep = state.step
            val previousStep = when (currentStep) {
                ImportStep.PHOTOS -> ImportStep.LOCATION
                ImportStep.DETAILS -> ImportStep.PHOTOS
                else -> currentStep
            }
            state.copy(step = previousStep)
        }
    }
    
    /**
     * 重置状态
     */
    fun reset() {
        screenModelScope.launch {
            draftService.clearDraft()
        }
        updateState { AddMemoryUiState() }
    }
}

/**
 * 导入步骤
 */
@Serializable
enum class ImportStep {
    LOCATION,  // 选择位置
    PHOTOS,    // 选择照片
    DETAILS,   // 填写详情
    SUCCESS    // 完成
}

/**
 * UI 状态
 */
@Serializable
data class AddMemoryUiState(
    val step: ImportStep = ImportStep.LOCATION,
    val isLoading: Boolean = false,
    
    // 位置
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val address: String? = null,
    val isAutoLocated: Boolean = false,
    
    // 照片
    val photoUris: List<String> = emptyList(),
    
    // 详情
    val emoji: String = "📍",
    val note: String? = null
)
