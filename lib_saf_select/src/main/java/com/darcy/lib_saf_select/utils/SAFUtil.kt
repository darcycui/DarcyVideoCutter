package com.darcy.lib_saf_select.utils

import android.app.Activity
import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult

object SAFUtil {
    const val IMAGE_MIME_TYPE = "image/*"
    const val VIDEO_MIME_TYPE = "video/*"
    const val DOCUMENT_MIME_TYPE = "*/*"

    const val IMAGE_PICKER_REQUEST_CODE = 1
    const val VIDEO_PICKER_REQUEST_CODE = 2
    const val DOCUMENT_PICKER_REQUEST_CODE = 3

    const val REQUEST_DIR_PERMISSION_CODE: Int = 100

    /**
     * 选择图片
     */
    fun selectImage(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = IMAGE_MIME_TYPE
        }
        activity.startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    /**
     * 选择视频
     */
    fun selectVideo(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = VIDEO_MIME_TYPE
        }
        activity.startActivityForResult(intent, VIDEO_PICKER_REQUEST_CODE)
    }

    /**
     * 选择视频
     */
    fun selectDocument(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = DOCUMENT_MIME_TYPE
        }
        activity.startActivityForResult(intent, DOCUMENT_PICKER_REQUEST_CODE)
    }

    /**
     * 启动目录选择器 持久授权读写权限
     */
    fun requestPersistentDirAccess(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.addFlags(
            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        activity.startActivityForResult(intent, REQUEST_DIR_PERMISSION_CODE)
    }
}