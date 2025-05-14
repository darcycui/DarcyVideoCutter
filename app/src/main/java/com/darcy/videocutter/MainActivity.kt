package com.darcy.videocutter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.toasts
import com.darcy.lib_saf_select.utils.SAFUtil
import com.darcy.lib_saf_select.utils.SPUtil
import com.darcy.videocutter.databinding.ActivityMainBinding
import com.darcy.videocutter.utils.TimeUtil
import com.darcy.videocutter.viewmodel.MainActivityViewModel
import com.darcy.videocutter.viewmodel.state.VideoCutState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val viewModel: MainActivityViewModel by viewModels<MainActivityViewModel>()
    private var startTime = -1L
    private var endTime = -1L
    private var videoUri: Uri? = null

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
        initFlowCollect()
    }

    private fun initFlowCollect() {
        // 观察状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is VideoCutState.Idle -> {
                            // 初始状态
                        }

                        is VideoCutState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            toasts("开始切割")
                        }

                        is VideoCutState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            toasts("切割成功")
//                            binding.videoPlayerView.setMediaUri(state.outputUri)
                        }

                        is VideoCutState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            toasts("切割错误: ${state.message}")
                        }

                        is VideoCutState.Toasts -> {
                            toasts(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.btnSelectOutputFolder.setOnClickListener {
            SAFUtil.requestPersistentDirAccess(this)
        }
        binding.btnSelectVideo.setOnClickListener {
//            viewModel.clearAppCacheFile(this)
            SAFUtil.selectDocument(this)
        }
        binding.btnMarkStartTime.setOnClickListener {
            startTime = binding.videoPlayerView.getCurrentPosition()
            if (startTime < 0) {
                startTime = 0
            }
            binding.videoPlayerView.pause()
            binding.btnMarkStartTime.text =
                getString(R.string.start_00_00_00, TimeUtil.millisecondsToTime(startTime))
        }
        binding.btnMarkEndTime.setOnClickListener {
            endTime = binding.videoPlayerView.getCurrentPosition()
            if (endTime < 0) {
                endTime = binding.videoPlayerView.getDuration()
            }
            binding.videoPlayerView.pause()
            if (endTime < startTime) {
                endTime = startTime
            }
            binding.btnMarkEndTime.text =
                getString(R.string.end_00_00_00, TimeUtil.millisecondsToTime(endTime))
        }
        binding.btnCut.setOnClickListener {
            if (endTime <= startTime) {
                toasts("结束时间不能小于开始时间，请重新选择")
            }
            videoUri?.let {
                viewModel.cutVideo(this, it, startTime, endTime)
            } ?: run {
                toasts("请先选择视频文件")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == RESULT_OK) {
            if (requestCode == SAFUtil.DOCUMENT_PICKER_REQUEST_CODE) {
                resultData?.data?.let { uri ->
                    binding.tvInfo.text = "文件: ${uri.path}"
                    logD("选择文件Uri-->${uri.path}")
                    binding.videoPlayerView.setMediaUri(uri)
                    binding.videoPlayerView.start()
//                    viewModel.cutVideo(this, uri, 1_000, 5_000)
                    resetUI()
                    videoUri = uri
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
                    viewModel.saveSAFTreeUri(uri)
                }
            }
        }
    }

    private fun resetUI() {
        binding.btnMarkStartTime.text = getString(R.string.start_00_00_00)
        binding.btnMarkEndTime.text = getString(R.string.end_00_00_00)
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
