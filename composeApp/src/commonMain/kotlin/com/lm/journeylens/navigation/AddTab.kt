package com.lm.journeylens.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.koin.getScreenModel
import com.lm.journeylens.feature.memory.AddMemoryScreen
import com.lm.journeylens.feature.memory.AddMemoryScreenModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

/**
 * 添加 Tab - 导入照片/创建记忆
 * 草稿检测已移至 AddMemoryScreen 中处理
 */
object AddTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Add)
            return remember {
                TabOptions(
                    index = 1u,
                    title = "添加",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // 使用 Voyager 的 getScreenModel 获取实例
        // 注意：AddMemoryScreenModel 在 DI 中被定义为 single，因此 getScreenModel 也会获取到同一个单例
        val screenModel = getScreenModel<AddMemoryScreenModel>()
        AddMemoryScreen(screenModel)
    }
}
