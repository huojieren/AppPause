package com.huojieren.apppause.data.repository

import android.content.Context
import android.os.Environment
import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LogRepository(
    context: Context
) {

    companion object {
        private const val LOG_FILE_NAME = "app_logs.log"
        private const val MAX_BACKUP_FILES = 5
    }

    private val cacheDir = context.cacheDir
    private val internalLogFile: File = File(cacheDir, LOG_FILE_NAME)

    fun log(tag: String, message: String, level: Int = Log.DEBUG, throwable: Throwable? = null) {
        when (level) {
            Log.DEBUG -> Timber.tag(tag).d(throwable, message)
            Log.INFO -> Timber.tag(tag).i(throwable, message)
            Log.WARN -> Timber.tag(tag).w(throwable, message)
            Log.ERROR -> Timber.tag(tag).e(throwable, message)
            else -> Timber.tag(tag).v(message)
        }
    }

    fun clearLog(): Boolean {
        return try {
            if (internalLogFile.exists()) {
                internalLogFile.writeText("")
            }
            for (i in 1..MAX_BACKUP_FILES) {
                val backupFile = File(cacheDir, "$LOG_FILE_NAME.$i")
                if (backupFile.exists()) {
                    backupFile.writeText("")
                }
            }
            true
        } catch (e: Exception) {
            log("LogUtil", "清空日志失败: ${e.message}", Log.ERROR)
            false
        }
    }

    fun saveLog(): Int {
        return try {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val saveDir = File(downloadsDir, "App Pause").apply { mkdirs() }

            val zipFile = File(saveDir, "app_logs.zip")
            val logFiles = getAllLogFiles()

            if (logFiles.isEmpty()) {
                return 1
            }

            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                logFiles.forEach { file ->
                    zipOut.putNextEntry(ZipEntry(file.name))
                    file.inputStream().use { it.copyTo(zipOut) }
                    zipOut.closeEntry()
                }
            }
            0
        } catch (e: Exception) {
            log("LogUtil", "保存日志失败: ${e.message}", Log.ERROR)
            -1
        }
    }

    private fun getAllLogFiles(): List<File> {
        val files = mutableListOf<File>()
        if (internalLogFile.exists()) {
            files.add(internalLogFile)
        }
        for (i in 1..MAX_BACKUP_FILES) {
            val backupFile = File(cacheDir, "$LOG_FILE_NAME.$i")
            if (backupFile.exists()) {
                files.add(backupFile)
            }
        }
        return files.sortedBy { it.name }
    }
}
