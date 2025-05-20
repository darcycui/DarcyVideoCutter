package com.darcy.videocutter.usecase

import com.darcy.videocutter.interface_dapters.ICutVideoRepository

/**
 * 切割视频
 */
class CutVideoUseCase(
    private val cutVideoRepository: ICutVideoRepository
) {
    // 重载operator
    suspend operator fun invoke(
        inputUriStr: String?,
        startMs: Long,
        endMs: Long
    ): String {
        return if (inputUriStr == null || startMs < 0 || endMs < 0 || startMs >= endMs) {
            ""
        } else {
            cutVideoRepository.cutVideo(inputUriStr, startMs, endMs) ?: ""
        }
    }
}