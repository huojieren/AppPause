package com.huojieren.apppause

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.core.content.edit
import com.huojieren.apppause.managers.ListenerManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var listenerManager: ListenerManager

    override fun onCreate() {
        super.onCreate()
        checkPreviousExit()
        initTimber()
    }

    private fun checkPreviousExit() {
        val prefs = getSharedPreferences("app_status", MODE_PRIVATE)
        val normalExit = prefs.getBoolean("normal_exit", true)
        if (!normalExit) {
            Timber.tag("App").w("App was killed unexpectedly last time")
        }
        prefs.edit { putBoolean("normal_exit", false) }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(RollingFileTree(cacheDir))
    }

    private class RollingFileTree(private val cacheDir: File) : Timber.Tree() {

        companion object {
            private const val MAX_FILE_SIZE = 5 * 1024 * 1024L
            private const val MAX_BACKUP_FILES = 5
            private const val LOG_FILE_NAME = "app_logs.log"
        }

        private val logFile: File = File(cacheDir, LOG_FILE_NAME)

        @SuppressLint("LogNotTimber")
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            try {
                checkRotation()
                writeLog(priority, tag, message, t)
            } catch (e: IOException) {
                Log.e("TimberFileTree", "Write log failed", e)
            }
        }

        private fun checkRotation() {
            if (logFile.exists() && logFile.length() >= MAX_FILE_SIZE) {
                rotateLogFiles()
            }
        }

        private fun rotateLogFiles() {
            for (i in MAX_BACKUP_FILES - 1 downTo 1) {
                val oldFile = File(cacheDir, "$LOG_FILE_NAME.$i")
                val newFile = File(cacheDir, "$LOG_FILE_NAME.${i + 1}")
                if (oldFile.exists()) {
                    oldFile.renameTo(newFile)
                }
            }
            val backupFile = File(cacheDir, "$LOG_FILE_NAME.1")
            logFile.renameTo(backupFile)
        }

        private fun cleanOldFiles() {
            val maxBackupNumber = MAX_BACKUP_FILES + 1
            for (i in maxBackupNumber..MAX_BACKUP_FILES * 2) {
                val oldFile = File(cacheDir, "$LOG_FILE_NAME.$i")
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            }
        }

        private fun writeLog(priority: Int, tag: String?, message: String, t: Throwable?) {
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formatted =
                "$timestamp ${dateFormat.format(Date())} ${priorityToString(priority)}/$tag: $message${
                    if (t != null) "\n${Log.getStackTraceString(t)}" else ""
                }\n"
            logFile.appendText(formatted)
            cleanOldFiles()
        }

        private fun priorityToString(priority: Int) = when (priority) {
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            else -> "V"
        }
    }
}
