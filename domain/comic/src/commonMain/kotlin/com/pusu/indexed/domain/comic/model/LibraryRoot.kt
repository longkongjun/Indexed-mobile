package com.pusu.indexed.domain.comic.model

import com.pusu.indexed.core.utils.TimeUtils

/**
 * 本地库根目录
 * 
 * 代表一个本地漫画库的根目录，可能来自三种来源：
 * 1. 应用内下载
 * 2. 应用内导入
 * 3. 应用外导入（SAF/Files）
 */
data class LibraryRoot(
    /**
     * 根目录唯一标识符
     * 建议使用 URI/路径的哈希值
     */
    val id: String,

    /**
     * 根目录显示名称
     * 用于 UI 展示
     */
    val displayName: String,

    /**
     * 根目录 URI/路径
     * - Android: content:// URI (SAF) 或文件路径
     * - iOS: file:// URL 或相对路径
     */
    val rootUri: String,

    /**
     * 来源类型
     */
    val source: LibraryRootSource,

    /**
     * 权限信息
     */
    val permission: LibraryRootPermission,

    /**
     * 最近扫描时间戳（毫秒）
     * 用于增量扫描判断
     */
    val lastScannedAtMillis: Long,

    /**
     * 扫描到的漫画数量
     * 缓存值，避免每次查询数据库
     */
    val comicCount: Int,

    /**
     * 创建时间戳（毫秒）
     */
    val createdAtMillis: Long,

    /**
     * 是否启用自动同步
     * 用户可禁用某些根目录的自动扫描
     */
    val autoSyncEnabled: Boolean = true,

    /**
     * 自定义排序键
     * 用于用户自定义根目录展示顺序
     */
    val sortKey: Int = 0
) {
    companion object {
        /**
         * 创建下载来源根目录
         */
        fun createDownloadedRoot(
            id: String,
            displayName: String,
            rootUri: String,
            timestampMillis: Long = TimeUtils.currentTimeMillis()
        ): LibraryRoot {
            return LibraryRoot(
                id = id,
                displayName = displayName,
                rootUri = rootUri,
                source = LibraryRootSource.DOWNLOADED,
                permission = LibraryRootPermission.fullControl(timestampMillis),
                lastScannedAtMillis = 0L,
                comicCount = 0,
                createdAtMillis = timestampMillis,
                autoSyncEnabled = true,
                sortKey = 0
            )
        }

        /**
         * 创建应用内导入根目录
         */
        fun createInternalImportedRoot(
            id: String,
            displayName: String,
            rootUri: String,
            timestampMillis: Long = TimeUtils.currentTimeMillis()
        ): LibraryRoot {
            return LibraryRoot(
                id = id,
                displayName = displayName,
                rootUri = rootUri,
                source = LibraryRootSource.IMPORTED_INTERNAL,
                permission = LibraryRootPermission.fullControl(timestampMillis),
                lastScannedAtMillis = 0L,
                comicCount = 0,
                createdAtMillis = timestampMillis,
                autoSyncEnabled = true,
                sortKey = 0
            )
        }

        /**
         * 创建应用外导入根目录
         */
        fun createExternalImportedRoot(
            id: String,
            displayName: String,
            rootUri: String,
            permission: LibraryRootPermission,
            timestampMillis: Long = TimeUtils.currentTimeMillis()
        ): LibraryRoot {
            return LibraryRoot(
                id = id,
                displayName = displayName,
                rootUri = rootUri,
                source = LibraryRootSource.IMPORTED_EXTERNAL,
                permission = permission,
                lastScannedAtMillis = 0L,
                comicCount = 0,
                createdAtMillis = timestampMillis,
                autoSyncEnabled = true,
                sortKey = 0
            )
        }
    }

    /**
     * 根目录是否可用（权限有效）
     */
    fun isAvailable(): Boolean = permission.isValid()

    /**
     * 是否需要重新扫描
     * @param intervalMillis 扫描间隔（毫秒）
     */
    fun needsRescan(intervalMillis: Long): Boolean {
        if (lastScannedAtMillis == 0L) return true
        val elapsed = TimeUtils.currentTimeMillis() - lastScannedAtMillis
        return elapsed >= intervalMillis
    }

    /**
     * 更新扫描信息
     */
    fun updateScanInfo(
        comicCount: Int,
        timestampMillis: Long = TimeUtils.currentTimeMillis()
    ): LibraryRoot {
        return copy(
            lastScannedAtMillis = timestampMillis,
            comicCount = comicCount
        )
    }

    /**
     * 更新权限信息
     */
    fun updatePermission(permission: LibraryRootPermission): LibraryRoot {
        return copy(permission = permission)
    }
}
