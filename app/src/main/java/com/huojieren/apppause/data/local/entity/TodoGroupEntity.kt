package com.huojieren.apppause.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todo_groups")
data class TodoGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)