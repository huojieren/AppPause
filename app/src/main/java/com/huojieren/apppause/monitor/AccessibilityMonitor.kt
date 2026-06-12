package com.huojieren.apppause.monitor

import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.managers.AppManager
import com.huojieren.apppause.service.AppPauseAccessibilityService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AccessibilityMonitor(
    private val appManager: AppManager,
    private val scope: CoroutineScope,
) : ForegroundAppMonitor {
    private val tag = "AccessibilityMonitor"
    private var collectionJob: Job? = null

    private val _appChangedEvent = MutableSharedFlow<AppInfo?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val appChangedEvent: SharedFlow<AppInfo?> = _appChangedEvent.asSharedFlow()

    override fun start() {
        stop()
        logger(tag, "start accessibility monitor")

        collectionJob = scope.launch {
            AppPauseAccessibilityService.windowChangedEvent.collect { packageName ->
                _appChangedEvent.tryEmit(appManager.getAppInfo(packageName))
            }
        }
    }

    override fun stop() {
        logger(tag, "stop accessibility monitor")
        collectionJob?.cancel()
        collectionJob = null
    }
}
