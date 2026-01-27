package com.pusu.indexed.data.local.scrape

import com.pusu.indexed.domain.comic.model.ScrapeTask
import com.pusu.indexed.domain.comic.model.ScrapeType

/**
 * 本地漫画刮削队列
 * 
 * 管理刮削任务的排队、执行、重试
 */
interface LocalScrapeQueue {
    /**
     * 队列状态
     */
    data class QueueStatus(
        val pendingCount: Int,
        val runningCount: Int,
        val completedCount: Int,
        val failedCount: Int
    )

    /**
     * 入队刮削任务
     * 
     * @param comicId 漫画 ID
     * @param comicTitle 漫画标题
     * @param scrapeType 刮削类型
     * @param priority 优先级（越大越优先）
     * @return 任务 ID，如果已存在则返回现有任务 ID
     */
    suspend fun enqueue(
        comicId: String,
        comicTitle: String,
        scrapeType: ScrapeType,
        priority: Int = 0
    ): String

    /**
     * 批量入队刮削任务
     */
    suspend fun enqueueAll(
        tasks: List<Triple<String, String, ScrapeType>>
    ): List<String>

    /**
     * 获取下一个待执行任务
     * 按优先级和创建时间排序
     */
    suspend fun dequeue(): ScrapeTask?

    /**
     * 更新任务状态
     */
    suspend fun updateTask(task: ScrapeTask)

    /**
     * 取消任务
     */
    suspend fun cancelTask(taskId: String)

    /**
     * 重试失败的任务
     */
    suspend fun retryFailed()

    /**
     * 清理已完成和失败的任务
     * @param olderThanMillis 清理超过指定时间的任务
     */
    suspend fun cleanup(olderThanMillis: Long)

    /**
     * 获取队列状态
     */
    suspend fun getStatus(): QueueStatus

    /**
     * 获取指定漫画的任务
     */
    suspend fun getTasksByComicId(comicId: String): List<ScrapeTask>

    /**
     * 检查漫画是否已有待处理任务
     */
    suspend fun hasPendingTask(comicId: String, scrapeType: ScrapeType): Boolean
}
