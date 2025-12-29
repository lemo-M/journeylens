package com.lm.journeylens.feature.memory.domain.usecase

import com.lm.journeylens.core.domain.model.Memory
import com.lm.journeylens.core.domain.model.Result
import com.lm.journeylens.core.repository.MemoryRepository
import kotlinx.datetime.Clock

class CreateMemoryUseCase(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double,
        locationName: String?,
        photoUris: List<String>,
        emoji: String,
        note: String?,
        isAutoLocated: Boolean
    ): Result<Long> = Result.runCatching {
        val memory = Memory(
            latitude = latitude,
            longitude = longitude,
            locationName = locationName,
            timestamp = Clock.System.now().toEpochMilliseconds(),
            photoUris = photoUris,
            emoji = emoji,
            note = note?.takeIf { it.isNotBlank() },
            isAutoLocated = isAutoLocated
        )
        memoryRepository.insert(memory)
    }
}
