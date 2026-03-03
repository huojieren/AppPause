package com.huojieren.apppause.ui.viewModel

import android.util.Log
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
        logRepository.log(tag, "SelectAppViewModel init", Log.DEBUG)
        refreshAllApps()
        refreshMonitoredApps()
    }

    fun addApp(appInfoUi: AppInfoUi) {
        logRepository.log(tag, "添加应用: ${appInfoUi.name}")
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
        logRepository.log(tag, "移除应用: ${appInfoUi.name}")
        dataStoreRepository.removeAppFromMonitor(appInfoUi.toEntity())
        _uiState.update { currentState ->
            currentState.copy(
                monitoredApps = currentState.monitoredApps.filter { it.packageName != appInfoUi.packageName }
            )
        }
    }

    fun refreshMonitoredApps() {
        logRepository.log(tag, "刷新已监控应用")
        viewModelScope.launch {
            val appInfoList = dataStoreRepository.getMonitoredApps().first()
            logRepository.log(tag, "已监控应用: $appInfoList")
            val appInfoUiList = appManager.toUiList(appInfoList)
            _uiState.update { it.copy(monitoredApps = appInfoUiList) }
        }
    }

    fun refreshAllApps() {
        logRepository.log(tag, "刷新所有应用")
        viewModelScope.launch {
            val appInfoList = appManager.loadInstalledApps()
            dataStoreRepository.saveAllApps(appInfoList)
            logRepository.log(tag, "所有应用: $appInfoList")
            val appsUiList = appManager.toUiList(appInfoList)
            _uiState.update { it.copy(allApps = appsUiList) }
        }
    }
}