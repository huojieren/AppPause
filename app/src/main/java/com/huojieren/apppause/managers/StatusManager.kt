package com.huojieren.apppause.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.huojieren.apppause.data.repository.LogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Singleton

val Context.statusDataStore: DataStore<Preferences> by preferencesDataStore(name = "status_preferences")

@Singleton
class StatusManager(
    context: Context,
    private val logRepository: LogRepository
) {
    private val dataStore: DataStore<Preferences> = context.statusDataStore
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val tag = "StatusManager"

    /**
     * 状态数据
     * isMonitoring - 启动时从持久化数据中恢复上次状态;
     * 权限状态 - 每次启动从系统检查;
     */
    companion object {
        val IS_MONITORING = booleanPreferencesKey("is_monitoring")
        val SERVICE_HEARTBEAT = longPreferencesKey("service_heartbeat")
        val MONITOR_START_TIME = longPreferencesKey("monitor_start_time")
    }

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _hasOverlay = MutableStateFlow(false)
    val hasOverlay: StateFlow<Boolean> = _hasOverlay.asStateFlow()

    private val _hasNotification = MutableStateFlow(false)
    val hasNotification: StateFlow<Boolean> = _hasNotification.asStateFlow()

    private val _hasUsageStats = MutableStateFlow(false)
    val hasUsageStats: StateFlow<Boolean> = _hasUsageStats.asStateFlow()

    private val _hasAccessibility = MutableStateFlow(false)
    val hasAccessibility: StateFlow<Boolean> = _hasAccessibility.asStateFlow()

    init {
        // 首次启动时从 DataStore 恢复 isMonitoring 状态
        scope.launch {
            val stored = dataStore.data.first()[IS_MONITORING] ?: false
            _isMonitoring.value = stored
            logRepository?.log(tag, "init: restored isMonitoring=$stored from DataStore")
        }
    }

    fun setIsMonitoring(value: Boolean) {
        _isMonitoring.value = value
        scope.launch {
            dataStore.edit { preferences ->
                preferences[IS_MONITORING] = value
            }
            logRepository?.log(tag, "setIsMonitoring: $value")
        }
    }

    fun setHasOverlay(value: Boolean) {
        _hasOverlay.value = value
    }

    fun setHasNotification(value: Boolean) {
        _hasNotification.value = value
    }

    fun setHasUsageStats(value: Boolean) {
        _hasUsageStats.value = value
    }

    fun setHasAccessibility(value: Boolean) {
        _hasAccessibility.value = value
    }

    fun setServiceHeartbeat() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[SERVICE_HEARTBEAT] = System.currentTimeMillis()
            }
        }
    }

    fun setMonitorStartTime() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[MONITOR_START_TIME] = System.currentTimeMillis()
            }
        }
    }

    fun clearMonitorStartTime() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences.remove(MONITOR_START_TIME)
            }
        }
    }

    suspend fun isServiceRunningNormally(): Boolean {
        val lastHeartbeat = dataStore.data.first()[SERVICE_HEARTBEAT] ?: 0
        val currentTime = System.currentTimeMillis()
        val isNormal = currentTime - lastHeartbeat < 15_000
        logRepository.log(
            tag,
            "isServiceRunningNormally: heartbeat=$lastHeartbeat, now=$currentTime, isNormal=$isNormal"
        )
        return isNormal
    }

    suspend fun validateAndFixMonitoringStatus() {
        val storedIsMonitoring = dataStore.data.first()[IS_MONITORING] ?: false
        logRepository.log(
            tag,
            "validateAndFixMonitoringStatus: storedIsMonitoring=$storedIsMonitoring"
        )

        if (storedIsMonitoring) {
            val serviceRunningNormally = isServiceRunningNormally()
            logRepository.log(
                tag,
                "validateAndFixMonitoringStatus: serviceRunningNormally=$serviceRunningNormally"
            )

            if (!serviceRunningNormally) {
                setIsMonitoring(false)
                logRepository.log(
                    tag,
                    "validateAndFixMonitoringStatus: service abnormal, reset to false"
                )
            } else {
                _isMonitoring.value = true
            }
        } else {
            val heartbeatRecent = isServiceRunningNormally()
            logRepository.log(
                tag,
                "validateAndFixMonitoringStatus: heartbeatRecent=$heartbeatRecent"
            )

            if (heartbeatRecent) {
                // 服务在运行但状态为 false，恢复状态
                setIsMonitoring(true)
                logRepository.log(
                    tag,
                    "validateAndFixMonitoringStatus: service running but state false, recover to true"
                )
            }
        }
    }
}
