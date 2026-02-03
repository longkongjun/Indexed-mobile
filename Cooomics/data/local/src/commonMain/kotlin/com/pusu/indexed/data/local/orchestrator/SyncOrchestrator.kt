package com.pusu.indexed.data.local.orchestrator

import com.pusu.indexed.domain.comic.model.ScanTask
import com.pusu.indexed.domain.comic.model.ScanType
import com.pusu.indexed.domain.comic.model.ScrapeType

/**
 * 同步编排器
 * 
 * 协调扫描、索引更新、刮削任务的执行流程：
 * 1. 扫描根目录 -> 发现漫画/章节/页面
 * 2. 更新索引缓存 -> 写入数据库
 * 3. 触发刮削队列 -> 获取元数据
 */
interface SyncOrchestrator {
    /**
     * 同步配置
     */
    data class SyncConfig(
        /**
         * 是否启用自动刮削
         */
        val enableAutoScrape: Boolean = true,

        /**
         * 刮削类型
         */
        val scrapeType: ScrapeType = ScrapeType.FULL,

        /**
         * 批量提交索引变更大小
         */
        val batchSize: Int = 50,

        /**
         * 扫描超时时间（毫秒）
         */
        val scanTimeoutMillis: Long = 30 * 60 * 1000L // 30分钟
    )

    /**
     * 同步结果
     */
    data class SyncResult(
        val scanTask: ScanTask,
        val newComicCount: Int,
        val updatedComicCount: Int,
        val newChapterCount: Int,
        val newPageCount: Int,
        val scrapeTaskIds: List<String>
    )

    /**
     * 同步进度回调
     */
    interface SyncProgressCallback {
        /**
         * 扫描阶段开始
         */
        suspend fun onScanStarted(libraryRootId: String)

        /**
         * 扫描进度更新
         */
        suspend fun onScanProgress(progress: Int, currentItem: String)

        /**
         * 索引更新阶段开始
         */
        suspend fun onIndexingStarted(totalItems: Int)

        /**
         * 索引更新进度
         */
        suspend fun onIndexingProgress(processed: Int, total: Int)

        /**
         * 刮削阶段开始
         */
        suspend fun onScrapeStarted(taskCount: Int)

        /**
         * 同步完成
         */
        suspend fun onSyncCompleted(result: SyncResult)

        /**
         * 同步失败
         */
        suspend fun onSyncFailed(error: String)
    }

    /**
     * 同步单个根目录
     * 
     * 执行完整的扫描-索引-刮削流程
     * 
     * @param libraryRootId 根目录 ID
     * @param scanType 扫描类型
     * @param config 同步配置
     * @param callback 进度回调
     * @return 同步结果
     */
    suspend fun syncRoot(
        libraryRootId: String,
        scanType: ScanType,
        config: SyncConfig = SyncConfig(),
        callback: SyncProgressCallback? = null
    ): SyncResult

    /**
     * 同步所有启用自动同步的根目录
     * 
     * @param scanType 扫描类型
     * @param config 同步配置
     * @return 所有根目录的同步结果
     */
    suspend fun syncAllRoots(
        scanType: ScanType = ScanType.INCREMENTAL,
        config: SyncConfig = SyncConfig()
    ): List<SyncResult>

    /**
     * 取消同步
     * 
     * @param libraryRootId 根目录 ID
     */
    suspend fun cancelSync(libraryRootId: String)

    /**
     * 获取正在进行的同步任务
     */
    suspend fun getActiveSyncTasks(): List<ScanTask>
}
