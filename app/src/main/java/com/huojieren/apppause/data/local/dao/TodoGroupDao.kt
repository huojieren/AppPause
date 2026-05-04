package com.huojieren.apppause.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.huojieren.apppause.data.local.entity.TodoGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoGroupDao {
    @Query("SELECT * FROM todo_groups ORDER BY isDefault DESC, createdAt ASC")
    fun getAllGroups(): Flow<List<TodoGroupEntity>>

    @Query("SELECT * FROM todo_groups WHERE id = :id")
    suspend fun getGroupById(id: Long): TodoGroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: TodoGroupEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<TodoGroupEntity>)

    @Update
    suspend fun updateGroup(group: TodoGroupEntity)

    @Delete
    suspend fun deleteGroup(group: TodoGroupEntity)

    @Query("SELECT COUNT(*) FROM todo_groups")
    suspend fun getGroupCount(): Int
}