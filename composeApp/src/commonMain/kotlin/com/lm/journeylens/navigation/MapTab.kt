package com.lm.journeylens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.lm.journeylens.feature.map.MapScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map

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

import cafe.adriel.voyager.koin.getScreenModel
import com.lm.journeylens.feature.map.MapScreenModel

    @Composable
    override fun Content() {
        val screenModel = getScreenModel<MapScreenModel>()
        MapScreen(screenModel)
    }
}
