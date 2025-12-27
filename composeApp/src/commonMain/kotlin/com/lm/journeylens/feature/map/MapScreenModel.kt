package com.lm.journeylens.feature.map

import cafe.adriel.voyager.core.model.ScreenModel
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
 * 地图页面 ScreenModel
 */
class MapScreenModel(
    private val memoryRepository: MemoryRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        loadMemories()
    }
    
    /**
     * 加载所有记忆点
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
     * 选中记忆点
     */
    fun selectMemory(memory: Memory) {
        _uiState.value = _uiState.value.copy(selectedMemory = memory)
    }
    
    /**
     * 清除选中
     */
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedMemory = null)
    }
}

/**
 * 地图 UI 状态
 */
data class MapUiState(
    val isLoading: Boolean = true,
    val memories: List<Memory> = emptyList(),
    val selectedMemory: Memory? = null
)
