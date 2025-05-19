package com.huojieren.apppause.ui.viewmodel

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.ui.state.AppState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val permissionManager: PermissionManager,
    private val appMonitor: AppMonitor
) : ViewModel() {
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        _appState.value = _appState.value.copy(
            // 监控状态
            isMonitoring = appMonitor.isMonitoring,
            monitoredApps = appMonitor.getMonitoredApps(),
            // 权限状态
            hasOverlay = permissionManager.checkOverlayPermission(),
            hasNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionManager.checkNotificationPermission()
            } else true,
            hasUsageStats = permissionManager.checkUsageStatsPermission(),
        )
    }

    fun toggleMonitoring() {
        viewModelScope.launch {
            if (appState.value.isMonitoring) {
                appMonitor.stopMonitoring()
            } else {
                appMonitor.startMonitoring()
            }
            refreshAll()
        }
    }
}