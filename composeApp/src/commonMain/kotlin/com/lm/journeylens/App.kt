package com.lm.journeylens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.lm.journeylens.core.theme.JourneyLensColors
import com.lm.journeylens.core.theme.JourneyLensTheme
import com.lm.journeylens.navigation.AddTab
import com.lm.journeylens.navigation.MapTab
import com.lm.journeylens.navigation.TimelineTab
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * JourneyLens 主应用入口
 * 苹果风液态玻璃 UI + 底部导航
 */
@Composable
@Preview
fun App() {
    JourneyLensTheme {
        TabNavigator(MapTab) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = JourneyLensColors.Background,
                bottomBar = {
                    GlassBottomBar()
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    CurrentTab()
                }
            }
        }
    }
}

/**
 * 液态玻璃效果底部导航栏
 */
@Composable
private fun GlassBottomBar() {
    val tabNavigator = LocalTabNavigator.current

    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        containerColor = JourneyLensColors.GlassBackground,
        tonalElevation = 0.dp
    ) {
        TabNavigationItem(
            tab = MapTab,
            selected = tabNavigator.current == MapTab,
            onClick = { tabNavigator.current = MapTab },
            icon = Icons.Default.Map,
            label = "地图"
        )
        TabNavigationItem(
            tab = AddTab,
            selected = tabNavigator.current == AddTab,
            onClick = { tabNavigator.current = AddTab },
            icon = Icons.Default.Add,
            label = "添加"
        )
        TabNavigationItem(
            tab = TimelineTab,
            selected = tabNavigator.current == TimelineTab,
            onClick = { tabNavigator.current = TimelineTab },
            icon = TimelineIcon,
            label = "时间轴"
        )
    }
}

/**
 * 单个导航项
 */
@Composable
private fun RowScope.TabNavigationItem(
    tab: Tab,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) JourneyLensColors.AppleBlue else JourneyLensColors.TextSecondary
            )
        },
        label = {
            Text(
                text = label,
                color = if (selected) JourneyLensColors.AppleBlue else JourneyLensColors.TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = JourneyLensColors.AppleBlue.copy(alpha = 0.1f)
        )
    )
}

// 自定义时间轴图标（因为 Material Icons 没有合适的螺旋图标）
private val TimelineIcon: ImageVector
    get() = Icons.Default.Map // TODO: 替换为自定义螺旋图标