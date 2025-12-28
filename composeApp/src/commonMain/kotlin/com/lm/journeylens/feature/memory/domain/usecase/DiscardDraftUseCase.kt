package com.lm.journeylens.feature.memory.domain.usecase

import com.lm.journeylens.feature.memory.service.DraftService

class DiscardDraftUseCase(private val draftService: DraftService) {
    suspend operator fun invoke() {
        draftService.clearDraft()
    }
}
