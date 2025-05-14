package com.darcy.videocutter.usecase

import com.darcy.videocutter.interface_dapters.IFileRepository

/**
 * 删除目录
 */
class DeleteOutputCacheFolderUseCase(
    private val fileRepository: IFileRepository
) {
    // 重载operator
    suspend operator fun invoke(): Boolean {
        return fileRepository.deleteOutputCacheFolder()
    }
}