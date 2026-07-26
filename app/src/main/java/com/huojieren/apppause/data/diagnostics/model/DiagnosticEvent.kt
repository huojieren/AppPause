package com.huojieren.apppause.data.diagnostics.model

/** 描述一条写入运行日志的诊断事件及其可选上下文属性。 */
data class DiagnosticEvent(
    val name: String,
    val attributes: Map<String, String> = emptyMap()
)
