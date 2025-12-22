package com.darcy.videocutter

import android.Manifest
import android.R.attr.data
import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.darcy.lib_log_toast.exts.toasts
import com.darcy.lib_saf_select.folder_data.DialogHelper
import com.darcy.lib_saf_select.folder_data.RequestCode
import com.darcy.lib_saf_select.utils.SAFUtil
import com.darcy.lib_saf_select.utils.SPKey
import com.darcy.lib_saf_select.utils.SPUtil
import com.darcy.videocutter.databinding.ActivityMainBinding
import com.darcy.videocutter.dialog.PermissionDialog
import com.darcy.videocutter.settings.SettingsUtil
import com.darcy.videocutter.ui.CutActivity
import com.darcy.videocutter.ui.JoinActivity

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

        binding.btnFolderTreePermission.setOnClickListener {
            val dataPathAll = "/storage/emulated/0/Android/data"
            val dataPathOne = "/storage/emulated/0/Android/data/com.xunlei.downloadprovider"
            val obbPath = "/storage/emulated/0/Android/obb"
            SPUtil.setUseNewDocument(this, true)
            DialogHelper.showRequestUriPermissionDialog(this, dataPathAll)
//            SPUtil.setUseNewDocument(this, false)
//            DialogHelper.showRequestUriPermissionDialog(this, dataPathOne)
        }

        binding.btnFolderTree.setOnClickListener {
            SAFUtil.requestPersistentDirAccess(this)
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultData: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, resultData, caller)
        if (resultCode == RESULT_OK) {
            if (requestCode == RequestCode.DOCUMENT) {
                resultData?.data?.let { uri ->
                    // 持久化权限 (关键)
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    // 保存 URI 到 SharedPreferences
                    SPUtil.saveUri(this, SPKey.KEY_ANDROID_DATA_URI, uri)
                }
            }
        } else {
            toasts("Activity result not OK")
        }
    }
}
