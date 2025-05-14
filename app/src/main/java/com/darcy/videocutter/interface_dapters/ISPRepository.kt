package com.darcy.videocutter.interface_dapters

interface ISPRepository {
    // 保存 URI 到本地
    fun saveTreeUri(uriPath: String)

    // 读取保存的 URI
    fun getSavedTreeUri(): String?
}