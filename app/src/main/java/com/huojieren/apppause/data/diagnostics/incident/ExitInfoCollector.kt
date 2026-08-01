package com.huojieren.apppause.data.diagnostics.incident

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.huojieren.apppause.data.diagnostics.storage.DiagnosticStore
import com.huojieren.apppause.data.logging.AppLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** 在新进程启动后收集上一次进程的系统退出记录。 */
@Singleton
class ExitInfoCollector @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val store: DiagnosticStore,
    private val incidentWriter: IncidentWriter
) {
    fun collect(limit: Int = DEFAULT_EXIT_REASON_LIMIT) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            AppLog.logger(TAG, "ApplicationExitInfo is unavailable below API 30", Log.INFO)
            return
        }
        collectApi30(limit)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun collectApi30(limit: Int) {
        runCatching {
            val activityManager = context.getSystemService(ActivityManager::class.java)
            val handledIds = store.getHandledExitIds()
            activityManager.getHistoricalProcessExitReasons(null, 0, limit)
                .asSequence()
                .filter { incidentWriter.exitId(it) !in handledIds }
                .forEach { exitInfo ->
                    if (incidentWriter.writeProcessExit(exitInfo)) {
                        store.markExitHandled(incidentWriter.exitId(exitInfo))
                    }
                }
        }.onFailure {
            Log.e(TAG, "Collect historical process exit reasons failed", it)
        }
    }

    companion object {
        private const val TAG = "ExitInfoCollector"
        private const val DEFAULT_EXIT_REASON_LIMIT = 20
    }
}
