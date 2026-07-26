package com.huojieren.apppause.data.diagnostics.model

/**
 * 在关键状态转换时保存的进程摘要。
 *
 * 内容会同时写入应用私有目录，并在 Android 11+ 写入 ApplicationExitInfo；不要放入应用名、用户数据等敏感内容。
 */
data class ProcessState(
    val source: String,
    val monitoring: Boolean,
    val accessibilityConnected: Boolean,
    val monitorServiceActive: Boolean,
    val monitorStrategy: String? = null
) {
    fun toCompactText(): String = listOfNotNull(
        "source=$source",
        "monitoring=$monitoring",
        "accessibility=$accessibilityConnected",
        "monitorService=$monitorServiceActive",
        monitorStrategy?.let { "strategy=$it" }
    ).joinToString(";")
}
