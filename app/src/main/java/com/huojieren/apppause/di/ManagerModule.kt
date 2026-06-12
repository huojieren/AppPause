package com.huojieren.apppause.di

import android.content.Context
import com.huojieren.apppause.data.repository.AppRepository
import com.huojieren.apppause.data.repository.SettingsRepository
import com.huojieren.apppause.data.repository.TodoRepository
import com.huojieren.apppause.managers.AppManager
import com.huojieren.apppause.managers.MonitorManager
import com.huojieren.apppause.managers.AppOverlayManager
import com.huojieren.apppause.managers.OverlayManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.managers.StatusManager
import com.huojieren.apppause.managers.TimerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
        appRepository: AppRepository,
        settingsRepository: SettingsRepository,
        timerManager: TimerManager,
        statusManager: StatusManager
    ): MonitorManager {
        return MonitorManager(
            context,
            appRepository,
            settingsRepository,
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
        @ApplicationContext context: Context,
        settingsRepository: SettingsRepository,
        scope: CoroutineScope
    ): TimerManager {
        return TimerManager(context, settingsRepository, scope)
    }

    @Provides
    @Singleton
    fun provideStausManager(): StatusManager {
        return StatusManager()
    }

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    @Provides
    @Singleton
    fun provideAppOverlayManager(
        @ApplicationContext context: Context,
        appManager: AppManager,
        monitorManager: MonitorManager,
        overlayManager: OverlayManager,
        timerManager: TimerManager,
        todoRepository: TodoRepository,
        settingsRepository: SettingsRepository,
        scope: CoroutineScope
    ): AppOverlayManager {
        return AppOverlayManager(
            context,
            monitorManager,
            overlayManager,
            timerManager,
            appManager,
            todoRepository,
            settingsRepository,
            scope
        )
    }
}
