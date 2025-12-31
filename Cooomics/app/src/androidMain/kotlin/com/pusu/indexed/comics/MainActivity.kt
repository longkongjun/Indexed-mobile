package com.pusu.indexed.comics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.pusu.indexed.comics.di.DependencyContainer
import com.pusu.indexed.comics.navigation.AppNavigation
import com.pusu.indexed.comics.platform.createHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * 主 Activity
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 使用平台特定的 HttpClient 工厂创建客户端
        val httpClient = createHttpClient()
        
        // 创建依赖注入容器
        val dependencyContainer = DependencyContainer(httpClient)

        setContent {
            MaterialTheme {
                AppNavigation(
                    dependencyContainer = dependencyContainer,
                )
            }
        }
    }
}
