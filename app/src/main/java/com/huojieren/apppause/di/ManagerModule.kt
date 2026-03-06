package com.huojieren.apppause.di

import android.content.Context
import com.huojieren.apppause.data.repository.DataStoreRepository
import com.huojieren.apppause.data.repository.LogRepository
import com.huojieren.apppause.managers.AppManager
import com.huojieren.apppause.managers.ListenerManager
import com.huojieren.apppause.managers.MonitorManager
import com.huojieren.apppause.managers.NotificationManager
import com.huojieren.apppause.managers.OverlayManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.managers.StatusManager
import com.huojieren.apppause.managers.TimerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {
    @Provides
    @Singleton
    fun provideAppManager(
        @ApplicationContext context: Context,
        logRepository: LogRepository
    ): AppManager {
        return AppManager(
            context,
            logRepository
        )
    }

    @Provides
    @Singleton
    fun provideMonitorManager(
        @ApplicationContext context: Context,
        dataStoreRepository: DataStoreRepository,
        logRepository: LogRepository,
        timerManager: TimerManager,
        statusManager: StatusManager
    ): MonitorManager {
        return MonitorManager(
            context,
            dataStoreRepository,
            logRepository,
            timerManager,
            statusManager
        )
    }

    @Provides
    @Singleton
    fun provideOverlayManager(
        @ApplicationContext context: Context,
    ): OverlayManager {
        return OverlayManager(
            context
        )
    }

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        logRepository: LogRepository
    ): NotificationManager {
        return NotificationManager(
            context,
            logRepository
        )
    }

    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return PermissionManager(context)
    }

    @Provides
    @Singleton
    fun provideTimerManager(
        logRepository: LogRepository
    ): TimerManager {
        return TimerManager(
            logRepository
        )
    }

    @Provides
    @Singleton
    fun provideStausManager(
        @ApplicationContext context: Context,
        logRepository: LogRepository
    ): StatusManager {
        return StatusManager(
            context,
            logRepository
        )
    }

    @Provides
    @Singleton
    fun provideListenersManager(
        @ApplicationContext context: Context,
        appManager: AppManager,
        monitorManager: MonitorManager,
        overlayManager: OverlayManager,
        timerManager: TimerManager,
        logRepository: LogRepository
    ): ListenerManager {
        return ListenerManager(
            context,
            monitorManager,
            overlayManager,
            timerManager,
            appManager,
            logRepository
        )
    }
}