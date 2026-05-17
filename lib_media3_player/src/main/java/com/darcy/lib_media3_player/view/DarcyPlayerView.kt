package com.darcy.lib_media3_player.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.darcy.lib_log_toast.exts.logD
import com.darcy.lib_log_toast.exts.logE
import com.darcy.lib_media3_player.view.listener.IPlayerListener
import com.darcy.lib_media3_player.view.listener.VideoGestureListener

//@UnstableApi
@SuppressLint("UnsafeOptInUsageError")
class DarcyPlayerView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : PlayerView(context, attrs, defStyleAttr), IPlayerView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    // 播放控制器
    private val player: ExoPlayer by lazy {
        // 创建 RenderersFactory 并设置扩展渲染器模式
        val renderersFactory = DefaultRenderersFactory(this.context).apply {
            // EXTENSION_RENDERER_MODE_PREFER 会优先使用 FFmpeg 等扩展解码器
            // 如果设为 EXTENSION_RENDERER_MODE_ON，则在系统硬解不支持时 fallback 到 FFmpeg
//            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
//            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        }

        // 使用自定义 RenderersFactory 构建 ExoPlayer
        ExoPlayer.Builder(this.context, renderersFactory)
            .setSeekForwardIncrementMs(30_000L)
            .setSeekBackIncrementMs(10_000L)
            .build().apply {
                // 4. 设置监听（1.6.1 推荐使用 addListener 而非 setListener）
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        logD("播放器状态：$playbackState")
                        playerListener?.onPlaybackStateChanged(playbackState)
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        // 处理错误
                        logE("播放器错误：$error")
                        playerListener?.onPlayerError(error)
                    }
                })

            }

    }

    private var hasPaused = false

    // 手势检测
    val gestureListener = VideoGestureListener(player, this)
    val gestureDetector = GestureDetector(context, gestureListener)

    // 播放监听
    private var playerListener: IPlayerListener? = null

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

    fun setPlayerListener(playerListener: IPlayerListener) {
        this.playerListener = playerListener
    }

}