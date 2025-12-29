package com.lm.journeylens.feature.memory.domain.usecase

import com.lm.journeylens.core.domain.model.Result
import com.lm.journeylens.feature.memory.AddMemoryUiState
import com.lm.journeylens.feature.memory.service.DraftService

/**
 * 获取有效草稿的 UseCase
 */
class GetDraftUseCase(private val draftService: DraftService) {
    suspend operator fun invoke(): Result<AddMemoryUiState?> = Result.runCatching {
        val draft = draftService.loadDraft()
        // 只返回有照片的有效草稿
        if (draft != null && draft.photoUris.isNotEmpty()) {
            draft
        } else {
            null
        }
    }
}
