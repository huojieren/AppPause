package com.huojieren.apppause.data.diagnostics.export

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.huojieren.apppause.data.diagnostics.model.ExportResult
import com.huojieren.apppause.data.diagnostics.storage.DiagnosticStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/** 将现有诊断材料打包导出；导出本身不触发新的日志采集。 */
@Singleton
class DiagnosticsExporter @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val store: DiagnosticStore
) {
    fun export(): ExportResult {
        val files = exportableFiles()
        return export(files, "diagnostics")
    }

    /** 导出单项事故材料，并附带当前滚动运行日志作为排查上下文。 */
    fun exportIncident(incidentId: String): ExportResult {
        val files = buildList {
            addAll(store.getIncidentMaterialFiles(incidentId))
            addAll(store.getRuntimeLogFiles())
        }.filter { it.exists() && it.length() > 0 }.distinctBy { it.absolutePath }
        return export(files, "diagnostic-$incidentId")
    }

    private fun export(files: List<File>, fileNamePrefix: String): ExportResult {
        if (files.isEmpty()) return ExportResult.NoLogs
        val fileName = "$fileNamePrefix-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}.zip"
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(fileName, files)
            } else {
                saveWithLegacyApi(fileName, files)
            }
            ExportResult.Success
        }.getOrElse(ExportResult::Failed)
    }

    private fun exportableFiles(): List<File> = buildList {
        addAll(store.getRuntimeLogFiles())
        addAll(store.getIncidentFiles())
        addAll(store.getLegacyFiles())
    }.filter { it.exists() && it.length() > 0 }.distinctBy { it.absolutePath }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveWithMediaStore(fileName: String, files: List<File>) {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/zip")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/AppPause")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: throw IllegalStateException("Cannot create diagnostics export")
        try {
            resolver.openOutputStream(uri)?.use { createZip(it, files) }
                ?: throw IllegalStateException("Cannot open diagnostics export")
            resolver.update(uri, ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }, null, null)
        } catch (throwable: Throwable) {
            resolver.delete(uri, null, null)
            throw throwable
        }
    }

    private fun saveWithLegacyApi(fileName: String, files: List<File>) {
        val directory = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "AppPause"
        ).apply { mkdirs() }
        FileOutputStream(File(directory, fileName)).use { createZip(it, files) }
    }

    private fun createZip(output: OutputStream, files: List<File>) {
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry("manifest.txt"))
            zip.write(
                "package=${context.packageName}\ncreatedAt=${System.currentTimeMillis()}\n".toByteArray()
            )
            zip.closeEntry()
            files.forEach { file ->
                zip.putNextEntry(ZipEntry(store.toZipEntryName(file)))
                FileInputStream(file).use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }
}
