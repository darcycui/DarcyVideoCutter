package com.darcy.lib_saf_select.folder_data

import android.R.attr.path
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile
import com.darcy.lib_saf_select.utils.SPUtil

object FileTools {

    val ROOT_PATH: String = Environment.getExternalStorageDirectory().path

    fun requestUriPermission(activity: Activity, path: String) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        intent.setFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val treeUri: Uri = pathToUri(activity, path)
            val df = DocumentFile.fromTreeUri(activity, treeUri)
            if (df != null) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, df.getUri())
            }
        }
        activity.startActivityForResult(intent, RequestCode.DOCUMENT)
    }


    private fun pathToUri(context: Context, path: String): Uri {
        val halfPath: String = path.replace("$ROOT_PATH/", "")
        val segments: Array<String?> =
            halfPath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val uriBuilder = Uri.Builder()
            .scheme("content")
            .authority("com.android.externalstorage.documents")
            .appendPath("tree")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (SPUtil.getUseNewDocument(context)) {
                uriBuilder.appendPath("primary:A\u200Bndroid/" + segments[1])
            } else {
                uriBuilder.appendPath("primary:Android/" + segments[1] + "/" + segments[2])
            }
        } else {
            uriBuilder.appendPath("primary:Android/" + segments[1])
        }
        uriBuilder.appendPath("document")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && SPUtil.getUseNewDocument(context)) {
            uriBuilder.appendPath("primary:A\u200Bndroid/" + halfPath.replace("Android/", ""))
        } else {
            uriBuilder.appendPath("primary:$halfPath")
        }
        return uriBuilder.build()
    }
    private fun isDataPath(path: String?): Boolean {
        return ("$ROOT_PATH/Android/data") == path
    }

    private fun isObbPath(path: String?): Boolean {
        return ("$ROOT_PATH/Android/obb") == path
    }
}