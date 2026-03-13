package com.huojieren.apppause.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.ui.components.AppListItem
import com.huojieren.apppause.ui.state.SelectAppUiState
import com.huojieren.apppause.ui.theme.AppTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SelectAppScreen(
    uiState: SelectAppUiState,
    modifier: Modifier = Modifier,
    onToggleApp: (AppInfoUi) -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val showFab by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 100 }
    }
    val monitoredPackages = remember(uiState.monitoredApps) {
        uiState.monitoredApps.map { it.packageName }.toSet()
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "回到顶部"
                    )
                }
            }
        }
    ) { _ ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "已监控应用",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                if (uiState.monitoredApps.isEmpty()) {
                    item {
                        Text(
                            text = "没有已监控的应用",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    items(uiState.monitoredApps, key = { "monitored_${it.packageName}" }) { app ->
                        AppListItem(
                            appInfoUi = app,
                            isMonitored = true,
                            onToggle = onToggleApp
                        )
                    }
                }
                item {
                    Text(
                        text = "所有应用",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                if (uiState.allApps.isEmpty()) {
                    item {
                        Text(
                            text = "未获取到应用列表",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                } else {
                    items(uiState.allApps, key = { "all_${it.packageName}" }) { app ->
                        val isMonitored = app.packageName in monitoredPackages
                        AppListItem(
                            appInfoUi = app,
                            isMonitored = isMonitored,
                            onToggle = onToggleApp
                        )
                    }
                }
            }
        }
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectAppScreenEmptyListPreview() {
    val mockState = SelectAppUiState(
        monitoredApps = emptyList(),
        allApps = emptyList()
    )
    AppTheme {
        SelectAppScreen(
            uiState = mockState,
            onToggleApp = {}
        )
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectAppScreenPreview() {
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
        allApps = listOf(
            AppInfoUi(
                name = "App 3",
                packageName = "com.example.app3",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            ),
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
    AppTheme {
        SelectAppScreen(
            uiState = mockState,
            onToggleApp = {}
        )
    }
}