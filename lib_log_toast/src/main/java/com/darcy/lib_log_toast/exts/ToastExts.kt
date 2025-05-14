package com.darcy.lib_log_toast.exts

import android.content.Context
import android.widget.Toast

fun Context.toasts(msg: String?) {
    if (msg.isNullOrEmpty()) return
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}