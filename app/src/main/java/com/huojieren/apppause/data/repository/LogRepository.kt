package com.huojieren.apppause.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class LogRepository(
    private val context: Context
) {
    private val cacheDir = context.cacheDir
    private val internalLogFile: File = File(cacheDir, LOG_FILE_NAME)

    companion object {
        private const val LOG_FILE_NAME = "app_logs.log"
        private const val MAX_BACKUP_FILES = 5

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
                else -> Timber.tag(tag).v(message)
            }
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
            logger("LogUtil", "Clear log failed: ${e.message}", Log.ERROR)
            false
        }
    }

    fun saveLog(): Int {
        val logFiles = getAllLogFiles()
        if (logFiles.isEmpty()) {
            return 1
        }

        return try {
            val zipFileName = "app_logs.zip"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(zipFileName, logFiles)
            } else {
                saveWithLegacyApi(zipFileName, logFiles)
            }
            0
        } catch (e: Exception) {
            logger("LogUtil", "Save log failed: ${e.message}", Log.ERROR)
            -1
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveWithMediaStore(fileName: String, logFiles: List<File>) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/zip")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AppPause")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("Cannot create file in Downloads")

        resolver.openOutputStream(uri)?.use { outputStream ->
            createZipOutputStream(outputStream, logFiles)
        }
    }

    private fun saveWithLegacyApi(fileName: String, logFiles: List<File>) {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, "AppPause").apply { mkdirs() }
        val zipFile = File(appDir, fileName)

        FileOutputStream(zipFile).use { outputStream ->
            createZipOutputStream(outputStream, logFiles)
        }
    }

    private fun createZipOutputStream(outputStream: OutputStream, logFiles: List<File>) {
        ZipOutputStream(outputStream).use { zipOut ->
            logFiles.forEach { file ->
                zipOut.putNextEntry(ZipEntry(file.name))
                FileInputStream(file).use { inputStream ->
                    inputStream.copyTo(zipOut)
                }
                zipOut.closeEntry()
            }
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
