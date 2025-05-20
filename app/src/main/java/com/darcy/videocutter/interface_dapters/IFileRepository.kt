package com.darcy.videocutter.interface_dapters

import android.net.Uri
import java.io.File

interface IFileRepository {
    // 临时输入文件夹
    fun getInputTempFile(): File?

    // 临时输出文件夹
    fun getOutputTempFile(): File?

    // 缩略图文件夹
    fun getVideoThumbnailFile(): File?

    // 临时输入文件路径
    fun getInputTempPath(): String

    // 临时输出文件路径
    fun getOutputTempPath(): String

    // 缩略图文件路径
    fun getVideoThumbnailPath(): String

    suspend fun copyToInputTemp(uriStr: String): String

    suspend fun copyToPublicOutput(
        cutFilePath: String?, uriStr: String?
    ): Uri?

    suspend fun deleteFile(filePath: String): Boolean

    suspend fun deleteInputCacheFolder(): Boolean

    suspend fun deleteOutputCacheFolder(): Boolean

    suspend fun deleteVideoThumbnailFolder(): Boolean
}