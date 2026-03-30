package com.huojieren.apppause.ui.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huojieren.apppause.data.Permissions
import com.huojieren.apppause.data.repository.LogRepository
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.managers.MonitorManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.managers.StatusManager
import com.huojieren.apppause.ui.state.MainScreenUiState
import com.huojieren.apppause.utils.showToast
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager,
    private val logRepository: LogRepository,
    private val monitorManager: MonitorManager,
    private val statusManager: StatusManager
) : ViewModel() {
    private val tag = "MainScreenViewModel"
    private val appContext = context.applicationContext

    val uiState = combine(
        statusManager.isMonitoring,
        statusManager.hasOverlay,
        statusManager.hasNotification,
        statusManager.hasUsageStats,
        statusManager.hasAccessibility
    ) { isMonitoring, hasOverlay, hasNotification, hasUsageStats, hasAccessibility ->
        MainScreenUiState(
            isMonitoring = isMonitoring,
            hasOverlay = hasOverlay,
            hasNotification = hasNotification,
            hasUsageStats = hasUsageStats,
            hasAccessibility = hasAccessibility
        )
    }

    init {
        logger(tag, "MainScreenViewModel init")
    }

    fun refreshState() {
        logger(tag, "refreshState")
        viewModelScope.launch {
            statusManager.setHasOverlay(permissionManager.refreshPermission(Permissions.Overlay))
            statusManager.setHasNotification(permissionManager.refreshPermission(Permissions.Notification))
            statusManager.setHasUsageStats(permissionManager.refreshPermission(Permissions.UsageStats))
            statusManager.setHasAccessibility(permissionManager.refreshPermission(Permissions.Accessibility))
        }
    }

    fun requestPermission(permission: Permissions) {
        logger(tag, "requestPermission $permission")
        permissionManager.requestPermission(permission)
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
                    // 刷新权限状态
                    refreshState()
                }
            } else {
                when {
                    !statusManager.hasOverlay.value -> showToast(appContext, "请先授予悬浮窗权限")
                    !statusManager.hasNotification.value -> showToast(
                        appContext,
                        "请先授予通知权限"
                    )

                    !statusManager.hasUsageStats.value -> showToast(
                        appContext,
                        "请先授予使用统计权限"
                    )

                    !statusManager.hasAccessibility.value -> showToast(
                        appContext,
                        "请先授予无障碍服务权限"
                    )

                    else -> showToast(appContext, "请先授予所有权限")
                }
            }
        }
    }
}