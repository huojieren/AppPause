package com.huojieren.apppause.data.diagnostics.model

/** 需要独立长期保留的事故类型。 */
enum class IncidentType(val filePrefix: String) {
    JAVA_CRASH("java-crash"),
    PROCESS_EXIT("process-exit")
}
