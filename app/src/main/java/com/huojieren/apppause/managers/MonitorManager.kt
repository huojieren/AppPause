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
    private var lastLoggedApp: String? = null // 上次记录的应用
    private var lastLogTime: Long? = null // 上次日志时间
    private var lastInvalidLogTime: Long? = null // 上次无效应用日志时间
    private val logCooldownMs = 1000L // 冷却时间：1秒内同一应用不重复日志

    fun setOnAppChangedListener(listener: (AppInfo?) -> Unit) {
        logRepository.log(tag, "set on app changed listener")
        onAppChanged = listener
    }

    fun startMonitor() {
        logRepository.log(tag, "start monitor")

        // 检查无障碍服务是否初始化
        if (!AppPauseAccessibilityService.isInitialized()) {
            throw IllegalStateException("无障碍服务未初始化，请先开启无障碍服务权限")
        }

        scope.launch {
            monitoredApps = dataStoreRepository.getMonitoredApps().first().toSet()
        }

        try {
            // 设置监控开始时间
            statusManager.setMonitorStartTime()

            val intent = Intent(context, MonitorService::class.java)
            // TODO 2025/11/30 21:55 监控策略切换
            intent.putExtra("strategy", MonitorStrategy.ACCESSIBILITY.name)
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            logRepository.log(tag, "Failed to start MonitorService: ${e.message}")
            throw IllegalStateException("启动监控服务失败：${e.message}", e)
        }
    }

    fun stopMonitor() {
        logRepository.log(tag, "stop monitor")
        context.stopService(
            Intent(context, MonitorService::class.java)
        )
        currentApp = null
        // 清除监控开始时间（正常停止）
        statusManager.clearMonitorStartTime()
    }

    fun handleAppChange(app: AppInfo?) {
        val packageName = app?.packageName ?: "no app info"
        val currentTime = System.currentTimeMillis()

        // 防重复日志
        if (packageName != lastLoggedApp ||
            (currentTime - (lastLogTime ?: 0) > logCooldownMs)
        ) {
            logRepository.log(tag, app?.let { "change to app: ${it.name}" } ?: "no app info")
            lastLoggedApp = packageName
            lastLogTime = currentTime
        }

        // 防重复日志
        if (!isValidApp(app)) {
            if (packageName != lastLoggedApp ||
                (currentTime - (lastInvalidLogTime ?: 0) > logCooldownMs)
            ) {
                logRepository.log(tag, "invalid app")
                lastInvalidLogTime = currentTime
            }
            return
        }

        // 检查是否是同一个应用
        if (currentApp?.packageName != app?.packageName) {
            // 切换到不同应用时，暂停上一个
            currentApp?.let { timerManager.pause(it.packageName) }
        }
        currentApp = app!!

        val remaining = timerManager.getRemainingTime(app)
        if (remaining > 0) {
            logRepository.log(tag, "${app.packageName} continue counting")
            // 检查计时器是否正在运行，如果没有则重新启动
            // 这样可以避免刚设置的倒计时被意外暂停
            logRepository.log(
                tag,
                "[MONITOR] Continuing existing timer for ${app.packageName}, remaining: ${remaining}ms"
            )
        } else {
            // 通知外部监听器，开始新的倒计时
            logRepository.log(tag, "${app.packageName} start new counting")
            onAppChanged?.invoke(app)
        }
    }

    private fun isValidApp(app: AppInfo?): Boolean {
        return app != null &&
                app.packageName.isNotEmpty() &&
                !app.packageName.startsWith("com.huojieren.apppause") &&
                monitoredApps.contains(app)
    }
}