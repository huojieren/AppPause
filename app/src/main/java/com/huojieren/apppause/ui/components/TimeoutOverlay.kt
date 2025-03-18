package com.huojieren.apppause.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun TimeoutOverlay(
    appName: String,          // 需要显示的应用名称
    onCloseRequest: () -> Unit // 关闭按钮点击回调
) {
    // 用于层叠元素
    Box(
        modifier = Modifier
            .fillMaxSize()    // 填充整个可用区域
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)) // 80% 透明黑色遮罩层
    ) {
        // 内容区域使用 Column 布局
        Column(
            modifier = Modifier
                .align(Alignment.Center) // 居中显示
                .padding(16.dp),         // 内边距
            horizontalAlignment = Alignment.CenterHorizontally // 水平居中
        ) {
            // 应用名称文本
            Text(
                text = appName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // 标题文本
            Text(
                text = stringResource(R.string.app_time_out),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            // 关闭按钮
            FilledTonalButton(
                onClick = onCloseRequest,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.return_to_home),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

// 预览组件
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TimeoutOverlayPreview() {
    AppTheme {
        TimeoutOverlay(
            appName = "抖音",
            onCloseRequest = {}
        )
    }
}
