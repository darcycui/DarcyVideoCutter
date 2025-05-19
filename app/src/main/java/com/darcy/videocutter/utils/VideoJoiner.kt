package com.darcy.videocutter.utils

import android.util.Log
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL
import com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS
import com.arthenica.mobileffmpeg.FFmpeg
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_log_toast.exts.logV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object VideoJoiner {

    /**
     * 无损合并多个视频
     * @param inputPaths 输入视频路径列表
     * @param outputPath 合并后的输出路径
     */
    suspend fun mergeVideosLossless(
        inputPaths: List<String>,
        outputPath: String,
    ): String? {
        return withContext(Dispatchers.IO) {
            // 生成文件列表
            val fileList = createFileList(inputPaths)
            if (fileList == null) {
                null
            }

            // 构建 FFmpeg 命令
            val command = arrayOf(
                "-y",
                "-f", "concat",
                "-safe", "0",
                "-i", fileList!!.absolutePath,
                "-c", "copy",
                outputPath
            )

            // 拼接
            val result = FFmpeg.execute(command)
            fileList.delete() // 清理临时文件

            if (result == RETURN_CODE_SUCCESS) {
                logD("视频拼接成功: $outputPath")
                outputPath
            } else if (result == RETURN_CODE_CANCEL) {
                logV("${Config.TAG} 视频拼接取消.", Config.TAG)
                null
            } else {
                logE("${Config.TAG} 视频拼接失败. result=$result")
                Config.printLastCommandOutput(Log.INFO)
                null
            }
        }
    }

    /**
     * 创建临时文件列表
     */
    private fun createFileList(inputPaths: List<String>): File? {
        return try {
            val fileList = File.createTempFile("filelist_", ".txt")
            fileList.writer().use { writer ->
                inputPaths.forEach { path ->
                    writer.write("file '${File(path).absolutePath}'\n")
                }
            }
            fileList
        } catch (e: Exception) {
            logE("创建文件列表失败 $e")
            null
        }
    }
}