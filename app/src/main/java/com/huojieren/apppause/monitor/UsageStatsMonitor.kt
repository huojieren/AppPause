package com.huojieren.apppause.monitor

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.managers.AppManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class UsageStatsMonitor(
    context: Context,
    private val appManager: AppManager
) : ForegroundAppMonitor {
    private val tag = "UsageStatsMonitor"
    private var lastApp: AppInfo? = null
    private var monitoringJob: Job? = null
    private var lastQueryEndTime = 0L
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val _appChangedEvent = MutableSharedFlow<AppInfo?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val appChangedEvent: SharedFlow<AppInfo?> = _appChangedEvent.asSharedFlow()

    override fun start() {
        stop()
        monitoringJob = CoroutineScope(Dispatchers.Default).launch {
            logger(tag, "start monitor foreground app")
            while (isActive) {
                val currentApp = getForegroundApp()
                logger(tag, "current app: [$currentApp], last app: [$lastApp]")
                if (currentApp != lastApp) {
                    lastApp = currentApp
                    logger(tag, "app changed: [$currentApp]")
                    _appChangedEvent.tryEmit(currentApp)
                }
                delay(1000)
            }
        }
    }

    override fun stop() {
        logger(tag, "stop monitor foreground app")
        monitoringJob?.cancel()
        lastApp = null
    }

    private fun getForegroundApp(): AppInfo? {
        val end = System.currentTimeMillis()
        val begin = if (lastQueryEndTime == 0L) end - 5000 else lastQueryEndTime
        lastQueryEndTime = end

        val events = usageStatsManager.queryEvents(begin, end)
        var event: UsageEvents.Event

        var detectedApp: AppInfo? = null

        while (events.hasNextEvent()) {
            event = UsageEvents.Event()
            events.getNextEvent(event)

            logger(
                tag,
                "event: pkg=${event.packageName}, type=${event.eventType}, time=${event.timeStamp}"
            )

            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                detectedApp = appManager.getAppInfo(event.packageName)
            }
        }

        return detectedApp
    }
}
