package com.huojieren.apppause.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
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
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.data.models.AppLetterGroup
import com.huojieren.apppause.ui.components.BottomBar
import com.huojieren.apppause.ui.screens.MainScreen
import com.huojieren.apppause.ui.screens.SelectAppScreen
import com.huojieren.apppause.ui.state.MainScreenUiState
import com.huojieren.apppause.ui.state.SelectAppUiState
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.ui.viewModel.MainScreenViewModel
import com.huojieren.apppause.ui.viewModel.SelectAppViewModel

enum class AppPauseScreen(val route: String, val title: String, val icon: ImageVector?) {
    MainScreen("main", "主页", Icons.Default.Home),
    AppManager("app_manager", "应用管理", Icons.AutoMirrored.Filled.List),
}

@Composable
fun AppPauseApp(
    mainScreenUiState: MainScreenUiState? = null,
    selectAppUiState: SelectAppUiState? = null,
    startDestination: String = AppPauseScreen.MainScreen.route
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val mainScreenViewModel: MainScreenViewModel? =
        if (mainScreenUiState == null) hiltViewModel() else null
    val selectAppViewModel: SelectAppViewModel? =
        if (selectAppUiState == null) hiltViewModel() else null

    val actualMainScreenUiState =
        mainScreenUiState ?: mainScreenViewModel!!.uiState.collectAsState(
            initial = MainScreenUiState(
                isMonitoring = false,
                hasOverlay = false,
                hasNotification = false,
                hasUsageStats = false,
                hasAccessibility = false
            ),
        ).value

    val actualSelectAppUiState =
        selectAppUiState ?: selectAppViewModel!!.uiState.collectAsState().value


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

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding()
            ),
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable(AppPauseScreen.MainScreen.route) {
                MainScreen(
                    uiState = actualMainScreenUiState,
                    onLifecycleChange = {
                        mainScreenViewModel?.refreshState()
                    },
                    onOverlayButtonClicked = {
                        mainScreenViewModel?.requestPermission(Permissions.Overlay)
                    },
                    onNotificationButtonClicked = {
                        mainScreenViewModel?.requestPermission(Permissions.Notification)
                    },
                    onUsageStatsButtonClicked = {
                        mainScreenViewModel?.requestPermission(Permissions.UsageStats)
                    },
                    onAccessibilityButtonClicked = {
                        mainScreenViewModel?.requestPermission(Permissions.Accessibility)
                    },
                    onClearLogButtonClicked = {
                        mainScreenViewModel?.clearLog()
                    },
                    onSaveLogButtonClicked = {
                        mainScreenViewModel?.saveLog()
                    },
                    onToggleMonitoring = {
                        mainScreenViewModel?.toggleMonitoring()
                    },
                    modifier = Modifier.padding(
                        vertical = 20.dp,
                        horizontal = 16.dp
                    )
                )
            }

            composable(AppPauseScreen.AppManager.route) {
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

@LightThemePreview
//@DarkThemePreview
@Composable
fun MainScreenPreview() {
    AppTheme {
        AppPauseApp(
            mainScreenUiState = MainScreenUiState(
                isMonitoring = false,
                hasOverlay = false,
                hasNotification = false,
                hasUsageStats = false,
                hasAccessibility = false
            ),
            selectAppUiState = SelectAppUiState(),
            startDestination = AppPauseScreen.MainScreen.route
        )
    }
}

@LightThemePreview
//@DarkThemePreview
@Composable
fun SelectAppScreenEmptyListPreview() {
    AppTheme {
        AppPauseApp(
            mainScreenUiState = MainScreenUiState(),
            selectAppUiState = SelectAppUiState(
                monitoredApps = emptyList(),
                allAppsGrouped = emptyList()
            ),
            startDestination = AppPauseScreen.AppManager.route
        )
    }
}

@LightThemePreview
//@DarkThemePreview
@Composable
fun SelectAppScreenPreview() {
    AppTheme {
        AppPauseApp(
            mainScreenUiState = MainScreenUiState(),
            selectAppUiState = mockSelectAppUiState(),
            startDestination = AppPauseScreen.AppManager.route
        )
    }
}

@Composable
private fun mockSelectAppUiState(): SelectAppUiState {
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