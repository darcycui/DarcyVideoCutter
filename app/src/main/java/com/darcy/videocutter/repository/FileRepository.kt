package com.darcy.videocutter.repository

import android.content.Context
import android.net.Uri
import com.darcy.lib_saf_select.utils.UriUtil
import com.darcy.videocutter.app.App
import com.darcy.videocutter.interface_dapters.IFileRepository
import java.io.File
import androidx.core.net.toUri
import com.darcy.videocutter.utils.TimeUtil

class FileRepository(private val context: Context = App.getInstance()) : IFileRepository {
    companion object {
        private const val INPUT_TEMP_FOLDER = "input_tmp"
        private const val OUTPUT_TEMP_FOLDER = "output_tmp"
        private const val VIDEO_THUMBNAIL_FOLDER = "video_thumbnail"
    }

    private val _inputTempFile: File? by lazy {
        context.applicationContext.getExternalFilesDir(INPUT_TEMP_FOLDER)
    }
    private val _outputTempFile: File? by lazy {
        context.applicationContext.getExternalFilesDir(OUTPUT_TEMP_FOLDER)
    }
    private val _videoThumbnailFile: File? by lazy {
        context.applicationContext.getExternalFilesDir(VIDEO_THUMBNAIL_FOLDER)
    }

    override fun getInputTempFile(): File? {
        return _inputTempFile
    }

    override fun getOutputTempFile(): File? {
        return _outputTempFile
    }

    override fun getVideoThumbnailFile(): File? {
        return _videoThumbnailFile
    }

    override fun getInputTempPath(): String {
        return _inputTempFile?.absolutePath ?: ""
    }

    override fun getOutputTempPath(): String {
        return _outputTempFile?.absolutePath ?: ""
    }

    override fun getVideoThumbnailPath(): String {
        return _videoThumbnailFile?.absolutePath ?: ""
    }

    override suspend fun copyToInputTemp(uriStr: String): String {
        if (_inputTempFile == null) {
            return ""
        }
        val inputUri = uriStr.toUri()
        val fileNameFull =
            UriUtil.getFileNameFromUri(context, inputUri) ?: TimeUtil.getCurrentTimeShort()
        val tempFile = File(_inputTempFile, fileNameFull)
        return if (UriUtil.copyFileToAppDir(context, inputUri, tempFile)) {
            tempFile.absolutePath
        } else {
            ""
        }
    }

    override suspend fun copyToPublicOutput(
        fromFilePath: String?,
        uriStr: String?
    ): Uri? {
        if (_outputTempFile == null || fromFilePath.isNullOrEmpty() || uriStr.isNullOrEmpty()) {
            return null
        }
        //复制到输出目录
        val publicOutFolderUri = uriStr.toUri()
        val fromFile = File(fromFilePath)
        if (fromFile.exists().not()) {
            return null
        }
        return UriUtil.copyFileToPublicDir(context, File(fromFilePath), publicOutFolderUri)
    }

    override suspend fun deleteInputCacheFolder(): Boolean {
        val inputCacheFolderPath = getInputTempPath()
        return deleteFolder(inputCacheFolderPath)
    }

    override suspend fun deleteOutputCacheFolder(): Boolean {
        val outputCacheFolderPath = getOutputTempPath()
        return deleteFolder(outputCacheFolderPath)
    }

    override suspend fun deleteVideoThumbnailFolder(): Boolean {
        val outputCacheFolderPath = getVideoThumbnailPath()
        return deleteFolder(outputCacheFolderPath)
    }

    override suspend fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }

    suspend fun deleteFolder(folderPath: String): Boolean {
        if (folderPath.isEmpty()) {
            return false
        }
        val folder = File(folderPath)
        return if (folder.exists()) {
            folder.deleteRecursively()
        } else {
            false
        }
    }

}