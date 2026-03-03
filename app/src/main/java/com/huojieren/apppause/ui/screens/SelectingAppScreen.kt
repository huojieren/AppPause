package com.huojieren.apppause.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.ui.components.AppList
import com.huojieren.apppause.ui.state.SelectAppUiState
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun SelectingAppScreen(
    uiState: SelectAppUiState,
    modifier: Modifier = Modifier,
    onAddAppItem: (AppInfoUi) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "添加应用到监控列表",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
            )
            AppList(
                appList = uiState.allApps,
                onAddApp = { onAddAppItem(it) }
            )
        }
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectingAppScreenPreview() {
    val mockState = SelectAppUiState(
        monitoredApps = listOf(
            AppInfoUi(
                name = "com.example.app1",
                packageName = "App 1",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            ),
            AppInfoUi(
                name = "com.example.app2",
                packageName = "App 2",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            ),
            AppInfoUi(
                name = "com.example.app3",
                packageName = "App 3",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            )
        ),
        allApps = listOf(
            AppInfoUi(
                name = "com.example.app4",
                packageName = "App 4",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            ),
            AppInfoUi(
                name = "com.example.app5",
                packageName = "App 5",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            ),
            AppInfoUi(
                name = "com.example.app6",
                packageName = "App 6",
                icon = painterResource(id = R.drawable.ic_launcher_foreground)
            )
        )
    )
    AppTheme {
        SelectingAppScreen(
            uiState = mockState,
            onAddAppItem = {}
        )
    }
}