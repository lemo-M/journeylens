package com.lm.journeylens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.lm.journeylens.feature.memory.AddMemoryScreen
import com.lm.journeylens.feature.memory.AddMemoryScreenModel
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
        val screenModel = getScreenModel<AddMemoryScreenModel>()
        val tabNavigator = LocalTabNavigator.current
        
        // 监听当前 Tab，当 Tab 变为 AddTab 时重新加载 Draft
        // 这确保从 MapScreen 点击"去添加"后 Draft 能被正确加载
        LaunchedEffect(tabNavigator.current) {
            if (tabNavigator.current == this@AddTab) {
                screenModel.loadDraft()
            }
        }
        
        AddMemoryScreen(screenModel)
    }
}
