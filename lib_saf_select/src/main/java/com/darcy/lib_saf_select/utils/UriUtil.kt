package com.darcy.lib_saf_select.utils

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.darcy.lib_access_skip.exts.logE
import com.darcy.lib_access_skip.exts.logI
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    fun copyFileToAppDir(context: Context, fromUri: Uri?, toFile: File?, toFileName: String?) {
        scope.launch {
            if (toFileName.isNullOrEmpty() || fromUri == null || toFile == null) {
                logE("copyFileToAppDir: fileName or uri or folder is null")
                return@launch
            }
            context.contentResolver.openInputStream(fromUri).use { ins ->
                if (ins == null) {
                    return@launch
                }
                FileOutputStream(toFile).use {outs->
                    ins.use { input ->
                        input.copyTo(outs)
                    }
                    logI("文件保存成功: ${toFile.absolutePath}")
                }
            }
        }
    }

    fun copyFileToPublicDir(context: Context, fromFolder: File?, fromFileName: String?, toUri: Uri?, ) {
        scope.launch {
            if (toUri == null || fromFolder == null || fromFileName == null) {
                logE("copyFileToPublicDir: fileName or uri or folder is null")
                return@launch
            }
            val inputStream = fromFolder.inputStream()
            val success = withContext(Dispatchers.IO) {
                copyFileStream(
                    context,
                    ins = inputStream,
                    targetDirUri = toUri,
                    fileName = fromFileName
                )
            }
            if (success) {
                logI("复制成功 fileName=$fromFileName")
            } else {
                logE("复制失败 fileName=$fromFileName")
            }
        }
    }

    private fun copyFileStream(
        context: Context,
        ins: InputStream?,
        targetDirUri: Uri?,
        fileName: String?
    ): Boolean {
        return try {
            if (ins == null || targetDirUri == null || fileName == null) {
                logE("copyFileStream inputStream or targetDirUri or fileName is null")
                return false
            }
            // 使用系统 API 构造真正可用的 document URI
            val documentId = DocumentsContract.getTreeDocumentId(targetDirUri)
            val realTargetUri = DocumentsContract.buildDocumentUriUsingTree(targetDirUri, documentId)

            // 创建文件
            val fileUri = DocumentsContract.createDocument(
                context.contentResolver,
                realTargetUri,
                getMimeType(fileName),
                fileName
            ) ?: return false

            // 写入文件内容
            context.contentResolver.openOutputStream(fileUri)?.use { outs ->
                ins.use { input ->
                    input.copyTo(outs)
                }
                true
            } == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
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
