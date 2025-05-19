package com.darcy.videocutter.viewmodel

import android.content.ClipData
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_saf_select.utils.UriUtil
import com.darcy.videocutter.app.App
import com.darcy.videocutter.repository.FileRepository
import com.darcy.videocutter.repository.JoinVideoRepository
import com.darcy.videocutter.repository.SPRepository
import com.darcy.videocutter.usecase.GetSAFTreeUseCase
import com.darcy.videocutter.viewmodel.state.VideoJoinState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * 视频拼接
 */
class JoinViewModel : ViewModel() {
    companion object {
        private val TAG = JoinViewModel::class.java.simpleName
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logE("$TAG error: $throwable")
        throwable.printStackTrace()
        ioScope.launch {
            _uiState.emit(VideoJoinState.Error(throwable.message ?: "未知错误"))
        }
    }
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO + exceptionHandler)


    private val getSAFTreeUseCase: GetSAFTreeUseCase by lazy {
        GetSAFTreeUseCase(SPRepository())
    }

    private val inputVideoUriStrings: MutableList<String> = mutableListOf()
    private val thumbnailImages: MutableList<String> = mutableListOf()
    private var outputPath: String? = null
    private var publicOutUri: Uri? = null

    private val _uiState: MutableSharedFlow<VideoJoinState> = MutableSharedFlow(replay = 0)
    val uiState = _uiState

    /**
     * 设置视频Uri
     */
    fun setupVideoUriStrings(clipData: ClipData?) {
        ioScope.launch {
            if (clipData == null) {
                logE("选择视频为空: clipData is null.")
                _uiState.emit(VideoJoinState.Error("选择视频为空: clipData is null."))
                return@launch
            }
            inputVideoUriStrings.clear()
            thumbnailImages.clear()
            for (i in 0 until clipData.itemCount) {
                inputVideoUriStrings.add(clipData.getItemAt(i).uri.toString())
                saveVideoThumbnail(clipData.getItemAt(i).uri)?.also {
                    thumbnailImages.add(it.absolutePath)
                }
            }
            thumbnailImages.forEachIndexed { index, uri ->
                logD("选择文件$index UriStr-->${uri}")
            }
            _uiState.emit(VideoJoinState.SelectedVideo(thumbnailImages))
        }
    }

    /**
     * 保存缩略图
     */
    suspend fun saveVideoThumbnail(videoUri: Uri): File? =
        withContext(Dispatchers.IO) {
            val context = App.getInstance()
            // 1. 获取视频缩略图
            val bitmap = getVideoThumbnailByRetriever(context, videoUri) ?: return@withContext null

            // 2. 创建目标目录
            val storageDir = File(context.getExternalFilesDir(null), "video_thumbnail").apply {
                if (!exists()) mkdirs() // 确保目录存在
            }

            // 3. 生成唯一文件名（示例：使用 URI 哈希 + 时间戳）
            val fileName = "thumb_${UriUtil.getFileNameFromUri(context, videoUri)}_${videoUri.hashCode()}.jpg"
            val thumbnailFile = File(storageDir, fileName)
            if (thumbnailFile.exists()) {
                return@withContext thumbnailFile
            }
            // 4. 保存到文件
            return@withContext try {
                thumbnailFile.apply {
                    FileOutputStream(this).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos) // JPEG 质量 85%
                        fos.flush()
                    }
                    logE("保存缩略图成功: $absolutePath")
                }
            } catch (e: Exception) {
                logE("保存缩略图失败 $e")
                null
            }
        }

    /**
     * 生成缩略图
     */
    private suspend fun getVideoThumbnailByRetriever(
        context: Context,
        uri: Uri
    ): Bitmap? = withContext(Dispatchers.IO) {
        var retriever: MediaMetadataRetriever? = null
        try {
            retriever = MediaMetadataRetriever().apply {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    setDataSource(pfd.fileDescriptor)
                }
            }
            retriever.getFrameAtTime(
                1000 * 1000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            ) // 第一秒的帧
        } catch (e: Exception) {
            logE("生成缩略图失败 $e")
            null
        } finally {
            retriever?.release()
        }
    }

    fun joinVideo() {
        ioScope.launch {
            logD("开始拼接视频")
            _uiState.emit(VideoJoinState.Loading)
            JoinVideoRepository().joinVideo(inputVideoUriStrings).let { it ->
                if (it.isNullOrEmpty()) {
                    logE("拼接视频失败: outputPath is null.")
                    _uiState.emit(VideoJoinState.Error("拼接视频失败"))
                    return@launch
                }
                outputPath = it
                logD("拼接视频成功: $it")
            }
            FileRepository().copyToPublicOutput(outputPath, getSAFTreeUseCase.invoke()).let {
                if (it == null || it.path.isNullOrEmpty()) {
                    logE("复制到out失败: publicOutUri is null.")
                    _uiState.emit(VideoJoinState.Error("复制到out失败"))
                    return@launch
                }
                publicOutUri = it
            }
            publicOutUri?.let {
                _uiState.emit(VideoJoinState.Success(it))
            } ?: run {
                logE("onError: 输出到公共目录失败 publicOutUri is null")
                _uiState.emit(VideoJoinState.Error("输出到公共目录失败"))
            }
        }
    }

}