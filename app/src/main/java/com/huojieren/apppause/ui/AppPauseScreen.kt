package com.huojieren.apppause.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
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
import com.huojieren.apppause.ui.screens.MainScreen
import com.huojieren.apppause.ui.screens.SelectAppScreen
import com.huojieren.apppause.ui.state.MainScreenUiState
import com.huojieren.apppause.ui.state.SelectAppUiState
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.ui.viewModel.MainScreenViewModel
import com.huojieren.apppause.ui.viewModel.SelectAppViewModel

enum class AppPauseScreen {
    MainScreen,
    SelectApp,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = "App Pause",
                style = MaterialTheme.typography.headlineLarge,
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Composable
fun BottomBar() {
    // TODO BottomBar Not yet implemented
}

@Composable
fun AppPauseApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    AppPauseScreen.valueOf(
        backStackEntry?.destination?.route ?: AppPauseScreen.MainScreen.name
    )

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar() }
    ) { innerPadding ->
        val selectAppViewModel: SelectAppViewModel = hiltViewModel()
        val selectAppUiState = selectAppViewModel.uiState.collectAsState()
        val mainScreenViewModel: MainScreenViewModel = hiltViewModel()
        val mainScreenUiState = mainScreenViewModel.uiState.collectAsState(
            initial = MainScreenUiState(
                isMonitoring = false,
                hasOverlay = false,
                hasNotification = false,
                hasUsageStats = false,
                hasAccessibility = false
            ),
        )

        NavHost(
            navController = navController,
            startDestination = AppPauseScreen.MainScreen.name,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding() + 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            composable(AppPauseScreen.MainScreen.name) {
                MainScreen(
                    uiState = mainScreenUiState.value,
                    onLifecycleChange = {
                        mainScreenViewModel.refreshState()
                    },
                    onOverlayButtonClicked = {
                        mainScreenViewModel.requestPermission(Permissions.Overlay)
                    },
                    onNotificationButtonClicked = {
                        mainScreenViewModel.requestPermission(Permissions.Notification)
                    },
                    onUsageStatsButtonClicked = {
                        mainScreenViewModel.requestPermission(Permissions.UsageStats)
                    },
                    onAccessibilityButtonClicked = {
                        mainScreenViewModel.requestPermission(Permissions.Accessibility)
                    },
                    onMonitoredAppButtonClicked = {
                        navController.navigate(AppPauseScreen.SelectApp.name)
                    },
                    onClearLogButtonClicked = {
                        mainScreenViewModel.clearLog()
                    },
                    onSaveLogButtonClicked = {
                        mainScreenViewModel.saveLog()
                    },
                    onToggleMonitoring = {
                        mainScreenViewModel.toggleMonitoring()
                    }
                )
            }

            composable(AppPauseScreen.SelectApp.name) {
                SelectAppScreen(
                    uiState = selectAppUiState.value,
                    onToggleApp = { app ->
                        selectAppViewModel.toggleApp(app)
                    },
                    getLetterPosition = { letter ->
                        selectAppViewModel.getLetterPosition(letter)
                    }
                )
            }
        }
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()

    AppTheme {
        Scaffold(
            topBar = { TopBar() }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppPauseScreen.MainScreen.name,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding() + 20.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                composable(AppPauseScreen.MainScreen.name) {
                    MainScreen(
                        uiState = MainScreenUiState(
                            isMonitoring = false,
                            hasOverlay = false,
                            hasNotification = false,
                            hasUsageStats = false,
                            hasAccessibility = false
                        ),
                        onLifecycleChange = {},
                        onOverlayButtonClicked = {},
                        onNotificationButtonClicked = {},
                        onUsageStatsButtonClicked = {},
                        onAccessibilityButtonClicked = {},
                        onMonitoredAppButtonClicked = {},
                        onClearLogButtonClicked = {},
                        onSaveLogButtonClicked = {},
                        onToggleMonitoring = {}
                    )
                }
            }

        }
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectAppScreenEmptyListPreview() {
    val navController = rememberNavController()

    AppTheme {
        Scaffold(
            topBar = { TopBar() }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppPauseScreen.SelectApp.name,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding() + 20.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                composable(AppPauseScreen.SelectApp.name) {
                    SelectAppScreen(
                        uiState = SelectAppUiState(
                            monitoredApps = emptyList(),
                            allAppsGrouped = emptyList()
                        ),
                        onToggleApp = {},
                        getLetterPosition = { 0 }
                    )
                }
            }
        }
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectAppScreenPreview() {
    val navController = rememberNavController()
    val mockState = SelectAppUiState(
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
                        name = "App 3",
                        packageName = "com.example.app3",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                )
            ),
            AppLetterGroup(
                "B", listOf(
                    AppInfoUi(
                        name = "App 4",
                        packageName = "com.example.app4",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    ),
                    AppInfoUi(
                        name = "App 5",
                        packageName = "com.example.app5",
                        icon = painterResource(id = R.drawable.ic_launcher_foreground)
                    )
                )
            )
        )
    )

    AppTheme {
        Scaffold(
            topBar = { TopBar() }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppPauseScreen.SelectApp.name,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding() + 20.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                composable(AppPauseScreen.SelectApp.name) {
                    SelectAppScreen(
                        uiState = mockState,
                        onToggleApp = {},
                        getLetterPosition = { 0 }
                    )
                }
            }
        }
    }
}