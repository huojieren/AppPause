package com.huojieren.apppause.monitor

import com.huojieren.apppause.data.models.AppInfo

interface ForegroundAppMonitor {
    enum class MonitorStrategy {
        USAGE_STATS,
        ACCESSIBILITY
    }

    fun start(onAppChanged: (AppInfo?) -> Unit)
    fun stop()
}