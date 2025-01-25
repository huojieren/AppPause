package com.huojieren.apppause.managers

import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast

class AppMonitor(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    private val monitoredApps = mutableSetOf<String>() // 被监控的应用列表
    private val appTimers = mutableMapOf<String, Int>() // 存储每个应用的剩余时长
    private val timeUnit = BuildConfig.TIME_UNIT // 从 BuildConfig 中获取计时单位

    init {
        // 初始化时加载被监控的应用列表
        loadMonitoredApps()
    }

    fun startMonitoring(onAppDetected: (String) -> Unit) {
        // 如果被监控的应用列表为空，提示用户并返回
        if (monitoredApps.isEmpty()) {
            showToast(context, "没有应用被监控，请先添加应用")
            return
        }

        // 初始化 runnable
        runnable = object : Runnable {
            override fun run() {
                for (packageName in monitoredApps) {
                    if (isAppInForeground(packageName)) {
                        onAppDetected(packageName) // 通知外部检测到应用在前台运行
                        handler.removeCallbacks(this)
                        return
                    }
                }

                // 循环运行run检查
                handler.postDelayed(this, timeUnit) // 使用 BuildConfig 中的计时单位
            }
        }

        // 启动监控
        handler.post(runnable!!)
    }

    fun stopMonitoring() {
        if (runnable != null) {
            handler.removeCallbacks(runnable!!)
        }
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
}