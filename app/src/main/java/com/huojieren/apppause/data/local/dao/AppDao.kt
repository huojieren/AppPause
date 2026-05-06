package com.huojieren.apppause.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.huojieren.apppause.data.local.entity.AppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM apps ORDER BY name ASC")
    fun getAllApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isMonitored = 1 ORDER BY name ASC")
    fun getMonitoredApps(): Flow<List<AppEntity>>

    @Query("SELECT * FROM apps WHERE isMonitored = 1 ORDER BY name ASC")
    suspend fun getMonitoredAppsOnce(): List<AppEntity>

    @Query("SELECT packageName FROM apps WHERE isMonitored = 1")
    suspend fun getMonitoredPackageNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApp(app: AppEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApps(apps: List<AppEntity>)

    @Query(
        """
        UPDATE apps
        SET isMonitored = :isMonitored, updatedAt = :updatedAt
        WHERE packageName = :packageName
        """
    )
    suspend fun updateMonitored(
        packageName: String,
        isMonitored: Boolean,
        updatedAt: Long = System.currentTimeMillis()
    )
}
