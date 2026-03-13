package com.huojieren.apppause.monitor

import android.accessibilityservice.AccessibilityService
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.repository.LogRepository
import com.huojieren.apppause.managers.AppManager
import com.huojieren.apppause.service.AppPauseAccessibilityService

class AccessibilityMonitor(
    private val appManager: AppManager,
    private val logRepository: LogRepository,
) : ForegroundAppMonitor {
    private val tag = "AccessibilityMonitor"
    private lateinit var service: AccessibilityService
    override fun start(onAppChanged: (AppInfo?) -> Unit) {
        stop()
        logRepository.log(tag, "start accessibility monitor")

        try {
            service = AppPauseAccessibilityService.getInstance()
            AppPauseAccessibilityService.setOnAppChangedListener { packageName ->
                onAppChanged(appManager.getAppInfo(packageName))
            }
        } catch (e: IllegalStateException) {
            logRepository.log(tag, "AccessibilityService not initialized: ${e.message}")
            throw e
        }
    }

    override fun stop() {
        logRepository.log(tag, "stop accessibility monitor")
        AppPauseAccessibilityService.removeOnAppChangedListener()
    }
}
