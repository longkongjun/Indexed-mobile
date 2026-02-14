package com.pusu.indexed.domain.comic.usecase

import com.pusu.indexed.domain.comic.model.LibraryRoot
import com.pusu.indexed.domain.comic.repository.LibraryRootRepository

/**
 * 添加库根目录用例
 */
class AddLibraryRootUseCase(
    private val repository: LibraryRootRepository
) {
    /**
     * 执行添加
     * 
     * @param root 要添加的根目录
     * @throws IllegalArgumentException 如果根目录已存在
     */
    suspend operator fun invoke(root: LibraryRoot) {
        // 验证根目录信息
        require(root.displayName.isNotBlank()) { "显示名称不能为空" }
        require(root.rootUri.isNotBlank()) { "根目录 URI 不能为空" }
        require(root.permission.isValid()) { "根目录权限无效" }

        // 添加到仓库
        repository.addRoot(root)
    }
}
