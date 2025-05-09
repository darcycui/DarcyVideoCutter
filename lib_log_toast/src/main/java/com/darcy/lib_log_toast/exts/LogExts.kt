package com.darcy.lib_access_skip.exts

import android.util.Log

const val LOG_TAG = " DarcyLog "
fun Any.logD(message: String?, tag: String? = LOG_TAG) {
    Log.d((tag ?: LOG_TAG), message ?: "")
}

fun Any.logI(message: String?, tag: String? = LOG_TAG) {
    Log.i((tag ?: LOG_TAG), message ?: "")
}

fun Any.logV(message: String?, tag: String? = LOG_TAG) {
    Log.v((tag ?: LOG_TAG), message ?: "")
}

fun Any.logW(message: String?, tag: String? = LOG_TAG) {
    Log.w((tag ?: LOG_TAG), message ?: "")
}

fun Any.logE(message: String?, tag: String? = LOG_TAG) {
    Log.e((tag ?: LOG_TAG), message ?: "")
}

fun Exception.print() {
    logE(message = "fail with Exception: ${this::class.java.simpleName} ${this.message}")
    this.printStackTrace()
}