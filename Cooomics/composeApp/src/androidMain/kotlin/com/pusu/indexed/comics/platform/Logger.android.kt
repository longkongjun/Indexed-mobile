package com.pusu.indexed.comics.platform

import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger

/**
 * Android 平台的 Logger 实现
 * 
 * 使用 android.util.Log 输出日志
 */
actual fun createPlatformLogger(): Logger = Logger.DEFAULT

