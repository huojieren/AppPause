package com.huojieren.apppause.data.diagnostics.storage

import android.content.Context
import android.util.Log
import com.huojieren.apppause.data.diagnostics.model.DiagnosticIncident
import com.huojieren.apppause.data.diagnostics.model.IncidentType
import com.huojieren.apppause.data.diagnostics.model.ProcessState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 统一管理诊断材料的文件布局、轮转、保留和原子状态写入。
 *
 * 所有方法均在同一把锁下操作，避免运行日志轮转、清理和事故写入互相覆盖。
 */
@Singleton
class DiagnosticStore @Inject constructor(
    @ApplicationContext context: Context
) {
    companion object {
        private const val TAG = "DiagnosticStore"
        private const val ROOT_DIR_NAME = "diagnostics"
        private const val RUNTIME_DIR_NAME = "runtime"
        private const val INCIDENTS_DIR_NAME = "incidents"
        private const val STATE_DIR_NAME = "state"
        private const val RUNTIME_LOG_FILE_NAME = "app.log"
        private const val LATEST_STATE_FILE_NAME = "latest-state.txt"
        private const val HANDLED_EXITS_FILE_NAME = "handled-exits.txt"
        private const val LEGACY_LOG_ROOT_DIR_NAME = "logs"
        private const val LEGACY_REPORTS_DIR_NAME = "diagnostics"
        private const val LEGACY_RUNTIME_LOG_FILE_NAME = "app_logs.log"
        private val INCIDENT_FILE_TIMESTAMP =
            Regex("(?:java-crash|process-exit)-(\\d{8}-\\d{6}-\\d{3})")
    }

    private val lock = Any()

    /** 不参与 Android Auto Backup 的诊断根目录。 */
    val rootDir = File(context.noBackupFilesDir, ROOT_DIR_NAME)
    val runtimeDir = File(rootDir, RUNTIME_DIR_NAME)
    val incidentsDir = File(rootDir, INCIDENTS_DIR_NAME)
    private val stateDir = File(rootDir, STATE_DIR_NAME)
    val runtimeLogFile = File(runtimeDir, RUNTIME_LOG_FILE_NAME)
    private val latestStateFile = File(stateDir, LATEST_STATE_FILE_NAME)
    private val handledExitsFile = File(stateDir, HANDLED_EXITS_FILE_NAME)

    private val legacyCacheDir = context.cacheDir
    private val legacyLogRootDir = File(context.filesDir, LEGACY_LOG_ROOT_DIR_NAME)
    private val legacyReportsDir = File(context.filesDir, LEGACY_REPORTS_DIR_NAME)

    fun ensureDirectories() = synchronized(lock) {
        listOf(rootDir, runtimeDir, incidentsDir, stateDir).forEach { directory ->
            if (!directory.exists() && !directory.mkdirs()) {
                throw IllegalStateException("Cannot create diagnostics directory: ${directory.path}")
            }
        }
    }

    fun appendRuntimeLog(text: String) = synchronized(lock) {
        ensureDirectoriesLocked()
        rotateRuntimeLogsIfNeededLocked(text.toByteArray(StandardCharsets.UTF_8).size)
        runtimeLogFile.appendText(text, StandardCharsets.UTF_8)
    }

    fun writeIncident(fileName: String, content: String): File = synchronized(lock) {
        ensureDirectoriesLocked()
        val file = File(incidentsDir, fileName)
        writeAtomicallyLocked(file, content.toByteArray(StandardCharsets.UTF_8))
        trimIncidentsLocked()
        file
    }

    fun writeIncidentTrace(fileName: String, bytes: ByteArray): File = synchronized(lock) {
        ensureDirectoriesLocked()
        val file = File(incidentsDir, fileName)
        writeAtomicallyLocked(file, bytes)
        file
    }

    fun writeLatestState(state: ProcessState) = synchronized(lock) {
        ensureDirectoriesLocked()
        writeAtomicallyLocked(latestStateFile, state.toCompactText().toByteArray(StandardCharsets.UTF_8))
    }

    fun readLatestState(): String? = synchronized(lock) {
        latestStateFile.takeIf(File::exists)?.readText(StandardCharsets.UTF_8)
    }

    fun getHandledExitIds(): Set<String> = synchronized(lock) {
        handledExitsFile.takeIf(File::exists)
            ?.readLines(StandardCharsets.UTF_8)
            ?.filter(String::isNotBlank)
            ?.toSet()
            .orEmpty()
    }

    fun markExitHandled(exitId: String) = synchronized(lock) {
        ensureDirectoriesLocked()
        val ids = getHandledExitIds().toMutableList()
        ids.remove(exitId)
        ids.add(exitId)
        val retained = ids.takeLast(LogRetentionPolicy.HANDLED_EXIT_MAX_IDS)
        writeAtomicallyLocked(
            handledExitsFile,
            retained.joinToString(separator = "\n", postfix = "\n").toByteArray(StandardCharsets.UTF_8)
        )
    }

    fun getRuntimeLogFiles(): List<File> = synchronized(lock) {
        buildList {
            if (runtimeLogFile.exists()) add(runtimeLogFile)
            for (index in 1..LogRetentionPolicy.RUNTIME_MAX_BACKUP_FILES) {
                getRuntimeBackupFile(index).takeIf { it.exists() }?.let(::add)
            }
        }.sortedBy { it.name }
    }

    fun getIncidentFiles(): List<File> = synchronized(lock) {
        incidentsDir.listFiles { file -> file.isFile }?.sortedBy { it.name }.orEmpty()
    }

    /** 返回一个事故描述及其 trace、tombstone 等附件。 */
    fun getIncidentMaterialFiles(incidentId: String): List<File> = synchronized(lock) {
        incidentMaterialFilesLocked(incidentId)
    }

    fun getLegacyFiles(): List<File> = synchronized(lock) {
        buildList {
            File(legacyCacheDir, LEGACY_RUNTIME_LOG_FILE_NAME).takeIf { it.exists() }?.let(::add)
            for (index in 1..LogRetentionPolicy.RUNTIME_MAX_BACKUP_FILES) {
                File(legacyCacheDir, "$LEGACY_RUNTIME_LOG_FILE_NAME.$index")
                    .takeIf { it.exists() }
                    ?.let(::add)
            }
            legacyLogRootDir.walkTopDown().filter { it.isFile }.forEach(::add)
            legacyReportsDir.walkTopDown().filter { it.isFile }.forEach(::add)
        }.distinctBy { it.absolutePath }.sortedBy { it.name }
    }

    fun getDiagnosticIncidents(): List<DiagnosticIncident> = synchronized(lock) {
        getIncidentFiles()
            .asSequence()
            .filter { it.extension == "log" }
            .mapNotNull(::readIncidentSummary)
            .sortedByDescending(DiagnosticIncident::occurredAtEpochMs)
            .toList()
    }

    fun clearAll() = synchronized(lock) {
        getRuntimeLogFiles().forEach(File::delete)
        getIncidentFiles().forEach(File::delete)
        latestStateFile.delete()
        handledExitsFile.delete()
        getLegacyFiles().forEach(File::delete)
    }

    /** 删除单个事故及其关联附件，不影响滚动运行日志。 */
    fun deleteIncident(incidentId: String): Boolean = synchronized(lock) {
        val files = incidentMaterialFilesLocked(incidentId)
        files.isNotEmpty() && files.all(File::delete)
    }

    fun toZipEntryName(file: File): String = synchronized(lock) {
        when (file.parentFile) {
            runtimeDir -> "runtime/${file.name}"
            incidentsDir -> "incidents/${file.name}"
            legacyCacheDir -> "legacy/cache/${file.name}"
            legacyReportsDir -> "legacy/reports/${file.name}"
            else -> "legacy/${file.name}"
        }
    }

    private fun getRuntimeBackupFile(index: Int): File = File(runtimeDir, "$RUNTIME_LOG_FILE_NAME.$index")

    private fun incidentMaterialFilesLocked(incidentId: String): List<File> =
        getIncidentFiles().filter { file ->
            file.name == "$incidentId.log" || file.name.startsWith("$incidentId.")
        }

    private fun readIncidentSummary(file: File): DiagnosticIncident? = runCatching {
        val fields = file.useLines { lines ->
            lines
                .takeWhile(String::isNotBlank)
                .mapNotNull { line ->
                    line.split('=', limit = 2).takeIf { it.size == 2 }?.let { (key, value) -> key to value }
                }
                .toMap()
        }
        val type = fields["type"]?.let(IncidentType::valueOf) ?: return null
        val id = file.nameWithoutExtension
        DiagnosticIncident(
            id = id,
            type = type,
            occurredAtEpochMs = readOccurredAtEpochMs(file, fields),
            reason = fields["reason"],
            description = fields["description"],
            exceptionName = fields["exception"],
            message = fields["message"],
            hasTrace = incidentsDir.listFiles { candidate ->
                candidate.name.startsWith("$id.") && candidate.extension != "log"
            }?.isNotEmpty() == true
        )
    }.getOrNull()

    private fun readOccurredAtEpochMs(file: File, fields: Map<String, String>): Long =
        fields["occurredAtEpochMs"]?.toLongOrNull()
            ?: fields["exitTimestamp"]?.parseTimestamp("yyyy-MM-dd HH:mm:ss.SSS")
            ?: fields["createdAt"]?.parseTimestamp("yyyy-MM-dd HH:mm:ss.SSS")
            ?: INCIDENT_FILE_TIMESTAMP.find(file.name)?.groupValues?.get(1)
                ?.parseTimestamp("yyyyMMdd-HHmmss-SSS")
            ?: file.lastModified()

    private fun String.parseTimestamp(pattern: String): Long? = runCatching {
        SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(this)?.time
    }.getOrNull()

    private fun ensureDirectoriesLocked() {
        listOf(rootDir, runtimeDir, incidentsDir, stateDir).forEach { directory ->
            if (!directory.exists() && !directory.mkdirs()) {
                throw IllegalStateException("Cannot create diagnostics directory: ${directory.path}")
            }
        }
    }

    private fun rotateRuntimeLogsIfNeededLocked(nextWriteSize: Int) {
        if (!runtimeLogFile.exists() ||
            runtimeLogFile.length() + nextWriteSize < LogRetentionPolicy.RUNTIME_MAX_FILE_SIZE_BYTES
        ) {
            return
        }

        getRuntimeBackupFile(LogRetentionPolicy.RUNTIME_MAX_BACKUP_FILES).delete()
        for (index in LogRetentionPolicy.RUNTIME_MAX_BACKUP_FILES - 1 downTo 1) {
            val source = getRuntimeBackupFile(index)
            if (source.exists() && !source.renameTo(getRuntimeBackupFile(index + 1))) {
                Log.w(TAG, "Cannot rotate runtime log: ${source.name}")
            }
        }
        if (runtimeLogFile.exists() && !runtimeLogFile.renameTo(getRuntimeBackupFile(1))) {
            Log.w(TAG, "Cannot rotate current runtime log")
        }
    }

    private fun trimIncidentsLocked() {
        val files = getIncidentFiles()
            .filter { it.extension == "log" }
            .sortedByDescending { it.lastModified() }
        files.drop(LogRetentionPolicy.INCIDENT_MAX_FILES)
            .forEach { file -> incidentMaterialFilesLocked(file.nameWithoutExtension).forEach(File::delete) }
    }

    private fun writeAtomicallyLocked(file: File, bytes: ByteArray) {
        val temporaryFile = File(file.parentFile, "${file.name}.tmp")
        FileOutputStream(temporaryFile).use { output ->
            output.write(bytes)
            output.fd.sync()
        }
        if (file.exists() && !file.delete()) {
            throw IllegalStateException("Cannot replace diagnostics file: ${file.path}")
        }
        if (!temporaryFile.renameTo(file)) {
            throw IllegalStateException("Cannot finalize diagnostics file: ${file.path}")
        }
    }
}
