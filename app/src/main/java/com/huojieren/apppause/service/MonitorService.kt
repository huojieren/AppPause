package com.huojieren.apppause.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.huojieren.apppause.MainActivity
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.MonitorIntent
import com.huojieren.apppause.data.diagnostics.DiagnosticsManager
import com.huojieren.apppause.data.diagnostics.model.DiagnosticEvent
import com.huojieren.apppause.data.diagnostics.model.ProcessState
import com.huojieren.apppause.data.logging.AppLog.logger
import com.huojieren.apppause.data.repository.SettingsRepository
import com.huojieren.apppause.managers.AppManager
import com.huojieren.apppause.managers.MonitorManager
import com.huojieren.apppause.managers.StatusManager
import com.huojieren.apppause.managers.TimerManager
import com.huojieren.apppause.monitor.AccessibilityMonitor
import com.huojieren.apppause.monitor.ForegroundAppMonitor
import com.huojieren.apppause.monitor.ForegroundAppMonitor.MonitorStrategy
import com.huojieren.apppause.monitor.UsageStatsMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MonitorService : Service() {

    companion object {
        private const val ACTION_STOP_MONITORING =
            "com.huojieren.apppause.action.STOP_MONITORING"
    }

    @Inject
    lateinit var monitorManager: MonitorManager

    @Inject
    lateinit var appManager: AppManager

    @Inject
    lateinit var statusManager: StatusManager

    @Inject
    lateinit var timerManager: TimerManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var diagnosticsManager: DiagnosticsManager

    private val tag = "MonitorService"

    // 唤醒锁
    private lateinit var wakeLock: PowerManager.WakeLock

    // 前台通知
    private val channelId = "monitor_channel"
    private val notificationId = 1
    private lateinit var notificationManager: NotificationManager

    // 与 Service 生命周期绑定的协程作用域
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private var timerStateJob: Job? = null
    private var wakeLockRefreshJob: Job? = null

    // 监控策略
    private var monitor: ForegroundAppMonitor? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        logger(tag, "onCreate start")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        acquireWakeLock()
        diagnosticsManager.recordEvent(DiagnosticEvent("monitor_service_created"))
        diagnosticsManager.updateProcessState(
            ProcessState(
                source = "monitor_service_created",
                monitoring = statusManager.isMonitoring.value,
                accessibilityConnected = AppPauseAccessibilityService.isInitialized(),
                monitorServiceActive = true
            )
        )
        logger(tag, "onCreate end")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logger(tag, "onStartCommand start, intent=$intent")

        if (intent?.action == ACTION_STOP_MONITORING) {
            stopMonitoringFromNotification()
            return START_NOT_STICKY
        }

        if (intent == null && getStoredMonitorIntent() == MonitorIntent.Disabled) {
            logger(tag, "sticky restart ignored because monitor intent is disabled")
            stopSelf()
            return START_NOT_STICKY
        }

        // 读取监控策略
        val strategy = intent?.getStringExtra("strategy") ?: getStoredMonitorStrategy().name
        logger(tag, "get strategy: [$strategy]")

        monitor?.stop()
        monitor = null

        // 创建检测器
        monitor = when (strategy) {
            MonitorStrategy.ACCESSIBILITY.name -> {
                if (AppPauseAccessibilityService.isInitialized()) {
                    logger(tag, "Creating AccessibilityMonitor")
                    AccessibilityMonitor(appManager, serviceScope)
                } else {
                    logger(
                        tag,
                        "Accessibility service not initialized, falling back to UsageStats"
                    )
                    UsageStatsMonitor(
                        this,
                        appManager
                    )
                }
            }

            MonitorStrategy.USAGE_STATS.name -> UsageStatsMonitor(
                this,
                appManager
            )

            else -> UsageStatsMonitor(
                this,
                appManager
            )
        }

        val strategyName = when (monitor) {
            is UsageStatsMonitor -> "应用使用情况"
            is AccessibilityMonitor -> "无障碍服务"
            else -> "Unknown"
        }
        diagnosticsManager.recordEvent(
            DiagnosticEvent(
                "monitor_service_started",
                mapOf(
                    "intentNull" to (intent == null).toString(),
                    "flags" to flags.toString(),
                    "startId" to startId.toString(),
                    "strategy" to strategy
                )
            )
        )
        diagnosticsManager.updateProcessState(
            ProcessState(
                source = "monitor_service_started",
                monitoring = true,
                accessibilityConnected = AppPauseAccessibilityService.isInitialized(),
                monitorServiceActive = true,
                monitorStrategy = strategy
            )
        )

        // 启动前台通知
        logger(tag, "startForeground start")
        startForeground(
            notificationId,
            buildMonitorNotification("正在使用${strategyName}监控已选应用")
        )
        logger(tag, "startForeground end")

        // 启动检测器
        monitor?.start()
        monitorManager.startCollecting(monitor!!, serviceScope)

        // 收集倒计时状态并更新通知
        timerStateJob?.cancel()
        timerStateJob = serviceScope.launch {
            timerManager.currentTimerState.collect { timerState ->
                if (timerState != null) {
                    // 根据 isRunning 判断是正在计时还是暂停
                    updateNotification(
                        timerState.appName,
                        timerState.remainingTimeMs,
                        timerState.isRunning,
                        timerState.isSharedTimingEnabled
                    )
                } else {
                    // 没有正在计时的应用（倒计时结束），显示初始状态
                    logger(tag, "Timer finished, showing initial notification")
                    showInitialNotification()
                }
            }
        }

        // 返回 START_STICKY 以保持服务
        return START_STICKY
    }

    override fun onDestroy() {
        logger(tag, "destroy notification")
        diagnosticsManager.recordEvent(
            DiagnosticEvent(
                "monitor_service_destroyed",
                mapOf(
                    "monitorClass" to (monitor?.javaClass?.simpleName ?: "null"),
                    "wakeLockHeld" to if (::wakeLock.isInitialized) wakeLock.isHeld.toString() else "uninitialized"
                )
            )
        )
        monitorManager.stopCollecting()
        timerStateJob?.cancel()
        wakeLockRefreshJob?.cancel()
        serviceJob.cancel()
        // 停止检测器
        monitor?.stop()
        monitor = null

        // 释放唤醒锁
        releaseWakeLock()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }

        // 清除监控状态
        statusManager.setIsMonitoring(false)
        diagnosticsManager.updateProcessState(
            ProcessState(
                source = "monitor_service_destroyed",
                monitoring = false,
                accessibilityConnected = AppPauseAccessibilityService.isInitialized(),
                monitorServiceActive = false
            )
        )

        super.onDestroy()
    }

    private fun createNotificationChannel() {
        logger(tag, "createNotificationChannel start")
        val channel = NotificationChannel(
            channelId,
            "监控保活通知",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "开启通知以保证监控服务存活" }

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
        logger(tag, "createNotificationChannel end")
    }

    private fun buildMonitorNotification(contentText: String) =
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("应用使用监控中")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(createOpenAppPendingIntent())
            .addAction(
                R.drawable.ic_notification,
                "停止监控",
                createStopMonitoringPendingIntent()
            )
            .build()

    private fun createOpenAppPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createStopMonitoringPendingIntent(): PendingIntent {
        val intent = Intent(this, MonitorService::class.java).apply {
            action = ACTION_STOP_MONITORING
        }
        return PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun stopMonitoringFromNotification() {
        logger(tag, "stop monitoring from notification")
        runBlocking {
            settingsRepository.setMonitorIntent(MonitorIntent.Disabled)
        }
        timerManager.clearAllTimers()
        statusManager.setIsMonitoring(false)
        stopSelf()
    }

    private fun getStoredMonitorIntent(): MonitorIntent = runBlocking {
        settingsRepository.getMonitorIntent().first()
    }

    private fun getStoredMonitorStrategy(): MonitorStrategy = runBlocking {
        settingsRepository.getMonitorStrategy().first()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AppPause::MonitorWakeLock")
        // 永久 acquire 更稳，这里 10 分钟后会自动过期
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L)
        }
        // 周期性续租，避免被系统回收（仅刷新，不释放长时间不安全）
        wakeLockRefreshJob?.cancel()
        wakeLockRefreshJob = serviceScope.launch {
            while (isActive) {
                delay(9 * 60 * 1000L)
                try {
                    if (wakeLock.isHeld) {
                        wakeLock.release()
                    }
                    wakeLock.acquire(10 * 60 * 1000L)
                } catch (e: Exception) {
                    logger(tag, "wakelock refresh failed: ${e.message}")
                }
            }
        }
    }

    private fun releaseWakeLock() {
        try {
            if (::wakeLock.isInitialized && wakeLock.isHeld) {
                wakeLock.release()
            }
        } catch (e: Exception) {
            logger(tag, "releaseWakeLock error: ${e.message}")
        }
    }

    private fun updateNotification(
        appName: String,
        remainingTimeMs: Long,
        isValid: Boolean,
        isSharedTimingEnabled: Boolean
    ) {
        val remainingSeconds = remainingTimeMs / 1000
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = if (minutes > 0) {
            "${minutes}分${seconds}秒"
        } else {
            "${seconds}秒"
        }

        val targetName = if (isSharedTimingEnabled) "本次使用时长" else appName
        val contentText = if (isValid) {
            "$targetName 剩余 $timeText"
        } else {
            "$targetName 已暂停，剩余 $timeText"
        }

        logger(
            tag,
            "updateNotification: appName=$appName, remaining=$remainingTimeMs, isValid=$isValid, content=$contentText"
        )

        val notification = buildMonitorNotification(contentText)

        notificationManager.notify(notificationId, notification)
    }

    private fun showInitialNotification() {
        val notification = buildMonitorNotification("暂无检测到的应用")

        notificationManager.notify(notificationId, notification)
    }
}
