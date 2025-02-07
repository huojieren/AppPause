package com.huojieren.apppause.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R

class NotificationManager(private val context: Context) {
    private val timeDesc = BuildConfig.TIME_DESC

    @Suppress("unused")
    fun showNotification(message: String, remainingTime: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            "AppPauseChannel",
            "AppPause Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, "AppPauseChannel")
            .setContentTitle("AppPause 提醒")
            .setContentText("$message 剩余时长: $remainingTime $timeDesc")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}