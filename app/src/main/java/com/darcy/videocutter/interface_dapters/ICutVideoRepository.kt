package com.darcy.videocutter.interface_dapters

interface ICutVideoRepository {
    suspend fun cutVideo(
        inputUriStr: String?,
        startMs: Long,
        endMs: Long
    ): String?
}