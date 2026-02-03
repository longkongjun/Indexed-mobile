package com.pusu.indexed.domain.comic.model

import com.pusu.indexed.core.utils.TimeUtils

/**
 * 本地漫画章节
 */
data class Chapter(
    /**
     * 章节唯一标识符
     */
    val id: String,

    /**
     * 所属漫画 ID
     */
    val comicId: String,

    /**
     * 章节标题（从目录/CBZ 名推断或元数据获取）
     */
    val title: String,

    /**
     * 章节目录/CBZ URI
     */
    val chapterUri: String,

    /**
     * 是否为 CBZ 格式
     */
    val isCbz: Boolean,

    /**
     * 最近扫描时间戳（毫秒）
     */
    val updatedAtMillis: Long,

    /**
     * 自然排序键
     */
    val sortKey: String,

    /**
     * 页数（缓存）
     */
    val pageCount: Int = 0,

    /**
     * 创建时间戳（毫秒）
     */
    val createdAtMillis: Long = TimeUtils.currentTimeMillis()
) {
    /**
     * 更新扫描时间
     */
    fun updateScanned(timestampMillis: Long = TimeUtils.currentTimeMillis()): Chapter {
        return copy(updatedAtMillis = timestampMillis)
    }

    /**
     * 更新页数
     */
    fun updatePageCount(count: Int): Chapter {
        return copy(pageCount = count)
    }
}
