package com.huojieren.apppause.managers

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.service.MonitorService
import com.huojieren.apppause.utils.LogUtil
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast

class AppMonitor(private val context: Context) {

    var isMonitoring: Boolean = false

    private val monitoredAppMap = mutableSetOf<String>() // 被监控的应用列表
    private val appTimerMap = mutableMapOf<String, Int>() // 存储每个应用的剩余时长
    private val timeUnit = BuildConfig.TIME_UNIT // 从 BuildConfig 中获取计时单位
    private val timeDesc = BuildConfig.TIME_DESC // 从 BuildConfig 中获取计时单位描述
    private val overlayManager = OverlayManager(context)
    private val tag = "AppMonitor" // 日志记录标签
    private val handler = Handler(Looper.getMainLooper()) // 用于处理定时器的Handler
    private val runningTimerRunnableMap = mutableMapOf<String, Runnable>() // 存储每个应用的倒计时任务
    private var monitorRunnable: Runnable? = null // 用于启动和停止监控的Runnable
    private var currentMonitorApp: String? = null // 当前前台被监控的应用包名（只会有一个活动倒计时）
    private var lastDetectedApp: String? = null // 用于记录上一次被检测到的应用包名

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
        return monitoredAppMap.isEmpty()
    }

    private fun loadMonitoredApps() {
        val sharedPreferences = context.getSharedPreferences("AppPause", Context.MODE_PRIVATE)
        val apps = sharedPreferences.getStringSet("monitoredApps", mutableSetOf()) ?: mutableSetOf()
        monitoredAppMap.clear()
        monitoredAppMap.addAll(apps)
    }

    private fun isMonitoredApp(packageName: String): Boolean {
        loadMonitoredApps()
        return monitoredAppMap.contains(packageName)
    }

    private fun isValidApp(packageName: String?): Boolean {
        return when {
            packageName.isNullOrEmpty() -> false
            packageName.startsWith("com.huojieren.apppause") -> false
            else -> true
        }
    }

    fun startMonitoring() {
        if (isMonitoring) return

        LogUtil(context).log(tag, "[STATE] 启动应用监控")
        // 创建监控任务
        monitorRunnable = object : Runnable {
            override fun run() {
                // 获取当前前台应用包名
                val currentApp = getForegroundApp(context)
                if (currentApp != lastDetectedApp) {
                    lastDetectedApp = currentApp
                    checkForegroundApp(currentApp)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(monitorRunnable!!)
        context.startService(MonitorService.getServiceIntent(context))
        isMonitoring = true
    }

    fun stopMonitoring() {
        if (!isMonitoring) return

        LogUtil(context).log(tag, "[STATE] 停止应用监控")

        // 停止所有定时器
        runningTimerRunnableMap.values.forEach {
            handler.removeCallbacks(it)
        }
        runningTimerRunnableMap.clear()

        // 停止主监控循环
        monitorRunnable?.let {
            handler.removeCallbacks(it)
            monitorRunnable = null
        }

        // 停止服务
        context.stopService(MonitorService.getServiceIntent(context))
        isMonitoring = false

        // 重置状态变量
        currentMonitorApp = null
        lastDetectedApp = null
    }

    private fun getForegroundApp(context: Context): String? {
        // 获取UsageStatsManager实例
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        // 获取当前时间作为查询的结束时间
        val endTime = System.currentTimeMillis()
        // 设置查询的开始时间为当前时间往前1秒
        val beginTime = endTime - 1000
        // 查询过去1秒时间范围内的事件
        val events = usageStatsManager.queryEvents(beginTime, endTime)
        // 用于存储最近被移到前台的应用
        var lastForegroundApp: String? = null
        // 用于存储事件
        val event = UsageEvents.Event()
        // 用于存储最近有效事件的时间戳
        var lastValidTime = 0L
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            // 如果事件类型为 ACTIVITY_RESUMED 且时间戳大于最近有效时间，则更新
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                && event.timeStamp > lastValidTime
            ) {
                // 更新最近有效时间戳
                lastValidTime = event.timeStamp
                // 更新最近被移到前台的应用
                lastForegroundApp = event.packageName
            }
        }
        // 如果最近有效事件的时间戳距离当前时间小于1秒，则返回最近被移到前台的应用
        return if (System.currentTimeMillis() - lastValidTime < 1000)
            lastForegroundApp
        else
            null
    }

    fun checkForegroundApp(packageName: String?) {
        when {
            !isValidApp(packageName) -> {
                LogUtil(context).log(
                    tag,
                    "[DEBUG] 无效应用" +
                            " | 包名=${packageName}" +
                            " | 原因=${if (packageName.isNullOrEmpty()) "空包名" else "自身应用"}"
                )
            }

            !isMonitoredApp(packageName!!) -> {
                LogUtil(context).log(
                    tag,
                    "[DEBUG] 非监控应用" +
                            " | 包名=${packageName}" +
                            " | 已监控应用数=${monitoredAppMap.size}"
                )
            }

            else -> {
                LogUtil(context).log(
                    tag,
                    "[STATE] 开始处理监控应用" +
                            " | 包名=${packageName}" +
                            " | 剩余时间=${appTimerMap[packageName] ?: 0}s"
                )
            }
        }

        // 重新加载被监控的应用列表
        loadMonitoredApps()
        // 检查应用是否有效
        if (!isValidApp(packageName)) {
            return
        }
        // 如果当前正在监控的应用和当前前台应用不同，则暂停当前正在监控应用的倒计时
        if (currentMonitorApp != null && currentMonitorApp != packageName) {
            pauseTimer(currentMonitorApp!!)
        }
        // 检查是否为正在监控的应用
        if (!isMonitoredApp(packageName!!)) {
            return
        }
        // 获取该应用剩余时间
        val remainingTime = appTimerMap[packageName] ?: 0
        if (remainingTime > 0) {
            // 如果该应用有剩余时间，继续倒计时
            startTimer(packageName, remainingTime)
            currentMonitorApp = packageName
            startMonitoring()
        } else {
            // 显示悬浮窗让用户设置时间
            OverlayManager(context).showFloatingWindow(
                onDisMiss = {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    LogUtil(context).log(tag, "[STATE] 取消计时，回到桌面")
                    startMonitoring()
                },
                onTimeSelected = { selectedTime ->
                    appTimerMap[packageName] = selectedTime
                    startTimer(packageName, selectedTime)
                    showToast(context, "已选择 $selectedTime $timeDesc")
                    currentMonitorApp = packageName
                    startMonitoring()
                },
                onExtendTime = { extendTime ->
                    appTimerMap[packageName] = extendTime
                    startTimer(packageName, extendTime)
                    showToast(context, "已延长 $extendTime $timeDesc")
                    currentMonitorApp = packageName
                    startMonitoring()
                }
            )
        }
    }

    private fun startTimer(packageName: String, time: Int) {
        // 初始化 remainingTime 为传入的时间
        var remainingTime = time
        // 记录上次日志时间
        // 取消当前任务（如果存在且是针对同一应用）
        runningTimerRunnableMap[packageName]?.let { handler.removeCallbacks(it) }
        // 创建一个 Runnable 对象，用于执行倒计时逻辑
        val timerRunnable = object : Runnable {
            override fun run() {
                // 如果剩余时间大于0，则继续倒计时
                if (remainingTime > 0) {
                    // 仅在特殊节点记录
                    if (((time - remainingTime) * 100 / time) % 10 == 0) {
                        LogUtil(context).log(
                            tag,
                            "[DEBUG] [$packageName] 剩余时间=${remainingTime}" + timeDesc +
                                    " | 总时长=${time}" + timeDesc +
                                    " | 进度=${(time - remainingTime) * 100 / time}%"
                        )
                    }
                    // 更新剩余时间，并保存到 appTimerMap 中
                    remainingTime--
                    appTimerMap[packageName] = remainingTime
                    // 延迟1个时间单位执行下一次倒计时
                    handler.postDelayed(this, timeUnit)
                } else {
                    LogUtil(context).log(
                        tag,
                        "[STATE] [$packageName] 倒计时结束" +
                                " | 总耗时=${time}" + timeDesc
                    )
                    // 如果剩余时间小于等于0，则倒计时结束，显示悬浮窗，重置相关变量
                    appTimerMap.remove(packageName)
                    overlayManager.showTimeoutOverlay()
                    currentMonitorApp = null
                    runningTimerRunnableMap.remove(packageName)
                }
            }
        }
        runningTimerRunnableMap[packageName] = timerRunnable
        handler.post(timerRunnable)
    }

    private fun pauseTimer(packageName: String) {
        runningTimerRunnableMap[packageName]?.let {
            handler.removeCallbacks(it)
        }
        showToast(context, "暂停倒计时")
        LogUtil(context).log(
            tag,
            "[STATE] [$packageName] 暂停倒计时"
        )
    }
}