package com.huojieren.apppause.managers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast

class AppMonitor(private val context: Context) {

    var isMonitoring: Boolean = false

    private val monitoredApps = mutableSetOf<String>() // 被监控的应用列表
    private val appTimers = mutableMapOf<String, Int>() // 存储每个应用的剩余时长
    private val timeUnit = BuildConfig.TIME_UNIT // 从 BuildConfig 中获取计时单位
    private val timeDesc = BuildConfig.TIME_DESC // 从 BuildConfig 中获取计时单位描述
    private val overlayManager = OverlayManager(context)
    private val tag = "AppMonitor"

    // 单例模式
    companion object {
        @Volatile
        @SuppressLint("StaticFieldLeak") // 忽略 Lint 内存泄漏警告
        private var instance: AppMonitor? = null
        fun getInstance(context: Context): AppMonitor {
            val appContext = context.applicationContext // 获取全局上下文
            return instance ?: synchronized(this) {
                instance ?: AppMonitor(appContext).also { instance = it } // 使用全局上下文避免内存泄漏
            }
        }
    }

    fun isEmptyMonitoredApps(): Boolean {
        loadMonitoredApps()
        return monitoredApps.isEmpty()
    }

    // 检查应用是否在被监控列表中
    private fun isMonitoredApp(packageName: String): Boolean {
        loadMonitoredApps()
        return monitoredApps.contains(packageName)
    }

    private fun setRemainingTime(packageName: String, time: Int) {
        appTimers[packageName] = time
    }

    private fun getRemainingTime(packageName: String): Int {
        return appTimers[packageName] ?: 0
    }

    fun startMonitoring() {
        isMonitoring = true
        Log.d(tag, "startMonitoring: isMonitoring = true")
    }

    fun stopMonitoring() {
        isMonitoring = false
        Log.d(tag, "stopMonitoring: isMonitoring = false")
    }

    // 加载被监控的应用列表
    private fun loadMonitoredApps() {
        val sharedPreferences = context.getSharedPreferences("AppPause", Context.MODE_PRIVATE)
        val apps = sharedPreferences.getStringSet("monitoredApps", mutableSetOf()) ?: mutableSetOf()
        monitoredApps.clear()
        monitoredApps.addAll(apps)
    }

    fun notifyForegroundApp(packageName: String?) {
        // 重新加载被监控的应用列表
        loadMonitoredApps()

        if (isMonitoring) {
            Log.d(tag, "notifyForegroundApp: isMonitoring = true")
            if (packageName != null) {
                Log.d(tag, "notifyForegroundApp: packageName = $packageName")
                // 检查应用是否在被监控列表中
                if (isMonitoredApp(packageName)) {
                    Log.d(tag, "notifyForegroundApp: isMonitoredApp = true")
                    // 获取剩余时间
                    val remainingTime = getRemainingTime(packageName)
                    // 显示倒计时弹窗
                    OverlayManager(context).showFloatingWindow(
                        remainingTime,
                        onTimeSelected = { selectedTime ->
                            setRemainingTime(packageName, selectedTime)
                            startTimer(packageName, selectedTime)
                            Log.d(tag, "notifyForegroundApp: selectedTime = $selectedTime")
                            showToast(context, "已选择 $selectedTime $timeDesc")
                        },
                        onExtendTime = { extendTime ->
                            setRemainingTime(packageName, extendTime)
                            startTimer(packageName, extendTime)
                            Log.d(tag, "notifyForegroundApp: extendTime = $extendTime")
                            showToast(context, "已延长 $extendTime $timeDesc")
                        }
                    )
                } else
                    Log.d(tag, "notifyForegroundApp: isMonitoredApp = false")
            } else
                Log.d(tag, "notifyForegroundApp: packageName = null")
        } else
            Log.d(tag, "notifyForegroundApp: isMonitoring = false")
    }

    private fun startTimer(packageName: String, time: Int) {
        val handler = Handler(Looper.getMainLooper())
        var remainingTime = time

        val runnable = object : Runnable {
            override fun run() {
                Log.d(tag, "run: 剩余时间: $remainingTime $timeDesc")
                if (remainingTime > 0) {
                    remainingTime--
                    handler.postDelayed(this, timeUnit) // 每秒/分钟执行一次
                } else {
                    setRemainingTime(packageName, 0)
                    overlayManager.showTimeoutOverlay()
                    Log.d(tag, "startTimer: 倒计时结束")
                }
            }
        }
        handler.post(runnable) // 启动倒计时任务
    }
}