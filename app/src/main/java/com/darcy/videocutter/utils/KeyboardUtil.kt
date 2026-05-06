package com.darcy.videocutter.utils

import android.app.Activity
import android.view.View

object KeyboardUtil {
    fun hideKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    fun showKeyboard(activity: Activity, view: View) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        view.requestFocus()
        inputMethodManager.showSoftInput(activity.currentFocus, 0)
    }
}