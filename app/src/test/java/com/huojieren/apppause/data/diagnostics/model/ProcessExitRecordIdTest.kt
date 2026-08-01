package com.huojieren.apppause.data.diagnostics.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessExitRecordIdTest {
    @Test
    fun create_returnsStableRecordId() {
        assertEquals(
            "1785599046293-16076-4-com.huojieren.apppause",
            ProcessExitRecordId.create(1_785_599_046_293, 16_076, 4, "com.huojieren.apppause")
        )
    }

    @Test
    fun fromFields_recoversLegacyProcessExitId() {
        val fields = mapOf(
            "occurredAtEpochMs" to "1785599046293",
            "pid" to "16076",
            "reason" to "CRASH(4)",
            "processName" to "com.huojieren.apppause"
        )

        assertEquals(
            "1785599046293-16076-4-com.huojieren.apppause",
            ProcessExitRecordId.fromFields(fields)
        )
    }
}
