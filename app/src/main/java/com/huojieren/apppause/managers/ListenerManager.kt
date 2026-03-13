package com.huojieren.apppause.managers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier

import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.models.toUI
import com.huojieren.apppause.data.repository.LogRepository
import com.huojieren.apppause.ui.screens.TimeOutScreen
import com.huojieren.apppause.ui.screens.TimeSelectionScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListenerManager @Inject constructor(
    private val context: Context,
    private val monitorManager: MonitorManager,
    private val overlayManager: OverlayManager,
    private val timerManager: TimerManager,
    private val appManager: AppManager,
    private val logRepository: LogRepository
) {
    private val tag = "ListenerManager"

    private var currentApp: AppInfo? = null

    init {
        setupListeners()
    }

    private fun setupListeners() {
        monitorManager.setOnAppChangedListener { app ->
            currentApp = app
            if (app != null) {
                showTimeSelectionOverlay(app)
            }
        }

        timerManager.setOnTimeOutListener { appInfo ->
            showTimeOutOverlay(appInfo)
        }
    }

    private fun showTimeSelectionOverlay(appInfo: AppInfo) {
        logRepository.log(tag, "--------------------")
        logRepository.log(tag, "Showing time selection overlay")
        logRepository.log(tag, "app: [${appInfo.packageName}]")
        logRepository.log(tag, "--------------------")
        CoroutineScope(Dispatchers.Main).launch {
            val icon = appManager.loadIcon(appInfo.packageName)
            overlayManager.showOverlay(
                isSlowFadeIn = false,
                content = {
                    TimeSelectionScreen(
                        modifier = Modifier.fillMaxSize(),
                        appInfoUi = appInfo.toUI(icon),
                        onExtend5Clicked = {
                            overlayManager.removeOverlay()
                            logRepository.log(tag, "Press extend 5 button")
                            timerManager.start(appInfo, 5 * 60 * 1000L) // 5分钟 = 300秒 = 300000毫秒
                        },
                        onExtend10Clicked = {
                            overlayManager.removeOverlay()
                            logRepository.log(tag, "Press extend 10 button")
                            timerManager.start(appInfo, 10 * 60 * 1000L) // 10分钟 = 600秒 = 600000毫秒
                        },
                        onCancelButtonClicked = {
                            overlayManager.removeOverlay()
                            // 返回桌面
                            context.startActivity(Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        },
                        onConfirmButtonClicked = { second ->
                            logRepository.log(tag, "--------------------")
                            logRepository.log(tag, "Confirm button clicked!")
                            logRepository.log(tag, "Stop last app and start current app.")
                            logRepository.log(
                                tag,
                                "last app: [${currentApp?.packageName ?: "null"}]"
                            )
                            logRepository.log(
                                tag,
                                "current app: [${currentApp?.packageName ?: "null"}]"
                            )
                            logRepository.log(tag, "second: $second")
                            logRepository.log(tag, "--------------------")
                            currentApp?.let { app ->
                                // 传入的是用户选择的秒数，转换为毫秒
                                val timeInMillis = second * 1000L
                                timerManager.start(app, timeInMillis)
                                overlayManager.removeOverlay()
                            } ?: run {
                                logRepository.log(
                                    tag,
                                    "ERROR: No current app when confirm button clicked!",
                                    Log.ERROR
                                )
                            }
                        }
                    )
                })
        }
    }

    private fun showTimeOutOverlay(appInfo: AppInfo) {
        logRepository.log(tag, "====================")
        logRepository.log(tag, "showTimeOutOverlay: app=${appInfo.packageName}")
        logRepository.log(tag, "====================")
        
        CoroutineScope(Dispatchers.Main).launch {
            val icon = appManager.loadIcon(appInfo.packageName)
            overlayManager.showOverlay(
                isSlowFadeIn = true,
                content = {
                    TimeOutScreen(
                        modifier = Modifier.fillMaxSize(),
                        appInfoUi = appInfo.toUI(icon),
                        fadeInCompleteEvent = overlayManager.fadeInCompleteEvent,
                        onReturnToHomeScreenClicked = {
                            logRepository.log(tag, "Clicked return to home")
                            overlayManager.removeOverlay()
                            context.startActivity(Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }
                    )
                })
        }
    }
}
