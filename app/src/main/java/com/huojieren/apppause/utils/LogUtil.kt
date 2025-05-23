package com.huojieren.apppause.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogUtil(private val context: Context) {
    private val logFile: File = File(context.filesDir, "app_logs.txt")

    // 获取当前时间戳
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    // 输出到 Logcat 和 文件
    private fun logToFile(message: String) {
        try {
            // 写入日志文件，追加模式
            val writer = FileWriter(logFile, true)
            writer.append("${getCurrentTime()} - $message\n")
            writer.close()
        } catch (e: IOException) {
            Log.e("LogUtil", "Error writing to log file", e)
        }
    }

    fun log(tag: String, message: String, level: Int = Log.DEBUG) {
        when (level) {
            Log.DEBUG -> {
                Log.d(tag, message)
                logToFile("DEBUG: [$tag] $message")
            }

            Log.INFO -> {
                Log.i(tag, message)
                logToFile("INFO: [$tag] $message")
            }

            Log.WARN -> {
                Log.w(tag, message)
                logToFile("WARN: [$tag] $message")
            }

            Log.ERROR -> {
                Log.e(tag, message)
                logToFile("ERROR: [$tag] $message")
            }
        }
    }


    // 获取保存位置目录
    private fun getLogFile(): File {
        // 获取外部存储下载目录
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val saveDir = File(downloadsDir, "App Pause")
        // 创建文件夹
        if (!saveDir.exists()) {
            if (saveDir.mkdirs()) {
                LogUtil(context).log("AppMonitor", "创建\"App Pause\"文件夹成功", Log.DEBUG)
            } else {
                LogUtil(context).log("AppMonitor", "创建\"App Pause\"文件夹失败", Log.ERROR)
            }
        } else {
            LogUtil(context).log("AppMonitor", "\"App Pause\"文件夹存在", Log.DEBUG)
        }
        return File(saveDir, "app_logs.txt")
    }

    // 清空日志
    fun clearLog() {
        try {
            val logFile = File(context.filesDir, "app_logs.txt")
            if (logFile.exists()) {
                logFile.writeText("")
            }
            showToast(context, "日志已清空")
        } catch (e: Exception) {
            LogUtil(context).log("AppMonitor", "清空日志失败", Log.ERROR)
            showToast(context, "清空日志失败")
        }
    }

    // 保存日志
    fun saveLog() {
        try {
            val logFile = getLogFile() // 获取文件保存位置
            val logContent = File(context.filesDir, "app_logs.txt").readText() // 从应用私有目录获取当前日志内容
            logFile.writeText(logContent) // 将日志保存到指定位置
            showToast(context, "日志已保存到：Download/App Pause/app_logs.txt")
        } catch (e: Exception) {
            LogUtil(context).log("AppMonitor", "保存日志失败", Log.ERROR)
            showToast(context, "保存日志失败")
        }
    }
}