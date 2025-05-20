package com.darcy.videocutter.viewmodel.state

import android.net.Uri

// VideoCutState.kt
sealed class VideoJoinState {
    object Idle : VideoJoinState()
    object Loading : VideoJoinState()
    data class Success(val outputUri: Uri) : VideoJoinState()
    data class Error(val error: String) : VideoJoinState()
    data class SelectedVideo(val videoUriStrings: List<String>, val thumbnailImages: List<String>) : VideoJoinState()
}
