package com.huojieren.apppause.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.huojieren.apppause.data.local.dao.TodoDao
import com.huojieren.apppause.data.local.dao.TodoGroupDao
import com.huojieren.apppause.data.local.entity.TodoEntity
import com.huojieren.apppause.data.local.entity.TodoGroupEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TodoEntity::class, TodoGroupEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
    abstract fun todoGroupDao(): TodoGroupDao

    companion object {
        private const val DATABASE_NAME = "app_pause_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultData(database.todoGroupDao())
                    }
                }
            }

            suspend fun populateDefaultData(todoGroupDao: TodoGroupDao) {
                val defaultGroups = listOf(
                    TodoGroupEntity(
                        name = "工作",
                        color = "#2196F3",
                        isDefault = true
                    ),
                    TodoGroupEntity(
                        name = "生活",
                        color = "#4CAF50",
                        isDefault = true
                    ),
                    TodoGroupEntity(
                        name = "学习",
                        color = "#FF9800",
                        isDefault = true
                    )
                )
                todoGroupDao.insertGroups(defaultGroups)
            }
        }
    }
}