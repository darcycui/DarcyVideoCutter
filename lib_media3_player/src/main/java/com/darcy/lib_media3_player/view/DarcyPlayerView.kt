package com.darcy.lib_media3_player.view

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SeekParameters
import androidx.media3.ui.PlayerControlView
import androidx.media3.ui.PlayerView
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logV
import com.darcy.lib_media3_player.view.listener.VideoGestureListener

@UnstableApi
class DarcyPlayerView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : PlayerView(context, attrs, defStyleAttr), IPlayerView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    // 播放控制器
    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(this.context)
            .setSeekForwardIncrementMs(30_000L)
            .setSeekBackIncrementMs(10_000L)
            .build()
    }

    private var hasPaused = false

    // 手势检测
    val gestureListener = VideoGestureListener(player, this)
    val gestureDetector = GestureDetector(context, gestureListener)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        if (event.action == MotionEvent.ACTION_UP) {
            if (gestureListener.getScrolledDistance() > 0) {
                // logV("调整进度")
                gestureListener.applyProgressAdjustment()
            } else {
                // logD("不调整进度")
            }
        }
        return true
    }

    init {
        this.setPlayer(player)
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