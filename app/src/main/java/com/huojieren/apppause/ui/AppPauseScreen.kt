package com.huojieren.apppause.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.huojieren.apppause.R
import com.huojieren.apppause.data.Permissions
import com.huojieren.apppause.data.local.entity.TodoEntity
import com.huojieren.apppause.data.local.entity.TodoGroupEntity
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.data.models.AppLetterGroup
import com.huojieren.apppause.data.diagnostics.model.DiagnosticIncident
import com.huojieren.apppause.data.diagnostics.model.IncidentType
import com.huojieren.apppause.ui.components.BottomBar
import com.huojieren.apppause.ui.components.ConfirmDialog
import com.huojieren.apppause.ui.components.DiagnosticIncidentActionDialog
import com.huojieren.apppause.ui.screens.DiagnosticsScreen
import com.huojieren.apppause.ui.screens.MainScreen
import com.huojieren.apppause.ui.screens.SelectAppScreen
import com.huojieren.apppause.ui.screens.SettingsScreen
import com.huojieren.apppause.ui.screens.TodoListScreen
import com.huojieren.apppause.ui.state.AppStatusUiState
import com.huojieren.apppause.ui.state.DiagnosticsUiState
import com.huojieren.apppause.ui.state.SelectAppUiState
import com.huojieren.apppause.ui.state.TodoListUiState
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.ui.viewModel.AppStatusViewModel
import com.huojieren.apppause.ui.viewModel.SelectAppViewModel
import com.huojieren.apppause.ui.viewModel.TodoViewModel

enum class AppPauseScreen(
    val route: String,
    val title: String,
    val icon: ImageVector?,
    val showInBottomBar: Boolean = true
) {
    MainScreen("main", "主页", Icons.Default.Home),
    AppManager("app_manager", "应用", Icons.AutoMirrored.Filled.List),
    TodoList("todo_list", "待办", Icons.Filled.CheckCircle),
    SettingsScreen("settings", "设置", Icons.Filled.Settings),
    Diagnostics("diagnostics", "诊断信息", null, showInBottomBar = false),
}

