package com.pusu.indexed.core.utils

/**
 * 跨平台时间工具
 */
expect object TimeUtils {
    /**
     * 获取当前时间戳（毫秒）
     */
    fun currentTimeMillis(): Long
}
