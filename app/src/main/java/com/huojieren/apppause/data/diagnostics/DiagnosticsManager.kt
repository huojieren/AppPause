package com.huojieren.apppause.data.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.data.diagnostics.export.DiagnosticsExporter
import com.huojieren.apppause.data.diagnostics.incident.ExitInfoCollector
import com.huojieren.apppause.data.diagnostics.incident.IncidentWriter
import com.huojieren.apppause.data.diagnostics.model.DiagnosticEvent
import com.huojieren.apppause.data.diagnostics.model.DiagnosticIncident
import com.huojieren.apppause.data.diagnostics.model.ExportResult
import com.huojieren.apppause.data.diagnostics.model.ProcessState
import com.huojieren.apppause.data.diagnostics.runtime.RuntimeLogTree
import com.huojieren.apppause.data.diagnostics.storage.DiagnosticStore
import com.huojieren.apppause.data.diagnostics.storage.LogRetentionPolicy
import com.huojieren.apppause.data.logging.AppLog
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * 诊断系统的唯一公共入口。
 *
 * 运行事件写入滚动 journal；只有 Java crash 和系统进程退出会写为独立 incident。
 */
@Singleton
class DiagnosticsManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val store: DiagnosticStore,
    private val incidentWriter: IncidentWriter,
    private val exitInfoCollector: ExitInfoCollector,
    private val exporter: DiagnosticsExporter
) {
    private val runtimeWriter = ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        ArrayBlockingQueue(LogRetentionPolicy.RUNTIME_QUEUE_CAPACITY),
        ThreadPoolExecutor.DiscardOldestPolicy()
    )
    private val diagnosticsWorker = ThreadPoolExecutor(
        1,
        1,
        0L,
        TimeUnit.MILLISECONDS,
        ArrayBlockingQueue(1),
        ThreadPoolExecutor.DiscardPolicy()
    )

    @Volatile
    private var initialized = false

    fun initialize() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            store.ensureDirectories()
            if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
            Timber.plant(RuntimeLogTree(store, runtimeWriter))
            installCrashHandler()
            initialized = true
            recordEvent(DiagnosticEvent("process_started", mapOf("apiLevel" to Build.VERSION.SDK_INT.toString())))
        }
    }

    /** 异步收集，以免 Application.onCreate 因读取 trace 或写文件阻塞。 */
    fun collectHistoricalExitInfo() {
        diagnosticsWorker.execute { exitInfoCollector.collect() }
    }

    /**
     * 记录一次可供后续排查的运行时事件，并将其属性附加到日志中。
     */
    fun recordEvent(event: DiagnosticEvent) {
        val details = event.attributes.entries.joinToString(separator = " ") { (key, value) -> "$key=$value" }
        AppLog.logger("Diagnostics", "event=${event.name}${if (details.isBlank()) "" else " $details"}", Log.INFO)
    }

    /**
     * 保存当前进程的最新运行状态，供下次启动时关联历史退出原因和事故信息。
     */
    fun updateProcessState(state: ProcessState) {
        runCatching {
            store.writeLatestState(state)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bytes = state.toCompactText().toByteArray(Charsets.UTF_8).take(128).toByteArray()
                context.getSystemService(ActivityManager::class.java).setProcessStateSummary(bytes)
            }
        }.onFailure {
            Log.e(TAG, "Update process state failed", it)
        }
    }

    fun clear(): Boolean = runCatching {
        store.clearAll()
        true
    }.getOrElse {
        AppLog.logger(TAG, "Clear diagnostics failed: ${it.message}", Log.ERROR, it)
        false
    }

    fun getDiagnosticIncidents(): List<DiagnosticIncident> = store.getDiagnosticIncidents()

    fun deleteIncident(incidentId: String): Boolean = store.deleteIncident(incidentId)

    fun export(): ExportResult = exporter.export()

    fun exportIncident(incidentId: String): ExportResult = exporter.exportIncident(incidentId)

    private fun installCrashHandler() {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        if (previous is CrashIncidentHandler) return
        Thread.setDefaultUncaughtExceptionHandler(CrashIncidentHandler(previous))
    }

    private inner class CrashIncidentHandler(
        private val delegate: Thread.UncaughtExceptionHandler?
    ) : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(thread: Thread, throwable: Throwable) {
            incidentWriter.writeJavaCrash(thread, throwable)
            delegate?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "DiagnosticsManager"
    }
}
