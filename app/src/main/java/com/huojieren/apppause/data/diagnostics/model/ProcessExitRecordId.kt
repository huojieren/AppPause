package com.huojieren.apppause.data.diagnostics.model

import java.text.SimpleDateFormat
import java.util.Locale

/** 为一条系统进程退出记录生成稳定标识，并兼容从旧事故字段恢复该标识。 */
object ProcessExitRecordId {
    fun create(timestamp: Long, pid: Int, reason: Int, processName: String?): String =
        "$timestamp-$pid-$reason-${processName.orEmpty()}"

    fun fromFields(fields: Map<String, String>): String? {
        fields["sourceExitId"]?.takeIf(String::isNotBlank)?.let { return it }
        val timestamp = fields["occurredAtEpochMs"]?.toLongOrNull()
            ?: fields["exitTimestamp"]?.parseTimestamp()
            ?: return null
        val pid = fields["pid"]?.toIntOrNull() ?: return null
        val reason = fields["reason"]
            ?.substringAfterLast('(', missingDelimiterValue = "")
            ?.removeSuffix(")")
            ?.toIntOrNull()
            ?: return null
        val processName = fields["processName"] ?: return null
        return create(timestamp, pid, reason, processName)
    }

    private fun String.parseTimestamp(): Long? = runCatching {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
            .apply { isLenient = false }
            .parse(this)
            ?.time
    }.getOrNull()
}
