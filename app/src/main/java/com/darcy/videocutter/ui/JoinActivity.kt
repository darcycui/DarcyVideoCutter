package com.darcy.videocutter.ui

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_log_toast.exts.logI
import com.darcy.lib_log_toast.exts.logV
import com.darcy.lib_log_toast.exts.toasts
import com.darcy.lib_saf_select.utils.SAFUtil
import com.darcy.videocutter.R
import com.darcy.videocutter.adapter.ViewPager2Adapter
import com.darcy.videocutter.databinding.ActivityJoinBinding
import com.darcy.videocutter.fragment.VideoThumbnailFragment
import com.darcy.videocutter.ui.base.BaseBindingActivity
import com.darcy.videocutter.viewmodel.JoinViewModel
import com.darcy.videocutter.viewmodel.state.VideoJoinState
import kotlinx.coroutines.launch

class JoinActivity : BaseBindingActivity<ActivityJoinBinding>() {
    //    private val binding: ActivityJoinBinding by lazy {
//        ActivityJoinBinding.inflate(layoutInflater)
//    }
    private val viewModel: JoinViewModel by viewModels()
    private val fragments: MutableList<Fragment> = mutableListOf()
    private val viewpager2Adapter: ViewPager2Adapter by lazy {
        ViewPager2Adapter(supportFragmentManager, lifecycle, fragments)
    }

    fun addItems(videoItems: List<String>, thumbnailItems: List<String>) {
        if (videoItems.isEmpty() || thumbnailItems.isEmpty()) {
            logE("视频或缩略图为空")
            toasts("视频或缩略图为空")
            return
        }
        if (videoItems.size != thumbnailItems.size) {
            logE("视频和缩略图数量不一致")
            toasts("视频和缩略图数量不一致")
            return
        }
        fragments.clear()
        for (i in 0 until videoItems.size) {
            logV("addItem: ${videoItems[i]}")
            fragments.add(VideoThumbnailFragment.newInstance(videoItems[i], thumbnailItems[i]))
        }
        viewpager2Adapter.notifyDataSetChanged()
    }

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
            window.navigationBarColor = resources.getColor(R.color.black, null) // 导航栏背景色
            insets
        }
        initView()
        initFlowCollect()
    }

    private fun initFlowCollect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is VideoJoinState.Error -> {
                            logE("拼接错误: ${state.error}")
                            toasts("拼接错误:${state.error}")
                            binding.progressBar.visibility = View.GONE
                        }

                        is VideoJoinState.Idle -> {
                            logD("Idle")
                        }

                        is VideoJoinState.Loading -> {
                            logD("开始拼接...")
                            toasts("开始拼接...")
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is VideoJoinState.Success -> {
                            logI("拼接成功: ${state.outputUri}")
                            toasts("拼接成功：${state.outputUri}")
                            binding.progressBar.visibility = View.GONE
                            binding.tvInfo.text =
                                getString(R.string.file_path_joined, state.outputUri.path)
                        }

                        is VideoJoinState.SelectedVideo -> {
                            logD("选择视频: ${state.thumbnailImages}")
                            addItems(state.videoUriStrings, state.thumbnailImages)
                            binding.btnSelectVideo2.visibility = View.GONE
                        }

                        is VideoJoinState.DynamicUI -> {
                            logD("动态UI: 横屏：${state.isLandScreen}")
                            setupScreenOrientationLock(state.isLandScreen)
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        setupScreenOrientationLock(viewModel.getIsLandScreen())
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.tvInfo.visibility = View.GONE
            binding.spaceTop.visibility = View.VISIBLE
            binding.spaceBottom.visibility = View.VISIBLE
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.tvInfo.visibility = View.GONE
            binding.spaceTop.visibility = View.VISIBLE
            binding.spaceBottom.visibility = View.GONE
        }
        binding.viewpager2.offscreenPageLimit = fragments.size - 1 // 缓存页数
        binding.viewpager2.adapter = viewpager2Adapter
        binding.btnSelectVideo.setOnClickListener {
            SAFUtil.selectVideoMultiple(this)
        }
        binding.btnSelectVideo2.setOnClickListener {
            SAFUtil.selectVideoMultiple(this)
        }
        binding.btnLandScreen.setOnClickListener {
            viewModel.setupIsLandScreen(!viewModel.getIsLandScreen())
        }
        binding.btnJoin.setOnClickListener {
            viewModel.joinVideo()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == RESULT_OK) {
            if (requestCode == SAFUtil.VIDEO_MULTIPLE_PICKER_REQUEST_CODE) {
                viewModel.setupVideoUriStrings(resultData?.clipData)
            }
        }
    }

    private fun setupScreenOrientationLock(landScreen: Boolean) {
        if (landScreen) {
            // 横屏
            logD("设置横屏")
            binding.btnLandScreen.text = getString(R.string.land_screen_orientation)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        } else {
            // 竖屏
            logI("设置竖屏")
            binding.btnLandScreen.text = getString(R.string.portrait_screen_orientation)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.tvInfo.visibility = View.VISIBLE
            binding.spaceTop.visibility = View.VISIBLE
            binding.spaceBottom.visibility = View.VISIBLE
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.tvInfo.visibility = View.GONE
            binding.spaceTop.visibility = View.GONE
            binding.spaceBottom.visibility = View.GONE
        }
    }
}