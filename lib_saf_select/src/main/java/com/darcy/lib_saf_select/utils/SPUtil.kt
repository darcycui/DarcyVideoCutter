package com.darcy.lib_saf_select.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import androidx.core.content.edit

/**
 * sp 工具类
 */
object SPUtil {
    private val TAG = SPUtil::class.java.simpleName
    private const val SP_NAME_SAF = "dir_prefs"
    private const val KEY_TREE_URI = "tree_uri"
    private const val KEY_TREE_PATH = "tree_path"

    // 保存 URI 到本地
    fun saveTreeUri(context: Context, uri: Uri) {
        val prefs: SharedPreferences = context.getSharedPreferences(SP_NAME_SAF, MODE_PRIVATE)
        prefs.edit {
            putString(KEY_TREE_URI, uri.toString())
        }
    }
    // 保存 URI 到本地
    fun saveTreePath(context: Context, filePath: String?) {
        val prefs: SharedPreferences = context.getSharedPreferences(SP_NAME_SAF, MODE_PRIVATE)
        prefs.edit {
            putString(KEY_TREE_PATH, filePath ?: "")
        }
    }

    // 读取保存的 URI
    fun getSavedTreeUri(context: Context): Uri? {
        val prefs: SharedPreferences = context.getSharedPreferences(SP_NAME_SAF, MODE_PRIVATE)
        val uriString = prefs.getString(KEY_TREE_URI, "")
        return uriString?.toUri()
    }

    // 读取保存的 path
    fun getSavedTreePath(context: Context): String? {
        val prefs: SharedPreferences = context.getSharedPreferences(SP_NAME_SAF, MODE_PRIVATE)
        val uriString = prefs.getString(KEY_TREE_PATH, "")
        return uriString
    }
}