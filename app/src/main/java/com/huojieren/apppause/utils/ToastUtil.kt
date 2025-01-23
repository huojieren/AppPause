package com.huojieren.apppause.utils

import android.content.Context
import android.widget.Toast

class ToastUtil {

    companion object {
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}