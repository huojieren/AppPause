package com.huojieren.apppause.ui.state

data class AppStatusUiState(
    val isMonitoring: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotification: Boolean = false,
    val hasUsageStats: Boolean = false,
    val hasAccessibility: Boolean = false,
    val isSharedTimingEnabled: Boolean = false,
    val isWaitBeforeReturnEnabled: Boolean = false,
    val isTimeoutTodoPromptEnabled: Boolean = false,
    val isTimeSelectionTodoPromptEnabled: Boolean = false,
)
