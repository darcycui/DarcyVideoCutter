package com.darcy.videocutter.viewmodel.state

import android.net.Uri

// VideoCutState.kt
sealed class VideoCutState {
    object Idle : VideoCutState()
    object Loading : VideoCutState()
    data class Success(val outputUri: Uri) : VideoCutState()
    data class Error(val error: String) : VideoCutState()
    data class SelectedVideo(val videoUri: Uri) : VideoCutState()
    data class MarkStartTime(val time: Long) : VideoCutState()
    data class MarkEndTime(val time: Long) : VideoCutState()
    data class Period(val text: String) : VideoCutState()
}
