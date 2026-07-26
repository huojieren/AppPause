package com.huojieren.apppause.data.diagnostics.runtime

import android.annotation.SuppressLint
import android.util.Log
import com.huojieren.apppause.data.diagnostics.storage.DiagnosticStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor
import timber.log.Timber

/** 将 Timber 事件异步写入有限大小的运行日志队列，避免业务线程直接执行磁盘 I/O。 */
class RuntimeLogTree(
    private val store: DiagnosticStore,
    private val executor: Executor
) : Timber.Tree() {

    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val entry = format(priority, tag, message, t)
        executor.execute {
            runCatching { store.appendRuntimeLog(entry) }
                .onFailure { Log.e("RuntimeLogTree", "Write runtime log failed", it) }
        }
    }

    private fun format(priority: Int, tag: String?, message: String, throwable: Throwable?): String {
        val timestamp = System.currentTimeMillis()
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(timestamp))
        return buildString {
            append("$timestamp $date ${priorityToString(priority)}/${tag ?: "AppPause"}: $message")
            throwable?.let { append("\n${Log.getStackTraceString(it)}") }
            append('\n')
        }
    }

    private fun priorityToString(priority: Int): String = when (priority) {
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        else -> "V"
    }
}
