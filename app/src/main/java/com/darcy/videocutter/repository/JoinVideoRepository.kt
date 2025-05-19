package com.darcy.videocutter.repository

import android.content.Context
import android.os.Environment
import androidx.core.net.toUri
import com.darcy.lib_saf_select.utils.UriConvertUtil
import com.darcy.videocutter.app.App
import com.darcy.videocutter.interface_dapters.IJoinVideoRepository
import com.darcy.videocutter.utils.TimeUtil
import com.darcy.videocutter.utils.VideoJoiner
import java.io.File


class JoinVideoRepository(private val context: Context = App.getInstance()) : IJoinVideoRepository {
    override suspend fun joinVideo(inputUriStrings: List<String>): String? {
        val videos = mutableListOf<String>()
        inputUriStrings.forEach { item ->
            val originalFile = UriConvertUtil.convertUriToFilePath(context, item.toUri())
            originalFile?.let {
                videos.add(it)
            }
        }
//        val outputPath = "/sdcard/Movies/merged_video.mp4"
        val moviesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "merged_video_"
        val fileExt = ".mp4"
        var dest = File(moviesDir, "${filePrefix}${TimeUtil.getCurrentTime()}$fileExt")
        val outputPath = dest.absolutePath

        return VideoJoiner.mergeVideosLossless(
            inputPaths = videos,
            outputPath = outputPath,
        )
    }
}