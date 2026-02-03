package com.pusu.indexed.domain.comic.model

import com.pusu.indexed.core.utils.TimeUtils

/**
 * 本地库根目录权限状态
 */
data class LibraryRootPermission(
    /**
     * 是否具有读权限
     */
    val canRead: Boolean,

    /**
     * 是否具有写权限（删除/重命名等操作）
     */
    val canWrite: Boolean,

    /**
     * 权限是否已持久化
     * - DOWNLOADED/IMPORTED_INTERNAL: 默认 true
     * - IMPORTED_EXTERNAL: 需要检查平台授权状态
     */
    val isPersisted: Boolean,

    /**
     * 权限授权时间戳（毫秒）
     * 用于追踪权限生命周期
     */
    val grantedAtMillis: Long,

    /**
     * 最后验证时间戳（毫秒）
     * 定期验证权限是否仍然有效
     */
    val lastVerifiedAtMillis: Long
) {
    companion object {
        /**
         * 创建完全控制权限（用于 DOWNLOADED/IMPORTED_INTERNAL）
         */
        fun fullControl(timestampMillis: Long = TimeUtils.currentTimeMillis()): LibraryRootPermission {
            return LibraryRootPermission(
                canRead = true,
                canWrite = true,
                isPersisted = true,
                grantedAtMillis = timestampMillis,
                lastVerifiedAtMillis = timestampMillis
            )
        }

        /**
         * 创建外部授权权限（用于 IMPORTED_EXTERNAL）
         */
        fun externalGrant(
            canRead: Boolean,
            canWrite: Boolean,
            isPersisted: Boolean,
            grantedAtMillis: Long = TimeUtils.currentTimeMillis()
        ): LibraryRootPermission {
            return LibraryRootPermission(
                canRead = canRead,
                canWrite = canWrite,
                isPersisted = isPersisted,
                grantedAtMillis = grantedAtMillis,
                lastVerifiedAtMillis = grantedAtMillis
            )
        }
    }

    /**
     * 权限是否有效（可读且已持久化）
     */
    fun isValid(): Boolean = canRead && isPersisted

    /**
     * 更新最后验证时间
     */
    fun updateVerified(timestampMillis: Long = TimeUtils.currentTimeMillis()): LibraryRootPermission {
        return copy(lastVerifiedAtMillis = timestampMillis)
    }
}
