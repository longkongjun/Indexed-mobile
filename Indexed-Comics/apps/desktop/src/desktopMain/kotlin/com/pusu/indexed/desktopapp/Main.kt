package com.pusu.indexed.desktopapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.pusu.indexed.desktopapp.di.DependencyContainer
import com.pusu.indexed.shared.feature.discover.DiscoverScreen
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

fun main() = application {
    // 创建 HttpClient
    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
        
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL  // 显示所有日志：请求头、请求体、响应头、响应体
        }
    }
    
    // 创建依赖注入容器
    val dependencyContainer = DependencyContainer(httpClient)
    
    // 创建 ViewModel
    val viewModel = dependencyContainer.createDiscoverViewModel(
        coroutineScope = CoroutineScope(Dispatchers.Default)
    )
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "Indexed Comics - Discover"
    ) {
        DiscoverScreen(
            viewModel = viewModel,
            onNavigateToDetail = { animeId ->
                println("Navigate to anime detail: $animeId")
            }
        )
    }
}
