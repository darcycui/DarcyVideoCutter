package com.darcy.lib_media3_player.view.listener

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import kotlin.math.absoluteValue

class VideoGestureListener(
    private val player: Player,
    private val playerView: PlayerView
) : GestureDetector.SimpleOnGestureListener() {
    private var sensitivity = 10.0f    // 灵敏度系数（越大调整越慢）
    private var isScrolling = false
    private var accumulatedDeltaX = 0f

    override fun onDown(e: MotionEvent): Boolean {
        accumulatedDeltaX = 0f
        isScrolling = false
        return true
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        // logD("onSingleTapUp--> isScrolling=$isScrolling")
        if (isScrolling) {
            applyProgressAdjustment()
            isScrolling = false
        } else {
            playerView.performClick()
        }
        return true
    }

    // 实际调整逻辑
    fun applyProgressAdjustment() {
        if (player.duration <= 0 || playerView.width == 0) return

        val duration = player.duration

        // 根据滑动距离调整进度
        val screenWidth = playerView.width.toFloat()
        val deltaProgress = (accumulatedDeltaX / (screenWidth * sensitivity)) * duration
        val newPosition = player.currentPosition + deltaProgress.toLong()

        // 确保进度在合法范围内
        player.seekTo(newPosition.coerceIn(0, duration))
        player.seekTo(newPosition)
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        isScrolling = true
        accumulatedDeltaX -= distanceX // 累积滑动量（distanceX为旧坐标-新坐标）
        // logI("onScroll--> isScrolling=true accumulatedDeltaX=$accumulatedDeltaX")
        return true
    }

    fun getScrolledDistance(): Float {
        return accumulatedDeltaX.absoluteValue
    }

}