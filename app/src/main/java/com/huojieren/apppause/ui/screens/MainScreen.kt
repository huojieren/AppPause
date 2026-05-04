package com.huojieren.apppause.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.state.AppStatusUiState
import com.huojieren.apppause.ui.theme.AppTheme

private class LazyLifeCycleEventObserver(
    private val onEvent: (LifecycleOwner, Lifecycle.Event) -> Unit
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        onEvent(source, event)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    uiState: AppStatusUiState,
    onLifecycleChange: () -> Unit,
    onToggleMonitoring: () -> Unit,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            LazyLifeCycleEventObserver { _, event ->
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularToggleButton(
                uiState = uiState,
                onToggleMonitoring = onToggleMonitoring,
            )
            StatusText(uiState = uiState)
        }
    }
}

@Composable
private fun CircularToggleButton(
    uiState: AppStatusUiState,
    onToggleMonitoring: () -> Unit,
) {
    val canToggle = uiState.isMonitoring ||
            (uiState.hasOverlay && uiState.hasNotification &&
                    uiState.hasUsageStats && uiState.hasAccessibility)


    FilledTonalButton(
        onClick = onToggleMonitoring,
        enabled = canToggle,
        modifier = Modifier.size(160.dp),
        shape = CircleShape,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (uiState.isMonitoring)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondary,
            contentColor = if (uiState.isMonitoring)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Text(
            text = if (uiState.isMonitoring) "停止" else "开启",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
private fun StatusText(uiState: AppStatusUiState) {
    val statusText = when {
        uiState.isMonitoring -> "监控中"
        !uiState.hasOverlay || !uiState.hasNotification ||
                !uiState.hasUsageStats || !uiState.hasAccessibility -> "请先在设置页授予所需权限"

        else -> "点击上方按钮开始监控"
    }

    Text(
        text = statusText,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun MainScreenOnMonitorPreview() {
    val mockState = AppStatusUiState(
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
            onToggleMonitoring = {},
        )
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun MainScreenOffMonitorPreview() {
    val mockState = AppStatusUiState(
        isMonitoring = false,
        hasOverlay = true,
        hasNotification = true,
        hasUsageStats = true,
        hasAccessibility = true
    )
    AppTheme {
        MainScreen(
            uiState = mockState,
            onLifecycleChange = {},
            onToggleMonitoring = {},
        )
    }
}