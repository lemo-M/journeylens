package com.lm.journeylens.core.data.mapper

import com.lm.journeylens.core.database.entity.MemoryEntity
import com.lm.journeylens.core.domain.model.Memory

fun MemoryEntity.toDomain(): Memory {
    return Memory(
        id = id,
        latitude = latitude,
        longitude = longitude,
        locationName = locationName,
        timestamp = timestamp,
        createdAt = createdAt,
        photoUris = photoUris,
        emoji = emoji,
        note = note,
        isAutoLocated = isAutoLocated
    )
}

fun Memory.toEntity(): MemoryEntity {
    return MemoryEntity(
        id = id,
        latitude = latitude,
        longitude = longitude,
        locationName = locationName,
        timestamp = timestamp,
        createdAt = createdAt,
        photoUris = photoUris,
        emoji = emoji,
        note = note,
        isAutoLocated = isAutoLocated
    )
}

fun List<MemoryEntity>.toDomain(): List<Memory> = map { it.toDomain() }
fun List<Memory>.toEntity(): List<MemoryEntity> = map { it.toEntity() }
