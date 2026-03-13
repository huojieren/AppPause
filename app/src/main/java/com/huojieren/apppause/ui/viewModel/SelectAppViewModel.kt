package com.huojieren.apppause.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.data.models.toEntity
import com.huojieren.apppause.data.repository.DataStoreRepository
import com.huojieren.apppause.data.repository.LogRepository
import com.huojieren.apppause.managers.AppManager
import com.huojieren.apppause.ui.state.SelectAppUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectAppViewModel @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val appManager: AppManager,
    private val logRepository: LogRepository,
) : ViewModel() {
    private val tag = "SelectAppViewModel"
    private val _uiState = MutableStateFlow(SelectAppUiState())
    val uiState: StateFlow<SelectAppUiState> = _uiState.asStateFlow()

    init {
        logRepository.log(tag, "SelectAppViewModel init")
        refreshAllApps()
        refreshMonitoredApps()
    }

    fun addApp(appInfoUi: AppInfoUi) {
        logRepository.log(tag, "Add app: ${appInfoUi.name}")
        if (!_uiState.value.monitoredApps.any { it.packageName == appInfoUi.packageName }) {
            dataStoreRepository.addAppToMonitored(appInfoUi.toEntity())
            _uiState.update { currentState ->
                currentState.copy(
                    monitoredApps = currentState.monitoredApps + appInfoUi
                )
            }
        }
    }

    fun removeApp(appInfoUi: AppInfoUi) {
        logRepository.log(tag, "Remove app: ${appInfoUi.name}")
        dataStoreRepository.removeAppFromMonitor(appInfoUi.toEntity())
        _uiState.update { currentState ->
            currentState.copy(
                monitoredApps = currentState.monitoredApps.filter { it.packageName != appInfoUi.packageName }
            )
        }
    }

    fun toggleApp(appInfoUi: AppInfoUi) {
        val isMonitored =
            _uiState.value.monitoredApps.any { it.packageName == appInfoUi.packageName }
        if (isMonitored) {
            removeApp(appInfoUi)
        } else {
            addApp(appInfoUi)
        }
    }

    fun refreshMonitoredApps() {
        logRepository.log(tag, "Refresh monitored apps")
        viewModelScope.launch {
            val appInfoList = dataStoreRepository.getMonitoredApps().first()
            logRepository.log(tag, "Monitored apps: $appInfoList")
            val appInfoUiList = appManager.toUiList(appInfoList)
            _uiState.update { it.copy(monitoredApps = appInfoUiList) }
        }
    }

    fun refreshAllApps() {
        logRepository.log(tag, "Refresh all apps")
        viewModelScope.launch {
            val appInfoList = appManager.loadInstalledApps()
            dataStoreRepository.saveAllApps(appInfoList)
            logRepository.log(tag, "All apps: $appInfoList")
            val appsUiList = appManager.toUiList(appInfoList)
            _uiState.update { it.copy(allApps = appsUiList) }
        }
    }
}