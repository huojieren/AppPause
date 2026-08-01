package com.huojieren.apppause.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.data.diagnostics.model.DiagnosticIncident
import com.huojieren.apppause.data.diagnostics.model.IncidentType
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.components.DiagnosticIncidentItem
import com.huojieren.apppause.ui.state.DiagnosticsUiState
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun DiagnosticsScreen(
    modifier: Modifier = Modifier,
    uiState: DiagnosticsUiState,
    onBack: () -> Unit,
    onIncidentClicked: (DiagnosticIncident) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回设置",
                )
            }
            Text(
                text = "诊断信息",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {


            if (uiState.incidents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "暂无异常记录",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.incidents, key = DiagnosticIncident::id) { incident ->
                        DiagnosticIncidentItem(
                            incident = incident,
                            onClick = { onIncidentClicked(incident) }
                        )
                    }
                }
            }

        }
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun DiagnosticsScreenPreview() {
    AppTheme {
        DiagnosticsScreen(
            uiState = DiagnosticsUiState(
                incidents = listOf(
                    DiagnosticIncident(
                        id = "process-exit-20260726-123000-100",
                        type = IncidentType.PROCESS_EXIT,
                        occurredAtEpochMs = 1_784_500_000_000,
                        reason = "LOW_MEMORY(7)",
                        description = "系统因内存紧张结束了进程",
                        hasTrace = true
                    ),
                    DiagnosticIncident(
                        id = "java-crash-20260725-221000-100",
                        type = IncidentType.JAVA_CRASH,
                        occurredAtEpochMs = 1_784_400_000_000,
                        exceptionName = "java.lang.IllegalStateException",
                        message = "Monitor service is not ready"
                    )
                )
            ),
            onBack = {},
            onIncidentClicked = {}
        )
    }
}
