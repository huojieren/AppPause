package com.huojieren.apppause

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.content.edit
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.ui.AppPauseApp
import com.huojieren.apppause.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preWarmNotificationChannels()

        enableEdgeToEdge()

        setContent {
            val isDarkTheme = isSystemInDarkTheme()

            SideEffect {
                window.insetsController?.setSystemBarsAppearance(
                    if (isDarkTheme) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }

            AppTheme {
                AppPauseApp()
            }
        }
    }

    private fun preWarmNotificationChannels() {
        logger(tag, "preWarmNotificationChannels start")

        val notificationManager = getSystemService(NotificationManager::class.java)

        val monitorChannel = NotificationChannel(
            "monitor_channel",
            "监控保活通知",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "开启通知以保证监控服务存活" }

        notificationManager.createNotificationChannel(monitorChannel)

        logger(tag, "preWarmNotificationChannels: channel created")
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences("app_status", MODE_PRIVATE)
            .edit { putBoolean("normal_exit", true) }
    }
}