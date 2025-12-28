package com.lm.journeylens.feature.memory.domain.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 创建会话数据
 */
data class CreationSession(
    val latitude: Double,
    val longitude: Double
)

/**
 * 全局创建状态
 * 用于在不同屏幕间共享"添加记忆"的上下文（例如从地图页带入位置信息）
 */
class GlobalCreationState {
    private val _session = MutableStateFlow<CreationSession?>(null)
    val session = _session.asStateFlow()

    /**
     * 开始新的创建会话（设置位置）
     */
    fun startCreation(latitude: Double, longitude: Double) {
        _session.value = CreationSession(latitude, longitude)
    }

    /**
     * 清除会话
     */
    fun clear() {
        _session.value = null
    }
}
