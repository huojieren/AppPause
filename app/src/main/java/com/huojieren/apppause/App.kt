package com.huojieren.apppause

import android.app.Application
import com.huojieren.apppause.data.diagnostics.DiagnosticsManager
import com.huojieren.apppause.managers.AppOverlayManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var appOverlayManager: AppOverlayManager

    @Inject
    lateinit var diagnosticsManager: DiagnosticsManager

    override fun onCreate() {
        super.onCreate()
        diagnosticsManager.initialize()
        diagnosticsManager.collectHistoricalExitInfo()
    }
}
