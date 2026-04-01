package com.huojieren.apppause.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

enum class AppPauseScreen(val route: String, val title: String, val icon: ImageVector?) {
    MainScreen("main", "主页", Icons.Default.Home),
    AppManager("app_manager", "应用管理", Icons.AutoMirrored.Filled.List),
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
fun BottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        AppPauseScreen.entries.forEach { screen ->
            NavigationBarItem(
                icon = {
                    screen.icon?.let { Icon(it, contentDescription = screen.title) }
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) }
            )
        }
    }
}

@Composable
fun AppPauseApp() {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomBar(currentRoute) { route -> navController.navigate(route) } }
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
            startDestination = AppPauseScreen.MainScreen.route,
            modifier = Modifier.padding(
                top = innerPadding.calculateTopPadding() + 20.dp,
                bottom = innerPadding.calculateBottomPadding() + 20.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            composable(AppPauseScreen.MainScreen.route) {
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

            composable(AppPauseScreen.AppManager.route) {
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

//@Preview("Light Theme")
//@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()

    AppTheme {
        Scaffold(
            topBar = { TopBar() },
            bottomBar = {
                BottomBar(currentRoute = AppPauseScreen.MainScreen.route) { route ->
                    navController.navigate(
                        route
                    )
                }
            }
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
                        onClearLogButtonClicked = {},
                        onSaveLogButtonClicked = {},
                        onToggleMonitoring = {}
                    )
                }
            }

        }
    }
}

//@Preview("Light Theme")
//@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectAppScreenEmptyListPreview() {
    val navController = rememberNavController()

    AppTheme {
        Scaffold(
            topBar = { TopBar() },
            bottomBar = {
                BottomBar(currentRoute = AppPauseScreen.AppManager.route) { route ->
                    navController.navigate(
                        route
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppPauseScreen.AppManager.route,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding() + 20.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                composable(AppPauseScreen.AppManager.route) {
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

    AppTheme {
        Scaffold(
            topBar = { TopBar() },
            bottomBar = {
                BottomBar(currentRoute = AppPauseScreen.AppManager.route) { route ->
                    navController.navigate(
                        route
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppPauseScreen.AppManager.route,
                modifier = Modifier.padding(
                    top = innerPadding.calculateTopPadding() + 20.dp,
                    bottom = innerPadding.calculateBottomPadding() + 20.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                composable(AppPauseScreen.AppManager.route) {
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