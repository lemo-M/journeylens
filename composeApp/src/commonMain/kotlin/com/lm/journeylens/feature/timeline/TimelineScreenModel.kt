package com.lm.journeylens.feature.timeline

import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 螺旋时间轴 ViewModel
 * 不再继承 Voyager ScreenModel，使用普通协程作用域
 */
class TimelineScreenModel(
    private val memoryRepository: MemoryRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()
    
    init {
        loadMemories()
    }
    
    /**
     * 加载所有记忆
     */
    private fun loadMemories() {
        scope.launch {
            memoryRepository.getAllMemories().collect { memories ->
                _uiState.value = _uiState.value.copy(
                    memories = memories,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 选中某个记忆
     */
    fun selectMemory(memory: Memory) {
        _uiState.value = _uiState.value.copy(selectedMemory = memory)
    }
    
    /**
     * 取消选中
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedMemory = null)
    }
}

/**
 * 时间轴 UI 状态
 */
data class TimelineUiState(
    val isLoading: Boolean = true,
    val memories: List<Memory> = emptyList(),
    val selectedMemory: Memory? = null
)
