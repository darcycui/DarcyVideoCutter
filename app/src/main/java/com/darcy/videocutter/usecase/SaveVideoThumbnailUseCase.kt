package com.darcy.videocutter.usecase

import android.graphics.Bitmap
import com.darcy.videocutter.interface_dapters.IVideoThumbnailRepository
import java.io.File

class SaveVideoThumbnailUseCase(private val videoThumbnailRepository: IVideoThumbnailRepository) {
    suspend operator fun invoke(videoUriStr: String?, bitmap: Bitmap?): File? {
        return if (videoUriStr.isNullOrEmpty()) {
            null
        } else {
            videoThumbnailRepository.saveVideoThumbnail(videoUriStr, bitmap)
        }
    }
}