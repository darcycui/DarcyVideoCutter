package com.darcy.videocutter.repository

import android.content.Context
import androidx.core.net.toUri
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_saf_select.utils.UriConvertUtil
import com.darcy.lib_saf_select.utils.UriUtil
import com.darcy.videocutter.app.App
import com.darcy.videocutter.interface_dapters.ICutVideoRepository
import com.darcy.videocutter.utils.VideoCutter
import java.io.File

class CutVideoRepository(private val context: Context = App.getInstance()) : ICutVideoRepository {
    //    override suspend fun cutVideo(
//        inputUriStr: String?,
//        startMs: Long,
//        endMs: Long
//    ): String {
//        if (inputUriStr == null) {
//            return ""
//        }
//        val fileNameFull = UriUtil.getFileNameFromUri(context, inputUriStr.toUri()) ?: ""
//        val fileName = fileNameFull.substringBeforeLast(".")
//        // darcyRefactor: 先复制到临时文件夹，再切割
//        val tempCacheFile = File(context.getExternalFilesDir("input_tmp"), fileNameFull)
//
//        val cutFileName = "${fileName}_cut.mp4"
//        val tempCutFile = File(context.getExternalFilesDir("output_tmp"), cutFileName)
//        return if (VideoCutter.cutVideo(
//                tempCacheFile.absolutePath, tempCutFile.absolutePath, startMs, endMs
//            )
//        ) {
//            tempCutFile.absolutePath
//        } else {
//            ""
//        }
//    }
    override suspend fun cutVideo(
        inputUriStr: String?,
        startMs: Long,
        endMs: Long
    ): String? {
        if (inputUriStr == null) {
            return null
        }
        val fileNameFull = UriUtil.getFileNameFromUri(context, inputUriStr.toUri()) ?: ""
        val fileName = fileNameFull.substringBeforeLast(".")
        // darcyRefactor: 直接切割原文件
        val originalFile = UriConvertUtil.convertUriToFilePath(context, inputUriStr.toUri())
        if (originalFile == null) {
            logE("cutVideo ERROR: originalFile is null")
            return null
        }

        val cutFileName = "${fileName}_cut.mp4"
        val tempCutFile = File(context.getExternalFilesDir("output_tmp"), cutFileName)
        return VideoCutter.cutVideo(originalFile, tempCutFile.absolutePath, startMs, endMs)
    }
}