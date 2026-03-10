package com.huojieren.apppause.managers

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.repository.DataStoreRepository
import com.huojieren.apppause.data.repository.LogRepository
import com.huojieren.apppause.monitor.ForegroundAppMonitor.MonitorStrategy
import com.huojieren.apppause.service.AppPauseAccessibilityService
import com.huojieren.apppause.service.MonitorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class MonitorManager(
    private val context: Context,
    private val dataStoreRepository: DataStoreRepository,
    private val logRepository: LogRepository,
    private val timerManager: TimerManager,
    private val statusManager: StatusManager
) {
    private val tag = "MonitorManager"
    private val scope = CoroutineScope(Dispatchers.Main)
    private var monitoredApps = setOf<AppInfo>()
    private var currentApp: AppInfo? = null
    private var onAppChanged: ((AppInfo?) -> Unit)? = null

    fun setOnAppChangedListener(listener: (AppInfo?) -> Unit) {
        logRepository.log(tag, "set on app changed listener")
        onAppChanged = listener
    }

    fun startMonitor() {
        logRepository.log(tag, "startMonitor called")

        // 检查无障碍服务是否初始化
        if (!AppPauseAccessibilityService.isInitialized()) {
            throw IllegalStateException("Accessibility service not initialized, please enable it first")
        }

        scope.launch {
            monitoredApps = dataStoreRepository.getMonitoredApps().first().toSet()
        }

        try {
            statusManager.setIsMonitoring(true)
            logRepository.log(tag, "startMonitor: setIsMonitoring(true)")

            val intent = Intent(context, MonitorService::class.java)
            // TODO 2025/11/30 21:55 监控策略切换
            intent.putExtra("strategy", MonitorStrategy.ACCESSIBILITY.name)
            ContextCompat.startForegroundService(context, intent)
            logRepository.log(tag, "startMonitor: foreground service started")
        } catch (e: Exception) {
            logRepository.log(tag, "Failed to start MonitorService: ${e.message}")
            throw IllegalStateException("Failed to start MonitorService：${e.message}", e)
        }
    }

    fun stopMonitor() {
        logRepository.log(tag, "stopMonitor called")
        context.stopService(
            Intent(context, MonitorService::class.java)
        )
        currentApp = null
        statusManager.setIsMonitoring(false)
        logRepository.log(tag, "stopMonitor: setIsMonitoring(false)")
    }

    fun handleAppChange(app: AppInfo?) {
        val packageName = app?.packageName

        // 同一应用且非首次调用，不做任何处理
        if (packageName == currentApp?.packageName) {
            return
        }

        val previousApp = currentApp
        currentApp = app

        // 日志输出
        logRepository.log(tag, "app changed: [${previousApp?.name}] -> [${app?.name ?: "null"}]")

        // 切换到无效应用时，暂停上一个被监控应用的计时器
        if (!isValidApp(app)) {
            logRepository.log(tag, "invalid app: [${packageName ?: "null"}], paused timer")
            previousApp?.let {
                timerManager.pause(it.packageName)
            }
            return
        }

        val validApp = app!!

        // 切换到不同应用时，暂停上一个
        previousApp?.let {
            timerManager.pause(it.packageName)
        }

        val remaining = timerManager.getRemainingTime(validApp)
        if (remaining > 0) {
            logRepository.log(
                tag,
                "[${validApp.packageName}] continue counting, remaining: ${remaining / 1000}s"
            )
            timerManager.start(validApp)
        } else {
            logRepository.log(tag, "[${validApp.packageName}] start new counting")
            onAppChanged?.invoke(validApp)
        }
    }

    private fun isValidApp(app: AppInfo?): Boolean {
        return app != null &&
                app.packageName.isNotEmpty() &&
                !app.packageName.startsWith("com.huojieren.apppause") &&
                monitoredApps.contains(app)
    }
}