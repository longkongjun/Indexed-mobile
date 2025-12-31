package com.pusu.indexed.comics

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.ComposeUIViewController
import com.pusu.indexed.comics.di.DependencyContainer
import com.pusu.indexed.comics.navigation.AppNavigation
import com.pusu.indexed.comics.platform.createHttpClient
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * iOS 应用入口
 */
@Composable
fun IOSApp() {
    // 使用平台特定的 HttpClient 工厂创建客户端
    val httpClient = remember { createHttpClient() }
    
    // 创建依赖容器
    val dependencyContainer = remember { DependencyContainer(httpClient) }
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }
    
    MaterialTheme {
        AppNavigation(
            dependencyContainer = dependencyContainer,
            scope = scope
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
fun MainViewController() = ComposeUIViewController(
    configure = {
        // 禁用帧率限制检查，允许使用高刷新率
        enforceStrictPlistSanityCheck = false
    }
) { 
    IOSApp() 
}
