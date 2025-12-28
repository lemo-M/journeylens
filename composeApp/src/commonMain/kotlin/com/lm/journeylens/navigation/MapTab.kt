package com.lm.journeylens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.lm.journeylens.feature.map.MapScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import cafe.adriel.voyager.koin.getScreenModel
import com.lm.journeylens.feature.map.MapScreenModel

/**
 * 地图 Tab - 战争迷雾主页面
 */
object MapTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Map)
            return remember {
                TabOptions(
                    index = 0u,
                    title = "地图",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<MapScreenModel>()
        val tabNavigator = LocalTabNavigator.current
        
        // 当返回 MapTab 时，清空之前的 selection
        // 这样用户可以看到更新后的地图标记，而不是旧的详情卡片
        LaunchedEffect(tabNavigator.current) {
            if (tabNavigator.current == this@MapTab) {
                screenModel.clearSelection()
            }
        }
        
        MapScreen(screenModel)
    }
}

