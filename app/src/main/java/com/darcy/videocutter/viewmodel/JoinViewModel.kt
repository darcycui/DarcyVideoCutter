package com.darcy.videocutter.viewmodel

import android.content.ClipData
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_log_toast.exts.logI
import com.darcy.videocutter.repository.FileRepository
import com.darcy.videocutter.repository.JoinVideoRepository
import com.darcy.videocutter.repository.VideoThumbnailRepository
import com.darcy.videocutter.usecase.DeleteVideoThumbnailFolderUseCase
import com.darcy.videocutter.usecase.GenerateVideoThumbnailUseCase
import com.darcy.videocutter.usecase.JoinVideoUseCase
import com.darcy.videocutter.usecase.SaveVideoThumbnailUseCase
import com.darcy.videocutter.viewmodel.state.VideoCutState
import com.darcy.videocutter.viewmodel.state.VideoJoinState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

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

    private val generateVideoThumbnailUseCase: GenerateVideoThumbnailUseCase by lazy {
        GenerateVideoThumbnailUseCase(VideoThumbnailRepository(bitmapSizes))
    }
    private val saveVideoThumbnailUseCase: SaveVideoThumbnailUseCase by lazy {
        SaveVideoThumbnailUseCase(VideoThumbnailRepository(bitmapSizes))
    }
    private val joinVideoUseCase: JoinVideoUseCase by lazy {
        JoinVideoUseCase(JoinVideoRepository())
    }
    private val deleteThumbnailFolderCase: DeleteVideoThumbnailFolderUseCase by lazy {
        DeleteVideoThumbnailFolderUseCase(FileRepository())
    }

    private val inputVideoUriStrings: MutableList<String> = mutableListOf()
    private val thumbnailImages: MutableList<String> = mutableListOf()
    private val bitmapSizes: MutableList<Pair<Int, Int>> = mutableListOf()
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
            bitmapSizes.clear()
            for (i in 0 until clipData.itemCount) {
                val uriStr = clipData.getItemAt(i).uri.toString()
                inputVideoUriStrings.add(uriStr)

                // 生成并保存缩略图
                saveVideoThumbnailUseCase.invoke(
                    uriStr, generateVideoThumbnailUseCase.invoke(uriStr)
                )?.also {
                    thumbnailImages.add(it.absolutePath)
                }
            }
            thumbnailImages.forEachIndexed { index, uri ->
                logD("选择文件$index UriStr-->${uri}")
            }
            _uiState.emit(VideoJoinState.SelectedVideo(inputVideoUriStrings, thumbnailImages))
        }
    }

    /**
     * 拼接视频
     */
    fun joinVideo() {
        ioScope.launch {
            if (inputVideoUriStrings.isEmpty() || thumbnailImages.isEmpty() || bitmapSizes.isEmpty()) {
                logE("拼接视频失败: inputVideoUriStrings or thumbnailImages or bitmapSizes is empty.")
                _uiState.emit(VideoJoinState.Error("拼接视频失败: 参数为空."))
            }
            if (inputVideoUriStrings.size == 1) {
                logE("拼接视频失败: inputVideoUriStrings.size <= 1.")
                _uiState.emit(VideoJoinState.Error("1个视频无需拼接"))
                return@launch
            }
            // 判断 bitmapSizes list中元素的宽高是否一致
            if (bitmapSizes.isNotEmpty()) {
                val firstSize = bitmapSizes[0]
                for (i in 1 until bitmapSizes.size) {
                    if (firstSize.first != bitmapSizes[i].first || firstSize.second != bitmapSizes[i].second) {
                        logE("拼接视频失败: bitmapSizes 不一致.")
                        _uiState.emit(VideoJoinState.Error("拼接视频失败: 视频尺寸不一致."))
                        return@launch
                    }
                }
            }
            logD("开始拼接视频")
            _uiState.emit(VideoJoinState.Loading)
            joinVideoUseCase.invoke(inputVideoUriStrings).let { it ->
                if (it.isEmpty()) {
                    logE("拼接视频失败: outputPath is null.")
                    _uiState.emit(VideoJoinState.Error("拼接视频失败"))
                    return@launch
                }
                outputPath = it
                _uiState.emit(VideoJoinState.Success(it.toUri()))
                logD("拼接视频成功: $it")
            }
        }
    }

    fun clearAppCacheFile() {
        ioScope.launch {
            try {
                // 清除input cache
                deleteThumbnailFolderCase.invoke().also {
                    if (!it) {
                        logE("清除缩略图文件夹-->失败: 删除文件夹失败")
                        _uiState.emit(VideoJoinState.Error("清除缩略图文件夹失败:删除文件夹失败"))
                        return@launch
                    }
                    logI("清除缩略图文件夹-->成功")
                }
            } catch (e: Exception) {
                logE("清除缩略图文件夹 output_tmp-->失败: $e")
                _uiState.emit(VideoJoinState.Error("清除缩略图文件夹 output_tmp失败:异常"))
                return@launch
            }
        }
    }


}