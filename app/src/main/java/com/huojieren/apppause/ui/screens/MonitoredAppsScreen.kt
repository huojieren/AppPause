package com.huojieren.apppause.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.ui.components.MyFilledTonalButton


@Composable
fun MonitoredAppsScreen(
    monitoredApps: List<String>,
    toAppSelectionClick: () -> Unit,
    onRemoveAppClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            // 标题
            Text(
                text = stringResource(R.string.monitored_apps),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(16.dp)
            )

            // 添加应用按钮
            MyFilledTonalButton(
                text = stringResource(R.string.add_app),
                onClick = toAppSelectionClick,
                modifier = Modifier.padding(16.dp),
                enabled = true
            )

            // 应用列表
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(monitoredApps, key = { it }) { packageName ->
                    MonitoredAppItem(
                        appName = getAppName(context, packageName),
                        onRemoveClick = { onRemoveAppClick(packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun MonitoredAppItem(
    appName: String,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 应用名称
            Text(
                text = appName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 删除按钮
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun getAppName(context: Context, packageName: String): String {
    return try {
        context.packageManager.getApplicationLabel(
            context.packageManager.getApplicationInfo(packageName, 0)
        ).toString()
    } catch (e: Exception) {
        packageName
    }
}

@Preview
@Composable
fun MonitoredAppsScreenPreview() {
    MonitoredAppsScreen(
        monitoredApps = listOf("app1", "app2"),
        toAppSelectionClick = {},
        onRemoveAppClick = {}
    )
}