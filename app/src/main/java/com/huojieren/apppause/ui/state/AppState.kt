package com.huojieren.apppause.ui.state

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.PermissionManager

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class AppState(
    private val permissionManager: PermissionManager,
    private val appMonitor: AppMonitor
) {
    // 权限状态
    var hasOverlayPermission by mutableStateOf(permissionManager.checkOverlayPermission())
    var hasNotificationPermission by mutableStateOf(permissionManager.checkNotificationPermission())
    var hasUsageStatsPermission by mutableStateOf(permissionManager.checkUsageStatsPermission())

    // 监控状态
    val hasMonitoredApps get() = !appMonitor.isEmptyMonitoredApps()
    val isMonitoring get() = appMonitor.isMonitoring

    // 刷新所有状态
    fun refresh() {
        hasOverlayPermission = permissionManager.checkOverlayPermission()
        hasNotificationPermission = permissionManager.checkNotificationPermission()
        hasUsageStatsPermission = permissionManager.checkUsageStatsPermission()
    }
}
