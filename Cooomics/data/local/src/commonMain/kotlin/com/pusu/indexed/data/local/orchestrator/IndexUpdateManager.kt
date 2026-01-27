package com.pusu.indexed.data.local.orchestrator

import com.pusu.indexed.domain.comic.model.Chapter
import com.pusu.indexed.domain.comic.model.Comic
import com.pusu.indexed.domain.comic.model.Page

/**
 * 索引更新管理器
 * 
 * 负责将扫描结果更新到数据库索引
 * 执行增量更新逻辑（新增/更新/删除）
 */
interface IndexUpdateManager {
    /**
     * 更新结果
     */
    data class UpdateResult(
        val newComics: Int,
        val updatedComics: Int,
        val deletedComics: Int,
        val newChapters: Int,
        val updatedChapters: Int,
        val deletedChapters: Int,
        val newPages: Int,
        val deletedPages: Int
    )

    /**
     * 更新漫画索引
     * 
     * 对比现有索引和扫描结果，执行增量更新
     * 
     * @param libraryRootId 根目录 ID
     * @param scannedComics 扫描到的漫画列表
     * @param scannedChapters 扫描到的章节列表
     * @param scannedPages 扫描到的页面列表
     * @return 更新结果
     */
    suspend fun updateIndex(
        libraryRootId: String,
        scannedComics: List<Comic>,
        scannedChapters: List<Chapter>,
        scannedPages: List<Page>
    ): UpdateResult

    /**
     * 批量更新索引（分批提交）
     * 
     * @param libraryRootId 根目录 ID
     * @param scannedComics 扫描到的漫画列表
     * @param scannedChapters 扫描到的章节列表
     * @param scannedPages 扫描到的页面列表
     * @param batchSize 批量大小
     * @param progressCallback 进度回调
     * @return 更新结果
     */
    suspend fun updateIndexBatch(
        libraryRootId: String,
        scannedComics: List<Comic>,
        scannedChapters: List<Chapter>,
        scannedPages: List<Page>,
        batchSize: Int = 50,
        progressCallback: ((processed: Int, total: Int) -> Unit)? = null
    ): UpdateResult

    /**
     * 删除根目录下的所有索引
     * 
     * @param libraryRootId 根目录 ID
     */
    suspend fun deleteRootIndex(libraryRootId: String)

    /**
     * 清理孤立的章节和页面
     * 
     * 删除没有对应漫画或章节的数据
     */
    suspend fun cleanupOrphans()
}
