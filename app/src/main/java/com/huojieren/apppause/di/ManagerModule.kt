package com.huojieren.apppause.di

import android.content.Context
import com.huojieren.apppause.data.repository.DataStoreRepository
import com.huojieren.apppause.managers.AppManager
import com.huojieren.apppause.managers.ListenerManager
import com.huojieren.apppause.managers.MonitorManager
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
        @ApplicationContext context: Context
    ): AppManager {
        return AppManager(context)
    }

    @Provides
    @Singleton
    fun provideMonitorManager(
        @ApplicationContext context: Context,
        dataStoreRepository: DataStoreRepository,
        timerManager: TimerManager,
        statusManager: StatusManager
    ): MonitorManager {
        return MonitorManager(
            context,
            dataStoreRepository,
            timerManager,
            statusManager
        )
    }

    @Provides
    @Singleton
    fun provideOverlayManager(
        @ApplicationContext context: Context
    ): OverlayManager {
        return OverlayManager(context)
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
        @ApplicationContext context: Context
    ): TimerManager {
        return TimerManager(context)
    }

    @Provides
    @Singleton
    fun provideStausManager(): StatusManager {
        return StatusManager()
    }

    @Provides
    @Singleton
    fun provideListenersManager(
        @ApplicationContext context: Context,
        appManager: AppManager,
        monitorManager: MonitorManager,
        overlayManager: OverlayManager,
        timerManager: TimerManager
    ): ListenerManager {
        return ListenerManager(
            context,
            monitorManager,
            overlayManager,
            timerManager,
            appManager
        )
    }
}