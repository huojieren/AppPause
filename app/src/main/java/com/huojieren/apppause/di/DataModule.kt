package com.huojieren.apppause.di

import android.content.Context
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
        @ApplicationContext context: Context,
        logRepository: LogRepository
    ): DataStoreRepository {
        return DataStoreRepository(
            context,
            logRepository
        )
    }

    @Provides
    @Singleton
    fun provideLogRepository(
        @ApplicationContext context: Context
    ): LogRepository {
        return LogRepository(context)
    }

}