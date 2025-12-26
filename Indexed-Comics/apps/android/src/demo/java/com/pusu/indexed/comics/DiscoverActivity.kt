package com.pusu.indexed.comics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.pusu.indexed.comics.ui.theme.CoooomicsTheme
import com.pusu.indexed.shared.feature.discover.DiscoverScreen

/**
 * Discover 功能的 Activity
 * 
 * 展示如何使用依赖注入容器创建 ViewModel
 */
class DiscoverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CoooomicsTheme {
                // 1. 获取 CoroutineScope
                val scope = rememberCoroutineScope()
                
                // 2. 从依赖注入容器创建 ViewModel
                val viewModel = remember {
                    App.instance.dependencyContainer.createDiscoverViewModel(scope)
                }
                
                // 3. 渲染 Screen
                DiscoverScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { animeId ->
                        // TODO: 导航到详情页
                        println("Navigate to detail: $animeId")
                    }
                )
            }
        }
    }
}

