package com.huojieren.apppause.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huojieren.apppause.data.Permissions
import com.huojieren.apppause.data.repository.LogRepository
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.data.repository.SettingsRepository
import com.huojieren.apppause.managers.MonitorManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.managers.StatusManager
import com.huojieren.apppause.managers.TimerManager
import com.huojieren.apppause.ui.state.AppStatusUiState
import com.huojieren.apppause.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

private data class PermissionFlags(
    val hasOverlay: Boolean,
    val hasNotification: Boolean,
    val hasUsageStats: Boolean,
    val hasAccessibility: Boolean,
    val hasBatteryOptimizationExemption: Boolean
)

@HiltViewModel
class AppStatusViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
    private val logRepository: LogRepository,
    private val settingsRepository: SettingsRepository,
    private val monitorManager: MonitorManager,
    private val statusManager: StatusManager,
    private val timerManager: TimerManager
) : ViewModel() {
    private val tag = "AppStatusViewModel"
    private val appContext = context.applicationContext

    private val monitorState = combine(
        statusManager.isMonitoring,
        settingsRepository.getMonitorIntent()
    ) { isMonitoring, monitorIntent ->
        isMonitoring to monitorIntent
    }

    private val permissionFlags = combine(
        statusManager.hasOverlay,
        statusManager.hasNotification,
        statusManager.hasUsageStats,
        statusManager.hasAccessibility,
        statusManager.hasBatteryOptimizationExemption
    ) { hasOverlay, hasNotification, hasUsageStats, hasAccessibility, hasBatteryOptimizationExemption ->
        PermissionFlags(
            hasOverlay = hasOverlay,
            hasNotification = hasNotification,
            hasUsageStats = hasUsageStats,
            hasAccessibility = hasAccessibility,
            hasBatteryOptimizationExemption = hasBatteryOptimizationExemption
        )
    }

    private val permissionState = combine(
        monitorState,
        permissionFlags
    ) { monitorState, permissionFlags ->
        AppStatusUiState(
            isMonitoring = monitorState.first,
            monitorIntent = monitorState.second,
            hasOverlay = permissionFlags.hasOverlay,
            hasNotification = permissionFlags.hasNotification,
            hasUsageStats = permissionFlags.hasUsageStats,
            hasAccessibility = permissionFlags.hasAccessibility,
            hasBatteryOptimizationExemption = permissionFlags.hasBatteryOptimizationExemption
        )
    }

    val uiState = combine(
        permissionState,
        settingsRepository.getSharedTimingEnabled(),
        settingsRepository.getWaitBeforeReturnEnabled(),
        settingsRepository.getWaitBeforeReturnSeconds(),
        settingsRepository.getTodoPromptEnabled()
    ) { state, isSharedTimingEnabled, isWaitBeforeReturnEnabled, waitBeforeReturnSeconds, isTodoPromptEnabled ->
        state.copy(
            isSharedTimingEnabled = isSharedTimingEnabled,
            isWaitBeforeReturnEnabled = isWaitBeforeReturnEnabled,
            waitBeforeReturnSeconds = waitBeforeReturnSeconds,
            isTodoPromptEnabled = isTodoPromptEnabled
        )
    }

    init {
        logger(tag, "AppStatusViewModel init")
        refreshState()
    }

    fun refreshState() {
        logger(tag, "refreshState")
        viewModelScope.launch {
            statusManager.setHasOverlay(permissionManager.refreshPermission(Permissions.Overlay))
            statusManager.setHasNotification(permissionManager.refreshPermission(Permissions.Notification))
            statusManager.setHasUsageStats(permissionManager.refreshPermission(Permissions.UsageStats))
            statusManager.setHasAccessibility(permissionManager.refreshPermission(Permissions.Accessibility))
            statusManager.setHasBatteryOptimizationExemption(
                permissionManager.refreshPermission(Permissions.BatteryOptimization)
            )
        }
    }

    fun requestPermission(permission: Permissions) {
        logger(tag, "requestPermission $permission")
        permissionManager.requestPermission(permission)
    }

    fun setSharedTimingEnabled(enabled: Boolean) {
        logger(tag, "setSharedTimingEnabled: $enabled")
        timerManager.setPerAppTimingEnabled(!enabled, clearTimers = true)
        monitorManager.resetCurrentAppTracking()
        viewModelScope.launch {
            settingsRepository.setSharedTimingEnabled(enabled)
        }
    }

    fun setWaitBeforeReturnEnabled(enabled: Boolean) {
        logger(tag, "setWaitBeforeReturnEnabled: $enabled")
        viewModelScope.launch {
            settingsRepository.setWaitBeforeReturnEnabled(enabled)
            timerManager.refreshSettings()
        }
    }

    fun setWaitBeforeReturnSeconds(seconds: Int) {
        logger(tag, "setWaitBeforeReturnSeconds: $seconds")
        viewModelScope.launch {
            settingsRepository.setWaitBeforeReturnSeconds(seconds.coerceIn(1, 99))
            timerManager.refreshSettings()
        }
    }

    fun setTodoPromptEnabled(enabled: Boolean) {
        logger(tag, "setTodoPromptEnabled: $enabled")
        viewModelScope.launch {
            settingsRepository.setTodoPromptEnabled(enabled)
            timerManager.refreshSettings()
        }
    }

    fun clearLog() {
        if (logRepository.clearLog()) {
            showToast(context, "日志已清空")
        } else {
            showToast(context, "清空日志失败")
        }
    }

    fun saveLog() {
        when (logRepository.saveLog()) {
            0 -> showToast(context, "日志已保存到：Download/App Pause/app_logs.zip")
            1 -> showToast(context, "没有日志可保存")
            -1 -> showToast(context, "保存日志失败")
        }
    }

    fun toggleMonitoring() {
        logger(tag, "toggleMonitoring")
        viewModelScope.launch {
            if (statusManager.isMonitoring.value) {
                monitorManager.stopMonitor()
                showToast(appContext, "已停止监控")
                statusManager.setIsMonitoring(false)
            } else if (statusManager.hasOverlay.value &&
                statusManager.hasNotification.value &&
                statusManager.hasUsageStats.value &&
                statusManager.hasAccessibility.value
            ) {
                try {
                    monitorManager.startMonitor()
                    showToast(appContext, "已开始监控")
                } catch (e: Exception) {
                    logger(tag, "Failed to start monitoring: ${e.message}")
                    showToast(appContext, "启动监控失败：${e.message}")
                    refreshState()
                }
            } else {
                when {
                    !statusManager.hasOverlay.value -> showToast(appContext, "请先授予悬浮窗权限")
                    !statusManager.hasNotification.value -> showToast(appContext, "请先授予通知权限")
                    !statusManager.hasUsageStats.value -> showToast(appContext, "请先授予使用统计权限")
                    !statusManager.hasAccessibility.value -> showToast(appContext, "请先授予无障碍服务权限")
                    else -> showToast(appContext, "请先授予所有权限")
                }
            }
        }
    }
}
