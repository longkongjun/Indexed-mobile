package com.pusu.indexed.domain.comic.usecase

import com.pusu.indexed.domain.comic.model.ScrapeType

/**
 * 入队刮削任务用例
 */
interface EnqueueScrapeUseCase {
    /**
     * 为单个漫画入队刮削任务
     * 
     * @param comicId 漫画 ID
     * @param comicTitle 漫画标题
     * @param scrapeType 刮削类型
     * @param priority 优先级
     * @return 任务 ID
     */
    suspend fun enqueueForComic(
        comicId: String,
        comicTitle: String,
        scrapeType: ScrapeType = ScrapeType.FULL,
        priority: Int = 0
    ): String

    /**
     * 为多个漫画批量入队刮削任务
     * 
     * @param comics 漫画列表（ID 和标题的对）
     * @param scrapeType 刮削类型
     * @return 任务 ID 列表
     */
    suspend fun enqueueForComics(
        comics: List<Pair<String, String>>,
        scrapeType: ScrapeType = ScrapeType.FULL
    ): List<String>

    /**
     * 重试失败的刮削任务
     */
    suspend fun retryFailed()
}
