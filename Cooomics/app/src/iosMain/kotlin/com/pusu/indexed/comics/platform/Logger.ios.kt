package com.pusu.indexed.comics.platform

import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.Logger

/**
 * iOS 平台的 Logger 实现
 * 
 * 使用默认 Logger
 */
actual fun createPlatformLogger(): Logger = Logger.DEFAULT

