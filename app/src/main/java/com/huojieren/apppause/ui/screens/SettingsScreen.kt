package com.huojieren.apppause.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
    onSharedTimingChanged: (Boolean) -> Unit,
    onWaitBeforeReturnChanged: (Boolean) -> Unit,
    onTimeoutTodoPromptChanged: (Boolean) -> Unit,
    onTimeSelectionTodoPromptChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PermissionGroup(
            uiState = uiState,
            onOverlayButtonClicked = onOverlayButtonClicked,
            onNotificationButtonClicked = onNotificationButtonClicked,
            onUsageStatsButtonClicked = onUsageStatsButtonClicked,
            onAccessibilityButtonClicked = onAccessibilityButtonClicked,
        )
        TimingGroup(
            uiState = uiState,
            onSharedTimingChanged = onSharedTimingChanged,
            onWaitBeforeReturnChanged = onWaitBeforeReturnChanged,
            onTimeoutTodoPromptChanged = onTimeoutTodoPromptChanged,
            onTimeSelectionTodoPromptChanged = onTimeSelectionTodoPromptChanged,
        )
        LogGroup(
            onClearLogButtonClicked = onClearLogButtonClicked,
            onSaveLogButtonClicked = onSaveLogButtonClicked
        )
        AboutGroup()
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
            onSharedTimingChanged = {},
            onWaitBeforeReturnChanged = {},
            onTimeoutTodoPromptChanged = {},
            onTimeSelectionTodoPromptChanged = {}
        )
    }
}

@Composable
private fun PermissionGroup(
    modifier: Modifier = Modifier,
    uiState: AppStatusUiState,
    onOverlayButtonClicked: () -> Unit,
    onNotificationButtonClicked: () -> Unit,
    onUsageStatsButtonClicked: () -> Unit,
    onAccessibilityButtonClicked: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "权限管理",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        SettingsClickableRow(
            title = if (uiState.hasOverlay) "悬浮窗权限" else "申请悬浮窗权限",
            onClick = onOverlayButtonClicked,
            enabled = !uiState.hasOverlay
        )
        SettingsClickableRow(
            title = if (uiState.hasNotification) "通知权限" else "申请通知权限",
            onClick = onNotificationButtonClicked,
            enabled = !uiState.hasNotification
        )
        SettingsClickableRow(
            title = if (uiState.hasUsageStats) "使用情况权限" else "申请使用情况权限",
            onClick = onUsageStatsButtonClicked,
            enabled = !uiState.hasUsageStats
        )
        SettingsClickableRow(
            title = if (uiState.hasAccessibility) "无障碍服务权限" else "申请无障碍服务权限",
            onClick = onAccessibilityButtonClicked,
            enabled = !uiState.hasAccessibility
        )
    }
}

@Composable
private fun TimingGroup(
    modifier: Modifier = Modifier,
    uiState: AppStatusUiState,
    onSharedTimingChanged: (Boolean) -> Unit,
    onWaitBeforeReturnChanged: (Boolean) -> Unit,
    onTimeoutTodoPromptChanged: (Boolean) -> Unit,
    onTimeSelectionTodoPromptChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "计时设置",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        SettingsSwitchRow(
            title = "所有应用共享额度",
            checked = uiState.isSharedTimingEnabled,
            onCheckedChange = onSharedTimingChanged
        )
        SettingsSwitchRow(
            title = "超时返回需等待5秒",
            checked = uiState.isWaitBeforeReturnEnabled,
            onCheckedChange = onWaitBeforeReturnChanged
        )
        SettingsSwitchRow(
            title = "超时窗口弹出todo",
            checked = uiState.isTimeoutTodoPromptEnabled,
            onCheckedChange = onTimeoutTodoPromptChanged
        )
        SettingsSwitchRow(
            title = "选择时间窗口弹出todo",
            checked = uiState.isTimeSelectionTodoPromptEnabled,
            onCheckedChange = onTimeSelectionTodoPromptChanged
        )
    }
}

@Composable
private fun LogGroup(
    modifier: Modifier = Modifier,
    onClearLogButtonClicked: () -> Unit,
    onSaveLogButtonClicked: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "日志管理",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        SettingsClickableRow(
            title = "保存缓存日志",
            onClick = onSaveLogButtonClicked,
            isHighlight = false
        )
        SettingsClickableRow(
            title = "清空缓存日志",
            onClick = onClearLogButtonClicked,
            isHighlight = true
        )
    }
}

@Composable
private fun AboutGroup(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Text(
            text = "关于",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Text(
            text = stringResource(R.string.version_text, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    modifier: Modifier = Modifier,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun SettingsClickableRow(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isHighlight: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                isHighlight -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}