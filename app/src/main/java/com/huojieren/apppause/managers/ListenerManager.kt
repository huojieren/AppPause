package com.huojieren.apppause.managers

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.huojieren.apppause.data.local.entity.TodoEntity
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.models.TimerTimeoutInfo
import com.huojieren.apppause.data.models.TimerTodoPrompt
import com.huojieren.apppause.data.models.TodoPromptInput
import com.huojieren.apppause.data.models.toUI
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.data.repository.SettingsRepository
import com.huojieren.apppause.data.repository.TodoRepository
import com.huojieren.apppause.ui.screens.TimeOutScreen
import com.huojieren.apppause.ui.screens.TimeSelectionScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
    private val todoRepository: TodoRepository,
    private val settingsRepository: SettingsRepository
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
        logger(tag, "--------------------")
        logger(tag, "Showing time selection overlay")
        logger(tag, "app: [${appInfo.packageName}]")
        logger(tag, "--------------------")
        CoroutineScope(Dispatchers.Main).launch {
            val icon = appManager.loadIcon(appInfo.packageName)
            val activeTodos = todoRepository.getActiveTodos().first()
            val isTimeSelectionTodoPromptEnabled = settingsRepository.getTimeSelectionTodoPromptEnabled().first()
            overlayManager.showOverlay(
                isSlowFadeIn = false,
                content = {
                    TimeSelectionScreen(
                        modifier = Modifier.fillMaxSize(),
                        appInfoUi = appInfo.toUI(icon),
                        isSharedTimingEnabled = !timerManager.isPerAppTimingEnabled(),
                        isTimeSelectionTodoPromptEnabled = isTimeSelectionTodoPromptEnabled,
                        activeTodos = activeTodos,
                        onExtend5Clicked = {
                            overlayManager.removeOverlay()
                            logger(tag, "Press extend 5 button")
                            timerManager.start(appInfo, 5 * 60 * 1000L) // 5分钟 = 300秒 = 300000毫秒
                        },
                        onExtend10Clicked = {
                            overlayManager.removeOverlay()
                            logger(tag, "Press extend 10 button")
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
                        onConfirmButtonClicked = { second, todoInput ->
                            logger(tag, "--------------------")
                            logger(tag, "Confirm button clicked!")
                            logger(tag, "Stop last app and start current app.")
                            logger(tag, "last app: [${currentApp?.packageName ?: "null"}]")
                            logger(tag, "current app: [${currentApp?.packageName ?: "null"}]")
                            logger(tag, "second: $second")
                            logger(tag, "--------------------")
                            CoroutineScope(Dispatchers.Main).launch {
                                currentApp?.let { app ->
                                    val todoPrompt = resolveTodoPrompt(todoInput, activeTodos)
                                    // 传入的是用户选择的秒数，转换为毫秒
                                    val timeInMillis = second * 1000L
                                    timerManager.start(app, timeInMillis, todoPrompt)
                                    overlayManager.removeOverlay()
                                } ?: run {
                                    logger(
                                        tag,
                                        "ERROR: No current app when confirm button clicked!",
                                        Log.ERROR
                                    )
                                }
                            }
                        }
                    )
                })
        }
    }

    private suspend fun resolveTodoPrompt(
        input: TodoPromptInput?,
        activeTodos: List<TodoEntity>
    ): TimerTodoPrompt? {
        val promptInput = input ?: return null
        val title = promptInput.title.trim()
        if (title.isBlank()) return null

        val existingTodo = promptInput.todoId?.let { id ->
            activeTodos.firstOrNull { it.id == id }
        }

        if (existingTodo != null) {
            return TimerTodoPrompt(
                todoId = existingTodo.id,
                title = existingTodo.name,
                isSavedTodo = true
            )
        }

        if (!promptInput.shouldSave) {
            return TimerTodoPrompt(
                todoId = null,
                title = title,
                isSavedTodo = false
            )
        }

        val todoId = todoRepository.insertTodo(
            TodoEntity(
                name = title,
                description = "",
                groupId = null
            )
        )
        return TimerTodoPrompt(
            todoId = todoId,
            title = title,
            isSavedTodo = true
        )
    }

    private fun showTimeOutOverlay(timeoutInfo: TimerTimeoutInfo) {
        val appInfo = timeoutInfo.appInfo
        logger(tag, "====================")
        logger(tag, "showTimeOutOverlay: app=${appInfo.packageName}")
        logger(tag, "====================")

        CoroutineScope(Dispatchers.Main).launch {
            val icon = appManager.loadIcon(appInfo.packageName)
            overlayManager.showOverlay(
                isSlowFadeIn = true,
                content = {
                    TimeOutScreen(
                        modifier = Modifier.fillMaxSize(),
                        appInfoUi = appInfo.toUI(icon),
                        isSharedTimingEnabled = timeoutInfo.isSharedTimingEnabled,
                        isWaitBeforeReturnEnabled = timeoutInfo.isWaitBeforeReturnEnabled,
                        isTimeoutTodoPromptEnabled = timeoutInfo.isTimeoutTodoPromptEnabled,
                        todoPrompt = timeoutInfo.todoPrompt,
                        fadeInCompleteEvent = overlayManager.fadeInCompleteEvent,
                        onClickReturnToHome = {
                            logger(tag, "Clicked return to home")
                            overlayManager.removeOverlay()
                            context.startActivity(Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        },
                        onAutoReturnToHome = {
                            logger(tag, "Auto returning to home after countdown")
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
