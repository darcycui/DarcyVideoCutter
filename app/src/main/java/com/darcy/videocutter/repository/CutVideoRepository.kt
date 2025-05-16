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
    ): String {
        if (inputUriStr == null) {
            return ""
        }
        val fileNameFull = UriUtil.getFileNameFromUri(context, inputUriStr.toUri()) ?: ""
        val fileName = fileNameFull.substringBeforeLast(".")
//        val tempCacheFile = File(context.getExternalFilesDir("input_tmp"), fileNameFull)
        val tempCacheFile = UriConvertUtil.convertUriToFilePath(context, inputUriStr.toUri())
        if (tempCacheFile == null) {
            logE("cutVideo ERROR: tempCacheFile is null")
            return ""
        }

        val cutFileName = "${fileName}_cut.mp4"
        val tempCutFile = File(context.getExternalFilesDir("output_tmp"), cutFileName)
        return if (VideoCutter.cutVideo(
                tempCacheFile, tempCutFile.absolutePath, startMs, endMs
            )
        ) {
            tempCutFile.absolutePath
        } else {
            ""
        }
    }
}