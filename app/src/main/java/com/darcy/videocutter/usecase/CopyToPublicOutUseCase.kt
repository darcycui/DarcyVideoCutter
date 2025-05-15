package com.darcy.videocutter.usecase

import android.net.Uri
import com.darcy.videocutter.interface_dapters.IFileRepository

/**
 * 复制文件到公共目录
 */
class CopyToPublicOutUseCase(
    private val fileRepository: IFileRepository
) {
    // 重载operator
    suspend operator fun invoke(cutFilePath: String?, uriStr: String?): Uri? {
        return if (cutFilePath == null || uriStr == null) {
            null
        } else {
            fileRepository.copyToPublicOutput(cutFilePath, uriStr)
        }
    }
}