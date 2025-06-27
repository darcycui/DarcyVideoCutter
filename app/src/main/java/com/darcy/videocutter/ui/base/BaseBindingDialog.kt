package com.darcy.videocutter.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.dylanc.viewbinding.base.ViewBindingUtil

class BaseBindingDialog<VB : ViewBinding>(context: Context, themeResId: Int = 0) :
    Dialog(context, themeResId) {
    lateinit var binding: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewBindingUtil.inflateWithGeneric<VB>(this, layoutInflater)
        setContentView(binding.root)
    }
}