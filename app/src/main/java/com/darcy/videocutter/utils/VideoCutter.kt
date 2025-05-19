package com.darcy.videocutter.utils

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_log_toast.exts.logV
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * 视频切割工具类
 */
object VideoCutter {
    private val TAG = VideoCutter::class.java.simpleName
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logE("$TAG error: $throwable")
        throwable.printStackTrace()
    }
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + exceptionHandler)

    /**
     * 使用 FFmpeg 视频切割
     */
    suspend fun cutVideo(
        inputPath: String?,
        outputPath: String?,
        startMs: Long,
        endMs: Long
    ): String? {
        return withContext(Dispatchers.IO) {
            if (inputPath.isNullOrEmpty() || outputPath.isNullOrEmpty()) {
                logE("$TAG inputPath or outputPath is null or empty")
                null
            }
            logV("$TAG inputPath-->$inputPath")
            logV("$TAG outputPath-->$outputPath")
            val startTime = TimeUtil.millisecondsToTime(startMs)
            val duration = TimeUtil.millisecondsToTime(endMs - startMs)
            val command = arrayOf<String?>(
                "-ss", startTime,
                "-i", inputPath,
                "-t", duration,
                "-c", "copy",
                "-y", outputPath
            )
            logD("command-->${command.joinToString(separator = " ")}")
            val result = FFmpeg.execute(command)
            if (result == RETURN_CODE_SUCCESS) {
                logD("${Config.TAG} 切割成功.")
                outputPath
            } else if (result == RETURN_CODE_CANCEL) {
                logV("${Config.TAG} 切割取消.", Config.TAG)
                null
            } else {
                logE("${Config.TAG} 切割失败. result=$result")
                Config.printLastCommandOutput(Log.INFO)
                null
            }
        }
    }
}