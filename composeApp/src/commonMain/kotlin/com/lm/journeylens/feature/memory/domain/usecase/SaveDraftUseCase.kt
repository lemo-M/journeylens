package com.lm.journeylens.feature.memory.domain.usecase

import com.lm.journeylens.feature.memory.AddMemoryUiState
import com.lm.journeylens.feature.memory.service.DraftService

class SaveDraftUseCase(private val draftService: DraftService) {
    suspend operator fun invoke(state: AddMemoryUiState) {
        draftService.saveDraft(state)
    }
}
