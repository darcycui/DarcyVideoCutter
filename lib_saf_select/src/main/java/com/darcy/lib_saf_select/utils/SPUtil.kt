package com.darcy.lib_saf_select.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import com.darcy.lib_saf_select.utils.SPKey.SP_NAME_SAF

/**
 * sp 工具类
 */
object SPUtil {
    private val TAG = SPUtil::class.java.simpleName

    // 保存 URI 到本地
    fun saveUri(
        context: Context, key: String, uri: Uri,
        prefs: SharedPreferences = context.getSharedPreferences(
            SP_NAME_SAF, MODE_PRIVATE
        )
    ) {
        prefs.edit {
            putString(key, uri.toString())
        }
    }

    // 读取保存的 URI
    fun getSavedUri(
        context: Context, key: String,
        prefs: SharedPreferences = context.getSharedPreferences(
            SP_NAME_SAF, MODE_PRIVATE
        )
    ): Uri? {
        return prefs.getString(key, "")?.toUri()
    }
}