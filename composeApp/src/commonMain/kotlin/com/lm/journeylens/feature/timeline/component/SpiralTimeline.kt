package com.lm.journeylens.feature.timeline.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import com.lm.journeylens.core.database.entity.Memory
import com.lm.journeylens.core.theme.JourneyLensColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 螺旋时间轴组件
 * 使用阿基米德螺旋线展示记忆点
 */
@Composable
fun SpiralTimeline(
    memories: List<Memory>,
    onMemoryClick: (Memory) -> Unit,
    modifier: Modifier = Modifier
) {
    // 缩放和偏移状态
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // 记忆点位置缓存
    var memoryPositions by remember { mutableStateOf<List<Pair<Offset, Memory>>>(emptyList()) }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3f)
                    offset += pan
                }
            }
            .pointerInput(memories) {
                detectTapGestures { tapOffset ->
                    // 检测点击了哪个记忆点
                    val clickedMemory = memoryPositions.find { (pos, _) ->
                        val distance = sqrt(
                            (tapOffset.x - pos.x) * (tapOffset.x - pos.x) +
                            (tapOffset.y - pos.y) * (tapOffset.y - pos.y)
                        )
                        distance < 30f * scale // 点击半径
                    }
                    clickedMemory?.let { onMemoryClick(it.second) }
                }
            }
    ) {
        val centerX = size.width / 2 + offset.x
        val centerY = size.height / 2 + offset.y
        
        // 绘制螺旋线
        drawSpiral(
            centerX = centerX,
            centerY = centerY,
            scale = scale,
            totalPoints = memories.size.coerceAtLeast(50)
        )
        
        // 绘制记忆点
        val positions = mutableListOf<Pair<Offset, Memory>>()
        memories.forEachIndexed { index, memory ->
            val pos = calculateSpiralPosition(
                index = index,
                totalCount = memories.size,
                centerX = centerX,
                centerY = centerY,
                scale = scale
            )
            positions.add(pos to memory)
            
            drawMemoryPoint(
                position = pos,
                isSelected = false,
                scale = scale
            )
        }
        memoryPositions = positions
        
        // 绘制中心点
        drawCircle(
            color = JourneyLensColors.AppleBlue,
            radius = 12f * scale,
            center = Offset(centerX, centerY)
        )
        
        // 中心文字提示（用圆点表示"起点"）
        drawCircle(
            color = Color.White,
            radius = 6f * scale,
            center = Offset(centerX, centerY)
        )
    }
}

/**
 * 绘制螺旋线
 */
private fun DrawScope.drawSpiral(
    centerX: Float,
    centerY: Float,
    scale: Float,
    totalPoints: Int
) {
    val pathPoints = mutableListOf<Offset>()
    val maxTheta = totalPoints * 0.5f  // 螺旋圈数
    val a = 20f * scale  // 起始半径
    val b = 15f * scale  // 螺旋增量
    
    var theta = 0f
    while (theta < maxTheta) {
        val r = a + b * theta
        val x = centerX + r * cos(theta)
        val y = centerY + r * sin(theta)
        pathPoints.add(Offset(x, y))
        theta += 0.1f
    }
    
    // 绘制螺旋路径
    for (i in 0 until pathPoints.size - 1) {
        drawLine(
            color = JourneyLensColors.TextTertiary.copy(alpha = 0.3f),
            start = pathPoints[i],
            end = pathPoints[i + 1],
            strokeWidth = 2f * scale,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
        )
    }
}

/**
 * 计算螺旋上某点的位置
 */
private fun calculateSpiralPosition(
    index: Int,
    totalCount: Int,
    centerX: Float,
    centerY: Float,
    scale: Float
): Offset {
    // 阿基米德螺旋: r = a + b * theta
    val a = 40f * scale  // 起始半径（比螺旋线大一点，让点在线上）
    val b = 15f * scale  // 螺旋增量
    val theta = (index + 1) * 0.5f  // 角度
    
    val r = a + b * theta
    val x = centerX + r * cos(theta)
    val y = centerY + r * sin(theta)
    
    return Offset(x, y)
}

/**
 * 绘制记忆点
 */
private fun DrawScope.drawMemoryPoint(
    position: Offset,
    isSelected: Boolean,
    scale: Float
) {
    val baseRadius = if (isSelected) 18f else 12f
    val radius = baseRadius * scale
    
    // 外圈光晕
    drawCircle(
        color = JourneyLensColors.AppleBlue.copy(alpha = 0.2f),
        radius = radius * 1.5f,
        center = position
    )
    
    // 主圆
    drawCircle(
        color = JourneyLensColors.AppleBlue,
        radius = radius,
        center = position
    )
    
    // 内圈高光
    drawCircle(
        color = Color.White.copy(alpha = 0.5f),
        radius = radius * 0.4f,
        center = Offset(position.x - radius * 0.2f, position.y - radius * 0.2f)
    )
}
