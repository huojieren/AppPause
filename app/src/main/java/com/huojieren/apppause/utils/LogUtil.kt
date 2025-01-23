package com.huojieren.apppause.utils

import android.util.Log

class LogUtil {
    companion object {
        private val TAG: String = "monitorTest"
        fun logDebug(message: String) {
            Log.d(TAG, message)
        }
    }
}