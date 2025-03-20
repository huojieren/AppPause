package com.huojieren.apppause.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.ui.state.MonitorState
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun MyFilledTonalButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun MonitorControlButton(
    state: MonitorState,
    onStartMonitor: () -> Unit,
    onStopMonitor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonText = if (state.isMonitoring) {
        stringResource(R.string.stop_monitor)
    } else {
        stringResource(R.string.start_monitor)
    }

    val enabled = when {
        state.isMonitoring -> true
        else -> state.hasAllPermissions && state.hasMonitoredApps
    }

    MyFilledTonalButton(
        text = buttonText,
        onClick = {
            if (state.isMonitoring) {
                onStopMonitor()
            } else {
                onStartMonitor()
            }
        },
        enabled = enabled,
        modifier = modifier
    )
}

// 预览组件
@Preview
@Composable
fun MainActivity() {
    AppTheme {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MyFilledTonalButton(
                text = "悬浮窗权限已授予",
                onClick = {},
                enabled = false
            )
            MyFilledTonalButton(
                text = "请求通知权限",
                onClick = {},
                enabled = true
            )
            MyFilledTonalButton(
                text = "请求使用情况权限",
                onClick = {},
                enabled = true
            )
            MyFilledTonalButton(
                text = "监控应用列表",
                onClick = {},
                enabled = true
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MyFilledTonalButton(
                    text = "清空日志",
                    onClick = {},
                    enabled = true,
                    modifier = Modifier.weight(1f)
                )
                MyFilledTonalButton(
                    text = "保存日志",
                    onClick = {},
                    enabled = true,
                    modifier = Modifier.weight(1f)
                )
            }
            MonitorControlButton(
                state = MonitorState(
                    isMonitoring = true,
                    hasAllPermissions = true,
                    hasMonitoredApps = true
                ),
                onStartMonitor = {},
                onStopMonitor = {}
            )

            // 未监控但有权限的状态
            MonitorControlButton(
                state = MonitorState(
                    isMonitoring = false,
                    hasAllPermissions = true,
                    hasMonitoredApps = true
                ),
                onStartMonitor = {},
                onStopMonitor = {}
            )
            Text(
                text = "当前版本：v0.7.0",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
