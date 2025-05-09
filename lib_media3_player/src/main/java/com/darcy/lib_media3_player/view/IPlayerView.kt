package com.darcy.lib_media3_player.view

import android.net.Uri

interface IPlayerView {
    fun setMediaUri(uri: Uri)
    fun start()
    fun pause()
    fun stop()
    fun release()
    fun isPlaying(): Boolean
    fun hasPaused(): Boolean
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun seekTo(position: Long)
}