package com.pusu.indexed.domain.comic.model

import com.pusu.indexed.core.utils.TimeUtils

/**
 * 本地漫画页
 */
data class Page(
    /**
     * 页唯一标识符
     */
    val id: String,

    /**
     * 所属章节 ID
     */
    val chapterId: String,

    /**
     * 页文件名
     */
    val fileName: String,

    /**
     * 页序号（从 0 开始）
     */
    val pageIndex: Int,

    /**
     * 页 URI/路径
     */
    val pageUri: String,

    /**
     * 图片宽度（可选，用于缓存）
     */
    val width: Int? = null,

    /**
     * 图片高度（可选，用于缓存）
     */
    val height: Int? = null,

    /**
     * 创建时间戳（毫秒）
     */
    val createdAtMillis: Long = TimeUtils.currentTimeMillis()
) {
    /**
     * 更新尺寸信息
     */
    fun updateDimensions(width: Int, height: Int): Page {
        return copy(width = width, height = height)
    }

    /**
     * 是否有尺寸信息
     */
    fun hasDimensions(): Boolean = width != null && height != null
}
