package com.darcy.videocutter.utils

object InputTimeFormat {
    /**
     * 将时间字符串转换为秒数
     * 时：分：秒
     */
    fun timeStringToMilliSeconds(time: String): Long {
        val timeList = splitTime(time)
        val seconds =  when (timeList.size) {
            3 -> {
                timeList[0].toLong() * 60 * 60 + timeList[1].toLong() * 60 + timeList[2].toLong()
            }

            2 -> {
                timeList[0].toLong() * 60 + timeList[1].toLong()
            }

            else -> {
                time.trim().toLongOrNull() ?: 0
            }
        }
        return seconds * 1000
    }

    private fun splitTime(time: String): List<String> {
        return time.trim().split(":")
    }
}