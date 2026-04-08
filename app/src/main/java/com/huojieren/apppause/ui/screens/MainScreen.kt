package com.huojieren.apppause.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.state.MainScreenUiState
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: MainScreenUiState,
    onLifecycleChange: () -> Unit,
    onOverlayButtonClicked: () -> Unit,
    onNotificationButtonClicked: () -> Unit,
    onUsageStatsButtonClicked: () -> Unit,
    onAccessibilityButtonClicked: () -> Unit,
    onClearLogButtonClicked: () -> Unit,
    onSaveLogButtonClicked: () -> Unit,
    onToggleMonitoring: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            LazyColumnEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    onLifecycleChange()
                }
            }
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            PermissionCard(
                uiState = uiState,
                onOverlayButtonClicked = onOverlayButtonClicked,
                onNotificationButtonClicked = onNotificationButtonClicked,
                onUsageStatsButtonClicked = onUsageStatsButtonClicked,
                onAccessibilityButtonClicked = onAccessibilityButtonClicked,
            )
            LogCard(
                onClearLogButtonClicked = onClearLogButtonClicked,
                onSaveLogButtonClicked = onSaveLogButtonClicked
            )
            MonitoredStatusCard(
                uiState = uiState,
                onToggleMonitoring = onToggleMonitoring,
            )
            VersionText()
        }
    }
}

private class LazyColumnEventObserver(
    private val onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        onEvent(source, event)
    }
}

@LightComponentPreview
@Composable
fun MainScreenPreview() {
    val mockState = MainScreenUiState(
        isMonitoring = true,
        hasOverlay = true,
        hasNotification = true,
        hasUsageStats = true,
        hasAccessibility = true
    )
    AppTheme {
        MainScreen(
            uiState = mockState,
            onLifecycleChange = {},
            onOverlayButtonClicked = {},
            onNotificationButtonClicked = {},
            onUsageStatsButtonClicked = {},
            onAccessibilityButtonClicked = {},
            onClearLogButtonClicked = {},
            onSaveLogButtonClicked = {},
            onToggleMonitoring = {},
        )
    }
}

@Composable
fun PermissionCard(
    modifier: Modifier = Modifier,
    uiState: MainScreenUiState,
    onOverlayButtonClicked: () -> Unit,
    onNotificationButtonClicked: () -> Unit,
    onUsageStatsButtonClicked: () -> Unit,
    onAccessibilityButtonClicked: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "权限管理",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = !uiState.hasOverlay,
                onClick = onOverlayButtonClicked
            ) {
                val buttonText =
                    if (uiState.hasOverlay) "已获得悬浮窗权限" else "申请悬浮窗权限"
                Text(text = buttonText)
            }
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = !uiState.hasNotification,
                onClick = onNotificationButtonClicked
            ) {
                val buttonText =
                    if (uiState.hasNotification) "已获得通知权限" else "申请通知权限"
                Text(text = buttonText)
            }
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = !uiState.hasUsageStats,
                onClick = onUsageStatsButtonClicked
            ) {
                val buttonText =
                    if (uiState.hasUsageStats) "已获得使用情况权限" else "申请使用情况权限"
                Text(text = buttonText)
            }
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = !uiState.hasAccessibility,
                onClick = onAccessibilityButtonClicked
            ) {
                val buttonText =
                    if (uiState.hasAccessibility) "已获得无障碍服务权限" else "申请无障碍服务权限"
                Text(text = buttonText)
            }
        }
    }
}

@Composable
fun LogCard(
    modifier: Modifier = Modifier,
    onClearLogButtonClicked: () -> Unit,
    onSaveLogButtonClicked: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "日志管理",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Row {
                FilledTonalButton(
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier
                        .weight(1f),
                    onClick = onClearLogButtonClicked
                ) {
                    Text(text = "清空缓存日志")
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    modifier = Modifier
                        .weight(1f),
                    onClick = onSaveLogButtonClicked
                ) {
                    Text(text = "保存缓存日志")
                }
            }
        }
    }
}

@Composable
fun MonitoredStatusCard(
    modifier: Modifier = Modifier,
    uiState: MainScreenUiState,
    onToggleMonitoring: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "监控状态",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth(),
                enabled = uiState.hasOverlay && uiState.hasNotification && uiState.hasUsageStats,
                onClick = {
                    onToggleMonitoring()
                },
                content = {
                    Text(text = if (uiState.isMonitoring) "停止监控" else "开始监控")
                }
            )
        }
    }
}

@Composable
fun VersionText(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.version_text, BuildConfig.VERSION_NAME),
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier,
    )
}