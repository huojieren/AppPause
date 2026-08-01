package com.huojieren.apppause.data.diagnostics.incident

import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresApi
import com.huojieren.apppause.data.diagnostics.model.IncidentType
import com.huojieren.apppause.data.diagnostics.storage.DiagnosticStore
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** 写入独立事故文件；仅 Java crash 与系统记录的进程退出会创建 incident。 */
@Singleton
class IncidentWriter @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val store: DiagnosticStore
) {
    fun writeJavaCrash(thread: Thread, throwable: Throwable) {
        val occurredAtEpochMs = System.currentTimeMillis()
        runCatching {
            val incidentId = newIncidentId(IncidentType.JAVA_CRASH, Process.myPid(), occurredAtEpochMs)
            store.writeIncident(
                "$incidentId.log",
                buildString {
                    appendLine("type=${IncidentType.JAVA_CRASH.name}")
                    appendLine("occurredAtEpochMs=$occurredAtEpochMs")
                    appendLine("createdAt=${formatTimestamp(System.currentTimeMillis())}")
                    appendProcessContext()
                    appendLine("thread=${thread.name}")
                    appendLine("exception=${throwable::class.java.name}")
                    appendLine("message=${throwable.message.orEmpty()}")
                    appendLine("lastState=${store.readLatestState().orEmpty()}")
                    appendLine()
                    append(Log.getStackTraceString(throwable))
                }
            )
        }.onFailure {
            Log.e("IncidentWriter", "Save Java crash incident failed", it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun writeProcessExit(exitInfo: ApplicationExitInfo): Boolean = runCatching {
        val incidentId = newIncidentId(IncidentType.PROCESS_EXIT, exitInfo.pid, exitInfo.timestamp)
        store.writeIncident(
            "$incidentId.log",
            buildString {
                appendLine("type=${IncidentType.PROCESS_EXIT.name}")
                appendLine("occurredAtEpochMs=${exitInfo.timestamp}")
                appendLine("createdAt=${formatTimestamp(System.currentTimeMillis())}")
                appendLine("exitTimestamp=${formatTimestamp(exitInfo.timestamp)}")
                appendLine("package=${context.packageName}")
                appendLine("pid=${exitInfo.pid}")
                appendLine("processName=${exitInfo.processName}")
                appendLine("reason=${reasonToString(exitInfo.reason)}(${exitInfo.reason})")
                appendLine("status=${exitInfo.status}")
                appendLine("importance=${exitInfo.importance}")
                appendLine("pssKb=${exitInfo.pss}")
                appendLine("rssKb=${exitInfo.rss}")
                appendLine("description=${exitInfo.description.orEmpty()}")
                appendLine("processStateSummary=${exitInfo.processStateSummary?.toString(Charsets.UTF_8).orEmpty()}")
                appendLine("lastState=${store.readLatestState().orEmpty()}")
            }
        )
        exitInfo.traceInputStream?.use { trace ->
            store.writeIncidentTrace(
                "$incidentId.${traceFileExtension(exitInfo)}",
                trace.readFully()
            )
        }
        true
    }.getOrElse {
        Log.e("IncidentWriter", "Save process exit incident failed", it)
        false
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun exitId(exitInfo: ApplicationExitInfo): String =
        "${exitInfo.timestamp}-${exitInfo.pid}-${exitInfo.reason}-${exitInfo.processName}"

    private fun StringBuilder.appendProcessContext() {
        appendLine("package=${context.packageName}")
        appendLine("pid=${Process.myPid()}")
        appendLine("apiLevel=${Build.VERSION.SDK_INT}")
    }

    private fun newIncidentId(type: IncidentType, pid: Int, timestamp: Long = System.currentTimeMillis()): String {
        val timestampText = SimpleDateFormat("yyyyMMdd-HHmmss-SSS", Locale.US).format(Date(timestamp))
        return "${type.filePrefix}-$timestampText-$pid-${UUID.randomUUID().toString().take(8)}"
    }

    private fun formatTimestamp(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date(timestamp))

    @RequiresApi(Build.VERSION_CODES.R)
    private fun traceFileExtension(exitInfo: ApplicationExitInfo): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            exitInfo.reason == ApplicationExitInfo.REASON_CRASH_NATIVE
        ) {
            "tombstone.pb"
        } else {
            "trace.txt"
        }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun reasonToString(reason: Int): String = when (reason) {
        ApplicationExitInfo.REASON_ANR -> "ANR"
        ApplicationExitInfo.REASON_CRASH -> "CRASH"
        ApplicationExitInfo.REASON_CRASH_NATIVE -> "CRASH_NATIVE"
        ApplicationExitInfo.REASON_DEPENDENCY_DIED -> "DEPENDENCY_DIED"
        ApplicationExitInfo.REASON_EXCESSIVE_RESOURCE_USAGE -> "EXCESSIVE_RESOURCE_USAGE"
        ApplicationExitInfo.REASON_EXIT_SELF -> "EXIT_SELF"
        ApplicationExitInfo.REASON_INITIALIZATION_FAILURE -> "INITIALIZATION_FAILURE"
        ApplicationExitInfo.REASON_LOW_MEMORY -> "LOW_MEMORY"
        ApplicationExitInfo.REASON_OTHER -> "OTHER"
        ApplicationExitInfo.REASON_PERMISSION_CHANGE -> "PERMISSION_CHANGE"
        ApplicationExitInfo.REASON_SIGNALED -> "SIGNALED"
        ApplicationExitInfo.REASON_UNKNOWN -> "UNKNOWN"
        ApplicationExitInfo.REASON_USER_REQUESTED -> "USER_REQUESTED"
        ApplicationExitInfo.REASON_USER_STOPPED -> "USER_STOPPED"
        ApplicationExitInfo.REASON_FREEZER -> "FREEZER"
        ApplicationExitInfo.REASON_PACKAGE_STATE_CHANGE -> "PACKAGE_STATE_CHANGE"
        ApplicationExitInfo.REASON_PACKAGE_UPDATED -> "PACKAGE_UPDATED"
        else -> "UNRECOGNIZED"
    }

    private fun InputStream.readFully(): ByteArray = ByteArrayOutputStream().use { output ->
        copyTo(output)
        output.toByteArray()
    }
}
