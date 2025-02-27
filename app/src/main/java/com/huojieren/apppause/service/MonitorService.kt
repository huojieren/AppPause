package com.huojieren.apppause.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.huojieren.apppause.R
import com.huojieren.apppause.managers.AppMonitor

class MonitorService : Service() {

    // 电源锁相关
    private lateinit var wakeLock: PowerManager.WakeLock
    private val channelId = "monitor_channel"
    private val notificationId = 1
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(notificationId, notification)
        AppMonitor.getInstance(this).startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        releaseWakeLock()
        AppMonitor.getInstance(this).stopMonitoring()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "App Pause监控留存通知",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "显示应用使用监控状态"
        }
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("应用使用监控中")
            .setContentText("正在监控已选应用")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AppPause::MonitorWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L) // 10分钟超时
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    wakeLock.acquire(10 * 60 * 1000L)
                }
                handler.postDelayed(this, 9 * 60 * 1000L) // 每9分钟刷新
            }
        }, 9 * 60 * 1000L)
    }

    private fun releaseWakeLock() {
        if (::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
