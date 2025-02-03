package com.huojieren.apppause.managers

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
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
        configureService()
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

    // TODO: 是否与xml配置重复？
    private fun configureService() {
        val serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED // 监听窗口状态改变事件
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC // 设置反馈类型为通用反馈
            notificationTimeout = 100 // 设置事件通知的间隔时间（毫秒）
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS // 包括不重要的视图
        }
        this.serviceInfo = serviceInfo
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AppPauseAccessibilityService::class.java)
            context.startService(intent)
        }
    }
}