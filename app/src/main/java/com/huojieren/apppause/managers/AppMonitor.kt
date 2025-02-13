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
    private val overlayManager: OverlayManager = OverlayManager(context)
    private val tag = "AppMonitor"
    private val handler = Handler(Looper.getMainLooper())
    private var currentActiveApp: String? = null // 当前前台被监控的应用包名（只会有一个活动倒计时）
    private var currentTimerRunnable: Runnable? = null // 当前正在运行的倒计时任务

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

    /**
     * 当检测到前台应用变化时调用：
     * 1. 如果packageName为null，直接不处理
     * 2. 如果packageName为被监控应用，则：
     *    - 若当前前台应用和新检测到的不一样，先暂停之前的倒计时
     *    - 若该应用已有剩余时间，则继续倒计时；否则显示倒计时弹窗
     * 3. 如果检测到的不是被监控应用，则暂停当前倒计时
     */
    fun notifyForegroundApp(packageName: String?) {
        // 重新加载被监控的应用列表
        loadMonitoredApps()
        if (isMonitoring) {
            Log.d(tag, "notifyForegroundApp: isMonitoring = true")
            if (packageName != null && packageName != "com.huojieren.apppause" && packageName != "com.huojieren.apppause.debug") {
                Log.d(tag, "notifyForegroundApp: packageName = $packageName")
                if (isMonitoredApp(packageName)) {
                    // 如果当前前台应用与新检测到的不同，先暂停之前的倒计时
                    if (currentActiveApp != null && currentActiveApp != packageName) {
                        pauseTimer(currentActiveApp!!)
                    }
                    currentActiveApp = packageName
                    // 如果该应用已保存倒计时数据，则继续倒计时
                    if (appTimers.containsKey(packageName)) {
                        Log.d(tag, "notifyForegroundApp: countdown exists for $packageName")
                        val remainingTime = getRemainingTime(packageName)
                        if (remainingTime > 0) {
                            showToast(context, "继续倒计时 $remainingTime $timeDesc")
                            startTimer(packageName, remainingTime)
                        } else {
                            appTimers.remove(packageName)
                            overlayManager.showTimeoutOverlay()
                        }
                    } else {
                        // 没有倒计时数据，则显示倒计时弹窗
                        Log.d(
                            tag,
                            "notifyForegroundApp: no countdown for $packageName, showing floating window"
                        )
                        OverlayManager(context).showFloatingWindow(
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
                    }
                } else {
                    Log.d(tag, "notifyForegroundApp: $packageName is not in monitored list")
                    // 如果前台应用不是被监控的，暂停当前倒计时
                    if (currentActiveApp != null) {
                        pauseTimer(currentActiveApp!!)
                        currentActiveApp = null
                    }
                }
            } else {
                Log.d(tag, "notifyForegroundApp: packageName = null or using App Pause")
            }
        } else {
            Log.d(tag, "notifyForegroundApp: isMonitoring = false")
        }
    }

    /**
     * 启动倒计时任务，对于指定应用从传入的剩余时间开始倒计时。
     * 如果已经有计时任务在运行，则先取消它。
     */
    private fun startTimer(packageName: String, time: Int) {
        // 取消当前任务（如果存在且是针对同一应用）
        currentTimerRunnable?.let { handler.removeCallbacks(it) }
        var remainingTime = time

        val runnable = object : Runnable {
            override fun run() {
                Log.d(tag, "run: [$packageName] 剩余时间: $remainingTime $timeDesc")
                if (remainingTime > 0) {
                    remainingTime--
                    setRemainingTime(packageName, remainingTime)
                    handler.postDelayed(this, timeUnit)
                } else {
                    appTimers.remove(packageName)
                    overlayManager.showTimeoutOverlay()
                    Log.d(tag, "startTimer: [$packageName] 倒计时结束")
                    // 倒计时结束后清除当前活动应用状态
                    if (currentActiveApp == packageName) {
                        currentActiveApp = null
                    }
                    currentTimerRunnable = null
                }
            }
        }
        currentTimerRunnable = runnable
        handler.post(runnable)
    }

    /**
     * 暂停当前针对指定应用的倒计时任务
     */
    private fun pauseTimer(packageName: String) {
        if (currentActiveApp == packageName && currentTimerRunnable != null) {
            handler.removeCallbacks(currentTimerRunnable!!)
            Log.d(tag, "pauseTimer: Timer paused for $packageName")
            currentTimerRunnable = null
        }
    }
}