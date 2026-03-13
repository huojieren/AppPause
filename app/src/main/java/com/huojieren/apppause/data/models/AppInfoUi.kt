package com.huojieren.apppause.data.models

import androidx.compose.ui.graphics.painter.Painter

data class AppInfoUi(
    val name: String,
    val packageName: String,
    val icon: Painter
)

fun AppInfoUi.toEntity(): AppInfo {
    return AppInfo(name, packageName)
}
