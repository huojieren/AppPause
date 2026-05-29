package com.huojieren.apppause.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.components.SettingsNumberInputRow
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
            subtitle = if (uiState.hasOverlay) "已获取，可正常显示计时窗口" else "允许在应用上方显示计时窗口",
            trailingText = if (uiState.hasOverlay) "已获取" else null,
            onClick = onOverlayButtonClicked,
            enabled = !uiState.hasOverlay
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "通知权限",
            subtitle = if (uiState.hasNotification) "已获取，可保持监控服务运行" else "允许显示前台服务通知",
            trailingText = if (uiState.hasNotification) "已获取" else null,
            onClick = onNotificationButtonClicked,
            enabled = !uiState.hasNotification
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "使用情况权限",
            subtitle = if (uiState.hasUsageStats) "已获取，可识别当前使用的应用" else "用于判断当前正在使用哪个应用",
            trailingText = if (uiState.hasUsageStats) "已获取" else null,
            onClick = onUsageStatsButtonClicked,
            enabled = !uiState.hasUsageStats
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "无障碍服务权限",
            subtitle = if (uiState.hasAccessibility) "已获取，可辅助返回桌面" else "用于超时后引导回到桌面",
            trailingText = if (uiState.hasAccessibility) "已获取" else null,
            onClick = onAccessibilityButtonClicked,
            enabled = !uiState.hasAccessibility
        )
        SettingsDivider()
        SettingsClickableRow(
            title = "电池优化白名单",
            subtitle = if (uiState.hasBatteryOptimizationExemption) "已关闭限制，可降低后台中断概率" else "允许系统减少对监控服务的省电限制",
            trailingText = if (uiState.hasBatteryOptimizationExemption) "已设置" else null,
            onClick = onBatteryOptimizationButtonClicked,
            enabled = !uiState.hasBatteryOptimizationExemption
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
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        thickness = DividerDefaults.Thickness,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    )
}

@Composable
private fun SettingsStaticRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsClickableRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    trailingText: String? = null,
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
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    isHighlight -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                }
            )
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
    }
}
