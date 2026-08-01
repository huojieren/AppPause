package com.huojieren.apppause.data.diagnostics.model

/** 供诊断页面展示的事故摘要，不包含运行日志和事故附件的完整内容。 */
data class DiagnosticIncident(
    val id: String,
    val type: IncidentType,
    val occurredAtEpochMs: Long,
    val reason: String? = null,
    val description: String? = null,
    val exceptionName: String? = null,
    val message: String? = null,
    val hasTrace: Boolean = false
)
