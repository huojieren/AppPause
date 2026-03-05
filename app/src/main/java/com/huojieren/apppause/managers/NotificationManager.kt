package com.huojieren.apppause.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat

import com.huojieren.apppause.R
import com.huojieren.apppause.data.repository.LogRepository

class NotificationManager(
    private val context: Context,
    private val logRepository: LogRepository
) {
    private val timeDesc = "秒"
    private val tag = "NotificationManager"

    companion object {
        private const val CHANNEL_ID = "AppPauseChannel"
        private const val NOTIFICATION_ID = 1
    }

    /**
     * 显示通知
     */
    fun showNotification(message: String, remainingTime: Int) {
        // TODO 2026/1/31 23:12 创建应用剩余时间通知
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 创建通知渠道（Android 8.0及以上版本）
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("AppPause 提醒")
                .setContentText("$message 剩余时长: $remainingTime $timeDesc")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            logRepository.log(tag, "Failed to show notification: ${e.message}", Log.ERROR)
        }
    }

    /**
     * 隐藏通知
     */
    fun hideNotification() {
        try {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            logRepository.log(tag, "Failed to hide notification: ${e.message}", Log.ERROR)
        }
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "AppPause Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "AppPause 应用使用提醒"
        }
        notificationManager.createNotificationChannel(channel)
    }
}