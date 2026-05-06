package com.huojieren.apppause.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.state.AppStatusUiState
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: AppStatusUiState,
    onOverlayButtonClicked: () -> Unit,
    onNotificationButtonClicked: () -> Unit,
    onUsageStatsButtonClicked: () -> Unit,
    onAccessibilityButtonClicked: () -> Unit,
    onClearLogButtonClicked: () -> Unit,
    onSaveLogButtonClicked: () -> Unit,
    onPerAppTimingChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
        TimingCard(
            uiState = uiState,
            onPerAppTimingChanged = onPerAppTimingChanged
        )
        AboutCard()
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun SettingsScreenPreview() {
    val mockState = AppStatusUiState(
        isMonitoring = true,
        hasOverlay = true,
        hasNotification = true,
        hasUsageStats = true,
        hasAccessibility = true
    )
    AppTheme {
        SettingsScreen(
            uiState = mockState,
            onOverlayButtonClicked = {},
            onNotificationButtonClicked = {},
            onUsageStatsButtonClicked = {},
            onAccessibilityButtonClicked = {},
            onClearLogButtonClicked = {},
            onSaveLogButtonClicked = {},
            onPerAppTimingChanged = {}
        )
    }
}

@Composable
private fun TimingCard(
    modifier: Modifier = Modifier,
    uiState: AppStatusUiState,
    onPerAppTimingChanged: (Boolean) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "计时设置",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "每个应用单独计时",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = uiState.isPerAppTimingEnabled,
                    onCheckedChange = onPerAppTimingChanged
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    modifier: Modifier = Modifier,
    uiState: AppStatusUiState,
    onOverlayButtonClicked: () -> Unit,
    onNotificationButtonClicked: () -> Unit,
    onUsageStatsButtonClicked: () -> Unit,
    onAccessibilityButtonClicked: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                enabled = !uiState.hasOverlay,
                onClick = onOverlayButtonClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val buttonText =
                    if (uiState.hasOverlay) "已获得悬浮窗权限" else "申请悬浮窗权限"
                Text(text = buttonText)
            }
            FilledTonalButton(
                enabled = !uiState.hasNotification,
                onClick = onNotificationButtonClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)

            ) {
                val buttonText =
                    if (uiState.hasNotification) "已获得通知权限" else "申请通知权限"
                Text(text = buttonText)
            }
            FilledTonalButton(
                enabled = !uiState.hasUsageStats,
                onClick = onUsageStatsButtonClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val buttonText =
                    if (uiState.hasUsageStats) "已获得使用情况权限" else "申请使用情况权限"
                Text(text = buttonText)
            }
            FilledTonalButton(
                enabled = !uiState.hasAccessibility,
                onClick = onAccessibilityButtonClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val buttonText =
                    if (uiState.hasAccessibility) "已获得无障碍服务权限" else "申请无障碍服务权限"
                Text(text = buttonText)
            }
        }
    }
}

@Composable
private fun LogCard(
    modifier: Modifier = Modifier,
    onClearLogButtonClicked: () -> Unit,
    onSaveLogButtonClicked: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    onClick = onClearLogButtonClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "清空缓存日志")
                }
                FilledTonalButton(
                    onClick = onSaveLogButtonClicked,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "保存缓存日志")
                }
            }
        }
    }
}

@Composable
private fun AboutCard(
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "关于",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Text(
                text = stringResource(R.string.version_text, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}
