package com.pusu.indexed.comics.platform

import io.ktor.client.plugins.logging.Logger

/**
 * 平台特定的 Logger
 * 
 * Android: 使用 android.util.Log
 * iOS/Desktop/Web: 使用默认 Logger
 */
expect fun createPlatformLogger(): Logger

