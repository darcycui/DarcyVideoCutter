package com.darcy.videocutter.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object SettingsUtil {
    fun openAppSettings(context: Context) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(this)
        }
    }
}