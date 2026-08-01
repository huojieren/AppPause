package com.huojieren.apppause.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun showToast(context: Context, message: String) {
    val show = {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    if (Looper.myLooper() == Looper.getMainLooper()) {
        show()
    } else {
        Handler(Looper.getMainLooper()).post(show)
    }
}
