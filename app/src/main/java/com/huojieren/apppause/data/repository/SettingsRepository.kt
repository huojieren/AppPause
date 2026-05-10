package com.huojieren.apppause.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.huojieren.apppause.data.DataStoreKeys
import com.huojieren.apppause.data.appDataStore
import com.huojieren.apppause.data.models.MonitorIntent
import com.huojieren.apppause.monitor.ForegroundAppMonitor.MonitorStrategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val dataStore: DataStore<Preferences> = context.appDataStore

    fun getSharedTimingEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.SHARED_TIMING_ENABLED] ?: false
        }
    }

    suspend fun setSharedTimingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.SHARED_TIMING_ENABLED] = enabled
        }
    }

    fun getMonitorIntent(): Flow<MonitorIntent> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.MONITOR_INTENT]
                ?.let { runCatching { MonitorIntent.valueOf(it) }.getOrNull() }
                ?: MonitorIntent.Disabled
        }
    }

    suspend fun setMonitorIntent(intent: MonitorIntent) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.MONITOR_INTENT] = intent.name
        }
    }

    fun getMonitorStrategy(): Flow<MonitorStrategy> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.MONITOR_STRATEGY]
                ?.let { runCatching { MonitorStrategy.valueOf(it) }.getOrNull() }
                ?: MonitorStrategy.ACCESSIBILITY
        }
    }

    suspend fun setMonitorStrategy(strategy: MonitorStrategy) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.MONITOR_STRATEGY] = strategy.name
        }
    }
}
