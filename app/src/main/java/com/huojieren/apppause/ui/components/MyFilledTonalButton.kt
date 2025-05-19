package com.huojieren.apppause.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huojieren.apppause.ui.state.AppState

@Composable
fun MyFilledTonalButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
        disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
    )
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        colors = colors
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun MonitorControlButton(
    state: AppState,
    onToggle: () -> Unit
) {
    val canStart = state.run {
        hasOverlay && hasNotification && hasUsageStats
    } && state.monitoredApps.isNotEmpty()

    MyFilledTonalButton(
        text = if (state.isMonitoring) "停止监控" else "开始监控",
        onClick = {
            if (!canStart) return@MyFilledTonalButton
            onToggle()
        },
        enabled = canStart || state.isMonitoring
    )
}

@Composable
fun PermissionButton(
    label: String,
    hasPermission: Boolean,
    onRequest: () -> Unit
) {
    MyFilledTonalButton(
        text = if (hasPermission) "$label 已授予" else "请求$label",
        onClick = { if (!hasPermission) onRequest() }
    )
}
