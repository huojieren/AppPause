package com.huojieren.apppause.ui.state

import com.huojieren.apppause.data.models.AppInfoUi

data class SelectAppUiState(
    val monitoredApps: List<AppInfoUi> = emptyList(),
    val allApps: List<AppInfoUi> = emptyList(),
)