@Composable
fun AppPauseApp(
    appStatusUiState: AppStatusUiState? = null,
    diagnosticsUiState: DiagnosticsUiState? = null,
    selectAppUiState: SelectAppUiState? = null,
    todoListUiState: TodoListUiState? = null,
    startDestination: String = AppPauseScreen.MainScreen.route
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val appStatusViewModel: AppStatusViewModel? =
        if (appStatusUiState == null) hiltViewModel() else null
    val selectAppViewModel: SelectAppViewModel? =
        if (selectAppUiState == null) hiltViewModel() else null
    val todoViewModel: TodoViewModel? =
        if (todoListUiState == null) hiltViewModel() else null

    val actualAppStatusUiState =
        appStatusUiState ?: appStatusViewModel!!.uiState.collectAsState(
            initial = AppStatusUiState(
                isMonitoring = false,
                hasOverlay = false,
                hasNotification = false,
                hasUsageStats = false,
                hasAccessibility = false
            ),
        ).value

    val actualSelectAppUiState =
        selectAppUiState ?: selectAppViewModel!!.uiState.collectAsState().value

    val actualTodoListUiState =
        todoListUiState ?: todoViewModel!!.uiState.collectAsState().value

    val actualDiagnosticsUiState = diagnosticsUiState ?: if (appStatusViewModel != null) {
        appStatusViewModel.diagnosticsUiState.collectAsState(initial = DiagnosticsUiState()).value
    } else {
        DiagnosticsUiState()
    }

    var showClearLogDialog by remember { mutableStateOf(false) }
    var selectedIncident by remember { mutableStateOf<DiagnosticIncident?>(null) }
    var incidentPendingDeletion by remember { mutableStateOf<DiagnosticIncident?>(null) }

    if (showClearLogDialog) {
        ConfirmDialog(
            title = "清空诊断材料",
            message = "确定要清空所有本地诊断材料吗？此操作不可撤销。",
            onDismiss = { showClearLogDialog = false },
            onConfirm = {
                appStatusViewModel?.clearLog()
                showClearLogDialog = false
            }
        )
    }

    selectedIncident?.let { incident ->
        DiagnosticIncidentActionDialog(
            incident = incident,
            onDismiss = { selectedIncident = null },
            onExport = {
                appStatusViewModel?.saveIncident(incident.id)
                selectedIncident = null
            },
            onDelete = {
                selectedIncident = null
                incidentPendingDeletion = incident
            }
        )
    }

    incidentPendingDeletion?.let { incident ->
        ConfirmDialog(
            title = "删除异常记录",
            message = "将删除该异常记录及其关联的系统追踪附件，无法恢复。",
            onDismiss = { incidentPendingDeletion = null },
            onConfirm = {
                appStatusViewModel?.deleteIncident(incident.id)
                incidentPendingDeletion = null
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppPauseScreen.MainScreen.route) { }
        composable(AppPauseScreen.AppManager.route) { }
        composable(AppPauseScreen.TodoList.route) { }
        composable(AppPauseScreen.SettingsScreen.route) { }
        composable(AppPauseScreen.Diagnostics.route) { }
    }

    Scaffold(
        bottomBar = {
            BottomBar(currentRoute) { route ->
                navController.navigate(route) {
                    popUpTo(
                        startDestination
                    ) { inclusive = true }
                }
            }
        },
        floatingActionButton = {
            when (currentRoute) {
                AppPauseScreen.TodoList.route -> {
                    FloatingActionButton(
                        onClick = { todoViewModel?.showAddTodoDialog() }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加待办")
                    }
                }

                AppPauseScreen.Diagnostics.route -> {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SmallFloatingActionButton(
                            onClick = { showClearLogDialog = true },
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "清空诊断材料"
                            )
                        }
                        SmallFloatingActionButton(
                            onClick = { appStatusViewModel?.saveLog() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "导出诊断材料"
                            )
                        }
                    }
                }

                else -> Unit
            }
        }
    ) { innerPadding ->

        AnimatedContent(
            targetState = currentRoute,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith
                        fadeOut(animationSpec = tween(200))
            },
            label = "screen_transition",
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .fillMaxSize()
        ) { route ->
            when (route) {
                AppPauseScreen.MainScreen.route -> {
                    MainScreen(
                        uiState = actualAppStatusUiState,
                        onLifecycleChange = {
                            appStatusViewModel?.refreshState()
                        },
                        onToggleMonitoring = {
                            appStatusViewModel?.toggleMonitoring()
                        },
                        modifier = Modifier.padding(
                            vertical = 20.dp,
                            horizontal = 16.dp
                        )
                    )
                }

                AppPauseScreen.SettingsScreen.route -> {
                    SettingsScreen(
                        uiState = actualAppStatusUiState,
                        onOverlayButtonClicked = {
                            appStatusViewModel?.requestPermission(Permissions.Overlay)
                        },
                        onNotificationButtonClicked = {
                            appStatusViewModel?.requestPermission(Permissions.Notification)
                        },
                        onUsageStatsButtonClicked = {
                            appStatusViewModel?.requestPermission(Permissions.UsageStats)
                        },
                        onAccessibilityButtonClicked = {
                            appStatusViewModel?.requestPermission(Permissions.Accessibility)
                        },
                        onBatteryOptimizationButtonClicked = {
                            appStatusViewModel?.requestPermission(Permissions.BatteryOptimization)
                        },
                        onDiagnosticsClicked = {
                            appStatusViewModel?.refreshDiagnostics()
                            navController.navigate(AppPauseScreen.Diagnostics.route)
                        },
                        onSharedTimingChanged = {
                            appStatusViewModel?.setSharedTimingEnabled(it)
                        },
                        onWaitBeforeReturnChanged = {
                            appStatusViewModel?.setWaitBeforeReturnEnabled(it)
                        },
                        onWaitBeforeReturnSecondsChanged = {
                            appStatusViewModel?.setWaitBeforeReturnSeconds(it)
                        },
                        onTodoPromptChanged = {
                            appStatusViewModel?.setTodoPromptEnabled(it)
                        },
                        modifier = Modifier.padding(
                            vertical = 20.dp,
                            horizontal = 16.dp
                        )
                    )
                }

                AppPauseScreen.Diagnostics.route -> {
                    DiagnosticsScreen(
                        uiState = actualDiagnosticsUiState,
                        onBack = { navController.popBackStack() },
                        onIncidentClicked = { selectedIncident = it },
                        modifier = Modifier.padding(
                            bottom = 10.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                    )
                }

                AppPauseScreen.TodoList.route -> {
                    TodoListScreen(
                        uiState = actualTodoListUiState,
                        onSelectGroup = {
                            todoViewModel?.selectGroup(it)
                        },
                        onHideAddTodoDialog = {
                            todoViewModel?.hideAddTodoDialog()
                        },
                        onAddTodo = { name, description, groupId ->
                            todoViewModel?.addTodo(name, description, groupId)
                        },
                        onShowEditTodoDialog = { todo ->
                            todoViewModel?.showEditTodoDialog(todo)
                        },
                        onHideEditTodoDialog = {
                            todoViewModel?.hideEditTodoDialog()
                        },
                        onUpdateTodo = { todo ->
                            todoViewModel?.updateTodo(todo)
                        },
                        onDeleteTodo = {
                            todoViewModel?.deleteTodo(it)
                        },
                        onToggleTodoCompletion = {
                            todoViewModel?.toggleTodoCompletion(it)
                        },
                        onShowAddGroupDialog = {
                            todoViewModel?.showAddGroupDialog()
                        },
                        onHideAddGroupDialog = {
                            todoViewModel?.hideAddGroupDialog()
                        },
                        onAddGroup = { name, color ->
                            todoViewModel?.addGroup(name, color)
                        },
                        onShowEditGroupDialog = { group ->
                            todoViewModel?.showEditGroupDialog(group)
                        },
                        onHideEditGroupDialog = {
                            todoViewModel?.hideEditGroupDialog()
                        },
                        onUpdateGroup = { group ->
                            todoViewModel?.updateGroup(group)
                        },
                        onDeleteGroup = { group ->
                            todoViewModel?.deleteGroup(group)
                        },
                        modifier = Modifier.padding(
                            vertical = 20.dp,
                            horizontal = 16.dp
                        )
                    )
                }

                AppPauseScreen.AppManager.route, null -> {
                    SelectAppScreen(
                        uiState = actualSelectAppUiState,
                        onToggleApp = { app ->
                            selectAppViewModel?.toggleApp(app)
                        },
                        getLetterPosition = { letter ->
                            selectAppViewModel?.getLetterPosition(letter)
                        },
                        modifier = Modifier.padding(
                            horizontal = 16.dp
                        )
                    )
                }
            }
        }
    }
}

