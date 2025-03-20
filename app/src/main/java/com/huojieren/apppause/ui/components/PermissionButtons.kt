package com.huojieren.apppause.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun PermissionButtons(
    overlayPermissionGranted: Boolean,
    notificationPermissionGranted: Boolean,
    usageStatsPermissionGranted: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestUsageStats: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 悬浮窗权限按钮
        PermissionButton(
            text = if (overlayPermissionGranted) "悬浮窗权限已授予" else "请求悬浮窗权限",
            onClick = onRequestOverlay,
            enabled = !overlayPermissionGranted
        )

        // 通知权限按钮
        PermissionButton(
            text = if (notificationPermissionGranted) "通知权限已授予" else "请求通知权限",
            onClick = onRequestNotification,
            enabled = !notificationPermissionGranted
        )

        // 使用情况权限按钮
        PermissionButton(
            text = if (usageStatsPermissionGranted) "使用情况权限已授予" else "请求使用情况权限",
            onClick = onRequestUsageStats,
            enabled = !usageStatsPermissionGranted
        )
    }
}

@Composable
private fun PermissionButton(
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

// 预览组件
@Preview
@Composable
fun PermissionButtonsPreview() {
    AppTheme {
        PermissionButtons(
            overlayPermissionGranted = false,
            notificationPermissionGranted = true,
            usageStatsPermissionGranted = false,
            onRequestOverlay = {},
            onRequestNotification = {},
            onRequestUsageStats = {}
        )
    }
}
