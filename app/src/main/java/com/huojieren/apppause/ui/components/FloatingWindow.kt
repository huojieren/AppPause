package com.huojieren.apppause.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun FloatingWindow(
    appName: String,
    timeUnitDesc: String,
    onConfirm: (Int) -> Unit,
    onCancel: () -> Unit,
    onExtend: (Int) -> Unit
) {
    var selectedTime by remember { mutableIntStateOf(1) }

    // 卡片容器
    Card(
        modifier = Modifier
            .focusable(true)
            .widthIn(min = 280.dp) // 最小宽度
            .padding(16.dp), // 外边距
        elevation = CardDefaults.cardElevation(8.dp), // 阴影效果
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // 使用主题表面色
        ),
    ) {
        // 垂直布局
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 标题
            Text(
                text = stringResource(R.string.for_appName, appName),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = stringResource(R.string.set_time),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
            // TODO: 时间选择器
//                NumberPicker(
//                    value = selectedTime,
//                    onValueChange = { selectedTime = it },
//                    range = 1..60,
//                    textStyle = LocalTextStyle.current.copy(
//                        color = MaterialTheme.colorScheme.onSurface,
//                        fontSize = 18.sp
//                    ),
//                    dividersColor = MaterialTheme.colorScheme.surfaceVariant,
//                    modifier = Modifier
//                        .padding(vertical = 8.dp)
//                        .focusable(true)
//                )
//                Text(
//                    text = timeUnitDesc,
//                    color = MaterialTheme.colorScheme.onSurface,
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }

            // 延长按钮组
            ExtendedTimeButtons(
                timeUnitDesc = timeUnitDesc,
                onExtend = onExtend,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // 确认/取消按钮组
            ActionButtons(
                onConfirm = { onConfirm(selectedTime) },
                onCancel = onCancel,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }

}

// 延长时长按钮组
@Composable
private fun ExtendedTimeButtons(
    timeUnitDesc: String,
    onExtend: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 延长5单位按钮
        ExtendedButton(
            units = 5,
            timeUnitDesc = timeUnitDesc,
            onClick = { onExtend(5) }
        )

        // 延长10单位按钮
        ExtendedButton(
            units = 10,
            timeUnitDesc = timeUnitDesc,
            onClick = { onExtend(10) }
        )
    }
}

// 单个延长按钮
@Composable
private fun ExtendedButton(
    units: Int,
    timeUnitDesc: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Text(stringResource(R.string.extend_time_with_unit, units, timeUnitDesc))
    }
}

// 操作按钮组
@Composable
private fun ActionButtons(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 取消按钮
        Button(
            onClick = onCancel,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.cancel))
        }

        // 确认按钮
        Button(
            onClick = onConfirm,
            modifier = Modifier.weight(1f)
        ) {
            Text(stringResource(R.string.confirm))
        }
    }
}

// 预览组件
@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FloatingWindowPreview() {
    AppTheme {
        FloatingWindow(
            appName = "抖音",
            timeUnitDesc = "分钟",
            onConfirm = {},
            onCancel = {},
            onExtend = {}
        )
    }
}
