package com.pusu.indexed.comics.platform

import io.ktor.client.plugins.logging.Logger

/**
 * Web (JS) 平台的 Logger 实现
 * 
 * 使用 console.log 输出日志
 */
actual fun createPlatformLogger(): Logger = object : Logger {
    override fun log(message: String) {
        console.log(message)
    }
}

