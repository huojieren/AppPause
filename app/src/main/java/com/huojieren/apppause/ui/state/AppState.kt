package com.huojieren.apppause.ui.state

data class AppState(
    // 监控状态
    val isMonitoring: Boolean = false,
    val monitoredApps: Set<String> = emptySet(),
    val currentApp: String? = null,
    // 权限状态
    val hasOverlay: Boolean = false,
    val hasNotification: Boolean = false,
    val hasUsageStats: Boolean = false
)
