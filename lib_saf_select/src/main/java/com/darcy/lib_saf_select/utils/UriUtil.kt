package com.darcy.lib_saf_select.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_log_toast.exts.logI
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


/**
 * Uri工具类
 */
object UriUtil {
    private val TAG = UriUtil::class.java.simpleName
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logE("$TAG error: $throwable")
        throwable.printStackTrace()
    }
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + exceptionHandler)

    /**
     * 获取文件名
     */
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cursor.getString(nameIndex)
                } else {
                    "unknown_file."
                }
            } ?: "unknown_file."
        }.onFailure {
            logE("getFileNameFromUri error:$it")
        }.getOrElse {
            "unknown_file."
        }
    }
    /**
     * 获取文件大小
     */
    fun getFileSizeFromUri(context: Context, uri: Uri): String {
        val projection = arrayOf(android.provider.OpenableColumns.SIZE)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val sizeIndex = cursor.getColumnIndexOrThrow(android.provider.OpenableColumns.SIZE)
                return formatFileSize(cursor.getLong(sizeIndex))
            }
        }
        return "未知大小"
    }

    /**
     * 格式化文件大小
     */
    private fun formatFileSize(size: Long): String {
        return when {
            size >= 1024 * 1024 * 1024 -> {
                "%.2f GB".format(size / (1024.0 * 1024.0 * 1024.0))
            }

            size >= 1024 * 1024 -> {
                "%.2f MB".format(size / (1024.0 * 1024.0))
            }

            size >= 1024 -> {
                "%.2f KB".format(size / 1024.0)
            }

            else -> {
                "$size B"
            }
        }
    }

    suspend fun copyFileToAppDir(
        context: Context,
        fromUri: Uri?,
        toFile: File?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            if (fromUri == null || toFile == null) {
                logE("文件保存到app目录-->失败: fromUri or toFile is null")
                false
            }
            context.contentResolver.openInputStream(fromUri!!).use { ins ->
                if (ins == null) {
                    false
                }
                FileOutputStream(toFile).use { outs ->
                    ins.use { input ->
                        input!!.copyTo(outs)
                    }
                    logI("文件保存到app目录-->成功: ${toFile!!.absolutePath}")
                    true
                }
            } == true
        }
    }

    suspend fun copyFileToPublicDir(
        context: Context,
        fromFile: File?,
        toUri: Uri?,
    ): Uri? {
        return withContext(Dispatchers.IO) {
            if (fromFile == null || fromFile.exists().not() || toUri == null) {
                logE("文件保存到公共目录-->失败: fromFile or toUri is null")
                false
            }
            val fileUri = copyFileToSAFTreeFolder(
                context,
                fromFile = fromFile!!,
                targetDirUri = toUri!!,
            )
            if (fileUri == null) {
                logE("文件保存到公共目录-->失败: fromFile=${fromFile.absolutePath}")
                null
            } else {
                logI("文件保存到公共目录-->成功: fromFile=${fromFile.absolutePath}")
                fileUri
            }
        }
    }

    private suspend fun copyFileToSAFTreeFolder(
        context: Context,
        fromFile: File,
        targetDirUri: Uri,
    ): Uri? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val fileName: String = fromFile.absolutePath.substringAfterLast("/")
                if (fileName.isEmpty()) {
                    logE("copyFileToSAFTreeFolder 失败: fileName is null")
                    null
                }
                // 使用系统 API 构造真正可用的 document URI
                val documentId = DocumentsContract.getTreeDocumentId(targetDirUri)
                val realTargetUri =
                    DocumentsContract.buildDocumentUriUsingTree(targetDirUri, documentId)

                // 创建文件
                val fileUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    realTargetUri,
                    getMimeType(fileName),
                    fileName
                )
                if (fileUri == null) {
                    logE("copyFileStream失败: createDocument failed fileUri==null")
                    null
                }
                // 写入文件内容
                context.contentResolver.openOutputStream(fileUri!!)?.use { outs ->
                    fromFile.inputStream().use { input ->
                        input.copyTo(outs)
                    }
                }
                fileUri
            }.onFailure {
                logE("copyFileToSAFTreeFolder 错误: $it")
                it.printStackTrace()
            }.getOrElse { null }
        }
    }

    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast(".")) {
            "txt" -> "text/plain"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/png"
            "mp4" -> "video/mp4"
            else -> "*/*"
        }
    }
}
