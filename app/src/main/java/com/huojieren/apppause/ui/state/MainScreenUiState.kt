package com.huojieren.apppause.ui.state

data class MainScreenUiState(
    val isMonitoring: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotification: Boolean = false,
    val hasUsageStats: Boolean = false,
    val hasAccessibility: Boolean = false,
)