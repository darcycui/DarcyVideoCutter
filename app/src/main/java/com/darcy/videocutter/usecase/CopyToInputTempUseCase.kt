package com.darcy.videocutter.usecase

import com.darcy.videocutter.interface_dapters.IFileRepository

/**
 * 复制文件到私有目录
 */
class CopyToInputTempUseCase(
    private val fileRepository: IFileRepository
) {
    // 重载operator
    suspend operator fun invoke(inputUriPath: String?): String {
        return if (inputUriPath == null) {
            ""
        } else {
            fileRepository.copyToInputTemp(inputUriPath)
        }
    }
}