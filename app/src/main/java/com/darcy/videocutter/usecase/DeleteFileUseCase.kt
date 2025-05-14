package com.darcy.videocutter.usecase

import com.darcy.videocutter.interface_dapters.IFileRepository

/**
 * 删除文件
 */
class DeleteFileUseCase(
    private val fileRepository: IFileRepository
) {
    // 重载operator
    suspend operator fun invoke(filePath: String?): Boolean {
        return if (filePath == null) {
            false
        } else {
            fileRepository.deleteFile(filePath)
        }
    }
}