package com.huojieren.apppause.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huojieren.apppause.data.Permissions
import com.huojieren.apppause.data.logging.AppLog.logger
import com.huojieren.apppause.data.diagnostics.DiagnosticsManager
import com.huojieren.apppause.data.diagnostics.model.ExportResult
import com.huojieren.apppause.data.repository.SettingsRepository
import com.huojieren.apppause.managers.MonitorManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.managers.StatusManager
import com.huojieren.apppause.managers.TimerManager
import com.huojieren.apppause.ui.state.AppStatusUiState
import com.huojieren.apppause.ui.state.DiagnosticsUiState
import com.huojieren.apppause.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val diagnosticsManager: DiagnosticsManager,
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

    private val _diagnosticsUiState = MutableStateFlow(DiagnosticsUiState())
    val diagnosticsUiState = _diagnosticsUiState.asStateFlow()

    init {
        logger(tag, "AppStatusViewModel init")
        refreshState()
        refreshDiagnostics()
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
        val isGranted = when (permission) {
            Permissions.Overlay -> statusManager.hasOverlay.value
            Permissions.Notification -> statusManager.hasNotification.value
            Permissions.UsageStats -> statusManager.hasUsageStats.value
            Permissions.Accessibility -> statusManager.hasAccessibility.value
            Permissions.BatteryOptimization -> statusManager.hasBatteryOptimizationExemption.value
        }
        if (isGranted) {
            showToast(appContext, "已获取")
        } else {
            permissionManager.requestPermission(permission)
        }
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
        if (diagnosticsManager.clear()) {
            refreshDiagnostics()
            showToast(context, "日志已清空")
        } else {
            showToast(context, "清空日志失败")
        }
    }

    fun refreshDiagnostics() {
        _diagnosticsUiState.value = DiagnosticsUiState(diagnosticsManager.getDiagnosticIncidents())
    }

    fun saveLog() {
        when (val result = diagnosticsManager.export()) {
            ExportResult.Success -> showToast(context, "诊断包已保存到：Download/AppPause")
            ExportResult.NoLogs -> showToast(context, "没有诊断材料可保存")
            is ExportResult.Failed -> {
                logger(tag, "Export diagnostics failed: ${result.error.message}", android.util.Log.ERROR, result.error)
                showToast(context, "保存诊断包失败")
            }
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
