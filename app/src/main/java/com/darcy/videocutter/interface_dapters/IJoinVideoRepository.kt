package com.darcy.videocutter.interface_dapters

interface IJoinVideoRepository {
    suspend fun joinVideo(
        inputUriStrings: List<String>,
    ): String?
}