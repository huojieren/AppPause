package com.huojieren.apppause.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.components.AlphabetIndexBar
import com.huojieren.apppause.ui.components.AppListItem
import com.huojieren.apppause.ui.mockSelectAppUiState
import com.huojieren.apppause.ui.state.SelectAppUiState
import com.huojieren.apppause.ui.theme.AppTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SelectAppScreen(
    uiState: SelectAppUiState,
    modifier: Modifier = Modifier,
    onToggleApp: (AppInfoUi) -> Unit,
    getLetterPosition: (String) -> Int?,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val enableFab = false // 是否启用 Fab
    val showFab by remember {
        derivedStateOf {
            if (!enableFab) false
            else listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 100
        }
    }

    val monitoredPackages = remember(uiState.monitoredApps) {
        uiState.monitoredApps.map { it.packageName }.toSet()
    }

    var touchedLetter by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.scrollToItem(0)
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
            Box(modifier = Modifier.fillMaxSize()) {
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
                                .padding(top = 16.dp)
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
                        items(
                            uiState.monitoredApps,
                            key = { "monitored_${it.packageName}" }) { app ->
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
                    if (uiState.allAppsGrouped.isEmpty()) {
                        item {
                            Text(
                                text = "未获取到应用列表",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    } else {
                        uiState.allAppsGrouped.forEach { group ->
                            item(key = "header_${group.letter}") {
                                Text(
                                    text = group.letter,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                )
                            }
                            items(
                                items = group.items,
                                key = { "all_${it.packageName}" }
                            ) { app ->
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

                AlphabetIndexBar(
                    displayLetter = touchedLetter,
                    onLetterSelected = { letter ->
                        if (letter.isNotEmpty()) {
                            touchedLetter = letter
                            getLetterPosition(letter).let { index ->
                                coroutineScope.launch {
                                    index?.let { listState.scrollToItem(it) }
                                }
                            }
                        } else {
                            touchedLetter = null
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun SelectAppScreenEmptyListPreview() {
    val mockState = SelectAppUiState(
        monitoredApps = emptyList(),
        allAppsGrouped = emptyList()
    )
    AppTheme {
        SelectAppScreen(
            uiState = mockState,
            onToggleApp = {},
            getLetterPosition = { 0 }
        )
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun SelectAppScreenPreview() {
    AppTheme {
        SelectAppScreen(
            uiState = mockSelectAppUiState(),
            onToggleApp = {},
            getLetterPosition = { 0 }
        )
    }
}