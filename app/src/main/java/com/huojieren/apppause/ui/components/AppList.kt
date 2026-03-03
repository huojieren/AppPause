package com.huojieren.apppause.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun AppList(
    modifier: Modifier = Modifier,
    appList: List<AppInfoUi>,
    onAddApp: ((AppInfoUi) -> Unit)? = null,
    onDeleteApp: ((AppInfoUi) -> Unit)? = null
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 使用packageName作为key以提高性能
        items(appList, key = { it.packageName }) {
            AppListItem(
                appInfoUi = it,
                onDeleteApp = onDeleteApp?.let { onDelete ->
                    { onDelete(it) }
                },
                onAddApp = onAddApp?.let { onAdd ->
                    { onAdd(it) }
                }
            )
        }
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppListPreview() {
    AppTheme {
        val appList = listOf(
            AppInfoUi(
                name = "App 2",
                packageName = "com.example.app1",
                icon = painterResource(id = R.drawable.ic_notification)
            ),
            AppInfoUi(
                name = "App 1",
                packageName = "com.example.app2",
                icon = painterResource(id = R.drawable.ic_notification)
            )
        )
        AppList(appList = appList)
    }
}

@Preview("Light Theme with Delete")
@Preview("Dark Theme with Delete", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppListWithDeletePreview() {
    AppTheme {
        val appList = listOf(
            AppInfoUi(
                name = "App 1",
                packageName = "com.example.app1",
                icon = painterResource(id = R.drawable.ic_notification)
            ),
            AppInfoUi(
                name = "App 2",
                packageName = "com.example.app2",
                icon = painterResource(id = R.drawable.ic_notification)
            )
        )
        AppList(appList = appList, onDeleteApp = {})
    }
}

@Preview("Light Theme with Add")
@Preview("Dark Theme with Add", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AppListWithAddPreview() {
    AppTheme {
        val appList = listOf(
            AppInfoUi(
                name = "App 1",
                packageName = "com.example.app1",
                icon = painterResource(id = R.drawable.ic_notification)
            ),
            AppInfoUi(
                name = "App 2",
                packageName = "com.example.app2",
                icon = painterResource(id = R.drawable.ic_notification)
            )
        )
        AppList(appList = appList, onAddApp = {})
    }
}
