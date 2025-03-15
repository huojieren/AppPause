package com.huojieren.apppause.ui.screens

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.promeg.pinyinhelper.Pinyin
import com.huojieren.apppause.models.AppInfo

@Composable
fun AppSelectionScreen(
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf(emptyList<AppInfo>()) }

    LaunchedEffect(Unit) {
        apps = loadInstalledApps(context.packageManager)
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = modifier
                .padding(16.dp)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppListItem(
                    appName = app.name,
                    onClick = { onItemClick(app.packageName) }
                )
            }
        }
    }
}

@Composable
private fun AppListItem(
    appName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = appName,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}

private fun loadInstalledApps(pm: PackageManager): List<AppInfo> {
    return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
        .sortedWith(pinyinComparator(pm))
        .map { AppInfo(pm.getApplicationLabel(it).toString(), it.packageName) }
}

private fun pinyinComparator(pm: PackageManager) = Comparator<ApplicationInfo> { a, b ->
    val nameA = pm.getApplicationLabel(a).toString()
    val nameB = pm.getApplicationLabel(b).toString()
    Pinyin.toPinyin(nameA, "").compareTo(Pinyin.toPinyin(nameB, ""))
}

@Preview
@Composable
fun AppSelectionScreenPreview() {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppListItem(
                appName = "App Pause",
                onClick = {}
            )
            AppListItem(
                appName = "Health Diary",
                onClick = {}
            )
        }
    }
}