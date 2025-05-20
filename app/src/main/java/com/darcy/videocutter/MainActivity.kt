package com.darcy.videocutter

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.darcy.lib_log_toast.exts.toasts
import com.darcy.videocutter.databinding.ActivityMainBinding
import com.darcy.videocutter.dialog.PermissionDialog
import com.darcy.videocutter.settings.SettingsUtil

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
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 允许延伸到状态栏和导航栏
        enableEdgeToEdge()
        setContentView(binding.root)
        // 禁用导航栏背景色对比度
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        initView()
        checkMediaPermission()
    }

    private fun initView() {
        binding.apply {
            btnCutPage.setOnClickListener {
                startActivity(Intent(this@MainActivity, CutActivity::class.java))
            }
            btnJoinPage.setOnClickListener {
                startActivity(Intent(this@MainActivity, JoinActivity::class.java))
            }
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
        // do something
    }
}
