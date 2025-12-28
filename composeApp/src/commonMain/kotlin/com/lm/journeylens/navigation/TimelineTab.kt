package com.lm.journeylens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.lm.journeylens.feature.timeline.TimelineScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timeline
import cafe.adriel.voyager.koin.getScreenModel
import com.lm.journeylens.feature.timeline.TimelineScreenModel

/**
 * 时间轴 Tab - 螺旋时间轴
 */
object TimelineTab : Tab {

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Timeline)
            return remember {
                TabOptions(
                    index = 2u,
                    title = "时间轴",
                    icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // 使用 Voyager 的 getScreenModel 获取实例，保持与 MapTab 一致
        val screenModel = getScreenModel<TimelineScreenModel>()
        TimelineScreen(screenModel)
    }
}
