package com.darcy.videocutter.utils

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
}