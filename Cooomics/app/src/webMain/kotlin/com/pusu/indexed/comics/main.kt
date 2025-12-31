package com.pusu.indexed.comics

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.pusu.indexed.comics.di.DependencyContainer
import com.pusu.indexed.comics.navigation.AppNavigation
import com.pusu.indexed.comics.platform.createHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        // 使用平台特定的 HttpClient 工厂创建客户端
        val httpClient = remember { createHttpClient() }

        // 创建依赖容器
        val dependencyContainer = remember { DependencyContainer(httpClient) }
        val scope = rememberCoroutineScope { SupervisorJob() + Dispatchers.Main }

        MaterialTheme {
            AppNavigation(dependencyContainer, scope)
        }
    }
}
