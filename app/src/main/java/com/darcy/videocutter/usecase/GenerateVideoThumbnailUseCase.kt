package com.darcy.videocutter.usecase

import android.graphics.Bitmap
import com.darcy.videocutter.interface_dapters.IVideoThumbnailRepository

class GenerateVideoThumbnailUseCase(private val videoThumbnailRepository: IVideoThumbnailRepository) {
    suspend operator fun invoke(videoUriStr: String?): Bitmap? {
        return if (videoUriStr.isNullOrEmpty()) {
            null
        } else {
            videoThumbnailRepository.generateVideoThumbnailByRetriever(videoUriStr)
        }
    }
}