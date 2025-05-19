package com.darcy.lib_saf_select.utils

import android.app.Activity
import android.content.Intent

object SAFUtil {
    const val IMAGE_MIME_TYPE = "image/*"
    const val VIDEO_MIME_TYPE = "video/*"
    const val DOCUMENT_MIME_TYPE = "*/*"

    const val IMAGE_PICKER_REQUEST_CODE = 10
    const val IMAGE_MULTIPLE_PICKER_REQUEST_CODE = 11
    const val VIDEO_PICKER_REQUEST_CODE = 20
    const val VIDEO_MULTIPLE_PICKER_REQUEST_CODE = 21
    const val DOCUMENT_PICKER_REQUEST_CODE = 30
    const val DOCUMENT_MULTIPLE_PICKER_REQUEST_CODE = 31

    const val SAF_TRR_DIR_PERMISSION_REQUEST_CODE: Int = 100

    /**
     * 选择单个图片
     */
    fun selectImage(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = IMAGE_MIME_TYPE
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        activity.startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    /**
     * 选择多个图片
     */
    fun selectImageMultiple(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = IMAGE_MIME_TYPE
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        activity.startActivityForResult(intent, IMAGE_MULTIPLE_PICKER_REQUEST_CODE)
    }

    /**
     * 选择单个视频
     */
    fun selectVideo(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = VIDEO_MIME_TYPE
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        activity.startActivityForResult(intent, VIDEO_PICKER_REQUEST_CODE)
    }

    /**
     * 选择多个视频
     */
    fun selectVideoMultiple(activity: Activity) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = VIDEO_MIME_TYPE
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        activity.startActivityForResult(intent, VIDEO_MULTIPLE_PICKER_REQUEST_CODE)
    }

    /**
     * 选择单个文件
     */
    fun selectDocument(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = DOCUMENT_MIME_TYPE
        }
        activity.startActivityForResult(intent, DOCUMENT_PICKER_REQUEST_CODE)
    }

    /**
     * 选择多个文件
     */
    fun selectDocumentMultiple(activity: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = DOCUMENT_MIME_TYPE
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        activity.startActivityForResult(intent, DOCUMENT_MULTIPLE_PICKER_REQUEST_CODE)
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
        activity.startActivityForResult(intent, SAF_TRR_DIR_PERMISSION_REQUEST_CODE)
    }
}