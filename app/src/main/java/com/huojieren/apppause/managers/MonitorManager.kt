package com.huojieren.apppause.managers

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.repository.AppRepository
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.data.repository.SettingsRepository
import com.huojieren.apppause.monitor.ForegroundAppMonitor.MonitorStrategy
import com.huojieren.apppause.service.AppPauseAccessibilityService
import com.huojieren.apppause.service.MonitorService
import kotlinx.coroutines.flow.first
import javax.inject.Singleton

@Singleton
class MonitorManager(
    private val context: Context,
    private val appRepository: AppRepository,
    private val settingsRepository: SettingsRepository,
    private val timerManager: TimerManager,
    private val statusManager: StatusManager
) {
    private val tag = "MonitorManager"
    private var monitoredPackages = setOf<String>()
    private var currentApp: AppInfo? = null
    private var onAppChanged: ((AppInfo?) -> Unit)? = null

    // 最后一次有效应用（用于通知显示）
    private var lastValidApp: AppInfo? = null
    private var lastRemainingTime: Long = 0

    fun setOnAppChangedListener(listener: (AppInfo?) -> Unit) {
        logger(tag, "set on app changed listener")
        onAppChanged = listener
    }

    fun resetCurrentAppTracking() {
        logger(tag, "resetCurrentAppTracking")
        currentApp = null
        lastValidApp = null
        lastRemainingTime = 0
    }

    suspend fun startMonitor() {
        logger(tag, "startMonitor called")

        // 检查无障碍服务是否初始化
        if (!AppPauseAccessibilityService.isInitialized()) {
            throw IllegalStateException("Accessibility service not initialized, please enable it first")
        }

        timerManager.setPerAppTimingEnabled(
            settingsRepository.getPerAppTimingEnabled().first(),
            clearTimers = false
        )

        // 清空所有倒计时，保证新的监控周期
        timerManager.clearAllTimers()

        monitoredPackages = appRepository.getMonitoredAppsOnce()
            .map { it.packageName }
            .toSet()

        try {
            statusManager.setIsMonitoring(true)
            logger(tag, "startMonitor: setIsMonitoring(true)")

            val intent = Intent(context, MonitorService::class.java)
            // TODO 2025/11/30 21:55 监控策略切换
            intent.putExtra("strategy", MonitorStrategy.ACCESSIBILITY.name)
            logger(tag, "startMonitor: calling startForegroundService")
            ContextCompat.startForegroundService(context, intent)
            logger(tag, "startMonitor: foreground service started, returning")
        } catch (e: Exception) {
            logger(tag, "Failed to start MonitorService: ${e.message}")
            throw IllegalStateException("Failed to start MonitorService：${e.message}", e)
        }
    }

    fun stopMonitor() {
        logger(tag, "stopMonitor called")

        // 清空所有倒计时
        timerManager.clearAllTimers()
        
        context.stopService(
            Intent(context, MonitorService::class.java)
        )
        currentApp = null
        statusManager.setIsMonitoring(false)
        logger(tag, "stopMonitor: setIsMonitoring(false)")
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
        logger(tag, "app changed: [${previousApp?.name}] -> [${app?.name ?: "null"}]")

        // 切换到无效应用时，暂停上一个被监控应用的计时器
        if (!isValidApp(app)) {
            logger(tag, "invalid app: [${packageName ?: "null"}], paused timer")
            previousApp?.let { app ->
                val remaining = timerManager.getRemainingTime(app)
                logger(
                    tag,
                    "pause timer for [${app.packageName}], current remaining: ${remaining}ms"
                )
                timerManager.pause(app.packageName)
                lastValidApp = app
                lastRemainingTime = remaining
                logger(tag, "saved last valid app: [${app.name}], remaining: ${remaining}ms")
            }
            return
        }

        val validApp = app!!

        // 切换到不同应用时，暂停上一个
        previousApp?.let {
            logger(
                tag,
                "pause timer for [${it.packageName}], current remaining: ${
                    timerManager.getRemainingTime(it)
                }ms"
            )
            timerManager.pause(it.packageName)
        }

        val remaining = timerManager.getRemainingTime(validApp)
        if (remaining > 0) {
            // 检查是否已经在运行，避免重复开始
            if (timerManager.isTimerRunning(validApp.packageName)) {
                logger(
                    tag,
                    "[${validApp.packageName}] timer already running, skipping"
                )
                return
            }
            logger(
                tag,
                "[${validApp.packageName}] continue counting, remaining: ${remaining / 1000}s"
            )
            timerManager.start(validApp)
// 更新通知状态为有效
            lastValidApp = validApp
            logger(tag, "start new timer, app: [${validApp.name}]")
        } else {
            logger(tag, "[${validApp.packageName}] start new counting")
            onAppChanged?.invoke(validApp)
            // 更新通知状态为有效
            lastValidApp = validApp
            logger(tag, "start new timer, app: [${validApp.name}]")
        }
    }

    private fun isValidApp(app: AppInfo?): Boolean {
        return app != null &&
                app.packageName.isNotEmpty() &&
                !app.packageName.startsWith("com.huojieren.apppause") &&
                monitoredPackages.contains(app.packageName)
    }
}
