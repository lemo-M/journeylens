package com.lm.journeylens.feature.memory

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lm.journeylens.feature.memory.domain.DraftManager
import com.lm.journeylens.feature.memory.domain.state.GlobalCreationState
import com.lm.journeylens.feature.memory.domain.usecase.CreateMemoryUseCase
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
    private val draftManager: DraftManager,
    private val createMemoryUseCase: CreateMemoryUseCase,
    private val globalCreationState: GlobalCreationState
) : ScreenModel {
    
    // UI 状态
    private val _uiState = MutableStateFlow(AddMemoryUiState())
    val uiState: StateFlow<AddMemoryUiState> = _uiState.asStateFlow()
    
    // 委托给 DraftManager 的状态
    val draftPhotoCount: StateFlow<Int> = draftManager.draftPhotoCount
    val showDraftDialog: StateFlow<Boolean> = draftManager.showDraftDialog
    val showExitConfirmDialog: StateFlow<Boolean> = draftManager.showExitConfirmDialog
    
    init {
        // 监听全局创建状态（从地图页带入的位置信息）
        screenModelScope.launch {
            globalCreationState.session.collect { session ->
                if (session != null) {
                    setLocationFromMapAndPrepare(session.latitude, session.longitude)
                    globalCreationState.clear()
                }
            }
        }
    }
    
    // ==================== 草稿相关（委托给 DraftManager）====================
    
    /**
     * 用户选择恢复草稿
     */
    fun restoreDraftPhotos() {
        screenModelScope.launch {
            val draft = draftManager.getDraft()
            if (draft != null) {
                _uiState.value = _uiState.value.copy(
                    step = ImportStep.PHOTOS,
                    photoUris = draft.photoUris,
                    emoji = draft.emoji,
                    note = draft.note
                )
            }
            draftManager.dismissDraftDialog()
        }
    }
    
    /**
     * 用户选择不恢复草稿
     */
    fun discardDraft() {
        screenModelScope.launch {
            draftManager.discardDraft()
            draftManager.dismissDraftDialog()
            _uiState.value = draftManager.createEmptyState().copy(
                latitude = _uiState.value.latitude,
                longitude = _uiState.value.longitude,
                locationName = _uiState.value.locationName,
                isAutoLocated = _uiState.value.isAutoLocated
            )
        }
    }
    
    fun dismissDraftDialog() = draftManager.dismissDraftDialog()
    
    // ==================== 退出确认相关 ====================
    
    fun requestExitFromPhotos() {
        val currentPhotos = _uiState.value.photoUris
        if (currentPhotos.isNotEmpty()) {
            draftManager.showExitConfirmDialog(currentPhotos.size)
        } else {
            _uiState.value = _uiState.value.copy(step = ImportStep.LOCATION)
        }
    }
    
    fun confirmExitWithSave() {
        draftManager.dismissExitConfirmDialog()
        _uiState.value = _uiState.value.copy(step = ImportStep.LOCATION)
    }
    
    fun confirmExitWithoutSave() {
        screenModelScope.launch {
            draftManager.discardDraft()
            draftManager.dismissExitConfirmDialog()
            _uiState.value = draftManager.createEmptyState().copy(
                step = ImportStep.LOCATION,
                latitude = _uiState.value.latitude,
                longitude = _uiState.value.longitude
            )
        }
    }
    
    fun dismissExitConfirmDialog() = draftManager.dismissExitConfirmDialog()
    
    // ==================== 状态更新 ====================
    
    private fun updateState(update: (AddMemoryUiState) -> AddMemoryUiState) {
        val newState = update(_uiState.value)
        _uiState.value = newState
        
        // 自动保存草稿
        screenModelScope.launch {
            draftManager.saveDraft(newState)
        }
    }
    
    // ==================== 步骤 1: 位置设置 ====================
    
    fun setLocationFromGps(latitude: Double, longitude: Double, locationName: String? = null) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            isAutoLocated = true
        )
        screenModelScope.launch {
            val hasDraft = draftManager.checkDraftBeforePhotos()
            if (!hasDraft) {
                _uiState.value = _uiState.value.copy(step = ImportStep.PHOTOS)
            }
        }
    }
    
    fun setLocationFromMap(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            isAutoLocated = false
        )
        screenModelScope.launch {
            val hasDraft = draftManager.checkDraftBeforePhotos()
            if (!hasDraft) {
                _uiState.value = _uiState.value.copy(step = ImportStep.PHOTOS)
            }
        }
    }
    
    private suspend fun setLocationFromMapAndPrepare(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            isAutoLocated = false
        )
        val hasDraft = draftManager.checkDraftBeforePhotos()
        if (!hasDraft) {
            _uiState.value = _uiState.value.copy(step = ImportStep.PHOTOS)
        }
    }
    
    // ==================== 步骤 2: 照片管理 ====================
    
    fun addPhotos(photoUris: List<String>) {
        updateState { state ->
            val currentPhotos = state.photoUris.toMutableList()
            val remainingSlots = MemoryConfig.MAX_PHOTOS - currentPhotos.size
            if (remainingSlots > 0) {
                val photosToAdd = photoUris.take(remainingSlots)
                currentPhotos.addAll(photosToAdd)
            }
            state.copy(photoUris = currentPhotos)
        }
    }
    
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
    
    fun confirmPhotos() {
        updateState { state ->
            if (state.photoUris.isNotEmpty()) {
                state.copy(step = ImportStep.DETAILS)
            } else {
                state
            }
        }
    }
    
    // ==================== 步骤 3: 详情填写 ====================
    
    fun updateEmoji(emoji: String) {
        updateState { it.copy(emoji = emoji) }
    }
    
    fun updateNote(note: String) {
        updateState { it.copy(note = note) }
    }
    
    // ==================== 保存记忆 ====================
    
    fun saveMemory() {
        val state = _uiState.value
        if (state.latitude == null || state.longitude == null || state.photoUris.isEmpty()) {
            return
        }
        
        screenModelScope.launch {
            updateState { it.copy(isLoading = true, errorMessage = null) }
            
            val result = createMemoryUseCase(
                latitude = state.latitude,
                longitude = state.longitude,
                locationName = state.locationName,
                photoUris = state.photoUris,
                emoji = state.emoji,
                note = state.note,
                isAutoLocated = state.isAutoLocated
            )
            
            result
                .onSuccess {
                    draftManager.discardDraft()
                    updateState { AddMemoryUiState(step = ImportStep.SUCCESS) }
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, errorMessage = error.message ?: "保存失败") }
                }
        }
    }
    
    // ==================== 导航控制 ====================
    
    fun goBack() {
        updateState { state ->
            val previousStep = when (state.step) {
                ImportStep.PHOTOS -> ImportStep.LOCATION
                ImportStep.DETAILS -> ImportStep.PHOTOS
                else -> state.step
            }
            state.copy(step = previousStep)
        }
    }
    
    fun reset() {
        screenModelScope.launch {
            draftManager.discardDraft()
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
    val errorMessage: String? = null,
    
    // 位置
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val address: String? = null,
    val isAutoLocated: Boolean = false,
    
    // 照片
    val photoUris: List<String> = emptyList(),
    
    // 详情
    val emoji: String = MemoryConfig.DEFAULT_EMOJI,
    val note: String? = null
)
