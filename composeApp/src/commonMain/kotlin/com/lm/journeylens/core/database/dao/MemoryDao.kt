package com.lm.journeylens.core.database.dao

import androidx.room.*
import com.lm.journeylens.core.database.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Memory 数据访问对象
 * 提供对记忆数据的 CRUD 操作
 */
@Dao
interface MemoryDao {
    
    /**
     * 插入单条记忆
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: MemoryEntity): Long
    
    /**
     * 批量插入记忆
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(memories: List<MemoryEntity>): List<Long>
    
    /**
     * 更新记忆
     */
    @Update
    suspend fun update(memory: MemoryEntity)
    
    /**
     * 删除记忆
     */
    @Delete
    suspend fun delete(memory: MemoryEntity)
    
    /**
     * 根据 ID 删除记忆
     */
    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    /**
     * 获取所有记忆（按时间降序）
     */
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>
    
    /**
     * 根据 ID 获取单条记忆
     */
    @Query("SELECT * FROM memories WHERE id = :id")
    suspend fun getMemoryById(id: Long): MemoryEntity?
    
    /**
     * 获取指定时间范围内的记忆
     */
    @Query("SELECT * FROM memories WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getMemoriesByTimeRange(startTime: Long, endTime: Long): Flow<List<MemoryEntity>>
    
    /**
     * 获取记忆总数
     */
    @Query("SELECT COUNT(*) FROM memories")
    suspend fun getMemoryCount(): Int
    
    /**
     * 获取所有唯一的年份（用于时间轴）
     */
    @Query("SELECT DISTINCT strftime('%Y', timestamp/1000, 'unixepoch') as year FROM memories ORDER BY year DESC")
    fun getDistinctYears(): Flow<List<String>>
}
