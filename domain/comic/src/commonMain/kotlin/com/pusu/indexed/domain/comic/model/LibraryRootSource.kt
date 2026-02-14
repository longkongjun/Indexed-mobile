package com.pusu.indexed.domain.comic.model

/**
 * 本地库根目录来源类型
 * 
 * 定义三种来源：
 * 1. DOWNLOADED - 应用内下载
 * 2. IMPORTED_INTERNAL - 应用内手动导入
 * 3. IMPORTED_EXTERNAL - 应用外手动导入（Android SAF/SD + iOS Files 选择器）
 */
enum class LibraryRootSource {
    /**
     * 应用内下载
     * - 完全控制权限
     * - 存储在应用私有目录
     */
    DOWNLOADED,

    /**
     * 应用内导入
     * - 完全控制权限
     * - 文件已复制到应用私有目录
     */
    IMPORTED_INTERNAL,

    /**
     * 应用外导入
     * - Android: 通过 SAF (Storage Access Framework) 授权
     * - iOS: 通过 Files 选择器授权
     * - 需要维护持久化访问权限
     */
    IMPORTED_EXTERNAL
}
