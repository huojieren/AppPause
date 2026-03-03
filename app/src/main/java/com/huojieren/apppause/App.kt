package com.huojieren.apppause

import android.app.Application
import com.huojieren.apppause.managers.ListenerManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    // 注入保证在应用启动时监听器是启动状态
    @Inject
    lateinit var listenerManager: ListenerManager

}