package com.darcy.videocutter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.darcy.lib_access_skip.exts.logD
import com.darcy.lib_access_skip.exts.logE
import com.darcy.lib_saf_select.utils.SAFUtil
import com.darcy.lib_saf_select.utils.SPUtil
import com.darcy.lib_saf_select.utils.UriUtil
import com.darcy.videocutter.databinding.ActivityMainBinding
import com.darcy.videocutter.utils.VideoCutter
import java.io.File

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
    }

    private fun initView() {
        binding.btnSelectOutputFolder.setOnClickListener {
            SAFUtil.requestPersistentDirAccess(this)
        }
        binding.btnSelectVideo.setOnClickListener {
            SAFUtil.selectDocument(this)
        }
        binding.btnTest.setOnClickListener {
            val fileName = "20250508_142350.mp4"
            val outFolderPath = getExternalFilesDir("output")
            val outputFile = File(outFolderPath, "${fileName}_cut.mp4")
            //复制到输出目录
            val publicOutFolderUri = SPUtil.getSavedTreeUri(this)
            UriUtil.copyFileToPublicDir(this, outputFile, fileName, publicOutFolderUri)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == RESULT_OK) {
            if (requestCode == SAFUtil.DOCUMENT_PICKER_REQUEST_CODE) {
                resultData?.data?.let { uri ->
                    binding.tvInfo.text = "文件: ${uri.path}"
                    logD("文件Uri-->${uri.path}")
                    binding.videoPlayerView.setMediaUri(uri)
                    binding.videoPlayerView.start()
                    testCut(uri, 10_000, 15_000)
                }
            }

            if (requestCode == SAFUtil.REQUEST_DIR_PERMISSION_CODE) {
                resultData?.data?.let { uri ->
                    // 持久化权限 (关键)
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    // 保存 URI 到 SharedPreferences
                    SPUtil.saveTreeUri(this, uri)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoPlayerView.isPlaying()) {
            binding.videoPlayerView.pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (binding.videoPlayerView.hasPaused()) {
            binding.videoPlayerView.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoPlayerView.release()
    }
}

private fun MainActivity.testCut(inputUri: Uri?, start: Long, end: Long) {
    if (inputUri == null || start <= 0L || end <= 0L || start >= end) {
        logD("参数不合法 inputUri: $inputUri, start: $start, end: $end")
        return
    }
//    复制到私有目录
    val inputFileNameFull = UriUtil.getFileNameFromUri(this, inputUri)?.apply {
        substring(0, lastIndexOf("."))
    } ?: ""
    val inputFileName = inputFileNameFull.substring(0, inputFileNameFull.lastIndexOf("."))
    val tempFile = File(getExternalFilesDir("input"), inputFileName)
    UriUtil.copyFileToAppDir(this, inputUri, tempFile, inputFileName)
//    无损切割
    val cacheFileName = "${inputFileName}_cut.mp4"
    val cacheFile = File(getExternalFilesDir("output"), cacheFileName)
    logD("输出路径: ${cacheFile.absolutePath}")
    VideoCutter.cutVideo(
        tempFile.absolutePath,
        cacheFile.absolutePath,
        start,
        end,
        onSuccess = { inputPath, outputPath ->
            //复制到输出目录
            val publicOutFolderUri = SPUtil.getSavedTreeUri(this)
            UriUtil.copyFileToPublicDir(this, cacheFile, cacheFileName, publicOutFolderUri)
        },
        onError = { inputPath, outputPath, throwable, message, code ->
            logE("onError: code=$code message=$message")
        })

}
