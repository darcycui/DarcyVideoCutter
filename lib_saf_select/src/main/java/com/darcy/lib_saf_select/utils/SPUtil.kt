package com.darcy.lib_saf_select.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.core.net.toUri
import com.darcy.lib_saf_select.utils.SPKey.KEY_USE_NEW_DOCUMENT
import com.darcy.lib_saf_select.utils.SPKey.SP_NAME_SAF

/**
 * sp 工具类
 */
object SPUtil {
    private val TAG = SPUtil::class.java.simpleName

    // 保存 URI 到本地
    fun saveUri(
        context: Context, key: String, uri: Uri,
        prefs: SharedPreferences = preferences(context)
    ) {
        prefs.edit {
            putString(key, uri.toString())
        }
    }

    // 读取保存的 URI
    fun getSavedUri(
        context: Context, key: String,
        prefs: SharedPreferences = preferences(context)
    ): Uri? {
        return prefs.getString(key, "")?.toUri()
    }

    private fun preferences(context: Context): SharedPreferences = context.getSharedPreferences(
        SP_NAME_SAF, MODE_PRIVATE
    )

    fun setUseNewDocument(
        context: Context, use: Boolean,
        prefs: SharedPreferences = preferences(context)
    ) {
        prefs.edit {
            putBoolean(KEY_USE_NEW_DOCUMENT, use)
        }
    }

    fun getUseNewDocument(
        context: Context,
        prefs: SharedPreferences = preferences(context)
    ): Boolean {
        return prefs.getBoolean(
            KEY_USE_NEW_DOCUMENT,
            true
        )
    }
}