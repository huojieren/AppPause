package com.huojieren.apppause.managers

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
    context: Context
) {
    private val dataStore: DataStore<Preferences> = context.statusDataStore
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // DataStore keys
    companion object {
        val IS_MONITORING = booleanPreferencesKey("is_monitoring")
        val HAS_OVERLAY = booleanPreferencesKey("has_overlay")
        val HAS_NOTIFICATION = booleanPreferencesKey("has_notification")
        val HAS_USAGE_STATS = booleanPreferencesKey("has_usage_stats")
        val HAS_ACCESSIBILITY = booleanPreferencesKey("has_accessibility")
        val SERVICE_HEARTBEAT = longPreferencesKey("service_heartbeat")
        val MONITOR_START_TIME = longPreferencesKey("monitor_start_time")
    }

    // In-memory state flows
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
        scope.launch {
            dataStore.data.collect { preferences ->
                _isMonitoring.value = preferences[IS_MONITORING] ?: false
                _hasOverlay.value = preferences[HAS_OVERLAY] ?: false
                _hasNotification.value = preferences[HAS_NOTIFICATION] ?: false
                _hasUsageStats.value = preferences[HAS_USAGE_STATS] ?: false
                _hasAccessibility.value = preferences[HAS_ACCESSIBILITY] ?: false
            }
        }
    }

    // TODO 2025/12/12 14:14 检查状态管理

    suspend fun getIsMonitoringValue(): Boolean {
        return dataStore.data.first()[IS_MONITORING] ?: false
    }

    fun setIsMonitoring(value: Boolean) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[IS_MONITORING] = value
            }
            _isMonitoring.value = value
        }
    }

    fun setHasOverlay(value: Boolean) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[HAS_OVERLAY] = value
            }
            _hasOverlay.value = value
        }
    }

    fun setHasNotification(value: Boolean) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[HAS_NOTIFICATION] = value
            }
            _hasNotification.value = value
        }
    }

    fun setHasUsageStats(value: Boolean) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[HAS_USAGE_STATS] = value
            }
            _hasUsageStats.value = value
        }
    }

    fun setHasAccessibility(value: Boolean) {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[HAS_ACCESSIBILITY] = value
            }
            _hasAccessibility.value = value
        }
    }

    // 更新服务心跳时间戳
    fun updateServiceHeartbeat() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[SERVICE_HEARTBEAT] = System.currentTimeMillis()
            }
        }
    }

    // 设置监控开始时间
    fun setMonitorStartTime() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences[MONITOR_START_TIME] = System.currentTimeMillis()
            }
        }
    }

    // 清除监控开始时间
    fun clearMonitorStartTime() {
        scope.launch {
            dataStore.edit { preferences ->
                preferences.remove(MONITOR_START_TIME)
            }
        }
    }

    // 检查服务是否正常运行（通过心跳机制）
    suspend fun isServiceRunningNormally(): Boolean {
        val preferences = dataStore.data.first()
        val lastHeartbeat = preferences[SERVICE_HEARTBEAT] ?: 0
        val currentTime = System.currentTimeMillis()
        // 如果最后一次心跳超过15秒，认为服务已异常停止
        return currentTime - lastHeartbeat < 15_000
    }

    // 检查并修复监控状态
    suspend fun validateAndFixMonitoringStatus() {
        val isMonitoring = dataStore.data.first()[IS_MONITORING] ?: false
        if (isMonitoring) {
            val serviceRunningNormally = isServiceRunningNormally()
            if (!serviceRunningNormally) {
                // 服务异常停止，重置监控状态
                dataStore.edit { preferences ->
                    preferences[IS_MONITORING] = false
                    preferences.remove(SERVICE_HEARTBEAT)
                    preferences.remove(MONITOR_START_TIME)
                }
                _isMonitoring.value = false
            }
        } else {
            // 检查是否有异常情况：监控状态为false但服务仍在运行
            val heartbeatRecent = isServiceRunningNormally()
            if (heartbeatRecent) {
                // 如果心跳正常但状态为false，可能是状态同步问题，更新状态
                dataStore.edit { preferences ->
                    preferences[IS_MONITORING] = true
                }
                _isMonitoring.value = true
            }
        }
    }
}
