package com.lm.journeylens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.lm.journeylens.feature.memory.AddMemoryScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

/**
 * 添加 Tab - 导入照片/创建记忆
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
        AddMemoryScreen()
    }
}
