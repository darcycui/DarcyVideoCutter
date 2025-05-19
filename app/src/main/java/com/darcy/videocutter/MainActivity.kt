package com.darcy.videocutter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.toasts
import com.darcy.lib_saf_select.utils.SAFUtil
import com.darcy.videocutter.databinding.ActivityMainBinding
import com.darcy.videocutter.dialog.PermissionDialog
import com.darcy.videocutter.settings.SettingsUtil
import com.darcy.videocutter.utils.TimeUtil
import com.darcy.videocutter.viewmodel.CutViewModel
import com.darcy.videocutter.viewmodel.state.VideoCutState
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val mediaPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            toasts("已获取权限")
            proceedAfterPermissionGranted()
        } else {
            toasts("未获取权限")
            PermissionDialog.showPermissionDeniedDialog(this, clickYes = {
                SettingsUtil.openAppSettings(this)
            })
        }
    }

    // 在 MainActivity 内添加以下方法
    private fun checkMediaPermission() {
        val requiredPermission = when {
            // Android 13+ 需要细粒度权限
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
                Manifest.permission.READ_MEDIA_VIDEO
            // Android 10-12 需要旧版存储权限
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                Manifest.permission.READ_EXTERNAL_STORAGE
            // Android 9 及以下自动授权
            else -> null
        }

        requiredPermission?.let { perm ->
            if (ContextCompat.checkSelfPermission(this, perm)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // 显示权限说明弹窗（首次拒绝后）
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                    PermissionDialog.showPermissionRationaleDialog(this, perm, clickYes = {
                        mediaPermissionLauncher.launch(perm)
                    })
                } else {
                    mediaPermissionLauncher.launch(perm)
                }
            } else {
                proceedAfterPermissionGranted()
            }
        } ?: run {
            proceedAfterPermissionGranted()
        }
    }

    private fun proceedAfterPermissionGranted() {
        // 原视频选择逻辑
        viewModel.clearAppCacheFile()
        SAFUtil.selectVideo(this)
    }

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val viewModel: CutViewModel by viewModels<CutViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
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

                        is VideoCutState.SelectedVideo -> {
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
    }

    private fun initView() {
        binding.tvInfo.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }
        binding.btnSelectOutputFolder.setOnClickListener {
            SAFUtil.requestPersistentDirAccess(this)
        }
        binding.btnSelectVideo.setOnClickListener {
            checkMediaPermission()
//            viewModel.clearAppCacheFile()
//            SAFUtil.selectDocument(this)
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
        val startTimeText = binding.btnMarkStartTime.text.toString().trim().substringAfterLast(" ")
        binding.btnMarkStartTime.text = getString(R.string.start_00_00_00, "$startTimeText ")
        val endTimeText = binding.btnMarkEndTime.text.toString().trim().substringAfterLast(" ")
        binding.btnMarkEndTime.text = getString(R.string.end_00_00_00, "$endTimeText ")
    }
}
