package com.huojieren.apppause.data.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogRepository(
    context: Context
) {
    // TODO 2025/12/12 14:07 使用 timber 替代 log

    /**
     * 内部缓存目录日志文件
     */
    private val internalLogFile: File = File(context.cacheDir, "app_logs.txt")

    /**
     * 获取当前时间
     */
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * 将日志写入内部缓存目录的日志文件
     */
    private fun logToInternalFile(message: String) {
        try {
            internalLogFile.appendText("${getCurrentTime()} - $message\n")
        } catch (e: IOException) {
            Log.e("LogUtil", "Error writing to log file", e)
        }
    }

    /**
     * 记录日志
     * @param tag 日志标签
     * @param message 日志内容
     * @param level 日志级别，默认为 DEBUG
     * @param throwable 异常
     */
    fun log(tag: String, message: String, level: Int = Log.DEBUG, throwable: Throwable? = null) {
        // TODO 2025/11/9 17:59 规范日志输出，设置重载方法，支持指定多个参数，并自动格式化
        val formatted = "${getCurrentTime()} ${levelToString(level)}/$tag: $message"

        // TODO 2026/1/31 16:18 使用单个字母 E、D 来指定日志级别
        when (level) {
            Log.DEBUG -> Log.d(tag, message, throwable)
            Log.INFO -> Log.i(tag, message, throwable)
            Log.WARN -> Log.w(tag, message, throwable)
            Log.ERROR -> Log.e(tag, message, throwable)
        }

        try {
            internalLogFile.appendText("$formatted\n")
        } catch (e: IOException) {
            Log.e("LogRepository", "Write failed", e)
        }
    }


    /**
     * 日志级别转换成字符串
     * @param level 日志级别
     */
    private fun levelToString(level: Int) = when (level) {
        Log.DEBUG -> "DEBUG"
        Log.INFO -> "INFO"
        Log.WARN -> "WARN"
        Log.ERROR -> "ERROR"
        else -> "UNKNOWN"
    }

    /**
     * 清空内部缓存日志
     * @return true: 成功 false: 失败
     */
    fun clearLog(): Boolean {
        try {
            if (internalLogFile.exists()) {
                internalLogFile.writeText("")
            }
            return true
        } catch (e: Exception) {
            log("LogUtil", "清空日志失败: ${e.message}", Log.ERROR)
            return false
        }
    }

    /**
     * 复制内部缓存日志到外部下载目录
     * @return 0: 成功 1: 内部日志不存在 -1: 保存失败
     */
    fun saveLog(): Int {
        try {
            // 获取 Downloads/App Pause/ 目录
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val saveDir = File(downloadsDir, "App Pause").apply { mkdirs() }

            // 外部日志文件
            val externalLogFile = File(saveDir, "app_logs.txt")

            // 复制内容
            if (internalLogFile.exists()) {
                val logContent = internalLogFile.readText()
                externalLogFile.writeText(logContent)
                return 0
            } else {
                return 1
            }
        } catch (e: Exception) {
            log("LogUtil", "保存日志失败: ${e.message}", Log.ERROR)
            return -1
        }
    }
}