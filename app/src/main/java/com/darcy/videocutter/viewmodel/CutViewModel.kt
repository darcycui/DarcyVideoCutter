package com.darcy.videocutter.viewmodel

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_log_toast.exts.logI
import com.darcy.videocutter.repository.CutVideoRepository
import com.darcy.videocutter.repository.FileRepository
import com.darcy.videocutter.repository.SPRepository
import com.darcy.videocutter.usecase.CopyToInputTempUseCase
import com.darcy.videocutter.usecase.CopyToPublicOutUseCase
import com.darcy.videocutter.usecase.CutVideoUseCase
import com.darcy.videocutter.usecase.DeleteFileUseCase
import com.darcy.videocutter.usecase.DeleteInputCacheFolderUseCase
import com.darcy.videocutter.usecase.DeleteOutputCacheFolderUseCase
import com.darcy.videocutter.usecase.GetSAFTreeUseCase
import com.darcy.videocutter.usecase.SaveSAFTreeUseCase
import com.darcy.videocutter.utils.TimeUtil
import com.darcy.videocutter.viewmodel.state.VideoCutState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/**
 * 视频切割
 */
class CutViewModel : ViewModel() {
    companion object {
        private val TAG = CutViewModel::class.java.simpleName
    }

    private val copyToInputTempUseCase: CopyToInputTempUseCase by lazy {
        CopyToInputTempUseCase(FileRepository())
    }
    private val copyToPublicOutUseCase: CopyToPublicOutUseCase by lazy {
        CopyToPublicOutUseCase(FileRepository())
    }
    private val deleteFileUseCase: DeleteFileUseCase by lazy {
        DeleteFileUseCase(FileRepository())
    }
    private val deleteInputCacheFolderCase: DeleteInputCacheFolderUseCase by lazy {
        DeleteInputCacheFolderUseCase(FileRepository())
    }
    private val deleteOutputCacheFolderCase: DeleteOutputCacheFolderUseCase by lazy {
        DeleteOutputCacheFolderUseCase(FileRepository())
    }
    private val cutVideoUseCase: CutVideoUseCase by lazy {
        CutVideoUseCase(CutVideoRepository())
    }
    private val getSAFTreeUseCase: GetSAFTreeUseCase by lazy {
        GetSAFTreeUseCase(SPRepository())
    }
    private val saveSAFTreeUseCase: SaveSAFTreeUseCase by lazy {
        SaveSAFTreeUseCase(SPRepository())
    }
    private var tempCacheFilePath = ""
    private var tempCutFilePath = ""
    private var publicOutUri: Uri? = null
    private var inputUri: Uri? = null
    private var startTime = -1L
    private var endTime = -1L

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logE("$TAG error: $throwable")
        throwable.printStackTrace()
        ioScope.launch {
            _uiState.emit(VideoCutState.Error(throwable.message ?: "未知错误"))
        }
    }
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO + exceptionHandler)
    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main + exceptionHandler)
    private val _uiState: MutableSharedFlow<VideoCutState> =
        MutableSharedFlow<VideoCutState>(replay = 0)
    val uiState: SharedFlow<VideoCutState> = _uiState

    fun saveSAFTreeUri(treeUri: Uri) {
        saveSAFTreeUseCase.invoke(treeUri.toString())
    }

    /**
     * 切割视频
     */
    fun cutVideo() {
        ioScope.launch {
            if (inputUri == null || startTime <= 0L || endTime <= 0L || startTime >= endTime) {
                logD("参数不合法 inputUri: $inputUri, startTime: $startTime, endTime: $endTime")
                _uiState.emit(VideoCutState.Error("参数不合法"))
                return@launch
            }
            // 开始切割
            _uiState.emit(VideoCutState.Loading)

            // 复制到私有目录
//            copyToInputTempUseCase.invoke(inputUri.toString()).also {
//                if (it.isEmpty()) {
//                    logE("onError: 复制到输入缓存文件夹失败")
//                    _uiState.emit(VideoCutState.Error("复制到输入缓存文件夹失败"))
//                    return@launch
//                }
//                tempCacheFilePath = it
//            }

            // 无损切割
            cutVideoUseCase.invoke(inputUri.toString(), startTime, endTime).also {
                if (it.isEmpty()) {
                    logE("切割失败")
                    _uiState.emit(VideoCutState.Error("切割失败"))
                    return@launch
                }
                tempCutFilePath = it
                _uiState.emit(VideoCutState.Success(it.toUri()))
            }

            //复制到输出目录
//            copyToPublicOutUseCase.invoke(tempCutFilePath, getSAFTreeUseCase.invoke()).also {
//                if (it == null) {
//                    logE("切割失败: 复制到out失败")
//                    _uiState.emit(VideoCutState.Error("切割失败: 复制到out失败"))
//                    return@launch
//                }
//                publicOutUri = it
//            }

            // 删除临时文件
//            deleteFileUseCase.invoke(tempCacheFilePath).also {
//                if (!it) {
//                    logE("删除失败: 删除输入缓存文件夹文件失败")
//                    _uiState.emit(VideoCutState.Error("删除输入缓存文件夹文件失败"))
//                    return@launch
//                }
//            }
//            deleteFileUseCase.invoke(tempCutFilePath).also {
//                if (!it) {
//                    logE("删除失败: 删除输出缓存文件夹文件失败")
//                    _uiState.emit(VideoCutState.Error("删除输出缓存文件夹文件失败"))
//                    return@launch
//                }
//            }
//            publicOutUri?.let {
//                _uiState.emit(VideoCutState.Success(it))
//            } ?: run {
//                logE("复制失败: 输出到公共目录失败 publicOutUri is null")
//                _uiState.emit(VideoCutState.Error("输出到公共目录失败"))
//            }
        }
    }

    fun clearAppCacheFile() {
        ioScope.launch {
            try {
                // 清除input cache
                deleteInputCacheFolderCase.invoke().also {
                    if (!it) {
                        logE("清除输入缓存文件夹-->失败: 删除文件夹失败")
                        _uiState.emit(VideoCutState.Error("清除输入缓存文件夹失败:删除文件夹失败"))
                        return@launch
                    }
                    logI("清除输入缓存文件夹-->成功")
                }
                deleteOutputCacheFolderCase.invoke().also {
                    if (!it) {
                        logD("清除输出缓存文件夹-->失败:删除文件夹失败")
                        _uiState.emit(VideoCutState.Error("清除失败:删除文件夹失败"))
                        return@launch
                    }
                    logI("清除输出缓存文件夹-->成功")
                }
            } catch (e: Exception) {
                logE("清除输入缓存文件夹 输出缓存文件夹-->失败: $e")
                _uiState.emit(VideoCutState.Error("清除输入缓存文件夹 输出缓存文件夹失败:异常"))
                return@launch
            }
        }
    }

    fun setupVideoUri(uri: Uri) {
        ioScope.launch {
            inputUri = uri
            _uiState.emit(VideoCutState.SelectedVideo(uri))
        }
    }

    fun setupStartTime(time: Long) {
        ioScope.launch {
            startTime = time
            if (startTime < 0) {
                startTime = 0
            }
            _uiState.emit(VideoCutState.MarkStartTime(startTime))
        }
    }

    fun setupEndTime(time: Long) {
        ioScope.launch {
            endTime = time
            _uiState.emit(VideoCutState.MarkEndTime(endTime))
        }
    }

    fun setupPeriod() {
        ioScope.launch {
            if (startTime < 0 || endTime < 0 || startTime >= endTime) {
                _uiState.emit(VideoCutState.Period(""))
                return@launch
            }
            val periodText = TimeUtil.millisecondsToTime(endTime - startTime)
            logI("切割视频时长: $periodText")
            _uiState.emit(VideoCutState.Period(periodText))
        }
    }

    fun setupDynamicUI() {
        ioScope.launch {
            _uiState.emit(VideoCutState.DynamicUI(startTime,  endTime))
            logI("当前startTime: $startTime, 当前endTime: $endTime")
        }
    }
}