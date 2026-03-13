package com.huojieren.apppause.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
}
