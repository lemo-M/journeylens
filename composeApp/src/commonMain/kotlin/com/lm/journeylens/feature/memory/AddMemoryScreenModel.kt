package com.lm.journeylens.feature.memory

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lm.journeylens.feature.memory.domain.usecase.CreateMemoryUseCase
import com.lm.journeylens.feature.memory.domain.usecase.DiscardDraftUseCase
import com.lm.journeylens.feature.memory.domain.usecase.GetDraftUseCase
import com.lm.journeylens.feature.memory.domain.usecase.SaveDraftUseCase
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
    private val getDraftUseCase: GetDraftUseCase,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val discardDraftUseCase: DiscardDraftUseCase,
    private val createMemoryUseCase: CreateMemoryUseCase,
    private val globalCreationState: com.lm.journeylens.feature.memory.domain.state.GlobalCreationState
) : ScreenModel {
    
    // UI 状态
    private val _uiState = MutableStateFlow(AddMemoryUiState())
    val uiState: StateFlow<AddMemoryUiState> = _uiState.asStateFlow()
    
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
    
    // 草稿中的照片数量（用于显示）
    private val _draftPhotoCount = MutableStateFlow(0)
    val draftPhotoCount: StateFlow<Int> = _draftPhotoCount.asStateFlow()
    
    // 是否显示草稿恢复对话框
    private val _showDraftDialog = MutableStateFlow(false)
    val showDraftDialog: StateFlow<Boolean> = _showDraftDialog.asStateFlow()
    
    // 是否显示退出确认对话框（从照片选择页返回时）
    private val _showExitConfirmDialog = MutableStateFlow(false)
    val showExitConfirmDialog: StateFlow<Boolean> = _showExitConfirmDialog.asStateFlow()
    
    /**
     * 进入照片选择步骤前检测草稿
     * 如果有照片草稿，显示对话框让用户选择
     */
    suspend fun checkDraftBeforePhotos(): Boolean {
        val draft = getDraftUseCase().getOrNull()
        if (draft != null) {
            _draftPhotoCount.value = draft.photoUris.size
            _showDraftDialog.value = true
            return true // 有草稿，需要用户决定
        }
        return false // 没有草稿，直接进入
    }
    
    /**
     * 用户选择恢复草稿（只恢复照片、emoji、备注）
     */
    fun restoreDraftPhotos() {
        screenModelScope.launch {
            val draft = getDraftUseCase().getOrNull()
            if (draft != null) {
                // 只恢复照片、emoji、备注，保持当前位置
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    step = ImportStep.PHOTOS,
                    photoUris = draft.photoUris,
                    emoji = draft.emoji,
                    note = draft.note
                )
            }
            _showDraftDialog.value = false
        }
    }
    
    /**
     * 用户选择不恢复草稿（清空并开始新选择）
     */
    fun discardDraft() {
        screenModelScope.launch {
            discardDraftUseCase()
            _showDraftDialog.value = false
            // 清空所有草稿内容，重置为初始值
            _uiState.value = _uiState.value.copy(
                step = ImportStep.PHOTOS,
                photoUris = emptyList(),
                emoji = "📍",
                note = null
            )
        }
    }
    
    /**
     * 关闭草稿对话框（视作放弃草稿）
     */
    fun dismissDraftDialog() {
        _showDraftDialog.value = false
    }
    
    /**
     * 从照片选择页请求返回
     * 如果有照片，显示确认对话框；否则直接返回
     */
    fun requestExitFromPhotos() {
        val currentPhotos = _uiState.value.photoUris
        if (currentPhotos.isNotEmpty()) {
            _draftPhotoCount.value = currentPhotos.size
            _showExitConfirmDialog.value = true
        } else {
            // 没有照片，直接返回
            _uiState.value = _uiState.value.copy(step = ImportStep.LOCATION)
        }
    }
    
    /**
     * 用户选择保存草稿后返回
     */
    fun confirmExitWithSave() {
        // 草稿已经在 updateState 中自动保存了，直接返回即可
        _showExitConfirmDialog.value = false
        _uiState.value = _uiState.value.copy(step = ImportStep.LOCATION)
    }
    
    /**
     * 用户选择不保存草稿后返回
     */
    fun confirmExitWithoutSave() {
        screenModelScope.launch {
            discardDraftUseCase()
            _showExitConfirmDialog.value = false
            // 清空照片等内容并返回
            _uiState.value = _uiState.value.copy(
                step = ImportStep.LOCATION,
                photoUris = emptyList(),
                emoji = "📍",
                note = null
            )
        }
    }
    
    /**
     * 关闭退出确认对话框
     */
    fun dismissExitConfirmDialog() {
        _showExitConfirmDialog.value = false
    }
    
    /**
     * 更新状态并自动保存草稿
     * 草稿只保存照片、emoji、备注（不保存位置）
     */
    private fun updateState(update: (AddMemoryUiState) -> AddMemoryUiState) {
        val newState = update(_uiState.value)
        _uiState.value = newState
        
        // 只有有照片时才保存草稿（成功状态除外）
        if (newState.step != ImportStep.SUCCESS && newState.photoUris.isNotEmpty()) {
            screenModelScope.launch {
                // 只保存照片相关内容，不保存位置
                val draftState = AddMemoryUiState(
                    step = ImportStep.PHOTOS,
                    photoUris = newState.photoUris,
                    emoji = newState.emoji,
                    note = newState.note
                )
                saveDraftUseCase(draftState)
            }
        }
    }
    
    /**
     * 步骤 1: 设置位置（当前定位）
     * 设置位置后检测是否有草稿
     */
    fun setLocationFromGps(latitude: Double, longitude: Double, locationName: String? = null) {
        // 先设置位置（但不进入 PHOTOS 步骤）
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            isAutoLocated = true
        )
        // 检测草稿并决定下一步
        screenModelScope.launch {
            val hasDraft = checkDraftBeforePhotos()
            if (!hasDraft) {
                // 没有草稿，直接进入 PHOTOS 步骤
                _uiState.value = _uiState.value.copy(step = ImportStep.PHOTOS)
            }
            // 如果有草稿，对话框会显示，用户选择后才更新步骤
        }
    }
    
    /**
     * 步骤 1: 设置位置（地图选点）
     * 设置位置后检测是否有草稿
     */
    fun setLocationFromMap(latitude: Double, longitude: Double) {
        // 先设置位置（但不进入 PHOTOS 步骤）
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            isAutoLocated = false
        )
        // 检测草稿并决定下一步
        screenModelScope.launch {
            val hasDraft = checkDraftBeforePhotos()
            if (!hasDraft) {
                // 没有草稿，直接进入 PHOTOS 步骤
                _uiState.value = _uiState.value.copy(step = ImportStep.PHOTOS)
            }
            // 如果有草稿，对话框会显示，用户选择后才更新步骤
        }
    }
    
    /**
     * 从地图页添加记忆时使用的挂起方法
     * 设置位置并等待草稿检测完成，返回后调用方可以安全导航
     */
    private suspend fun setLocationFromMapAndPrepare(latitude: Double, longitude: Double) {
        // 先设置位置
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            isAutoLocated = false
        )
        // 检测草稿并设置下一步（同步等待）
        val hasDraft = checkDraftBeforePhotos()
        if (!hasDraft) {
            // 没有草稿，直接进入 PHOTOS 步骤
            _uiState.value = _uiState.value.copy(step = ImportStep.PHOTOS)
        }
        // 如果有草稿，对话框会显示，用户选择后才更新步骤
    }
    
    /**
     * 步骤 2: 添加照片 (最多 20 张)
     */
    fun addPhotos(photoUris: List<String>) {
        updateState { state ->
            val currentPhotos = state.photoUris.toMutableList()
            val remainingSlots = 20 - currentPhotos.size
            if (remainingSlots > 0) {
                // 只添加不超过剩余配额的照片
                val photosToAdd = photoUris.take(remainingSlots)
                currentPhotos.addAll(photosToAdd)
            }
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
                    // 成功后清除草稿
                    discardDraftUseCase()
                    updateState { AddMemoryUiState(step = ImportStep.SUCCESS) }
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, errorMessage = error.message ?: "保存失败") }
                }
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
            discardDraftUseCase()
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
    val emoji: String = "📍",
    val note: String? = null
)
