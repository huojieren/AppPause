package com.huojieren.apppause.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.activities.MonitoredAppsActivity
import com.huojieren.apppause.ui.components.MonitorControlButton
import com.huojieren.apppause.ui.components.MyFilledTonalButton
import com.huojieren.apppause.ui.components.PermissionButton
import com.huojieren.apppause.ui.state.AppState
import com.huojieren.apppause.utils.LogUtil

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context, appState: AppState, tag: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Pause",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 权限组
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "权限管理",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PermissionButton(
                    type = "overlay",
                    label = "悬浮窗权限",
                    appState = appState
                )

                PermissionButton(
                    type = "notification",
                    label = "通知权限",
                    appState = appState
                )

                PermissionButton(
                    type = "usageStats",
                    label = "使用情况权限",
                    appState = appState
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "应用管理",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MyFilledTonalButton(
                    text = "应用监控列表",
                    onClick = {
                        val intent =
                            Intent(context, MonitoredAppsActivity::class.java)
                        context.startActivity(intent)
                    },
                    enabled = true
                )
            }

            // 日志操作
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "日志管理",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MyFilledTonalButton(
                        text = "清空日志",
                        onClick = {
                            LogUtil(context).log(tag, "[DEBUG] 清除日志")
                            LogUtil(context).clearLog()
                        },
                        enabled = true,
                        modifier = Modifier.weight(1f)
                    )
                    MyFilledTonalButton(
                        text = "保存日志",
                        onClick = {
                            LogUtil(context).log(tag, "[DEBUG] 保存日志")
                            LogUtil(context).saveLog()
                        },
                        enabled = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 监控控制
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "监控状态",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                MonitorControlButton(appState = appState)
            }

            // 版本信息
            Text(
                text = context.getString(R.string.version_text, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}