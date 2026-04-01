package com.huojieren.apppause.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.data.models.AppLetterGroup
import com.huojieren.apppause.data.models.toEntity
import com.huojieren.apppause.data.repository.DataStoreRepository
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
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
) : ViewModel() {
    private val tag = "SelectAppViewModel"
    private val _uiState = MutableStateFlow(SelectAppUiState())
    val uiState: StateFlow<SelectAppUiState> = _uiState.asStateFlow()

    init {
        logger(tag, "SelectAppViewModel init")
        viewModelScope.launch {
            refreshAllApps()
            refreshMonitoredApps()
            calculateLetterPositions()
        }
    }

    fun addApp(appInfoUi: AppInfoUi) {
        logger(tag, "Add app: ${appInfoUi.name}")
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
        logger(tag, "Remove app: ${appInfoUi.name}")
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
        calculateLetterPositions()
    }

    suspend fun refreshMonitoredApps() {
        logger(tag, "Refresh monitored apps")
        val appInfoList = dataStoreRepository.getMonitoredApps().first()
        logger(tag, "Monitored apps: $appInfoList")

        val appInfoUiList = appManager.toUiList(appInfoList)
        _uiState.update { it.copy(monitoredApps = appInfoUiList) }
    }

    suspend fun refreshAllApps() {
        logger(tag, "Refresh all apps")
        val appInfoGrouped = appManager.loadInstalledAppsGrouped()
        logger(tag, "All apps grouped: ${appInfoGrouped.map { it.letter to it.items.size }}")

        dataStoreRepository.saveAllApps(appInfoGrouped.flatMap { it.items })

        val appsUiGrouped = appInfoGrouped.map { group ->
            val appsUiList = appManager.toUiList(group.items)
            AppLetterGroup(group.letter, appsUiList)
        }
        _uiState.update { it.copy(allAppsGrouped = appsUiGrouped) }
    }

    private fun calculateLetterPositions() {
        logger(tag, "Calculate letter positions")
        logger(tag, "Monitored apps: ${_uiState.value.monitoredApps.size}")
        logger(tag, "All apps: ${_uiState.value.allAppsGrouped.map { it.letter to it.items.size }}")

        val monitoredApps = _uiState.value.monitoredApps
        val allAppsGrouped = _uiState.value.allAppsGrouped

        val positions = mutableMapOf<String, Int>()
        positions["↑"] = 0

        val headerCount = 2 // "已监控应用"标题 and "所有应用"标题
        var currentIndex = if (monitoredApps.isNotEmpty())
            headerCount + monitoredApps.size
        else
            headerCount + 1

        allAppsGrouped.forEach { group ->
            positions[group.letter] = currentIndex
            currentIndex += group.items.size + 1
        }
        logger(tag, "positions: $positions")

        _uiState.update { it.copy(letterPositions = positions) }
    }

    fun getLetterPosition(letter: String): Int? {
        logger(tag, "Get letter position: $letter, positions: ${_uiState.value.letterPositions}")
        return _uiState.value.letterPositions[letter]
    }
}