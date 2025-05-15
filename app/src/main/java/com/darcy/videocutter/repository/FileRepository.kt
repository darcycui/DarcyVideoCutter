package com.darcy.videocutter.repository

import android.content.Context
import android.net.Uri
import com.darcy.lib_saf_select.utils.UriUtil
import com.darcy.videocutter.app.App
import com.darcy.videocutter.interface_dapters.IFileRepository
import java.io.File
import androidx.core.net.toUri

class FileRepository(private val context: Context = App.getInstance()) : IFileRepository {
    companion object {
        private const val INPUT_TEMP_FOLDER = "input_tmp"
        private const val OUTPUT_TEMP_FOLDER = "output_tmp"
    }

    private val _inputTempFile: File? by lazy {
        context.applicationContext.getExternalFilesDir(INPUT_TEMP_FOLDER)
    }
    private val _outputTempFile: File? by lazy {
        context.applicationContext.getExternalFilesDir(OUTPUT_TEMP_FOLDER)
    }

    override fun getInputTempFile(): File? {
        return _inputTempFile
    }

    override fun getOutputTempFile(): File? {
        return _outputTempFile
    }

    override fun getInputTempPath(): String {
        return _inputTempFile?.absolutePath ?: ""
    }

    override fun getOutputTempPath(): String {
        return _outputTempFile?.absolutePath ?: ""
    }

    override suspend fun copyToInputTemp(uriStr: String): String {
        if (_inputTempFile == null) {
            return ""
        }
        val inputUri = uriStr.toUri()
        val fileNameFull = UriUtil.getFileNameFromUri(context, inputUri)
        val tempFile = File(_inputTempFile, fileNameFull)
        return if (UriUtil.copyFileToAppDir(context, inputUri, tempFile)) {
            tempFile.absolutePath
        } else {
            ""
        }
    }

    override suspend fun copyToPublicOutput(
        cutFilePath: String,
        uriStr: String
    ): Uri? {
        if (_outputTempFile == null) {
            return null
        }
        //复制到输出目录
        val publicOutFolderUri = uriStr.toUri()
        val fromFile = File(cutFilePath)
        if (fromFile.exists().not()) {
            return null
        }
        return UriUtil.copyFileToPublicDir(context, File(cutFilePath), publicOutFolderUri)
    }

    override suspend fun deleteInputCacheFolder(): Boolean {
        val inputCacheFolderPath = getInputTempPath()
        return deleteFolder(inputCacheFolderPath)
    }

    override suspend fun deleteOutputCacheFolder(): Boolean {
        val outputCacheFolderPath = getOutputTempPath()
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