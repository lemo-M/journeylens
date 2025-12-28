package com.lm.journeylens.feature.map

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lm.journeylens.core.domain.model.Memory
import com.lm.journeylens.core.repository.MemoryRepository
import com.lm.journeylens.core.service.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 地图 UI 状态
 */
data class MapUiState(
    val isLoading: Boolean = true,
    val memories: List<Memory> = emptyList(),
    val selectedMemories: List<Memory> = emptyList(),
    // 相机位置 (latitude, longitude, zoom)
    val cameraPosition: MapCameraPosition? = null
)

data class MapCameraPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float
)


class MapScreenModel(
    private val memoryRepository: MemoryRepository,
    private val locationService: LocationService
) : ScreenModel {
    
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()
    
    init {
        loadMemories()
        initLocation()
    }
    
    private fun initLocation() {
        screenModelScope.launch {
            // 如果还没有保存的相机位置，尝试获取当前位置
            if (_uiState.value.cameraPosition == null) {
                try {
                    locationService.getCurrentLocation()?.let { location ->
                        _uiState.value = _uiState.value.copy(
                            cameraPosition = MapCameraPosition(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                zoom = 15f
                            )
                        )
                    }
                } catch (e: Exception) {
                    // 定位失败，忽略错误，使用默认位置
                }
            }
        }
    }
    
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
    
    fun selectMemories(memories: List<Memory>) {
        // 按时间倒序排列（最新的在前面）
        _uiState.value = _uiState.value.copy(
            selectedMemories = memories.sortedByDescending { it.timestamp }
        )
    }
    
    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedMemories = emptyList())
    }

    fun updateCameraPosition(latitude: Double, longitude: Double, zoom: Float) {
        _uiState.value = _uiState.value.copy(
            cameraPosition = MapCameraPosition(latitude, longitude, zoom)
        )
    }
}
