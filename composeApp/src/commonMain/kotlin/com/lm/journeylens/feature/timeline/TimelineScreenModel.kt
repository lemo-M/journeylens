package com.lm.journeylens.feature.timeline

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lm.journeylens.core.domain.model.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 螺旋时间轴 ViewModel
 */
class TimelineScreenModel(
    private val memoryRepository: MemoryRepository
) : ScreenModel {
    
    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()
    
    init {
        loadMemories()
    }
    
    /**
     * 加载所有记忆
     */
    private fun loadMemories() {
        screenModelScope.launch {
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
