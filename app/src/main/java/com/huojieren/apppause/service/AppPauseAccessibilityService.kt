package com.huojieren.apppause.service

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import com.huojieren.apppause.data.diagnostics.DiagnosticsManager
import com.huojieren.apppause.data.diagnostics.model.DiagnosticEvent
import com.huojieren.apppause.data.diagnostics.model.ProcessState
import com.huojieren.apppause.data.logging.AppLog.logger
import com.huojieren.apppause.managers.StatusManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@SuppressLint("AccessibilityPolicy")// 忽略无障碍服务隐私警告
@AndroidEntryPoint
class AppPauseAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var statusManager: StatusManager

    @Inject
    lateinit var diagnosticsManager: DiagnosticsManager

    private val tag = "AppPauseAccessibilityService"

    private var lastLogTime = 0L
    private var lastLogPackage: String? = null
    private val logIntervalMs = 5000L

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: AppPauseAccessibilityService? = null

        private val _windowChangedEvent = MutableSharedFlow<String>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        val windowChangedEvent: SharedFlow<String> = _windowChangedEvent.asSharedFlow()

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
        logger(tag, "AccessibilityService connected")
        instance = this
        statusManager.setHasAccessibility(true)
        diagnosticsManager.recordEvent(DiagnosticEvent("accessibility_service_connected"))
        diagnosticsManager.updateProcessState(
            ProcessState(
                source = "accessibility_service_connected",
                monitoring = statusManager.isMonitoring.value,
                accessibilityConnected = true,
                monitorServiceActive = statusManager.isMonitoring.value
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        logger(tag, "AccessibilityService destroyed")
        instance = null
        statusManager.setHasAccessibility(false)
        diagnosticsManager.recordEvent(
            DiagnosticEvent(
                "accessibility_service_destroyed",
                mapOf("lastLogTime" to lastLogTime.toString())
            )
        )
        diagnosticsManager.updateProcessState(
            ProcessState(
                source = "accessibility_service_destroyed",
                monitoring = statusManager.isMonitoring.value,
                accessibilityConnected = false,
                monitorServiceActive = statusManager.isMonitoring.value
            )
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentTime = System.currentTimeMillis()
        val hasExceededLogInterval = currentTime - lastLogTime > logIntervalMs

        if (!statusManager.isMonitoring.value) {
            if (hasExceededLogInterval

            ) {
                lastLogTime = currentTime
                logger(tag, "onAccessibilityEvent: isMonitoring: false")
            }
            return
        }
        if (event == null) {
            if (hasExceededLogInterval

            ) {
                lastLogTime = currentTime
                logger(tag, "onAccessibilityEvent: event is null")
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
                logger(tag, "onAccessibilityEvent: topPackage [$topPackage]")
            }
            topPackage?.let { _windowChangedEvent.tryEmit(it) }
        }
    }

    override fun onInterrupt() {
        logger(tag, "AccessibilityService interrupted")
        diagnosticsManager.recordEvent(DiagnosticEvent("accessibility_feedback_interrupted"))
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logger(tag, "AccessibilityService unbound")
        diagnosticsManager.recordEvent(
            DiagnosticEvent(
                "accessibility_service_unbound",
                mapOf("lastLogTime" to lastLogTime.toString())
            )
        )
        instance = null
        statusManager.setHasAccessibility(false)
        diagnosticsManager.updateProcessState(
            ProcessState(
                source = "accessibility_service_unbound",
                monitoring = statusManager.isMonitoring.value,
                accessibilityConnected = false,
                monitorServiceActive = statusManager.isMonitoring.value
            )
        )
        return super.onUnbind(intent)
    }
}
