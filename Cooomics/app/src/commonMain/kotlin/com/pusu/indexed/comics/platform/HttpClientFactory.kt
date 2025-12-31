package com.pusu.indexed.comics.platform

import io.ktor.client.HttpClient

/**
 * 创建平台特定的 HttpClient
 * 
 * 公共配置（Json、ContentNegotiation）在 commonMain 中统一处理，
 * 平台特定的部分（引擎、Logger）通过 expect/actual 实现。
 */
expect fun createHttpClient(): HttpClient

