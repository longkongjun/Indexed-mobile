package com.pusu.indexed.domain.comic.model

import com.pusu.indexed.core.utils.TimeUtils

/**
 * 本地漫画
 */
data class Comic(
    /**
     * 漫画唯一标识符（可使用路径/URI 哈希）
     */
    val id: String,

    /**
     * 漫画标题（从目录名推断或元数据获取）
     */
    val title: String,

    /**
     * 漫画根目录 URI/路径
     */
    val rootUri: String,

    /**
     * 所属库根目录 ID
     */
    val libraryRootId: String,

    /**
     * 封面页 URI/路径（可选）
     * 通常是第一话第一页
     */
    val coverPageUri: String?,

    /**
     * 最近扫描时间戳（毫秒）
     */
    val updatedAtMillis: Long,

    /**
     * 自然排序键
     * 用于列表稳定排序
     */
    val sortKey: String,

    /**
     * 章节数量（缓存）
     */
    val chapterCount: Int = 0,

    /**
     * 创建时间戳（毫秒）
     */
    val createdAtMillis: Long = TimeUtils.currentTimeMillis()
) {
    /**
     * 更新扫描时间
     */
    fun updateScanned(timestampMillis: Long = TimeUtils.currentTimeMillis()): Comic {
        return copy(updatedAtMillis = timestampMillis)
    }

    /**
     * 更新章节数量
     */
    fun updateChapterCount(count: Int): Comic {
        return copy(chapterCount = count)
    }
}
