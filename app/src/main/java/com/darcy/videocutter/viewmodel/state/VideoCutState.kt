package com.darcy.videocutter.viewmodel.state

import android.net.Uri

// VideoCutState.kt
sealed class VideoCutState {
    object Idle : VideoCutState()
    object Loading : VideoCutState()
    data class Success(val outputUri: Uri) : VideoCutState()
    data class Error(val error: String) : VideoCutState()
    data class Toasts(val message: String) : VideoCutState()
    data class SelectVideo(val videoUri: Uri) : VideoCutState()
    data class MarkStartTime(val time: Long) : VideoCutState()
    data class MarkEndTime(val time: Long) : VideoCutState()
    data class Period(val text: String) : VideoCutState()
}
