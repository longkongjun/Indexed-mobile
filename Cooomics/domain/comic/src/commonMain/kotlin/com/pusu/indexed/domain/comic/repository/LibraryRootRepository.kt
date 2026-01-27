package com.pusu.indexed.domain.comic.repository

import com.pusu.indexed.domain.comic.model.LibraryRoot

/**
 * 本地库根目录仓库接口
 */
interface LibraryRootRepository {
    /**
     * 获取所有根目录
     */
    suspend fun getAllRoots(): List<LibraryRoot>

    /**
     * 根据 ID 获取根目录
     */
    suspend fun getRootById(id: String): LibraryRoot?

    /**
     * 添加根目录
     */
    suspend fun addRoot(root: LibraryRoot)

    /**
     * 更新根目录
     */
    suspend fun updateRoot(root: LibraryRoot)

    /**
     * 删除根目录
     */
    suspend fun deleteRoot(id: String)

    /**
     * 验证根目录权限
     * 更新权限状态并返回最新的根目录信息
     */
    suspend fun verifyRootPermission(id: String): LibraryRoot?

    /**
     * 获取启用自动同步的根目录
     */
    suspend fun getAutoSyncEnabledRoots(): List<LibraryRoot>
}
