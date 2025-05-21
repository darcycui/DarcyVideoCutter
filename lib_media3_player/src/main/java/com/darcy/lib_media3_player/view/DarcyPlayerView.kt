package com.darcy.lib_media3_player.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView

class DarcyPlayerView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : PlayerView(context, attrs, defStyleAttr), IPlayerView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this.context).build()
    }
    // 添加控制器引用
    private val controllerView by lazy {
        findViewById<PlayerControlView>(androidx.media3.ui.R.id.exo_controller)
    }
    private var hasPaused = false

    init {
        this.setPlayer(player)
        // 获取默认的 PlayerControlView 并设置间隔
        controllerView?.apply {
        }
    }

    override fun setMediaUri(uri: Uri) {
        player.setMediaItem(MediaItem.fromUri(uri))
        player.prepare()
    }

    override fun start() {
        player.play()
        hasPaused = false
    }

    override fun pause() {
        player.pause()
        hasPaused = true
    }

    override fun stop() {
        player.stop()
    }

    override fun release() {
        player.release()
    }

    override fun isPlaying(): Boolean {
        return player.isPlaying
    }

    override fun hasPaused(): Boolean {
        return hasPaused
    }

    override fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    override fun getDuration(): Long {
        return player.duration
    }

    override fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

}