package com.huojieren.apppause.data.repository

import com.huojieren.apppause.data.local.dao.AppDao
import com.huojieren.apppause.data.local.entity.toEntity
import com.huojieren.apppause.data.models.AppInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreMigrationManager @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
    private val appDao: AppDao
) {
    private val mutex = Mutex()

    suspend fun migrateIfNeeded() {
        mutex.withLock {
            if (dataStoreRepository.isRoomAppMigrationCompleted()) return

            val allApps = dataStoreRepository.getLegacyAllAppsOnce()
            val monitoredApps = dataStoreRepository.getLegacyMonitoredAppsOnce()
            val monitoredPackages = monitoredApps.map { it.packageName }.toSet()
            val appsByPackage = linkedMapOf<String, AppInfo>()

            allApps.forEach { app ->
                appsByPackage[app.packageName] = app
            }
            monitoredApps.forEach { app ->
                appsByPackage[app.packageName] = app
            }

            if (appsByPackage.isNotEmpty()) {
                appDao.upsertApps(
                    appsByPackage.values.map { app ->
                        app.toEntity(isMonitored = app.packageName in monitoredPackages)
                    }
                )
            }

            dataStoreRepository.setRoomAppMigrationCompleted()
        }
    }
}
