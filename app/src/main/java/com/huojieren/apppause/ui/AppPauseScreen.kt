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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.huojieren.apppause.data.Permissions
import com.huojieren.apppause.ui.screens.MainScreen
import com.huojieren.apppause.ui.screens.SelectedAppScreen
import com.huojieren.apppause.ui.screens.SelectingAppScreen
import com.huojieren.apppause.ui.state.MainScreenUiState
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.ui.viewModel.MainScreenViewModel
import com.huojieren.apppause.ui.viewModel.SelectAppViewModel

enum class AppPauseScreen() {
    MainScreen,
    SelectingApp,
    SelectedApp,
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

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TopBarPreview() {
    AppTheme {
        TopBar()
    }
}

@Composable
fun BottomBar() {
    // TODO BottomBar Not yet implemented
}

//@Preview("Light Theme")
//@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Composable
//fun BottomBarPreview() {
//    AppTheme {
//        BottomBar()
//    }
//}

@Composable
fun AppPauseApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    AppPauseScreen.valueOf(
        backStackEntry?.destination?.route ?: AppPauseScreen.MainScreen.name
    )

    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier
                    .padding(bottom = 24.dp)
            )
        },
        bottomBar = {
            BottomBar()
        }
    ) { innerPadding ->
        // 在最外层的 ViewModelStoreOwner 创建 ViewModel，确保即使多个页面也引用同一个 viewModel  和 state 状态
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
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppPauseScreen.MainScreen.name) {
                MainScreen(
                    uiState = mainScreenUiState.value,
                    onLifecycleChange = {
                        mainScreenViewModel.refreshPermission()
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
                        navController.navigate(AppPauseScreen.SelectedApp.name)
                    },
                    onClearLogButtonClicked = {
                        mainScreenViewModel.clearLog()
                    },
                    onSaveLogButtonClicked = {
                        mainScreenViewModel.saveLog()
                    },
                    onToggleMonitoring = {
                        mainScreenViewModel.toggleMonitoring()
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )
            }

            composable(AppPauseScreen.SelectingApp.name) {
                SelectingAppScreen(
                    uiState = selectAppUiState.value,
                    onAddAppItem = {
                        selectAppViewModel.addApp(it)
                    }
                )
            }

            composable(AppPauseScreen.SelectedApp.name) {
                SelectedAppScreen(
                    uiState = selectAppUiState.value,
                    onToSelectingApp = {
                        navController.navigate(AppPauseScreen.SelectingApp.name)
                    },
                    onDeleteAppItem = {
                        selectAppViewModel.removeApp(it)
                    }
                )
            }
        }
    }
}