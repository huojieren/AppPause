package com.huojieren.apppause.managers

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

class AppMonitor(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private var startTime: Long = 0
    private var selectedTime: Int = 5 // 默认使用时长
    private val monitoredApps = mutableSetOf<String>() // 被监控的应用列表
    private val appTimers = mutableMapOf<String, Int>() // 存储每个应用的剩余时长

    init {
        // 初始化时加载被监控的应用列表
        loadMonitoredApps()
    }

    fun startMonitoring(onAppDetected: (String) -> Unit) {
        // 检查使用情况访问权限
        if (!checkUsageStatsPermission()) {
            // 如果没有权限，提示用户并返回
            Toast.makeText(context, "请授予使用情况访问权限", Toast.LENGTH_SHORT).show()
            return
        }

        // 如果被监控的应用列表为空，提示用户并返回
        if (monitoredApps.isEmpty()) {
            Toast.makeText(context, "没有应用被监控，请先添加应用", Toast.LENGTH_SHORT).show()
            return
        }

        // 初始化 runnable
        runnable = object : Runnable {
            override fun run() {
                for (packageName in monitoredApps) {
                    if (isAppInForeground(packageName)) {
                        val remainingTime = appTimers[packageName] ?: selectedTime
                        if (remainingTime > 0) {
                            appTimers[packageName] = remainingTime - 1
                            onAppDetected(packageName)
                        } else {
                            onAppDetected(packageName)
                        }
                        handler.removeCallbacks(this)
                        return
                    }
                }

                // 每隔1秒检查一次
                handler.postDelayed(this, 1000)
            }
        }

        // 启动监控
        handler.post(runnable!!)
    }

    fun stopMonitoring() {
        handler.removeCallbacks(runnable!!)
    }

    fun setRemainingTime(packageName: String, time: Int) {
        appTimers[packageName] = time
    }

    fun getRemainingTime(packageName: String): Int {
        return appTimers[packageName] ?: 0
    }

    // 加载被监控的应用列表
    private fun loadMonitoredApps() {
        val sharedPreferences = context.getSharedPreferences("AppPause", Context.MODE_PRIVATE)
        val apps = sharedPreferences.getStringSet("monitoredApps", mutableSetOf()) ?: mutableSetOf()
        monitoredApps.clear()
        monitoredApps.addAll(apps)
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