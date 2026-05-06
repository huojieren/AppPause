package com.huojieren.apppause.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.huojieren.apppause.data.models.AppInfo

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey
    val packageName: String,
    val name: String,
    val isMonitored: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

fun AppEntity.toModel(): AppInfo {
    return AppInfo(
        name = name,
        packageName = packageName
    )
}

fun AppInfo.toEntity(
    isMonitored: Boolean = false,
    updatedAt: Long = System.currentTimeMillis()
): AppEntity {
    return AppEntity(
        packageName = packageName,
        name = name,
        isMonitored = isMonitored,
        updatedAt = updatedAt
    )
}
