package com.pusu.indexed.comics.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

/**
 * Web (JS) 平台的 HttpClient 实现
 * 
 * 使用 Js 引擎
 */
actual fun createHttpClient(): HttpClient = HttpClient(Js) {
    // 配置公共部分：JSON 序列化
    install(ContentNegotiation) {
        json(HttpClientConfig.json)
    }
    
    // 配置平台特定的 Logger
    install(Logging) {
        logger = createPlatformLogger()
        level = LogLevel.INFO
    }
}

