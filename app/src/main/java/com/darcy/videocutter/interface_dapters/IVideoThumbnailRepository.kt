package com.darcy.videocutter.interface_dapters

import android.graphics.Bitmap
import java.io.File

interface IVideoThumbnailRepository {
    suspend fun generateVideoThumbnailByRetriever(videoUriStr: String?): Bitmap?

    suspend fun saveVideoThumbnail(videoUriStr: String?, bitmap: Bitmap?): File?
}