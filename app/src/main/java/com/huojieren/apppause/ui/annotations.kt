package com.huojieren.apppause.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light Theme", uiMode = Configuration.UI_MODE_NIGHT_NO, showSystemUi = true)
annotation class LightThemePreview

@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES, showSystemUi = true)
annotation class DarkThemePreview