//@LightAppPreview
//@DarkAppPreview
@Composable
fun MainScreenPreview() {
    AppTheme {
        AppPauseApp(
            appStatusUiState = AppStatusUiState(
                isMonitoring = false,
                hasOverlay = false,
                hasNotification = false,
                hasUsageStats = false,
                hasAccessibility = false
            ),
            selectAppUiState = SelectAppUiState(),
            todoListUiState = TodoListUiState(),
            startDestination = AppPauseScreen.MainScreen.route
        )
    }
}

//@LightAppPreview
//@DarkAppPreview
@Composable
fun SelectAppScreenEmptyListPreview() {
    AppTheme {
        AppPauseApp(
            appStatusUiState = AppStatusUiState(),
            selectAppUiState = SelectAppUiState(
                monitoredApps = emptyList(),
                allAppsGrouped = emptyList()
            ),
            todoListUiState = TodoListUiState(),
            startDestination = AppPauseScreen.AppManager.route
        )
    }
}

//@LightAppPreview
//@DarkAppPreview
@Composable
fun SelectAppScreenPreview() {
    AppTheme {
        AppPauseApp(
            appStatusUiState = AppStatusUiState(),
            selectAppUiState = mockSelectAppUiState(),
            todoListUiState = TodoListUiState(),
            startDestination = AppPauseScreen.AppManager.route
        )
    }
}

