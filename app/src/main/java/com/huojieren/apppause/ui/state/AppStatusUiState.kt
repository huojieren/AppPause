package com.huojieren.apppause.ui.state

import com.huojieren.apppause.data.models.MonitorIntent

data class AppStatusUiState(
    val isMonitoring: Boolean = false,
    val monitorIntent: MonitorIntent = MonitorIntent.Disabled,
    val hasOverlay: Boolean = false,
    val hasNotification: Boolean = false,
    val hasUsageStats: Boolean = false,
    val hasAccessibility: Boolean = false,
    val hasBatteryOptimizationExemption: Boolean = false,
    val isSharedTimingEnabled: Boolean = false,
    val isWaitBeforeReturnEnabled: Boolean = false,
    val waitBeforeReturnSeconds: Int = 5,
    val isTodoPromptEnabled: Boolean = false,
) {
    val isMonitoringInterrupted: Boolean
        get() = monitorIntent == MonitorIntent.Enabled && !isMonitoring
}
