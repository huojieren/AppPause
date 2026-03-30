package com.huojieren.apppause.managers

import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class StatusManager {
    private val tag = "StatusManager"

    /**
     * 状态数据 (仅内存，不持久化)
     * isMonitoring - 默认 false，异常退出时保持 false，需要用户重新开始;
     * 权限状态 - 每次启动从系统检查;
     */

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _hasOverlay = MutableStateFlow(false)
    val hasOverlay: StateFlow<Boolean> = _hasOverlay.asStateFlow()

    private val _hasNotification = MutableStateFlow(false)
    val hasNotification: StateFlow<Boolean> = _hasNotification.asStateFlow()

    private val _hasUsageStats = MutableStateFlow(false)
    val hasUsageStats: StateFlow<Boolean> = _hasUsageStats.asStateFlow()

    private val _hasAccessibility = MutableStateFlow(false)
    val hasAccessibility: StateFlow<Boolean> = _hasAccessibility.asStateFlow()

    fun setIsMonitoring(value: Boolean) {
        _isMonitoring.value = value
        logger(tag, "setIsMonitoring: $value")
    }

    fun setHasOverlay(value: Boolean) {
        _hasOverlay.value = value
    }

    fun setHasNotification(value: Boolean) {
        _hasNotification.value = value
    }

    fun setHasUsageStats(value: Boolean) {
        _hasUsageStats.value = value
    }

    fun setHasAccessibility(value: Boolean) {
        _hasAccessibility.value = value
    }
}
