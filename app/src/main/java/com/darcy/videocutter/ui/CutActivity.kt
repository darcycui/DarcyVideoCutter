package com.darcy.videocutter.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_log_toast.exts.logI
import com.darcy.lib_log_toast.exts.toasts
import com.darcy.lib_saf_select.utils.SAFUtil
import com.darcy.videocutter.R
import com.darcy.videocutter.databinding.ActivityCutBinding
import com.darcy.videocutter.ui.base.BaseBindingActivity
import com.darcy.videocutter.utils.TimeUtil
import com.darcy.videocutter.viewmodel.CutViewModel
import com.darcy.videocutter.viewmodel.state.VideoCutState
import kotlinx.coroutines.launch

class CutActivity : BaseBindingActivity<ActivityCutBinding>() {
//    private val binding: ActivityCutBinding by lazy {
//        ActivityCutBinding.inflate(layoutInflater)
//    }
    private val viewModel: CutViewModel by viewModels<CutViewModel>()
    private var isClockScreenOrientation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            val controller = WindowCompat.getInsetsController(window, v)
            controller.isAppearanceLightStatusBars = false // 关闭浅色模式，字体变白
            window.statusBarColor = resources.getColor(R.color.black, null) // 状态栏背景色
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
                            logD("开始切割...")
                            toasts("开始切割...")
                        }

                        is VideoCutState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            logI("切割成功:${state.outputUri}")
                            binding.tvInfo.text =
                                getString(R.string.file_path_cut, state.outputUri.path)
                            toasts("切割成功")
                        }

                        is VideoCutState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            logE("切割错误:${state.error}")
                            toasts("切割错误: ${state.error}")
                        }

                        is VideoCutState.SelectedVideo -> {
                            binding.btnSelectVideo2.visibility = View.GONE
                            binding.tvInfo.text = getString(R.string.file_path, state.videoUri.path)
                            binding.videoPlayerView.setMediaUri(state.videoUri)
                            binding.videoPlayerView.start()
                            resetTimeUI()
                        }

                        is VideoCutState.MarkStartTime -> {
                            binding.videoPlayerView.pause()
                            setStartTime(state.time)
                            setPeriodTextInfo()
                        }

                        is VideoCutState.MarkEndTime -> {
                            binding.videoPlayerView.pause()
                            setEndTime(state.time)
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

                        is VideoCutState.DynamicUI -> {
                            if (state.startTime < 0) {
                                resetStartTimeUI()
                            } else {
                                setStartTime(state.startTime)
                            }
                            if (state.endTime < 0) {
                                resetEndTimeUI()
                            } else {
                                setEndTime(state.endTime)
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun initView() {
        setupScreenOrientationLock()
        binding.tvInfo.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }
        binding.btnClockScreenOrientation.setOnClickListener {
            isClockScreenOrientation = !isClockScreenOrientation
            setupScreenOrientationLock()
        }
        binding.btnSelectVideo.setOnClickListener {
            proceedAfterPermissionGranted()
        }
        binding.btnSelectVideo2.setOnClickListener {
            proceedAfterPermissionGranted()
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

    private fun setupScreenOrientationLock() {
        if (isClockScreenOrientation) {
            binding.btnClockScreenOrientation.text = getString(R.string.lock_screen_orientation)
            // 设置为传感器方向
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)
        } else {
            binding.btnClockScreenOrientation.text = getString(R.string.unlock_screen_orientation)
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    private fun proceedAfterPermissionGranted() {
        // 原视频选择逻辑
//        viewModel.clearAppCacheFile()
        SAFUtil.selectVideo(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == RESULT_OK) {
            if (requestCode == SAFUtil.VIDEO_PICKER_REQUEST_CODE) {
                resultData?.data?.let { uri ->
                    logD("选择文件Uri-->${uri.path}")
                    viewModel.setupVideoUri(uri)
                }
            }

            if (requestCode == SAFUtil.SAF_TRR_DIR_PERMISSION_REQUEST_CODE) {
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

    private fun resetTimeUI() {
        resetStartTimeUI()
        resetEndTimeUI()
    }

    private fun setStartTime(time: Long) {
        binding.btnMarkStartTime.text =
            getString(R.string.start_00_00_00, TimeUtil.millisecondsToTime(time))
    }

    private fun setEndTime(time: Long) {
        binding.btnMarkEndTime.text =
            getString(R.string.end_00_00_00, TimeUtil.millisecondsToTime(time))
    }

    private fun resetStartTimeUI() {
        binding.btnMarkStartTime.text = getString(R.string.start_00_00_00_empty)
    }

    private fun resetEndTimeUI() {
        binding.btnMarkEndTime.text = getString(R.string.end_00_00_00_empty)
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
        binding.videoPlayerView.release()
        viewModel.clearAppCacheFile()
        super.onDestroy()
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
        viewModel.setupDynamicUI()
    }
}
