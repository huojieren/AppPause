package com.huojieren.apppause.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun AppListItem(
    modifier: Modifier = Modifier,
    appInfoUi: AppInfoUi,
    onAddApp: (() -> Unit)? = null,
    onDeleteApp: (() -> Unit)? = null,
) {
    Card(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = appInfoUi.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = appInfoUi.name,
                modifier = Modifier
                    .weight(1f),
                style = MaterialTheme.typography.titleLarge
            )

            // 根据传入的参数显示删除或添加按钮
            when {
                onDeleteApp != null -> {
                    IconButton(
                        onClick = onDeleteApp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "删除应用",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                onAddApp != null -> {
                    IconButton(
                        onClick = onAddApp
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加应用",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Preview("App List Item")
@Composable
fun AppListItemPreview() {
    val mockApp = AppInfoUi(
        name = "App Name",
        packageName = "com.example.app",
        icon = painterResource(id = R.drawable.ic_launcher_foreground)
    )
    AppTheme {
        AppListItem(
            appInfoUi = mockApp,
        )
    }
}

@Preview("App List Item with Delete")
@Composable
fun AppListItemWithDeletePreview() {
    val mockApp = AppInfoUi(
        name = "App Name",
        packageName = "com.example.app",
        icon = painterResource(id = R.drawable.ic_launcher_foreground)
    )
    AppTheme {
        AppListItem(
            appInfoUi = mockApp,
            onDeleteApp = {}
        )
    }
}

@Preview("App List Item with Add")
@Composable
fun AppListItemWithAddPreview() {
    val mockApp = AppInfoUi(
        name = "App Name",
        packageName = "com.example.app",
        icon = painterResource(id = R.drawable.ic_launcher_foreground)
    )
    AppTheme {
        AppListItem(
            appInfoUi = mockApp,
            onAddApp = {}
        )
    }
}