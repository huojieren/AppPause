package com.huojieren.apppause.data.logging

import android.util.Log
import timber.log.Timber

/**
 * 应用内的统一日志入口。
 *
 * 业务代码只负责描述事件，日志落盘策略在应用启动时配置。
 */
object AppLog {

    fun logger(
        tag: String,
        message: String,
        level: Int = Log.DEBUG,
        throwable: Throwable? = null
    ) {
        when (level) {
            Log.DEBUG -> Timber.tag(tag).d(throwable, message)
            Log.INFO -> Timber.tag(tag).i(throwable, message)
            Log.WARN -> Timber.tag(tag).w(throwable, message)
            Log.ERROR -> Timber.tag(tag).e(throwable, message)
            else -> Timber.tag(tag).v(throwable, message)
        }
    }
}
