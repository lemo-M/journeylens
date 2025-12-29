package com.lm.journeylens.feature.memory.domain.usecase

import com.lm.journeylens.core.domain.model.Result
import com.lm.journeylens.feature.memory.service.DraftService

class DiscardDraftUseCase(private val draftService: DraftService) {
    suspend operator fun invoke(): Result<Unit> = Result.runCatching {
        draftService.clearDraft()
    }
}
