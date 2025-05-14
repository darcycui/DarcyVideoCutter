package com.darcy.videocutter.repository

import android.content.Context
import androidx.core.net.toUri
import com.darcy.lib_saf_select.utils.SPKey
import com.darcy.lib_saf_select.utils.SPUtil
import com.darcy.videocutter.app.App
import com.darcy.videocutter.interface_dapters.ISPRepository

class SPRepository(private val context: Context = App.getInstance()) : ISPRepository {
    override fun saveTreeUri(uriPath: String) {
        val uri = uriPath.toUri()
        SPUtil.saveUri(context, SPKey.KEY_TREE_URI, uri)
    }

    override fun getSavedTreeUri(): String? {
        return SPUtil.getSavedUri(context, SPKey.KEY_TREE_URI)?.toString()
    }
}