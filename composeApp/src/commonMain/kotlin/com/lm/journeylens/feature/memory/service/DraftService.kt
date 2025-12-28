package com.lm.journeylens.feature.memory.service

import com.lm.journeylens.feature.memory.AddMemoryUiState

/**
 * 草稿服务 - 用于保存和恢复编辑中的状态
 */
expect class DraftService {
    /**
     * 保存草稿
     */
    suspend fun saveDraft(state: AddMemoryUiState)
    
    /**
     * 读取草稿
     */
    suspend fun loadDraft(): AddMemoryUiState?
    
    /**
     * 清除草稿
     */
    suspend fun clearDraft()
}
