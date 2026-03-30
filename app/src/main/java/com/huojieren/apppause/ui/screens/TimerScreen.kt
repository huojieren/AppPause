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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.ui.components.Picker
import com.huojieren.apppause.ui.state.rememberPickerState
import com.huojieren.apppause.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first

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
    val units = remember { listOf("分钟", "小时", "秒钟") }
    val unitsPickerState = rememberPickerState()

    // 使用 remember 创建派生状态
    val selectedTimeInSeconds = remember(
        valuesPickerState.selectedItem,
        unitsPickerState.selectedItem
    ) {
        val value = valuesPickerState.selectedItem.toIntOrNull() ?: 1
        val unit = unitsPickerState.selectedItem

        when (unit) {
            "秒钟" -> value
            "分钟" -> value * 60
            "小时" -> value * 3600
            else -> value
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .width(300.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
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
                    Spacer(modifier = Modifier.height(8.dp))
                    // TODO 2025/11/7 23:05 根据 text 动态设置行数
                    Text(
                        text = "为 ${appInfoUi.name} 设置使用时长",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                    Spacer(modifier = Modifier.height(8.dp))
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
                            onClick = { onConfirmButtonClicked(selectedTimeInSeconds) }
                        ) {
                            Text(text = "确定")
                        }
                    }
                }
            }
        }
    }
}

//@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TimeSelectionCardPreView() {
    val mockAppInfo = AppInfoUi(
        name = "App Name",
        packageName = "com.example.app",
        icon = painterResource(id = R.drawable.ic_launcher_foreground)
    )

    AppTheme {
        TimeSelectionScreen(
            modifier = Modifier.fillMaxSize(),
            appInfoUi = mockAppInfo,
            onExtend5Clicked = {},
            onExtend10Clicked = {},
            onCancelButtonClicked = {},
            onConfirmButtonClicked = {}
        )
    }
}

private const val TAG = "TimeOutScreen"

@Composable
fun TimeOutScreen(
    modifier: Modifier = Modifier,
    appInfoUi: AppInfoUi,
    fadeInCompleteEvent: SharedFlow<Unit>,
    onClickReturnToHome: () -> Unit,
    onAutoReturnToHome: () -> Unit = {},
) {
    var countDown by remember { mutableIntStateOf(5) }
    val canClick = countDown <= 0

    LaunchedEffect(fadeInCompleteEvent) {
        logger(TAG, "Waiting for fadeInCompleteEvent...")
        fadeInCompleteEvent.first()
        logger(TAG, "FadeInCompleteEvent received")
        logger(TAG, "Auto returning to home")
        onAutoReturnToHome()
        logger(TAG, "Starting countdown")
        while (countDown > 0) {
            delay(1000)
            countDown--
            logger(TAG, "countDown: $countDown")
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
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
            Spacer(modifier = Modifier.height(16.dp))
            // TODO 2025/11/7 23:05 根据 text 动态设置行数
            Text(
                text = appInfoUi.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "使用时间已到",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onClickReturnToHome,
                enabled = canClick,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = if (canClick) "返回桌面" else "返回桌面 ($countDown)",
                    style = MaterialTheme.typography.titleMedium
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

    val mockFlow = remember { kotlinx.coroutines.flow.MutableSharedFlow<Unit>() }

    AppTheme {
        TimeOutScreen(
            modifier = Modifier.fillMaxSize(),
            appInfoUi = mockAppInfoUi,
            onClickReturnToHome = {},
            fadeInCompleteEvent = mockFlow
        )
    }
}