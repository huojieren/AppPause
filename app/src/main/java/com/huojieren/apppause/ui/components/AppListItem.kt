package com.huojieren.apppause.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme

private val ICON_SIZE = 40.dp
private val ICON_CORNER_RADIUS = 8.dp

@Composable
fun AppListItem(
    modifier: Modifier = Modifier,
    appInfoUi: AppInfoUi,
    isMonitored: Boolean = false,
    onToggle: ((AppInfoUi) -> Unit),
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
                modifier = Modifier
                    .size(ICON_SIZE)
                    .clip(RoundedCornerShape(ICON_CORNER_RADIUS))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = appInfoUi.name,
                modifier = Modifier
                    .weight(1f),
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(
                onClick = { onToggle(appInfoUi) }
            ) {
                Icon(
                    imageVector = if (isMonitored) Icons.Default.Delete else Icons.Default.Add,
                    contentDescription = if (isMonitored) "删除应用" else "添加应用",
                    tint = if (isMonitored) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@LightComponentPreview
@Composable
fun AppListItemNotMonitoredPreview() {
    val mockApp = AppInfoUi(
        name = "App Name",
        packageName = "com.example.app",
        icon = painterResource(id = R.drawable.ic_launcher_foreground)
    )
    AppTheme {
        AppListItem(
            appInfoUi = mockApp,
            isMonitored = false,
            onToggle = {}
        )
    }
}

@LightComponentPreview
@Composable
fun AppListItemMonitoredPreview() {
    val mockApp = AppInfoUi(
        name = "App Name",
        packageName = "com.example.app",
        icon = painterResource(id = R.drawable.ic_launcher_foreground)
    )
    AppTheme {
        AppListItem(
            appInfoUi = mockApp,
            isMonitored = true,
            onToggle = {}
        )
    }
}