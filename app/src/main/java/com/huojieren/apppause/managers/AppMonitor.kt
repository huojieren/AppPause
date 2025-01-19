package com.huojieren.apppause.managers

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Handler
import android.os.Looper

class AppMonitor(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var startTime: Long = 0
    private var selectedTime: Int = 5 // 默认使用时长
    private val monitoredApps = mutableSetOf<String>() // 被监控的应用列表

    fun startMonitoring(onAppDetected: (String) -> Unit) {
        if (!checkUsageStatsPermission()) {
            return
        }

        runnable = object : Runnable {
            override fun run() {
                for (packageName in monitoredApps) {
                    if (isAppInForeground(packageName)) {
                        onAppDetected(packageName)
                        handler.removeCallbacks(this)
                        return
                    }
                }

                // 每隔1秒检查一次
                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable!!)
    }

    fun stopMonitoring() {
        handler.removeCallbacks(runnable!!)
    }

    private fun isAppInForeground(packageName: String): Boolean {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            endTime - 1000, // 检查过去1秒内的使用情况
            endTime
        )

        for (usageStat in usageStats) {
            if (usageStat.packageName == packageName && usageStat.lastTimeUsed >= endTime - 1000) {
                return true
            }
        }
        return false
    }

    private fun checkUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}