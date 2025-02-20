package com.huojieren.apppause.managers

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.huojieren.apppause.utils.LogUtil

class AppPauseAccessibilityService : AccessibilityService() {

    private val tag = "AppPauseAccessibilityService"
    private lateinit var appMonitor: AppMonitor

    override fun onServiceConnected() {
        LogUtil(this).d(tag, "onServiceConnected: 无障碍服务已连接")
        appMonitor = AppMonitor.getInstance(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            val className = event.className?.toString()
            LogUtil(this).d(tag, "onAccessibilityEvent: 当前前台应用: $packageName, $className")
            // 通知 AppMonitor 检测到前台应用
            appMonitor.notifyForegroundApp(packageName)
        }
    }

    override fun onInterrupt() {
        LogUtil(this).d(tag, "onInterrupt: 无障碍服务中断")
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AppPauseAccessibilityService::class.java)
            context.startService(intent)
        }
    }
}