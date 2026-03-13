package com.huojieren.apppause.data.models

import androidx.compose.ui.graphics.painter.Painter
import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    var name: String,
    val packageName: String
)

fun AppInfo.toUI(painter: Painter): AppInfoUi {
    return AppInfoUi(
        name = name,
        packageName = packageName,
        icon = painter
    )
}