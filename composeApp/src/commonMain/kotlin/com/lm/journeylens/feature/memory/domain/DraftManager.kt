package com.lm.journeylens.feature.memory.domain

import com.lm.journeylens.feature.memory.AddMemoryUiState
import com.lm.journeylens.feature.memory.ImportStep
import com.lm.journeylens.feature.memory.MemoryConfig
import com.lm.journeylens.feature.memory.domain.usecase.DiscardDraftUseCase
import com.lm.journeylens.feature.memory.domain.usecase.GetDraftUseCase
import com.lm.journeylens.feature.memory.domain.usecase.SaveDraftUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 草稿管理器
 * 负责草稿的检测、恢复、保存和丢弃逻辑
 */
class DraftManager(
    private val getDraftUseCase: GetDraftUseCase,
    private val saveDraftUseCase: SaveDraftUseCase,
    private val discardDraftUseCase: DiscardDraftUseCase
) {
    // 草稿中的照片数量（用于显示）
    private val _draftPhotoCount = MutableStateFlow(0)
    val draftPhotoCount: StateFlow<Int> = _draftPhotoCount.asStateFlow()
    
    // 是否显示草稿恢复对话框
    private val _showDraftDialog = MutableStateFlow(false)
    val showDraftDialog: StateFlow<Boolean> = _showDraftDialog.asStateFlow()
    
    // 是否显示退出确认对话框
    private val _showExitConfirmDialog = MutableStateFlow(false)
    val showExitConfirmDialog: StateFlow<Boolean> = _showExitConfirmDialog.asStateFlow()
    
    /**
     * 检测是否有有效草稿
     * @return true 如果有草稿需要用户决定，false 如果没有草稿
     */
    suspend fun checkDraftBeforePhotos(): Boolean {
        val draft = getDraftUseCase().getOrNull()
        if (draft != null) {
            _draftPhotoCount.value = draft.photoUris.size
            _showDraftDialog.value = true
            return true
        }
        return false
    }
    
    /**
     * 获取草稿内容（用于恢复）
     */
    suspend fun getDraft(): AddMemoryUiState? {
        return getDraftUseCase().getOrNull()
    }
    
    /**
     * 保存草稿（只保存照片、emoji、备注）
     */
    suspend fun saveDraft(state: AddMemoryUiState) {
        if (state.step != ImportStep.SUCCESS && state.photoUris.isNotEmpty()) {
            val draftState = AddMemoryUiState(
                step = ImportStep.PHOTOS,
                photoUris = state.photoUris,
                emoji = state.emoji,
                note = state.note
            )
            saveDraftUseCase(draftState)
        }
    }
    
    /**
     * 丢弃草稿
     */
    suspend fun discardDraft() {
        discardDraftUseCase()
    }
    
    /**
     * 关闭草稿对话框
     */
    fun dismissDraftDialog() {
        _showDraftDialog.value = false
    }
    
    /**
     * 显示退出确认对话框
     */
    fun showExitConfirmDialog(photoCount: Int) {
        _draftPhotoCount.value = photoCount
        _showExitConfirmDialog.value = true
    }
    
    /**
     * 关闭退出确认对话框
     */
    fun dismissExitConfirmDialog() {
        _showExitConfirmDialog.value = false
    }
    
    /**
     * 创建空白状态（丢弃草稿后）
     */
    fun createEmptyState(): AddMemoryUiState {
        return AddMemoryUiState(
            step = ImportStep.PHOTOS,
            photoUris = emptyList(),
            emoji = MemoryConfig.DEFAULT_EMOJI,
            note = null
        )
    }
}
