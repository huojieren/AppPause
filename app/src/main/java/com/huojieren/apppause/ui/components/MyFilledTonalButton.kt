package com.huojieren.apppause.ui.components

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.ui.state.AppState
import com.huojieren.apppause.utils.ToastUtil

@Composable
fun MyFilledTonalButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
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

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun MonitorControlButton(
    appState: AppState,
    context: Context = LocalContext.current
) {
    val canStartMonitoring = appState.hasOverlayPermission &&
            appState.hasNotificationPermission &&
            appState.hasUsageStatsPermission &&
            appState.hasMonitoredApps

    MyFilledTonalButton(
        text = if (appState.isMonitoring) "停止监控" else "开始监控",
        onClick = {
            if (!canStartMonitoring) {
                when {
                    !appState.hasMonitoredApps -> ToastUtil.showToast(context, "请先添加监控应用")
                    else -> ToastUtil.showToast(context, "请先授予所有权限")
                }
                return@MyFilledTonalButton
            }

            if (appState.isMonitoring) {
                AppMonitor.getInstance(context).stopMonitoring()
            } else {
                AppMonitor.getInstance(context).startMonitoring()
            }
        },
        enabled = canStartMonitoring || appState.isMonitoring // 允许在监控中停止
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun PermissionButton(
    type: String,
    label: String,
    appState: AppState,
) {
    val hasPermission = when (type) {
        "overlay" -> appState.hasOverlayPermission
        "notification" -> appState.hasNotificationPermission
        else -> appState.hasUsageStatsPermission
    }

    val context = LocalContext.current
    val activity = context as Activity
    val permissionManager = remember { PermissionManager.get() }

    MyFilledTonalButton(
        text = if (hasPermission) "$label 已授予" else "请求$label",
        onClick = {
            if (!hasPermission) {
                when (type) {
                    "overlay" -> permissionManager.requestOverlayPermission(activity)
                    "notification" -> permissionManager.requestNotificationPermission(activity)
                    "usageStats" -> permissionManager.requestUsageStatsPermission(activity)
                }
            } else {
                // 已授权时点击提示
                ToastUtil.showToast(context, "$label 已授予")
            }
        },
        enabled = !hasPermission
    )
}
