package com.huojieren.apppause.ui.state

import com.huojieren.apppause.data.diagnostics.model.DiagnosticIncident

data class DiagnosticsUiState(
    val incidents: List<DiagnosticIncident> = emptyList()
)
