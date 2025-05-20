package com.darcy.videocutter.usecase

import com.darcy.videocutter.interface_dapters.IJoinVideoRepository

/**
 * 拼接视频
 */
class JoinVideoUseCase(
    private val joinVideoRepository: IJoinVideoRepository
) {
    // 重载operator
    suspend operator fun invoke(
        inputVideoUriStrings: List<String>
    ): String {
        return if (inputVideoUriStrings.isEmpty()) {
            ""
        } else {
            joinVideoRepository.joinVideo(inputVideoUriStrings) ?: ""
        }
    }
}