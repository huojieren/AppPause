package com.huojieren.apppause.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberPickerState() = remember { PickerState() }

// TODO: 2025年10月12日23:15:10 uiState 两种构建方式的不同

class PickerState {
    var selectedItem by mutableStateOf("")
}