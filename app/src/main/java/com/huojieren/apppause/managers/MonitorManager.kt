package com.huojieren.apppause.managers

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.repository.DataStoreRepository
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.monitor.ForegroundAppMonitor.MonitorStrategy
import com.huojieren.apppause.service.AppPauseAccessibilityService
import com.huojieren.apppause.service.MonitorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Singleton
class MonitorManager(
    private val context: Context,
    private val dataStoreRepository: DataStoreRepository,
    private val timerManager: TimerManager,
    private val statusManager: StatusManager
) {
    private val tag = "MonitorManager"
    private val scope = CoroutineScope(Dispatchers.Main)
    private var monitoredApps = setOf<AppInfo>()
    private var currentApp: AppInfo? = null
    private var onAppChanged: ((AppInfo?) -> Unit)? = null

    // 最后一次有效应用（用于通知显示）
    private var lastValidApp: AppInfo? = null
    private var lastRemainingTime: Long = 0

    // 通知显示状态
    private val _notificationState = MutableStateFlow(NotificationDisplayState())
    val notificationState: StateFlow<NotificationDisplayState> = _notificationState.asStateFlow()

    /**
     * 通知显示状态
     */
    data class NotificationDisplayState(
        val appName: String? = null,
        val remainingTimeMs: Long = 0,
        val isValid: Boolean = false  // 当前是否有效应用
    )

    fun setOnAppChangedListener(listener: (AppInfo?) -> Unit) {
        logger(tag, "set on app changed listener")
        onAppChanged = listener
    }

    fun startMonitor() {
        logger(tag, "startMonitor called")

        // 检查无障碍服务是否初始化
        if (!AppPauseAccessibilityService.isInitialized()) {
            throw IllegalStateException("Accessibility service not initialized, please enable it first")
        }

        // 清空所有倒计时，保证新的监控周期
        timerManager.clearAllTimers()

        scope.launch {
            monitoredApps = dataStoreRepository.getMonitoredApps().first().toSet()
        }

        try {
            statusManager.setIsMonitoring(true)
            logger(tag, "startMonitor: setIsMonitoring(true)")

            val intent = Intent(context, MonitorService::class.java)
            // TODO 2025/11/30 21:55 监控策略切换
            intent.putExtra("strategy", MonitorStrategy.ACCESSIBILITY.name)
            ContextCompat.startForegroundService(context, intent)
            logger(tag, "startMonitor: foreground service started")
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
            previousApp?.let {
                logger(
                    tag,
                    "pause timer for [${it.packageName}], current remaining: ${
                        timerManager.getRemainingTime(it)
                    }ms"
                )
                timerManager.pause(it.packageName)
                // 保存最后一次有效应用信息用于通知显示
                lastValidApp = it
                lastRemainingTime = timerManager.getRemainingTime(it)
                logger(
                    tag,
                    "saved last valid app: [${it.name}], remaining: ${lastRemainingTime}ms"
                )
                updateNotificationState(it.name, lastRemainingTime, false)
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
            lastRemainingTime = remaining
            logger(
                tag,
                "resume timer, app: [${validApp.name}], remaining: ${remaining}ms"
            )
            updateNotificationState(validApp.name, remaining, true)
        } else {
            logger(tag, "[${validApp.packageName}] start new counting")
            onAppChanged?.invoke(validApp)
            // 更新通知状态为有效
            lastValidApp = validApp
            logger(tag, "start new timer, app: [${validApp.name}]")
            updateNotificationState(validApp.name, 0, true)
        }
    }

    private fun updateNotificationState(appName: String, remainingMs: Long, isValid: Boolean) {
        _notificationState.value = NotificationDisplayState(
            appName = appName,
            remainingTimeMs = remainingMs,
            isValid = isValid
        )
    }

    private fun isValidApp(app: AppInfo?): Boolean {
        return app != null &&
                app.packageName.isNotEmpty() &&
                !app.packageName.startsWith("com.huojieren.apppause") &&
                monitoredApps.contains(app)
    }
}