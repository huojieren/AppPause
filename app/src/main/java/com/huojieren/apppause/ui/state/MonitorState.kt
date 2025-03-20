package com.huojieren.apppause.ui.state

import android.os.Build
import androidx.annotation.RequiresApi
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.PermissionManager

class MonitorState(
    val isMonitoring: Boolean,
    val hasAllPermissions: Boolean,
    val hasMonitoredApps: Boolean
) {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    constructor(appMonitor: AppMonitor, permissionManager: PermissionManager) : this(
        isMonitoring = appMonitor.isMonitoring,
        hasAllPermissions = permissionManager.checkOverlayPermission() &&
                permissionManager.checkNotificationPermission() &&
                permissionManager.checkUsageStatsPermission(),
        hasMonitoredApps = !appMonitor.isEmptyMonitoredApps()
    )
}
