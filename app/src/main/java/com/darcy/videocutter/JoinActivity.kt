package com.darcy.videocutter

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logV
import com.darcy.lib_log_toast.exts.toasts
import com.darcy.lib_saf_select.utils.SAFUtil
import com.darcy.videocutter.adapter.ViewPager2Adapter
import com.darcy.videocutter.databinding.ActivityJoinBinding
import com.darcy.videocutter.fragment.VideoThumbnailFragment
import com.darcy.videocutter.viewmodel.JoinViewModel
import com.darcy.videocutter.viewmodel.state.VideoJoinState
import kotlinx.coroutines.launch
import java.io.File

class JoinActivity : AppCompatActivity() {
    private val binding: ActivityJoinBinding by lazy {
        ActivityJoinBinding.inflate(layoutInflater)
    }
    private val viewModel: JoinViewModel by viewModels()
    private val fragments: MutableList<Fragment> = mutableListOf()
    private val viewpager2Adapter: ViewPager2Adapter by lazy {
        ViewPager2Adapter(supportFragmentManager, lifecycle, fragments)
    }

    fun initTestData() {
        val imageFolder = getExternalFilesDir("video_thumbnail")
        val image1 = File(imageFolder, "image1.jpg")
        val image2 = File(imageFolder, "image2.jpg")
        val image3 = File(imageFolder, "image3.jpg")
        fragments.add(VideoThumbnailFragment.newInstance(image1.absolutePath, image1.absolutePath))
        fragments.add(VideoThumbnailFragment.newInstance(image2.absolutePath, image2.absolutePath))
        fragments.add(VideoThumbnailFragment.newInstance(image3.absolutePath, image3.absolutePath))
    }

    fun addItems(items: List<String>) {
        fragments.clear()
        for (imageFile in items) {
            logV("addItem: $imageFile")
            fragments.add(VideoThumbnailFragment.newInstance(imageFile, imageFile))
        }
        viewpager2Adapter.notifyDataSetChanged()
    }

    fun addItem(imageFile: File) {
        fragments.add(
            VideoThumbnailFragment.newInstance(imageFile.absolutePath, imageFile.absolutePath)
        )
        viewpager2Adapter.notifyItemInserted(fragments.size - 1)
    }

    fun removeItem(position: Int) {
        fragments.removeAt(position)
        viewpager2Adapter.notifyItemRemoved(position)
    }

    fun updateItem(position: Int, imageFile: File) {
        fragments[position] =
            VideoThumbnailFragment.newInstance(imageFile.absolutePath, imageFile.absolutePath)
        viewpager2Adapter.notifyItemChanged(position)
    }

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
        initTestData()
        initView()
        initFlowCollect()
    }

    private fun initFlowCollect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is VideoJoinState.Error -> {
                            logD("拼接错误: ${state.error}")
                            toasts(state.error)
                        }

                        is VideoJoinState.Idle -> {
                            logD("Idle")
                        }

                        is VideoJoinState.Loading -> {
                            logD("开始拼接视频")
                            toasts("开始拼接视频...")
                        }

                        is VideoJoinState.Success -> {
                            logD("拼接成功: ${state.outputUri}")
                            toasts("拼接成功：${state.outputUri}")
                        }

                        is VideoJoinState.SelectedVideo -> {
                            logD("选择视频: ${state.thumbnailImages}")
                            addItems(state.thumbnailImages)
                        }
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.viewpager2.offscreenPageLimit = fragments.size - 1 // 缓存页数
        binding.viewpager2.adapter = viewpager2Adapter
        binding.btnSelectVideo.setOnClickListener {
            SAFUtil.selectVideoMultiple(this)
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
}