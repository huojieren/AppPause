package com.huojieren.apppause.data.repository

import com.huojieren.apppause.data.local.dao.AppDao
import com.huojieren.apppause.data.local.entity.toEntity
import com.huojieren.apppause.data.local.entity.toModel
import com.huojieren.apppause.data.models.AppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val appDao: AppDao,
    private val migrationManager: DataStoreMigrationManager
) {
    fun getAllApps(): Flow<List<AppInfo>> {
        return appDao.getAllApps().map { apps -> apps.map { it.toModel() } }
    }

    fun getMonitoredApps(): Flow<List<AppInfo>> {
        return appDao.getMonitoredApps().map { apps -> apps.map { it.toModel() } }
    }

    suspend fun getMonitoredAppsOnce(): List<AppInfo> {
        migrationManager.migrateIfNeeded()
        return appDao.getMonitoredAppsOnce().map { it.toModel() }
    }

    suspend fun saveAllApps(apps: List<AppInfo>) {
        migrationManager.migrateIfNeeded()
        val monitoredPackages = appDao.getMonitoredPackageNames().toSet()
        appDao.upsertApps(
            apps.map { app ->
                app.toEntity(isMonitored = app.packageName in monitoredPackages)
            }
        )
    }

    suspend fun addAppToMonitored(appInfo: AppInfo) {
        migrationManager.migrateIfNeeded()
        appDao.upsertApp(appInfo.toEntity(isMonitored = true))
    }

    suspend fun removeAppFromMonitor(appInfo: AppInfo) {
        migrationManager.migrateIfNeeded()
        appDao.upsertApp(appInfo.toEntity(isMonitored = false))
    }
}
