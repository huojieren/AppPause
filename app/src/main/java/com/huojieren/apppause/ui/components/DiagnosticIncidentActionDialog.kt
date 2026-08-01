package com.huojieren.apppause.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.huojieren.apppause.data.diagnostics.model.DiagnosticIncident
import com.huojieren.apppause.data.diagnostics.model.IncidentType
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun DiagnosticIncidentActionDialog(
    incident: DiagnosticIncident,
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(incident.displayName()) },
        text = { Text("可导出此事件的材料和当前运行日志上下文，或删除该事件及其附件。") },
        confirmButton = {
            TextButton(onClick = onExport) {
                Text("导出事件材料")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("删除事件", color = MaterialTheme.colorScheme.error)
            }
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun DiagnosticIncident.displayName(): String = when (type) {
    IncidentType.JAVA_CRASH -> "应用崩溃"
    IncidentType.PROCESS_EXIT -> if (reason?.startsWith("USER_") == true) {
        "进程主动结束"
    } else {
        "进程异常退出"
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun DiagnosticIncidentActionDialogPreview() {
    AppTheme {
        DiagnosticIncidentActionDialog(
            incident = DiagnosticIncident(
                id = "process-exit-20260726-123000-100",
                type = IncidentType.PROCESS_EXIT,
                occurredAtEpochMs = 1_784_500_000_000
            ),
            onDismiss = {},
            onExport = {},
            onDelete = {}
        )
    }
}
