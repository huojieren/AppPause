package com.huojieren.apppause.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_pause_preferences")

/**
 * DataStore 键
 */
object DataStoreKeys {
    // 已选择的应用
    val MONITORED_APPS = stringSetPreferencesKey("monitored_apps")

    // 所有应用
    val ALL_APPS = stringSetPreferencesKey("all_apps")

    // DataStore 应用数据迁移到 Room 的完成标记
    val ROOM_APP_MIGRATION_COMPLETED = booleanPreferencesKey("room_app_migration_completed")

    val SHARED_TIMING_ENABLED = booleanPreferencesKey("shared_timing_enabled")

    val WAIT_BEFORE_RETURN_ENABLED = booleanPreferencesKey("wait_before_return_enabled")

    val TODO_PROMPT_ENABLED = booleanPreferencesKey("todo_prompt_enabled")
}
