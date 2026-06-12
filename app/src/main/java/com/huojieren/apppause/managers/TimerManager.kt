package com.huojieren.apppause.managers

import android.content.Context
import android.util.Log
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.models.TimerTimeoutInfo
import com.huojieren.apppause.data.models.TimerTodoPrompt
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.data.repository.SettingsRepository
import com.huojieren.apppause.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TimerManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val scope: CoroutineScope
) {
    private val tag = "TimerManager"
    private val sharedTimerKey = "__shared_timer__"
    private var perAppTimingEnabled = true

    // 缓存的设置值（避免在start()中阻塞）
    private var cachedWaitBeforeReturnEnabled = false
    private var cachedWaitBeforeReturnSeconds = SettingsRepository.DEFAULT_WAIT_BEFORE_RETURN_SECONDS
    private var cachedTodoPromptEnabled = false

    // 使用可变Map来存储倒计时状态
    private val timerStateMap = mutableMapOf<String, TimerState>()

    // 每个倒计时对应的 Job，用于取消
    private val timerJobs = mutableMapOf<String, Job>()

    // 当前正在计时的应用及剩余时间
    private val _currentTimerState = MutableStateFlow<TimerDisplayState?>(null)
    val currentTimerState: StateFlow<TimerDisplayState?> = _currentTimerState.asStateFlow()

    private val _timeOutEvent = MutableSharedFlow<TimerTimeoutInfo>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val timeOutEvent: SharedFlow<TimerTimeoutInfo> = _timeOutEvent.asSharedFlow()

    // 日志控制
    private var logCounter = 0
    private val logInterval = 5

    init {
        scope.launch { loadSettings() }
    }

    private suspend fun loadSettings() {
        try {
            cachedWaitBeforeReturnEnabled = settingsRepository.getWaitBeforeReturnEnabled().first()
            cachedWaitBeforeReturnSeconds = settingsRepository.getWaitBeforeReturnSeconds().first()
            cachedTodoPromptEnabled = settingsRepository.getTodoPromptEnabled().first()
            logger(tag, "Settings loaded: waitBeforeReturn=$cachedWaitBeforeReturnEnabled, waitSeconds=$cachedWaitBeforeReturnSeconds, todoPrompt=$cachedTodoPromptEnabled")
        } catch (e: Exception) {
            logger(tag, "Failed to load settings: ${e.message}")
        }
    }

    fun refreshSettings() {
        scope.launch { loadSettings() }
    }

    data class TimerDisplayState(
        val packageName: String,
        val appName: String,
        val remainingTimeMs: Long,
        val isRunning: Boolean,
        val isSharedTimingEnabled: Boolean
    )

    data class TimerState(
        var remainingTime: Long,
        var isRunning: Boolean = false,
        var startTime: Long = 0,
        var appInfo: AppInfo? = null,
        var todoPrompt: TimerTodoPrompt? = null,
        var isWaitBeforeReturnEnabled: Boolean = false,
        var waitBeforeReturnSeconds: Int = SettingsRepository.DEFAULT_WAIT_BEFORE_RETURN_SECONDS,
        var isTodoPromptEnabled: Boolean = false
    )

    fun setPerAppTimingEnabled(enabled: Boolean, clearTimers: Boolean = true) {
        if (perAppTimingEnabled == enabled) {
            return
        }
        perAppTimingEnabled = enabled
        logger(tag, "setPerAppTimingEnabled: $enabled")
        if (clearTimers) {
            clearAllTimers()
        }
    }

    private fun timerKey(packageName: String): String {
        return if (perAppTimingEnabled) packageName else sharedTimerKey
    }

    fun isPerAppTimingEnabled(): Boolean {
        return perAppTimingEnabled
    }

    fun getRemainingTime(app: AppInfo): Long {
        synchronized(timerStateMap) {
            val state = timerStateMap[timerKey(app.packageName)]
            return state?.remainingTime ?: 0
        }
    }

    fun start(
        app: AppInfo,
        timeMs: Long? = null,
        todoPrompt: TimerTodoPrompt? = null
    ) {
        val packageName = app.packageName
        val key = timerKey(packageName)

        synchronized(timerStateMap) {
            val previousState = timerStateMap[key]
            val isContinueTimer = previousState != null

            // 取消已有倒计时 Job
            timerJobs[key]?.cancel()

            // 获取或设置倒计时时间
            val targetTimeMs = timeMs ?: timerStateMap[key]?.remainingTime ?: 0
            val targetTodoPrompt = todoPrompt ?: previousState?.todoPrompt

            if (targetTimeMs <= 0) {
                logger(tag, "No valid time for [$packageName], aborting start", Log.ERROR)
                return
            }

            val state = TimerState(
                remainingTime = targetTimeMs,
                isRunning = true,
                startTime = System.currentTimeMillis(),
                appInfo = app,
                todoPrompt = targetTodoPrompt,
                isWaitBeforeReturnEnabled = cachedWaitBeforeReturnEnabled,
                waitBeforeReturnSeconds = cachedWaitBeforeReturnSeconds,
                isTodoPromptEnabled = cachedTodoPromptEnabled
            )
            timerStateMap[key] = state

            _currentTimerState.value = TimerDisplayState(
                packageName = packageName,
                appName = app.name,
                remainingTimeMs = targetTimeMs,
                isRunning = true,
                isSharedTimingEnabled = !perAppTimingEnabled
            )

            logger(tag, "--------------------")
            logger(tag, "Starting timer")
            logger(tag, "app: [${app.name}]")
            logger(tag, "targetTime: ${targetTimeMs / 1000}s")
            logger(tag, "--------------------")

            showStartedToast(app, isContinueTimer, targetTimeMs)

            // 启动协程倒计时
            timerJobs[key] = scope.launch {
                if (isContinueTimer) delay(800) else delay(800)
                countdownLoop(key, packageName, state)
            }
        }
    }

    private suspend fun countdownLoop(timerKey: String, packageName: String, state: TimerState) {
        while (state.isRunning && state.remainingTime > 0) {
            delay(1000)

            synchronized(timerStateMap) {
                if (!state.isRunning) {
                    logger(tag, "Timer not running, stopping countdown for [$packageName]")
                    return
                }

                state.remainingTime -= 1000L

                _currentTimerState.value?.let {
                    if (it.packageName == packageName) {
                        _currentTimerState.value = it.copy(remainingTimeMs = state.remainingTime)
                    }
                }

                logCounter++
                if (logCounter % logInterval == 0) {
                    logger(tag, "[$packageName] remaining: ${state.remainingTime / 1000}s")
                }
            }
        }

        if (state.remainingTime <= 0) {
            synchronized(timerStateMap) {
                state.remainingTime = 0
                state.isRunning = false
                val finishedAppInfo = state.appInfo
                timerStateMap.remove(timerKey)
                timerJobs.remove(timerKey)
                logCounter = 0

                if (_currentTimerState.value?.packageName == packageName) {
                    _currentTimerState.value = null
                }

                logger(tag, "[$packageName] timer finished")
                finishedAppInfo?.let {
                    _timeOutEvent.tryEmit(
                        TimerTimeoutInfo(
                            appInfo = it,
                            todoPrompt = if (state.isTodoPromptEnabled) state.todoPrompt else null,
                            isSharedTimingEnabled = !perAppTimingEnabled,
                            isWaitBeforeReturnEnabled = state.isWaitBeforeReturnEnabled,
                            waitBeforeReturnSeconds = state.waitBeforeReturnSeconds,
                            isTodoPromptEnabled = state.isTodoPromptEnabled
                        )
                    )
                }
            }
        }
    }

    private fun showStartedToast(app: AppInfo, isContinueTimer: Boolean, targetTimeMs: Long) {
        val message = if (isContinueTimer) {
            val remainingSeconds = targetTimeMs / 1000
            val hours = remainingSeconds / 3600
            val minutes = (remainingSeconds % 3600) / 60
            val seconds = remainingSeconds % 60

            val timeText = if (hours > 0) {
                "${hours}时${minutes}分${seconds}秒"
            } else if (minutes > 0) {
                "${minutes}分${seconds}秒"
            } else {
                "${seconds}秒"
            }
            if (perAppTimingEnabled) {
                "${app.name} 继续计时，剩余 $timeText"
            } else {
                "继续计时，剩余 $timeText"
            }
        } else {
            if (perAppTimingEnabled) {
                "${app.name} 开始倒计时"
            } else {
                "已开始倒计时"
            }
        }
        showToast(context, message)
    }

    fun stop(packageName: String) {
        val key = timerKey(packageName)
        synchronized(timerStateMap) {
            val state = timerStateMap[key]
            if (state?.isRunning == true) {
                state.isRunning = false
                timerJobs[key]?.cancel()

                if (_currentTimerState.value?.packageName == packageName || !perAppTimingEnabled) {
                    _currentTimerState.value = TimerDisplayState(
                        packageName = _currentTimerState.value?.packageName ?: packageName,
                        appName = _currentTimerState.value?.appName ?: state.appInfo?.name ?: "",
                        remainingTimeMs = state.remainingTime,
                        isRunning = false,
                        isSharedTimingEnabled = !perAppTimingEnabled
                    )
                    logger(tag, "Paused timer for [$packageName], remaining: ${state.remainingTime}ms, updated StateFlow to paused")
                }

                logger(tag, "Stopped timer for [$packageName], remaining: ${state.remainingTime / 1000}s")
            } else {
                logger(tag, "No active timer to stop for [$packageName]")
            }
        }
    }

    fun clearAllTimers() {
        synchronized(timerStateMap) {
            logger(tag, "Clearing all timers, count: ${timerStateMap.size}")
            timerJobs.values.forEach { it.cancel() }
            timerJobs.clear()
            timerStateMap.clear()
            _currentTimerState.value = null
            logger(tag, "All timers cleared")
        }
    }

    fun isTimerRunning(packageName: String): Boolean {
        synchronized(timerStateMap) {
            val state = timerStateMap[timerKey(packageName)]
            return state?.isRunning == true
        }
    }

    fun pause(packageName: String) = stop(packageName)
}
