package com.huojieren.apppause.managers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast

class AppMonitor(private val context: Context) {

    private val monitoredApps = mutableSetOf<String>() // 被监控的应用列表
    private val appTimers = mutableMapOf<String, Int>() // 存储每个应用的剩余时长
    private val timeUnit = BuildConfig.TIME_UNIT // 从 BuildConfig 中获取计时单位
    private val timeDesc = BuildConfig.TIME_DESC // 从 BuildConfig 中获取计时单位描述
    private val overlayManager = OverlayManager(context)
    private var isMonitoring: Boolean = false
    private val TAG = "AppMonitor"

    // 单例模式
    companion object {
        @Volatile
        private var instance: AppMonitor? = null

        fun getInstance(context: Context): AppMonitor {
            return instance ?: synchronized(this) {
                instance ?: AppMonitor(context).also { instance = it }
            }
        }
    }

    init {
        // 初始化时加载被监控的应用列表
        loadMonitoredApps()
    }

    fun isEmptyMonitoredApps(): Boolean {
        return monitoredApps.isEmpty()
    }

    // 检查应用是否在被监控列表中
    private fun isMonitoredApp(packageName: String): Boolean {
        return monitoredApps.contains(packageName)
    }

    private fun setRemainingTime(packageName: String, time: Int) {
        appTimers[packageName] = time
    }

    private fun getRemainingTime(packageName: String): Int {
        return appTimers[packageName] ?: 0
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
        // 从 SharedPreferences 中读取 isMonitoring
        val sharedPreferences = context.getSharedPreferences("AppPause", Context.MODE_PRIVATE)
        isMonitoring = sharedPreferences.getBoolean("isMonitoring", false)

        if (isMonitoring) {
            Log.d(TAG, "notifyForegroundApp: isMonitoring = true")
            if (packageName != null) {
                Log.d(TAG, "notifyForegroundApp: packageName = $packageName")
                // 检查应用是否在被监控列表中
                if (isMonitoredApp(packageName)) {
                    Log.d(TAG, "notifyForegroundApp: isMonitoredApp = true")
                    // 获取剩余时间
                    val remainingTime = getRemainingTime(packageName)
                    // 显示倒计时弹窗
                    OverlayManager(context).showFloatingWindow(
                        remainingTime,
                        onTimeSelected = { selectedTime ->
                            setRemainingTime(packageName, selectedTime)
                            startTimer(packageName, selectedTime)
                            Log.d(TAG, "notifyForegroundApp: selectedTime = $selectedTime")
                            showToast(context, "已选择 $selectedTime $timeDesc")
                        },
                        onExtendTime = { extendTime ->
                            setRemainingTime(packageName, extendTime)
                            startTimer(packageName, extendTime)
                            Log.d(TAG, "notifyForegroundApp: extendTime = $extendTime")
                            showToast(context, "已延长 $extendTime $timeDesc")
                        }
                    )
                } else
                    Log.d(TAG, "notifyForegroundApp: isMonitoredApp = false")
            } else
                Log.d(TAG, "notifyForegroundApp: packageName = null")
        } else
            Log.d(TAG, "notifyForegroundApp: isMonitoring = false")
    }

    private fun startTimer(packageName: String, time: Int) {
        val handler = Handler(Looper.getMainLooper())
        var remainingTime = time // 剩余时间

        // 创建日志输出任务
        val logRunnable = object : Runnable {
            override fun run() {
                Log.d(TAG, "run: 剩余时间: $remainingTime $timeDesc")
                if (remainingTime > 0) {
                    remainingTime--
                    handler.postDelayed(this, timeUnit) // 每秒/分钟执行一次
                }
            }
        }
        // 启动日志输出任务
        handler.post(logRunnable)

        // 创建倒计时结束任务
        val timerRunnable = Runnable {
            overlayManager.showTimeoutOverlay()
            setRemainingTime(packageName, 0)
            Log.d(TAG, "startTimer: 倒计时结束")
        }
        // 将选定的时间转换为毫秒
        val delayMillis = if (BuildConfig.DEBUG) time * 1000L else time * 60 * 1000L
        // 延迟执行倒计时结束任务
        handler.postDelayed(timerRunnable, delayMillis)
    }
}