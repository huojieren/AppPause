package com.huojieren.apppause.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun SettingsNumberInputRow(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    value: Int,
    onValueChange: (Int) -> Unit,
    suffix: String,
    valueRange: IntRange = 1..99,
) {
    var text by remember { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        if (text.toIntOrNull() != value) {
            text = value.toString()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { input ->
                        if (input.length <= 2 && input.all { it.isDigit() }) {
                            text = input
                            input.toIntOrNull()
                                ?.coerceIn(valueRange)
                                ?.let(onValueChange)
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(28.dp)
                )
                Text(
                    text = suffix,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun SettingsNumberInputRowPreview() {
    AppTheme {
        SettingsNumberInputRow(
            title = "等待时长",
            subtitle = "超时后返回桌面前的等待时间",
            value = 5,
            onValueChange = {},
            suffix = "秒"
        )
    }
}
