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

    private var lastLogTime = 0L
    private var lastLogPackage: String? = null
    private val logIntervalMs = 5000L

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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentTime = System.currentTimeMillis()
        val hasExceededLogInterval = currentTime - lastLogTime > logIntervalMs

        if (!statusManager.isMonitoring.value) {
            if (hasExceededLogInterval

            ) {
                lastLogTime = currentTime
                logRepository.log(tag, "onAccessibilityEvent: isMonitoring: false")
            }
            return
        }
        if (event == null) {
            if (hasExceededLogInterval

            ) {
                lastLogTime = currentTime
                logRepository.log(tag, "onAccessibilityEvent: event is null")
            }
            return
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val topPackage = instance?.rootInActiveWindow?.packageName?.toString()
            if (topPackage != null && (topPackage != lastLogPackage || hasExceededLogInterval

                        )
            ) {
                lastLogPackage = topPackage
                lastLogTime = currentTime
                logRepository.log(tag, "onAccessibilityEvent: topPackage $topPackage")
            }
            topPackage?.let { onAppChangedListener?.invoke(topPackage) }
        }
    }

    override fun onInterrupt() {
        logRepository.log(tag, "AccessibilityService interrupted")
        statusManager.setHasAccessibility(false)
    }
}