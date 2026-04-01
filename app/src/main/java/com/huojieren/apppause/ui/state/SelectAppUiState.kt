package com.huojieren.apppause.ui.state

import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.data.models.AppLetterGroup

data class SelectAppUiState(
    val monitoredApps: List<AppInfoUi> = emptyList(),
    val allAppsGrouped: List<AppLetterGroup<AppInfoUi>> = emptyList(),
    val letterPositions: Map<String, Int> = emptyMap(),
)