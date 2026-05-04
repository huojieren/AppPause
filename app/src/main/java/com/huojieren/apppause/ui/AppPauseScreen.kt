package com.huojieren.apppause.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
import com.huojieren.apppause.ui.components.BottomBar
import com.huojieren.apppause.ui.screens.MainScreen
import com.huojieren.apppause.ui.screens.SelectAppScreen
import com.huojieren.apppause.ui.screens.SettingsScreen
import com.huojieren.apppause.ui.screens.TodoListScreen
import com.huojieren.apppause.ui.state.AppStatusUiState
import com.huojieren.apppause.ui.state.SelectAppUiState
import com.huojieren.apppause.ui.state.TodoListUiState
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.ui.viewModel.AppStatusViewModel
import com.huojieren.apppause.ui.viewModel.SelectAppViewModel
import com.huojieren.apppause.ui.viewModel.TodoViewModel

enum class AppPauseScreen(val route: String, val title: String, val icon: ImageVector?) {
    MainScreen("main", "主页", Icons.Default.Home),
    AppManager("app_manager", "应用", Icons.AutoMirrored.Filled.List),
    TodoList("todo_list", "待办", Icons.Filled.CheckCircle),
    SettingsScreen("settings", "设置", Icons.Filled.Settings),
}

@Composable
fun AppPauseApp(
    appStatusUiState: AppStatusUiState? = null,
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

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppPauseScreen.MainScreen.route) { }
        composable(AppPauseScreen.AppManager.route) { }
        composable(AppPauseScreen.TodoList.route) { }
        composable(AppPauseScreen.SettingsScreen.route) { }
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
                        onClearLogButtonClicked = {
                            appStatusViewModel?.clearLog()
                        },
                        onSaveLogButtonClicked = {
                            appStatusViewModel?.saveLog()
                        },
                        modifier = Modifier.padding(
                            vertical = 20.dp,
                            horizontal = 16.dp
                        )
                    )
                }

                AppPauseScreen.TodoList.route -> {
                    TodoListScreen(
                        uiState = actualTodoListUiState,
                        onSelectGroup = {
                            todoViewModel?.selectGroup(it)
                        },
                        onShowAddDialog = {
                            todoViewModel?.showAddDialog()
                        },
                        onHideAddDialog = {
                            todoViewModel?.hideAddDialog()
                        },
                        onAddTodo = { name, description, groupId ->
                            todoViewModel?.addTodo(name, description, groupId)
                        },
                        onDeleteTodo = {
                            todoViewModel?.deleteTodo(it)
                        },
                        onToggleTodoCompletion = {
                            todoViewModel?.toggleTodoCompletion(it)
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

@LightAppPreview
@DarkAppPreview
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

@LightAppPreview
@DarkAppPreview
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

@LightAppPreview
@DarkAppPreview
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

@LightAppPreview
@DarkAppPreview
@Composable
fun SettingsScreenPreview() {
    AppTheme {
        AppPauseApp(
            appStatusUiState = AppStatusUiState(
                isMonitoring = true,
                hasOverlay = true,
                hasNotification = true,
                hasUsageStats = true,
                hasAccessibility = true
            ),
            selectAppUiState = SelectAppUiState(),
            todoListUiState = TodoListUiState(),
            startDestination = AppPauseScreen.SettingsScreen.route
        )
    }
}

@LightAppPreview
@DarkAppPreview
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
        editingTodo = null
    )
}