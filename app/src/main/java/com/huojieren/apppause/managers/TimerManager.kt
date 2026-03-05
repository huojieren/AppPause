package com.huojieren.apppause.managers

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.repository.LogRepository

class TimerManager(
    private val logRepository: LogRepository
) {
    private val tag = "TimerManager"
    private val handler = Handler(Looper.getMainLooper())

    // 使用可变Map来存储倒计时状态
    private val timerStateMap = mutableMapOf<String, TimerState>()

    private var onTimeOut: ((Boolean) -> Unit)? = null

    // 日志控制
    private var logCounter = 0
    private val logInterval = 5 // 每5次倒计时间隔输出一次日志

    /**
     * 倒计时状态
     */
    data class TimerState(
        var remainingTime: Long,
        var isRunning: Boolean = false,
        var startTime: Long = 0
    )

    /**
     * 获取剩余时间
     */
    fun getRemainingTime(app: AppInfo): Long {
        val state = timerStateMap[app.packageName]
        return state?.remainingTime ?: 0
    }

    /**
     * 设置超时监听器
     */
    fun setOnTimeOutListener(listener: (Boolean) -> Unit) {
        onTimeOut = listener
    }

    /**
     * 启动倒计时
     * @param app 应用信息
     * @param timeMs 倒计时时间（毫秒），如果为null则使用当前剩余时间
     */
    fun start(app: AppInfo, timeMs: Long? = null) {
        val packageName = app.packageName

        // 先停止现有倒计时
        stop(packageName)

        // 获取或设置倒计时时间
        val targetTimeMs = timeMs ?: timerStateMap[packageName]?.remainingTime ?: 0

        if (targetTimeMs <= 0) {
            logRepository.log(
                tag,
                "No valid time for [$packageName], aborting start",
                Log.ERROR
            )
            return
        }

        // 创建新的倒计时状态
        val state = TimerState(
            remainingTime = targetTimeMs,
            isRunning = true,
            startTime = System.currentTimeMillis()
        )
        timerStateMap[packageName] = state

        logRepository.log(tag, "--------------------")
        logRepository.log(tag, "Starting timer")
        logRepository.log(tag, "app: [${app.name}]")
        logRepository.log(tag, "targetTime: ${targetTimeMs / 1000}s")
        logRepository.log(tag, "--------------------")

        // 启动倒计时
        startCountdown(packageName, state)
    }

    /**
     * 停止倒计时
     */
    fun stop(packageName: String) {
        val state = timerStateMap[packageName]
        if (state?.isRunning == true) {
            state.isRunning = false
            logRepository.log(
                tag,
                "Stopped timer for [$packageName], remaining: ${state.remainingTime / 1000}s"
            )
        } else {
            logRepository.log(tag, "No active timer to stop for [$packageName]")
        }
    }

    /**
     * 暂停倒计时（与停止相同，但语义更明确）
     */
    fun pause(packageName: String) = stop(packageName)

    /**
     * 启动倒计时循环
     */
    private fun startCountdown(packageName: String, state: TimerState) {
        val runnable = object : Runnable {
            override fun run() {
                if (!state.isRunning) {
                    logRepository.log(
                        tag,
                        "Timer not running, stopping countdown for [$packageName]"
                    )
                    return
                }

                if (state.remainingTime > 0) {
                    // 减少剩余时间 - 固定使用1秒递减间隔
                    state.remainingTime -= 1000L

                    // 定期输出日志
                    logCounter++
                    if (logCounter % logInterval == 0) {
                        logRepository.log(
                            tag,
                            "[$packageName] remaining: ${state.remainingTime / 1000}s"
                        )
                    }

                    // 继续下一次倒计时 - 固定使用1秒间隔
                    handler.postDelayed(this, 1000L)
                } else {
                    // 倒计时结束
                    state.remainingTime = 0
                    state.isRunning = false
                    timerStateMap.remove(packageName)
                    logCounter = 0

                    logRepository.log(tag, "[$packageName] timer finished")
                    onTimeOut?.invoke(true)
                }
            }
        }

        handler.post(runnable)
        logRepository.log(
            tag,
            "Countdown started for $packageName, next tick in 1s"
        )
    }
}