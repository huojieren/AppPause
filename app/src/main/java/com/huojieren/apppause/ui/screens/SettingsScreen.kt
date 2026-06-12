package com.huojieren.apppause.ui.screens

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.components.SettingsClickableRow
import com.huojieren.apppause.ui.components.SettingsNumberInputRow
import com.huojieren.apppause.ui.components.SettingsStaticRow
import com.huojieren.apppause.ui.components.SettingsSwitchRow
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
    onBatteryOptimizationButtonClicked: () -> Unit,
    onClearLogButtonClicked: () -> Unit,
    onSaveLogButtonClicked: () -> Unit,
    onSharedTimingChanged: (Boolean) -> Unit,
    onWaitBeforeReturnChanged: (Boolean) -> Unit,
    onWaitBeforeReturnSecondsChanged: (Int) -> Unit,
    onTodoPromptChanged: (Boolean) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    focusManager.clearFocus()
                }
            }
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PermissionGroup(
            uiState = uiState,
            onOverlayButtonClicked = onOverlayButtonClicked,
            onNotificationButtonClicked = onNotificationButtonClicked,
            onUsageStatsButtonClicked = onUsageStatsButtonClicked,
            onAccessibilityButtonClicked = onAccessibilityButtonClicked,
            onBatteryOptimizationButtonClicked = onBatteryOptimizationButtonClicked,
        )
        TimingGroup(
            uiState = uiState,
            onSharedTimingChanged = onSharedTimingChanged,
            onWaitBeforeReturnChanged = onWaitBeforeReturnChanged,
            onWaitBeforeReturnSecondsChanged = onWaitBeforeReturnSecondsChanged,
            onTodoPromptChanged = onTodoPromptChanged,
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
        hasAccessibility = true,
        isWaitBeforeReturnEnabled = true,
        waitBeforeReturnSeconds = 5
    )
    AppTheme {
        SettingsScreen(
            uiState = mockState,
            onOverlayButtonClicked = {},
            onNotificationButtonClicked = {},
            onUsageStatsButtonClicked = {},
            onAccessibilityButtonClicked = {},
            onBatteryOptimizationButtonClicked = {},
            onClearLogButtonClicked = {},
            onSaveLogButtonClicked = {},
            onSharedTimingChanged = {},
            onWaitBeforeReturnChanged = {},
            onWaitBeforeReturnSecondsChanged = {},
            onTodoPromptChanged = {}
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
    onBatteryOptimizationButtonClicked: () -> Unit = {},
) {
    SettingsCard(modifier = modifier) {
        SettingsClickableRow(
            title = "悬浮窗权限",
            subtitle = "用于在应用上方显示计时窗口",
            trailingText = if (uiState.hasOverlay) "已获取" else null,
            onClick = onOverlayButtonClicked,
            isGranted = uiState.hasOverlay
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "通知权限",
            subtitle = "允许显示前台服务通知，帮助保持监控服务运行",
            trailingText = if (uiState.hasNotification) "已获取" else null,
            onClick = onNotificationButtonClicked,
            isGranted = uiState.hasNotification
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "使用情况权限",
            subtitle = "用于统计应用使用情况",
            trailingText = if (uiState.hasUsageStats) "已获取" else null,
            onClick = onUsageStatsButtonClicked,
            isGranted = uiState.hasUsageStats
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "无障碍服务权限",
            subtitle = "用于识别当前使用的应用",
            trailingText = if (uiState.hasAccessibility) "已获取" else null,
            onClick = onAccessibilityButtonClicked,
            isGranted = uiState.hasAccessibility
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "电池优化白名单",
            subtitle = "允许系统减少对监控服务的省电限制，可降低后台中断概率",
            trailingText = if (uiState.hasBatteryOptimizationExemption) "已设置" else null,
            onClick = onBatteryOptimizationButtonClicked,
            isGranted = uiState.hasBatteryOptimizationExemption
        )
    }
}

@Composable
private fun TimingGroup(
    modifier: Modifier = Modifier,
    uiState: AppStatusUiState,
    onSharedTimingChanged: (Boolean) -> Unit,
    onWaitBeforeReturnChanged: (Boolean) -> Unit,
    onWaitBeforeReturnSecondsChanged: (Int) -> Unit,
    onTodoPromptChanged: (Boolean) -> Unit,
) {
    SettingsCard(modifier = modifier) {
        SettingsSwitchRow(
            title = "所有应用共享额度",
            infoText = "开启后所有应用一起计算使用时长",
            checked = uiState.isSharedTimingEnabled,
            onCheckedChange = onSharedTimingChanged
        )
        SettingsDivider()
        SettingsSwitchRow(
            title = "超时返回前等待",
            infoText = "开启后可自定义返回桌面前的等待时间",
            checked = uiState.isWaitBeforeReturnEnabled,
            onCheckedChange = onWaitBeforeReturnChanged
        )
        if (uiState.isWaitBeforeReturnEnabled) {
            SettingsDivider()
            SettingsNumberInputRow(
                title = "等待时长",
                subtitle = "默认 5 秒，可设置 1 到 99 秒",
                value = uiState.waitBeforeReturnSeconds,
                onValueChange = onWaitBeforeReturnSecondsChanged,
                suffix = "秒"
            )
        }
        SettingsDivider()
        SettingsSwitchRow(
            title = "计时窗口显示待办提醒",
            infoText = "开启后选择时间和超时时显示待办提醒",
            checked = uiState.isTodoPromptEnabled,
            onCheckedChange = onTodoPromptChanged
        )
    }
}

@Composable
private fun LogGroup(
    modifier: Modifier = Modifier,
    onClearLogButtonClicked: () -> Unit,
    onSaveLogButtonClicked: () -> Unit
) {
    SettingsCard(modifier = modifier) {
        SettingsClickableRow(
            title = "保存缓存日志",
            subtitle = "导出当前缓存日志用于排查问题",
            onClick = onSaveLogButtonClicked,
            isHighlight = false
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "清空缓存日志",
            subtitle = "删除本地缓存日志",
            onClick = onClearLogButtonClicked,
            isHighlight = true
        )
    }
}

@Composable
private fun AboutGroup(
    modifier: Modifier = Modifier,
) {
    SettingsCard(modifier = modifier) {
        SettingsStaticRow(
            title = "版本",
            subtitle = stringResource(R.string.version_text, BuildConfig.VERSION_NAME)
        )
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.padding(start = 16.dp),
        thickness = DividerDefaults.Thickness,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}
