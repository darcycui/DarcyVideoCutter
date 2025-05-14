package com.darcy.videocutter.usecase

import com.darcy.videocutter.interface_dapters.ISPRepository

/**
 * 保存 SAF Tree Uri
 */
class SaveSAFTreeUseCase(
    private val spRepository: ISPRepository
) {
    // 重载operator
    operator fun invoke(inputUriPath: String): Unit {
        if (inputUriPath.isEmpty()) {
            return
        }
        spRepository.saveTreeUri(inputUriPath)
    }
}