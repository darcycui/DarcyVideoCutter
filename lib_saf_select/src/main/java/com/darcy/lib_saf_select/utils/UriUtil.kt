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
import java.io.InputStream


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
    ): Boolean {
        return withContext(Dispatchers.IO) {
            if (fromFile == null || fromFile.exists().not() || toUri == null) {
                logE("文件保存到公共目录-->失败: fromFile or toUri is null")
                false
            }
            val inputStream = fromFile!!.inputStream()
            val fromFileName: String = fromFile.absolutePath.substringAfterLast("/")
            val success = copyFileStream(
                context,
                ins = inputStream,
                targetDirUri = toUri,
                fileName = fromFileName
            )
            if (success) {
                logI("文件保存到公共目录-->成功: fileName=$fromFileName")
                true
            } else {
                logE("文件保存到公共目录-->失败: fileName=$fromFileName")
                false
            }
        }
    }

    private suspend fun copyFileStream(
        context: Context,
        ins: InputStream?,
        targetDirUri: Uri?,
        fileName: String?
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (ins == null || targetDirUri == null || fileName == null) {
                    logE("copyFileStream失败: inputStream or targetDirUri or fileName is null")
                    false
                }
                // 使用系统 API 构造真正可用的 document URI
                val documentId = DocumentsContract.getTreeDocumentId(targetDirUri)
                val realTargetUri =
                    DocumentsContract.buildDocumentUriUsingTree(targetDirUri, documentId)

                // 创建文件
                val fileUri = DocumentsContract.createDocument(
                    context.contentResolver,
                    realTargetUri,
                    getMimeType(fileName!!),
                    fileName
                )
                if (fileUri == null) {
                    logE("copyFileStream失败: createDocument failed fileUri==null")
                    false
                }
                // 写入文件内容
                context.contentResolver.openOutputStream(fileUri!!)?.use { outs ->
                    ins.use { input ->
                        input!!.copyTo(outs)
                    }
                    true
                } == true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
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