//@LightAppPreview
//@DarkAppPreview
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        AppPauseApp(
            appStatusUiState = AppStatusUiState(
                isMonitoring = true,
                hasOverlay = true,
                hasNotification = true,
                hasUsageStats = true,
                hasAccessibility = true,
                isWaitBeforeReturnEnabled = true,
                waitBeforeReturnSeconds = 5
            ),
            selectAppUiState = SelectAppUiState(),
            todoListUiState = TodoListUiState(),
            startDestination = AppPauseScreen.SettingsScreen.route
        )
    }
}

//@LightAppPreview
//@DarkAppPreview
@Composable
fun TodoListScreenPreview() {
    AppTheme {
        AppPauseApp(
            appStatusUiState = AppStatusUiState(),
            selectAppUiState = SelectAppUiState(),
            todoListUiState = mockTodoListUiState(),
            startDestination = AppPauseScreen.TodoList.route
        )
    }
}

@LightAppPreview
//@DarkAppPreview
@Composable
fun DiagnosticsScreenPreview() {
    AppTheme {
        AppPauseApp(
            appStatusUiState = AppStatusUiState(),
            diagnosticsUiState = DiagnosticsUiState(
                incidents = mockDiagnosticIncidents()
            ),
            selectAppUiState = SelectAppUiState(),
            todoListUiState = TodoListUiState(),
            startDestination = AppPauseScreen.Diagnostics.route
        )
    }
}

private fun mockDiagnosticIncidents(): List<DiagnosticIncident> = listOf(
    DiagnosticIncident(
        id = "process-exit-20260801-120000-100",
        type = IncidentType.PROCESS_EXIT,
        occurredAtEpochMs = 1_784_800_000_000,
        reason = "LOW_MEMORY(7)",
        description = "系统因内存紧张结束了进程",
        hasTrace = true
    ),
    DiagnosticIncident(
        id = "java-crash-20260801-091500-100",
        type = IncidentType.JAVA_CRASH,
        occurredAtEpochMs = 1_784_789_000_000,
        exceptionName = "java.lang.IllegalStateException",
        message = "Monitor service is not ready"
    )
)

@Composable
fun mockSelectAppUiState(): SelectAppUiState {
    return SelectAppUiState(
        monitoredApps = listOf(
            AppInfoUi(
                name = "App 1",
                packageName = "com.example.app1",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            ),
            AppInfoUi(
                name = "App 2",
                packageName = "com.example.app2",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            )
        ),
        allAppsGrouped = listOf(
            AppLetterGroup(
                "A", listOf(
                    AppInfoUi(
                        name = "A App",
                        packageName = "com.example.app3",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                )
            ),
            AppLetterGroup(
                "B", listOf(
                    AppInfoUi(
                        name = "B App 1",
                        packageName = "com.example.app4",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    ),
                    AppInfoUi(
                        name = "B App 2",
                        packageName = "com.example.app5",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                )
            ),
            AppLetterGroup(
                "C", listOf(
                    AppInfoUi(
                        name = "C App 1",
                        packageName = "com.example.app6",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    ),
                    AppInfoUi(
                        name = "C App 2",
                        packageName = "com.example.app7",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                )
            )
        )
    )
}

private fun mockTodoListUiState(): TodoListUiState {
    return TodoListUiState(
        todos = listOf(
            TodoEntity(
                id = 1,
                name = "学习 Kotlin",
                description = "学习协程和Flow",
                isCompleted = false,
                groupId = 1
            ),
            TodoEntity(
                id = 2,
                name = "完成项目",
                description = "App Pause 开发",
                isCompleted = true,
                groupId = 1
            ),
            TodoEntity(
                id = 3,
                name = "健身",
                description = "每周三次",
                isCompleted = false,
                groupId = 2
            )
        ),
        groups = listOf(
            TodoGroupEntity(id = 1, name = "工作", color = "#2196F3", isDefault = true),
            TodoGroupEntity(id = 2, name = "生活", color = "#4CAF50", isDefault = true),
            TodoGroupEntity(id = 3, name = "学习", color = "#FF9800", isDefault = true)
        ),
        selectedGroupId = null,
        isLoading = false,
        showAddDialog = false,
        showEditDialog = false,
        editingTodo = null,
        showAddGroupDialog = false,
        showEditGroupDialog = false,
        editingGroup = null
    )
}
