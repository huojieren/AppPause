package com.huojieren.apppause.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.ui.components.Picker
import com.huojieren.apppause.ui.state.rememberPickerState
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun TimeSelectionScreen(
    modifier: Modifier = Modifier,
    appInfoUi: AppInfoUi,
    onExtend5Clicked: () -> Unit,
    onExtend10Clicked: () -> Unit,
    onCancelButtonClicked: () -> Unit,
    onConfirmButtonClicked: (Int) -> Unit
) {
    val timeValues = remember { (1..60).map { it.toString() } }
    val valuesPickerState = rememberPickerState()
    val units = remember { listOf("秒钟", "分钟", "小时") }
    val unitsPickerState = rememberPickerState()

    // 使用derivedStateOf创建派生状态，当Picker状态变化时自动重新计算
    val selectedTimeInSeconds = remember {
        derivedStateOf {
            val value = valuesPickerState.selectedItem.toIntOrNull() ?: 1
            val unit = unitsPickerState.selectedItem

            when (unit) {
                "秒钟" -> value
                "分钟" -> value * 60
                "小时" -> value * 3600
                else -> value
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = modifier
                    .width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = appInfoUi.icon,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    // TODO 2025/11/7 23:05 根据 text 动态设置行数
                    Text(
                        text = "为 ${appInfoUi.name} 设置使用时长",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Row(
                        modifier = Modifier
                            .width(150.dp)
                    ) {
                        Picker(
                            modifier = Modifier
                                .weight(3f),
                            items = timeValues,
                            state = valuesPickerState,
                            textModifier = Modifier.padding(8.dp),
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Picker(
                            modifier = Modifier
                                .weight(7f),
                            items = units,
                            state = unitsPickerState,
                            textModifier = Modifier.padding(8.dp),
                        )
                    }
                    Row {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onExtend5Clicked
                        ) {
                            Text(text = "延长 5 分钟")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onExtend10Clicked
                        ) {
                            Text(text = "延长 10 分钟")
                        }
                    }
                    Row {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onCancelButtonClicked
                        ) {
                            Text(text = "取消")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onConfirmButtonClicked(selectedTimeInSeconds.value) }
                        ) {
                            Text(text = "确定")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TimeSelectionCardPreView() {
    val mockAppInfo = AppInfoUi(
        name = "App Name",
        packageName = "com.example.app",
        icon = painterResource(id = R.drawable.ic_launcher_foreground)
    )

    AppTheme {
        TimeSelectionScreen(
            appInfoUi = mockAppInfo,
            onExtend5Clicked = {},
            onExtend10Clicked = {},
            onCancelButtonClicked = {},
            onConfirmButtonClicked = {}
        )
    }
}

@Composable
fun TimeOutScreen(
    modifier: Modifier = Modifier,
    appInfoUi: AppInfoUi,
    onReturnToHomeScreenClicked: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        modifier = modifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = appInfoUi.icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp)
            )
            // TODO 2025/11/7 23:05 根据 text 动态设置行数
            Text(
                text = appInfoUi.name,
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "使用时间已到",
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onReturnToHomeScreenClicked
            ) {
                Text(
                    text = "返回桌面"
                )
            }
        }
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TimeOutScreenPreview() {
    val mockAppInfoUi = AppInfoUi(
        name = "App Name",
        packageName = "com.example.app",
        icon = painterResource(id = R.drawable.ic_launcher_foreground)
    )

    AppTheme {
        TimeOutScreen(
            appInfoUi = mockAppInfoUi,
            onReturnToHomeScreenClicked = {}
        )
    }
}