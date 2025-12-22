package com.darcy.lib_saf_select.folder_data

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.darcy.lib_saf_select.R

object DialogHelper {

    fun showRequestUriPermissionDialog(context: Activity, mPathCache: String) {
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setMessage(R.string.dialog_need_uri_permission_message)
            .setPositiveButton(R.string.dialog_button_request_permission, { dialog, which ->
                FileTools.requestUriPermission(context, mPathCache)
            })
            .setNegativeButton(R.string.dialog_button_cancel, { dialog, which -> }).create().show()
    }
}