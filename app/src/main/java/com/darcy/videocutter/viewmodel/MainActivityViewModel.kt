package com.darcy.videocutter.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_saf_select.utils.UriUtil
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
import com.darcy.videocutter.viewmodel.state.VideoCutState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {
    companion object {
        private val TAG = MainActivityViewModel::class.java.simpleName
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


    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logE("$TAG error: $throwable")
        throwable.printStackTrace()
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
    fun cutVideo(context: Context, inputUri: Uri?, start: Long, end: Long) {
        ioScope.launch {
            if (inputUri == null || start <= 0L || end <= 0L || start >= end) {
                logD("参数不合法 inputUri: $inputUri, start: $start, end: $end")
                _uiState.emit(VideoCutState.Error("参数不合法"))
                return@launch
            }
            // 开始切割
            _uiState.emit(VideoCutState.Loading)

            // 复制到私有目录
            copyToInputTempUseCase.invoke(inputUri.toString()).also {
                if (it.isEmpty()) {
                    logE("onError: 复制到input_tmp失败")
                    _uiState.emit(VideoCutState.Error("复制到input_tmp失败"))
                    return@launch
                }
                tempCacheFilePath = it
            }

            // 无损切割
            cutVideoUseCase.invoke(inputUri.toString(), start, end).also {
                if (it.isEmpty()) {
                    logE("onError: 切割失败")
                    _uiState.emit(VideoCutState.Error("切割失败"))
                    return@launch
                }
                tempCutFilePath = it
            }

            //复制到输出目录
            copyToPublicOutUseCase.invoke(tempCutFilePath, getSAFTreeUseCase.invoke()).also {
                if (!it) {
                    logE("onError: 复制到out失败")
                    _uiState.emit(VideoCutState.Error("复制到out失败"))
                    return@launch
                }
            }

            // 删除临时文件
            deleteFileUseCase.invoke(tempCacheFilePath).also {
                if (!it) {
                    logE("onError: 删除output_tmp文件失败")
                    _uiState.emit(VideoCutState.Error("删除output_tmp文件失败"))
                    return@launch
                }
            }
            deleteFileUseCase.invoke(tempCutFilePath).also {
                if (!it) {
                    logE("onError: 删除output_tmp文件失败")
                    _uiState.emit(VideoCutState.Error("删除output_tmp文件失败"))
                    return@launch
                }
            }
            _uiState.emit(VideoCutState.Success(null))
        }
    }

    fun clearAppCacheFile(context: Context) {
        ioScope.launch {
            try {
                // 清除input cache
                deleteInputCacheFolderCase.invoke().also {
                    if (!it) {
                        logE("清除input_tmp-->失败: 删除文件夹失败")
                        _uiState.emit(VideoCutState.Error("清除input_tmp失败:删除文件夹失败"))
                        return@launch
                    }
                    logD("清除input_tmp-->成功")
                    _uiState.emit(VideoCutState.Toasts("清除input_tmp成功"))
                }
                deleteOutputCacheFolderCase.invoke().also {
                    if (!it) {
                        logD("清除output_tmp-->失败:删除文件夹失败")
                        _uiState.emit(VideoCutState.Error("清除output_tmp失败:删除文件夹失败"))
                        return@launch
                    }
                }
            } catch (e: Exception) {
                logE("清除input_tmp output_tmp-->失败: $e")
                _uiState.emit(VideoCutState.Error("清除input_tmp output_tmp失败:异常"))
                return@launch
            }
        }
    }
}