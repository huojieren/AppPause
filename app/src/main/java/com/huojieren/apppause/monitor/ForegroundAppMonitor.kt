package com.huojieren.apppause.monitor

import com.huojieren.apppause.data.models.AppInfo
import kotlinx.coroutines.flow.SharedFlow

interface ForegroundAppMonitor {
    enum class MonitorStrategy {
        USAGE_STATS,
        ACCESSIBILITY
    }

    val appChangedEvent: SharedFlow<AppInfo?>

    fun start()
    fun stop()
}