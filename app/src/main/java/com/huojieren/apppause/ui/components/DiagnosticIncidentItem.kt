package com.huojieren.apppause.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.huojieren.apppause.data.diagnostics.model.DiagnosticIncident
import com.huojieren.apppause.data.diagnostics.model.IncidentType
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 直接展示一次异常节点的类型、发生时间与关键原因。 */
@Composable
fun DiagnosticIncidentItem(
    incident: DiagnosticIncident,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        ListItem(
            headlineContent = {
                Text(incident.type.displayName(), style = MaterialTheme.typography.titleMedium)
            },
            supportingContent = {
                Text(
                    text = listOfNotNull(
                        incident.occurredAt.formatTimestamp(),
                        incident.summary(),
                        "附带系统追踪".takeIf { incident.hasTrace }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}

private fun IncidentType.displayName(): String = when (this) {
    IncidentType.JAVA_CRASH -> "应用崩溃"
    IncidentType.PROCESS_EXIT -> "进程异常退出"
}

private fun DiagnosticIncident.summary(): String? = when (type) {
    IncidentType.JAVA_CRASH -> listOfNotNull(
        exceptionName?.substringAfterLast('.'),
        message?.takeIf(String::isNotBlank)
    ).joinToString(": ").ifBlank { null }
    IncidentType.PROCESS_EXIT -> listOfNotNull(
        reason?.toReadableExitReason(),
        description?.takeIf(String::isNotBlank)
    ).joinToString("；").ifBlank { null }
}

private fun String.toReadableExitReason(): String = when {
    startsWith("ANR") -> "应用无响应"
    startsWith("CRASH_NATIVE") -> "原生层崩溃"
    startsWith("CRASH") -> "应用崩溃"
    startsWith("LOW_MEMORY") -> "内存不足"
    startsWith("EXCESSIVE_RESOURCE_USAGE") -> "资源使用过多"
    startsWith("DEPENDENCY_DIED") -> "依赖进程已结束"
    startsWith("USER_") -> "用户或系统主动结束"
    else -> substringBefore('(').replace('_', ' ')
}

private fun Long.formatTimestamp(): String = DateTimeFormatter
    .ofPattern("yyyy-MM-dd HH:mm")
    .withZone(ZoneId.systemDefault())
    .format(Instant.ofEpochMilli(this))

@LightComponentPreview
@DarkComponentPreview
@Composable
fun DiagnosticIncidentItemPreview() {
    AppTheme {
        DiagnosticIncidentItem(
            incident = DiagnosticIncident(
                id = "process-exit-20260726-123000-100",
                type = IncidentType.PROCESS_EXIT,
                occurredAt = 1_784_500_000_000,
                reason = "LOW_MEMORY(7)",
                description = "系统因内存紧张结束了进程",
                hasTrace = true
            )
        )
    }
}
