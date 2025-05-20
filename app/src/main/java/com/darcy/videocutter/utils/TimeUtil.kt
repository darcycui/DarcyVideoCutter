package com.darcy.videocutter.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 时间工具类
 */
object TimeUtil {
    /**
     * 将毫秒数转换为时间格式
     */
    fun millisecondsToTime(milliseconds: Long): String {
        return String.format(
            locale = Locale.CHINA,
            "%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliseconds),
            TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60,
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
        )
    }

    /**
     * 获取当前时间 yyyy-MM-dd-HH:mm:ss
     */
    fun getCurrentTimeLong(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        return LocalDateTime.now().format(formatter)
    }

    /**
     * 获取当前时间 yy-MMdd-HH:mm
     */
    fun getCurrentTimeShort(): String {
        val formatter = DateTimeFormatter.ofPattern("yy-MMdd-HH-mm")
        return LocalDateTime.now().format(formatter)
    }

}