package com.pusu.indexed.domain.comic.model

import com.pusu.indexed.core.utils.TimeUtils

/**
 * 刮削任务
 * 
 * 用于从在线源获取漫画元数据、封面等信息
 */
data class ScrapeTask(
    /**
     * 任务 ID
     */
    val id: String,

    /**
     * 目标漫画 ID
     */
    val comicId: String,

    /**
     * 漫画标题（用于搜索匹配）
     */
    val comicTitle: String,

    /**
     * 刮削类型
     */
    val scrapeType: ScrapeType,

    /**
     * 任务状态
     */
    val status: TaskStatus,

    /**
     * 优先级（越大越优先）
     */
    val priority: Int = 0,

    /**
     * 重试次数
     */
    val retryCount: Int = 0,

    /**
     * 最大重试次数
     */
    val maxRetries: Int = 3,

    /**
     * 创建时间戳（毫秒）
     */
    val createdAtMillis: Long,

    /**
     * 开始时间戳（毫秒）
     */
    val startedAtMillis: Long? = null,

    /**
     * 完成时间戳（毫秒）
     */
    val completedAtMillis: Long? = null,

    /**
     * 错误信息
     */
    val errorMessage: String? = null
) {
    /**
     * 标记任务开始
     */
    fun markStarted(timestampMillis: Long = TimeUtils.currentTimeMillis()): ScrapeTask {
        return copy(
            status = TaskStatus.RUNNING,
            startedAtMillis = timestampMillis
        )
    }

    /**
     * 标记任务完成
     */
    fun markCompleted(timestampMillis: Long = TimeUtils.currentTimeMillis()): ScrapeTask {
        return copy(
            status = TaskStatus.COMPLETED,
            completedAtMillis = timestampMillis
        )
    }

    /**
     * 标记任务失败并准备重试
     */
    fun markFailedWithRetry(error: String, timestampMillis: Long = TimeUtils.currentTimeMillis()): ScrapeTask {
        val newRetryCount = retryCount + 1
        return if (newRetryCount >= maxRetries) {
            copy(
                status = TaskStatus.FAILED,
                completedAtMillis = timestampMillis,
                errorMessage = error,
                retryCount = newRetryCount
            )
        } else {
            copy(
                status = TaskStatus.PENDING,
                errorMessage = error,
                retryCount = newRetryCount
            )
        }
    }

    /**
     * 是否可以重试
     */
    fun canRetry(): Boolean = retryCount < maxRetries

    /**
     * 标记任务取消
     */
    fun markCancelled(timestampMillis: Long = TimeUtils.currentTimeMillis()): ScrapeTask {
        return copy(
            status = TaskStatus.CANCELLED,
            completedAtMillis = timestampMillis
        )
    }
}

/**
 * 刮削类型
 */
enum class ScrapeType {
    /**
     * 元数据（标题、简介、标签等）
     */
    METADATA,

    /**
     * 封面图
     */
    COVER,

    /**
     * 完整信息（元数据 + 封面）
     */
    FULL
}
