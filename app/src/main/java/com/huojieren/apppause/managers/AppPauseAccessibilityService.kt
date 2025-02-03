package com.huojieren.apppause.managers

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class AppPauseAccessibilityService : AccessibilityService() {

    private val TAG = "AppPauseAccessibilityService"
    private lateinit var appMonitor: AppMonitor

    override fun onServiceConnected() {
        Log.d(TAG, "onServiceConnected: 无障碍服务已连接")
        appMonitor = AppMonitor.getInstance(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            val className = event.className?.toString()
            Log.d(TAG, "onAccessibilityEvent: 当前前台应用: $packageName, $className")
            // 通知 AppMonitor 检测到前台应用
            appMonitor.notifyForegroundApp(packageName)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "onInterrupt: 无障碍服务中断")
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AppPauseAccessibilityService::class.java)
            context.startService(intent)
        }
    }
}