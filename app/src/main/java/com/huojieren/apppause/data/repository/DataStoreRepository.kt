package com.huojieren.apppause.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.huojieren.apppause.data.DataStoreKeys
import com.huojieren.apppause.data.appDataStore
import com.huojieren.apppause.data.models.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class DataStoreRepository(
    context: Context,
    private val logRepository: LogRepository
) {
    private val dataStore: DataStore<Preferences> = context.appDataStore
    private val json = Json { ignoreUnknownKeys = true }
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * 添加应用到DataStore中
     */
    fun addAppToMonitored(appInfo: AppInfo) {
        repositoryScope.launch {
            dataStore.edit { preferences ->
                val currentApps = preferences[DataStoreKeys.MONITORED_APPS] ?: emptySet()
                preferences[DataStoreKeys.MONITORED_APPS] =
                    currentApps + json.encodeToString(appInfo)
            }
        }
    }

    /**
     * 从DataStore中删除应用
     */
    fun removeAppFromMonitor(appInfo: AppInfo) {
        repositoryScope.launch {
            dataStore.edit { preferences ->
                val currentApps = preferences[DataStoreKeys.MONITORED_APPS] ?: emptySet()
                preferences[DataStoreKeys.MONITORED_APPS] =
                    currentApps - json.encodeToString(appInfo)
            }
        }
    }

    /**
     * 获取DataStore中的所有被监控应用
     */
    fun getMonitoredApps(): Flow<List<AppInfo>> {
        return dataStore.data
            .map { preferences ->
                val appsJson = preferences[DataStoreKeys.MONITORED_APPS] ?: emptySet()
                try {
                    appsJson.map { json.decodeFromString<AppInfo>(it) }
                } catch (e: Exception) {
                    logRepository.log(
                        "DataStoreRepository",
                        "Error decoding appsJson: $e"
                    )
                    emptyList()
                }
            }
    }

    /**
     * 保存所有应用到DataStore中
     */
    fun saveAllApps(apps: List<AppInfo>) {
        repositoryScope.launch {
            dataStore.edit { preferences ->
                val appsJson = json.encodeToString(apps)
                preferences[DataStoreKeys.ALL_APPS] = setOf(appsJson)
            }
        }
    }
}
