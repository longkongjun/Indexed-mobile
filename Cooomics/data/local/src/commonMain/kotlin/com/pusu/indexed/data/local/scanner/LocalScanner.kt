package com.pusu.indexed.data.local.scanner

import com.pusu.indexed.domain.comic.model.Chapter
import com.pusu.indexed.domain.comic.model.Comic
import com.pusu.indexed.domain.comic.model.LibraryRoot
import com.pusu.indexed.domain.comic.model.Page
import com.pusu.indexed.domain.comic.model.ScanType

/**
 * 本地漫画扫描器
 * 
 * 负责扫描本地文件系统，识别漫画、章节、页面
 * 平台相关实现使用 expect/actual
 */
interface LocalScanner {
    /**
     * 扫描结果
     */
    data class ScanResult(
        val comics: List<Comic>,
        val chapters: List<Chapter>,
        val pages: List<Page>
    )

    /**
     * 扫描进度回调
     */
    interface ProgressCallback {
        /**
         * 进度更新
         * @param progress 进度（0-100）
         * @param currentItem 当前处理项描述
         */
        suspend fun onProgress(progress: Int, currentItem: String)

        /**
         * 发现漫画
         */
        suspend fun onComicFound(comic: Comic)

        /**
         * 发现章节
         */
        suspend fun onChapterFound(chapter: Chapter)
    }

    /**
     * 扫描根目录
     * 
     * @param root 要扫描的根目录
     * @param scanType 扫描类型（全量/增量）
     * @param progressCallback 进度回调（可选）
     * @return 扫描结果
     */
    suspend fun scanRoot(
        root: LibraryRoot,
        scanType: ScanType,
        progressCallback: ProgressCallback? = null
    ): ScanResult

    /**
     * 扫描单个漫画
     * 
     * @param comicUri 漫画目录 URI
     * @param libraryRootId 所属根目录 ID
     * @return 扫描结果（单个漫画及其章节、页面）
     */
    suspend fun scanComic(
        comicUri: String,
        libraryRootId: String
    ): ScanResult

    /**
     * 验证 URI 是否可访问
     */
    suspend fun verifyUri(uri: String): Boolean

    /**
     * 计算 URI 哈希（用于生成 ID）
     */
    fun calculateUriHash(uri: String): String
}
