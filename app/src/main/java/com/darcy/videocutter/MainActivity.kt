package com.darcy.videocutter

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.toasts
import com.darcy.lib_saf_select.utils.SAFUtil
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val controller = WindowCompat.getInsetsController(window,v)
            controller.isAppearanceLightStatusBars = false // 关闭浅色模式，字体变白
            window.statusBarColor = resources.getColor(R.color.black, null)
            insets
        }
        initView()
        initFlowCollect()
    }

    private fun initFlowCollect() {
        // 观察状态
        lifecycleScope.launch {
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
                        toasts("切割成功:${state.outputUri}")
                    }

                    is VideoCutState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        toasts("切割错误: ${state.error}")
                    }

                    is VideoCutState.Toasts -> {
                        toasts(state.message)
                    }

                    is VideoCutState.SelectVideo -> {
                        binding.tvInfo.text = getString(R.string.file_path, state.videoUri.path)
                        binding.videoPlayerView.setMediaUri(state.videoUri)
                        binding.videoPlayerView.start()
                        resetUI()
                    }

                    is VideoCutState.MarkStartTime -> {
                        binding.videoPlayerView.pause()
                        binding.btnMarkStartTime.text =
                            getString(
                                R.string.start_00_00_00,
                                TimeUtil.millisecondsToTime(state.time)
                            )
                        setPeriodTextInfo()
                    }

                    is VideoCutState.MarkEndTime -> {
                        binding.videoPlayerView.pause()
                        binding.btnMarkEndTime.text =
                            getString(
                                R.string.end_00_00_00,
                                TimeUtil.millisecondsToTime(state.time)
                            )
                        setPeriodTextInfo()
                    }

                    is VideoCutState.Period -> {
                        val text = state.text
                        if (text.isEmpty()) {
                            binding.tvCutPeriod.text = getString(R.string.cut_period_default)
                        } else {
                            binding.tvCutPeriod.text = getString(R.string.cut_period, text)
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
//            viewModel.clearAppCacheFile()
            SAFUtil.selectDocument(this)
        }
        binding.btnMarkStartTime.setOnClickListener {
            viewModel.setupStartTime(binding.videoPlayerView.getCurrentPosition())
        }
        binding.btnMarkEndTime.setOnClickListener {
            viewModel.setupEndTime(binding.videoPlayerView.getCurrentPosition())
        }
        binding.btnCut.setOnClickListener {
            viewModel.cutVideo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == RESULT_OK) {
            if (requestCode == SAFUtil.DOCUMENT_PICKER_REQUEST_CODE) {
                resultData?.data?.let { uri ->
                    logD("选择文件Uri-->${uri.path}")
                    viewModel.setupVideoUri(uri)
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

    private fun setPeriodTextInfo() {
        viewModel.setupPeriod()
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setDynamicUI()
            binding.tvInfo.visibility = View.VISIBLE
            binding.spaceTop.visibility = View.VISIBLE
            binding.spaceBottom.visibility = View.VISIBLE
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setDynamicUI()
            binding.tvInfo.visibility = View.GONE
            binding.spaceTop.visibility = View.GONE
            binding.spaceBottom.visibility = View.GONE
        }
    }

    private fun setDynamicUI() {
        val startTimeText = binding.btnMarkStartTime.text.toString().trim().substringAfterLast(" ")
        binding.btnMarkStartTime.text = getString(R.string.start_00_00_00, "$startTimeText ")
        val endTimeText = binding.btnMarkEndTime.text.toString().trim().substringAfterLast(" ")
        binding.btnMarkEndTime.text = getString(R.string.end_00_00_00, "$endTimeText ")
    }
}
