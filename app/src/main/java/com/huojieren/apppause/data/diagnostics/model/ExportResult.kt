package com.huojieren.apppause.data.diagnostics.model

/** 表示诊断材料导出的结果。 */
sealed interface ExportResult {
    data object Success : ExportResult
    data object NoLogs : ExportResult
    data class Failed(val error: Throwable) : ExportResult
}
