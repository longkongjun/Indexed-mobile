package com.pusu.indexed.comics.platform

import kotlinx.serialization.json.Json

/**
 * HttpClient 公共配置
 * 
 * 所有平台共享的配置：
 * - JSON 序列化配置
 */
object HttpClientConfig {
    /**
     * 公共的 JSON 配置
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }
}

