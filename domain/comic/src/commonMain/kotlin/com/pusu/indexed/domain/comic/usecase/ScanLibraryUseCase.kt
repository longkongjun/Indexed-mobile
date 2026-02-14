package com.pusu.indexed.domain.comic.usecase

import com.pusu.indexed.domain.comic.model.ScanType

/**
 * 扫描库用例
 * 
 * 触发库扫描任务，由数据层的 SyncOrchestrator 实际执行
 */
interface ScanLibraryUseCase {
    /**
     * 扫描结果
     */
    data class Result(
        val success: Boolean,
        val newComicCount: Int,
        val updatedComicCount: Int,
        val newChapterCount: Int,
        val error: String? = null
    )

    /**
     * 扫描单个根目录
     * 
     * @param libraryRootId 根目录 ID
     * @param scanType 扫描类型
     * @return 扫描结果
     */
    suspend fun scanRoot(
        libraryRootId: String,
        scanType: ScanType = ScanType.INCREMENTAL
    ): Result

    /**
     * 扫描所有启用自动同步的根目录
     * 
     * @param scanType 扫描类型
     * @return 所有根目录的扫描结果
     */
    suspend fun scanAllRoots(
        scanType: ScanType = ScanType.INCREMENTAL
    ): List<Result>
}
