package com.pusu.indexed.data.local.store

import com.pusu.indexed.domain.comic.model.LibraryRoot

/**
 * 本地库根目录存储接口
 * 
 * 负责根目录的持久化存储与管理
 * 实现层可使用 SQLDelight 或其他持久化方案
 */
interface LocalLibraryRootStore {
    /**
     * 保存或更新根目录
     */
    suspend fun saveRoot(root: LibraryRoot)

    /**
     * 批量保存根目录
     */
    suspend fun saveRoots(roots: List<LibraryRoot>)

    /**
     * 根据 ID 获取根目录
     */
    suspend fun getRootById(id: String): LibraryRoot?

    /**
     * 获取所有根目录，按 sortKey 排序
     */
    suspend fun getAllRoots(): List<LibraryRoot>

    /**
     * 获取指定来源类型的根目录
     */
    suspend fun getRootsBySource(source: String): List<LibraryRoot>

    /**
     * 获取启用自动同步的根目录
     */
    suspend fun getAutoSyncEnabledRoots(): List<LibraryRoot>

    /**
     * 删除根目录
     */
    suspend fun deleteRoot(id: String)

    /**
     * 更新根目录的扫描信息
     */
    suspend fun updateScanInfo(id: String, comicCount: Int, timestampMillis: Long)

    /**
     * 检查根目录 URI 是否已存在
     */
    suspend fun existsByUri(rootUri: String): Boolean
}
