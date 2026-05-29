package com.huojieren.apppause.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.huojieren.apppause.data.DataStoreKeys
import com.huojieren.apppause.data.appDataStore
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

    fun getWaitBeforeReturnEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.WAIT_BEFORE_RETURN_ENABLED] ?: false
        }
    }

    suspend fun setWaitBeforeReturnEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.WAIT_BEFORE_RETURN_ENABLED] = enabled
        }
    }

    fun getTodoPromptEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[DataStoreKeys.TODO_PROMPT_ENABLED] ?: false
        }
    }

    suspend fun setTodoPromptEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DataStoreKeys.TODO_PROMPT_ENABLED] = enabled
        }
    }
}
