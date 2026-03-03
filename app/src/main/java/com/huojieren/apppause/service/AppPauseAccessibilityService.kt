package com.huojieren.apppause.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.view.accessibility.AccessibilityEvent
import com.huojieren.apppause.data.repository.LogRepository
import com.huojieren.apppause.managers.StatusManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("AccessibilityPolicy")// 忽略无障碍服务隐私警告
@AndroidEntryPoint
class AppPauseAccessibilityService : AccessibilityService() {
    @Inject
    lateinit var logRepository: LogRepository

    @Inject
    lateinit var statusManager: StatusManager
    private val tag = "AppPauseAccessibilityService"

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: AppPauseAccessibilityService? = null

        private var onAppChangedListener: ((String) -> Unit)? = null

        fun setOnAppChangedListener(listener: (String) -> Unit) {
            onAppChangedListener = listener
        }

        fun removeOnAppChangedListener() {
            onAppChangedListener = null
        }

        fun getInstance(): AppPauseAccessibilityService {
            return instance
                ?: throw IllegalStateException("AppPauseAccessibilityService is not initialized")
        }

        fun isInitialized(): Boolean {
            return instance != null
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        logRepository.log(tag, "AccessibilityService connected")
        instance = this
        statusManager.setHasAccessibility(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        logRepository.log(tag, "AccessibilityService destroyed")
        instance = null
        statusManager.setHasAccessibility(false)
    }

    @Deprecated("监听事件会频繁监听到系统隐藏组件比如的变化，影响判断，使用")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!statusManager.isMonitoring.value) {
            logRepository.log(tag, "onAccessibilityEvent: isMonitoring: false")
            return
        }
        if (event == null) {
            logRepository.log(tag, "onAccessibilityEvent: event is null")
            return
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            event.packageName?.toString()
            // TODO 2025/12/12 14:00 配置 debug 版本日志输出
//            eventPackage?.let {
//                logRepository.log(tag, "onAccessibilityEvent: ${event.eventType} $event Package")
//            }
            val topPackage = instance?.rootInActiveWindow?.packageName?.toString()
            logRepository.log(tag, "onAccessibilityEvent: topPackage $topPackage")
            topPackage?.let { onAppChangedListener?.invoke(topPackage) }
        }
    }

    override fun onInterrupt() {
        logRepository.log(tag, "AccessibilityService interrupted")
        statusManager.setHasAccessibility(false)
    }
}