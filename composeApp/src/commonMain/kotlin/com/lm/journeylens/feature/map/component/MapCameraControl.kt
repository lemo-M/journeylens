package com.lm.journeylens.feature.map.component

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 地图相机控制
 */
class MapCameraControl {
    sealed interface CameraEvent {
        data object MoveToCurrentLocation : CameraEvent
    }
    
    private val _events = MutableSharedFlow<CameraEvent>()
    val events = _events.asSharedFlow()
    
    suspend fun moveToCurrentLocation() {
        _events.emit(CameraEvent.MoveToCurrentLocation)
    }
}
