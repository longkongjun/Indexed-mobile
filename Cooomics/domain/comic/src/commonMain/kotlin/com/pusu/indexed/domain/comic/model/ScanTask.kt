package com.pusu.indexed.domain.comic.model

import com.pusu.indexed.core.utils.TimeUtils

/**
 * 扫描任务
 */
data class ScanTask(
    /**
     * 任务 ID
     */
    val id: String,

    /**
     * 目标根目录 ID
     */
    val libraryRootId: String,

    /**
     * 扫描类型
     */
    val scanType: ScanType,

    /**
     * 任务状态
     */
    val status: TaskStatus,

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
    val errorMessage: String? = null,

    /**
     * 扫描进度（0-100）
     */
    val progress: Int = 0,

    /**
     * 发现的漫画数量
     */
    val foundComicCount: Int = 0,

    /**
     * 新增章节数量
     */
    val newChapterCount: Int = 0,

    /**
     * 新增页面数量
     */
    val newPageCount: Int = 0
) {
    /**
     * 标记任务开始
     */
    fun markStarted(timestampMillis: Long = TimeUtils.currentTimeMillis()): ScanTask {
        return copy(
            status = TaskStatus.RUNNING,
            startedAtMillis = timestampMillis
        )
    }

    /**
     * 更新进度
     */
    fun updateProgress(progress: Int, foundComics: Int = foundComicCount): ScanTask {
        return copy(
            progress = progress.coerceIn(0, 100),
            foundComicCount = foundComics
        )
    }

    /**
     * 标记任务完成
     */
    fun markCompleted(
        foundComics: Int,
        newChapters: Int,
        newPages: Int,
        timestampMillis: Long = TimeUtils.currentTimeMillis()
    ): ScanTask {
        return copy(
            status = TaskStatus.COMPLETED,
            completedAtMillis = timestampMillis,
            progress = 100,
            foundComicCount = foundComics,
            newChapterCount = newChapters,
            newPageCount = newPages
        )
    }

    /**
     * 标记任务失败
     */
    fun markFailed(error: String, timestampMillis: Long = TimeUtils.currentTimeMillis()): ScanTask {
        return copy(
            status = TaskStatus.FAILED,
            completedAtMillis = timestampMillis,
            errorMessage = error
        )
    }

    /**
     * 标记任务取消
     */
    fun markCancelled(timestampMillis: Long = TimeUtils.currentTimeMillis()): ScanTask {
        return copy(
            status = TaskStatus.CANCELLED,
            completedAtMillis = timestampMillis
        )
    }
}

/**
 * 扫描类型
 */
enum class ScanType {
    /**
     * 全量扫描
     */
    FULL,

    /**
     * 增量扫描（仅扫描变化）
     */
    INCREMENTAL
}

/**
 * 任务状态
 */
enum class TaskStatus {
    /**
     * 等待中
     */
    PENDING,

    /**
     * 运行中
     */
    RUNNING,

    /**
     * 已完成
     */
    COMPLETED,

    /**
     * 失败
     */
    FAILED,

    /**
     * 已取消
     */
    CANCELLED
}
