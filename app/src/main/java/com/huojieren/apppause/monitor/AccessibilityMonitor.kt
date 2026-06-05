package com.huojieren.apppause.monitor

import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.managers.AppManager
import com.huojieren.apppause.service.AppPauseAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AccessibilityMonitor(
    private val appManager: AppManager,
    private val scope: CoroutineScope,
) : ForegroundAppMonitor {
    private val tag = "AccessibilityMonitor"
    private var collectionJob: Job? = null

    override fun start(onAppChanged: (AppInfo?) -> Unit) {
        stop()
        logger(tag, "start accessibility monitor")

        collectionJob = scope.launch {
            AppPauseAccessibilityService.windowChangedEvent.collect { packageName ->
                onAppChanged(appManager.getAppInfo(packageName))
            }
        }
    }

    override fun stop() {
        logger(tag, "stop accessibility monitor")
        collectionJob?.cancel()
        collectionJob = null
    }
}
