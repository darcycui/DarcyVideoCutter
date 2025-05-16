package com.darcy.videocutter.dialog

import android.Manifest
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.darcy.videocutter.R

object PermissionDialog {

    fun showPermissionDeniedDialog(
        context: Context,
        clickYes: () -> Unit,
        clickNo: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context).apply {
            setTitle(R.string.title_permission_denied)
            setMessage(R.string.msg_permission_denied)
            setPositiveButton(R.string.btn_app_settings) { _, _ ->
                clickYes.invoke()
            }
            setNegativeButton(R.string.btn_cancel) { _, _ ->
                clickNo?.invoke()
            }
            create().show()
        }
    }

    fun showPermissionRationaleDialog(
        context: Context,
        permission: String,
        clickYes: () -> Unit,
        clickNo: (() -> Unit)? = null
    ) {
        val (titleRes, messageRes) = when (permission) {
            Manifest.permission.READ_MEDIA_VIDEO ->
                Pair(R.string.title_media_permission, R.string.rationale_media_permission)

            else ->
                Pair(R.string.title_storage_permission, R.string.rationale_storage_permission)
        }

        AlertDialog.Builder(context).apply {
            setTitle(context.getString(titleRes))
            setMessage(context.getString(messageRes))
            setPositiveButton(R.string.btn_continue_request) { _, _ ->
                clickYes.invoke()
            }
            setNegativeButton(R.string.btn_cancel) { _, _ ->
                clickNo?.invoke()
            }
            create().show()
        }
    }
}