package com.pusu.indexed.data.local.background

/**
 * 后台同步工作器接口
 * 
 * 定义后台任务的执行逻辑
 * 由平台特定实现调用
 */
interface BackgroundSyncWorker {
    /**
     * 执行同步任务
     * 
     * @return 任务结果
     */
    suspend fun doWork(): WorkResult

    /**
     * 任务是否应该停止
     * 用于支持任务取消
     */
    fun shouldStop(): Boolean
}

/**
 * 工作结果
 */
sealed class WorkResult {
    /**
     * 成功
     */
    data class Success(
        val message: String = "同步完成"
    ) : WorkResult()

    /**
     * 失败（不重试）
     */
    data class Failure(
        val error: String
    ) : WorkResult()

    /**
     * 重试
     */
    data class Retry(
        val error: String,
        val delayMinutes: Long = 15
    ) : WorkResult()
}
