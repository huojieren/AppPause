package com.huojieren.apppause.managers

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.huojieren.apppause.utils.LogUtil

class AppPauseAccessibilityService : AccessibilityService() {

    private val TAG = "AppPauseAccessibilityService"

    override fun onServiceConnected() {
        LogUtil.logDebug("AccessibilityService connected")
        configureService()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString()
            val className = event.className?.toString()
            LogUtil.logDebug("Foreground app: $packageName, $className")

            // 通知 MainActivity 或 AppMonitor 检测到前台应用
            notifyForegroundApp(packageName)
        }
    }

    override fun onInterrupt() {
        LogUtil.logDebug("AccessibilityService interrupted")
    }

    private fun configureService() {
        val serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100 // 设置事件通知的间隔时间（毫秒）
            flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
        this.serviceInfo = serviceInfo
    }

    private fun notifyForegroundApp(packageName: String?) {
        // 这里可以通过回调或广播通知 MainActivity 或 AppMonitor
        // 例如：通过 LiveData、EventBus 或直接调用 AppMonitor 的方法
        if (packageName != null) {
            // 假设 AppMonitor 有一个方法可以处理前台应用
            AppMonitor.getInstance(this).onForegroundAppDetected(packageName)
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AppPauseAccessibilityService::class.java)
            context.startService(intent)
        }
    }
}