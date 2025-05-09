package com.darcy.videocutter.utils

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.darcy.lib_access_skip.exts.logD
import com.darcy.lib_access_skip.exts.logE
import com.darcy.lib_access_skip.exts.logV
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.Long
import kotlin.arrayOf

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

    fun cutVideo(
        inputPath: String?,
        outputPath: String?,
        startMs: Long,
        endMs: Long,
        onSuccess: (inputPath: String, outputPath: String) -> Unit,
        onError: (inputPath: String, outputPath: String, throwable: Throwable?, message: String, code: Int) -> Unit
    ) {
        scope.launch {
            if (inputPath.isNullOrEmpty() || outputPath.isNullOrEmpty()) {
                logE("$TAG inputPath or outputPath is null or empty")
                return@launch
            }
            val startTime = millisecondsToTime(startMs)
            val duration = millisecondsToTime(endMs - startMs)
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
                logD("切割成功.", Config.TAG)
                onSuccess(inputPath, outputPath)
            } else if (result == RETURN_CODE_CANCEL) {
                logV("切割取消.", Config.TAG)
                onError(inputPath, outputPath, null, "cancelled", -1)
            } else {
                logE(
                    String.format(
                        Locale.CHINA,
                        "切割失败.",
                        result
                    ), Config.TAG
                )
                Config.printLastCommandOutput(Log.INFO)
                onError(inputPath, outputPath, null, "error:$result", result)
            }
        }
    }

    private fun millisecondsToTime(milliseconds: Long): String {
        // 转换毫秒为 HH:mm:ss.SSS 格式
        return String.format(
            locale = Locale.CHINA,
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliseconds),
            TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60,
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        )
    }
}