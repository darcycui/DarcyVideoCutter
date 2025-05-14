package com.darcy.videocutter.usecase

import com.darcy.videocutter.interface_dapters.ISPRepository

/**
 * 保存 SAF Tree Uri
 */
class GetSAFTreeUseCase(
    private val spRepository: ISPRepository
) {
    // 重载operator
    operator fun invoke(): String {
        return spRepository.getSavedTreeUri() ?: ""
    }
}