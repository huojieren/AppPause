package com.huojieren.apppause.data.models

data class TimerTodoPrompt(
    val todoId: Long?,
    val title: String,
    val isSavedTodo: Boolean
)

data class TimerTimeoutInfo(
    val appInfo: AppInfo,
    val todoPrompt: TimerTodoPrompt?
)

data class TodoPromptInput(
    val todoId: Long?,
    val title: String,
    val shouldSave: Boolean
)
