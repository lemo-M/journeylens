package com.lm.journeylens.core.repository

import com.lm.journeylens.core.database.dao.MemoryDao
import com.lm.journeylens.core.domain.model.Memory
import com.lm.journeylens.core.data.mapper.toDomain
import com.lm.journeylens.core.data.mapper.toEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Memory 仓库接口
 */
interface MemoryRepository {
    fun getAllMemories(): Flow<List<Memory>>
    fun getMemoriesByTimeRange(startTime: Long, endTime: Long): Flow<List<Memory>>
    fun getDistinctYears(): Flow<List<String>>
    suspend fun getMemoryById(id: Long): Memory?
    suspend fun getMemoryCount(): Int
    suspend fun insert(memory: Memory): Long
    suspend fun insertAll(memories: List<Memory>): List<Long>
    suspend fun update(memory: Memory)
    suspend fun delete(memory: Memory)
    suspend fun deleteById(id: Long)
}

/**
 * Memory 仓库实现
 */
class MemoryRepositoryImpl(
    private val memoryDao: MemoryDao
) : MemoryRepository {
    
    override fun getAllMemories(): Flow<List<Memory>> = 
        memoryDao.getAllMemories().map { list -> list.toDomain() }
    
    override fun getMemoriesByTimeRange(startTime: Long, endTime: Long): Flow<List<Memory>> =
        memoryDao.getMemoriesByTimeRange(startTime, endTime).map { list -> list.toDomain() }
    
    override fun getDistinctYears(): Flow<List<String>> = memoryDao.getDistinctYears()
    
    override suspend fun getMemoryById(id: Long): Memory? = 
        memoryDao.getMemoryById(id)?.toDomain()
    
    override suspend fun getMemoryCount(): Int = memoryDao.getMemoryCount()
    
    override suspend fun insert(memory: Memory): Long = memoryDao.insert(memory.toEntity())
    
    override suspend fun insertAll(memories: List<Memory>): List<Long> = 
        memoryDao.insertAll(memories.toEntity())
    
    override suspend fun update(memory: Memory) = memoryDao.update(memory.toEntity())
    
    override suspend fun delete(memory: Memory) = memoryDao.delete(memory.toEntity())
    
    override suspend fun deleteById(id: Long) = memoryDao.deleteById(id)
}
