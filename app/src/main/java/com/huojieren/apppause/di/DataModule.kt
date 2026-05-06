package com.huojieren.apppause.di

import android.content.Context
import com.huojieren.apppause.data.local.AppDatabase
import com.huojieren.apppause.data.local.dao.AppDao
import com.huojieren.apppause.data.local.dao.TodoDao
import com.huojieren.apppause.data.local.dao.TodoGroupDao
import com.huojieren.apppause.data.repository.DataStoreRepository
import com.huojieren.apppause.data.repository.LogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDataStoreRepository(
        @ApplicationContext context: Context
    ): DataStoreRepository {
        return DataStoreRepository(context)
    }

    @Provides
    @Singleton
    fun provideLogRepository(
        @ApplicationContext context: Context
    ): LogRepository {
        return LogRepository(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }

    @Provides
    @Singleton
    fun provideTodoDao(database: AppDatabase): TodoDao {
        return database.todoDao()
    }

    @Provides
    @Singleton
    fun provideTodoGroupDao(database: AppDatabase): TodoGroupDao {
        return database.todoGroupDao()
    }
}
