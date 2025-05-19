package com.huojieren.apppause.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.activities.MonitoredAppsActivity
import com.huojieren.apppause.ui.components.MonitorControlButton
import com.huojieren.apppause.ui.components.MyFilledTonalButton
import com.huojieren.apppause.ui.components.PermissionButton
import com.huojieren.apppause.ui.viewmodel.MainViewModel
import com.huojieren.apppause.utils.LogUtil


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(context: Context, viewModel: MainViewModel) {
    val state by viewModel.appState.collectAsStateWithLifecycle()
    val logTag = "MainScreen"
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "App Pause",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 权限管理板块
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "权限管理",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    )
                    PermissionButton(
                        label = "悬浮窗权限",
                        hasPermission = state.hasOverlay,
                        onRequest = {
                            viewModel.refreshAll()
                        }
                    )
                    PermissionButton(
                        label = "通知权限",
                        hasPermission = state.hasNotification,
                        onRequest = {
                            viewModel.refreshAll()
                        }
                    )
                    PermissionButton(
                        label = "使用情况权限",
                        hasPermission = state.hasUsageStats,
                        onRequest = {
                            viewModel.refreshAll()
                        }
                    )
                }
            }

            // 应用管理板块
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
            }

            // 日志管理板块
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            text = "清空缓存日志",
                            onClick = {
                                LogUtil(context).log(logTag, "[DEBUG] 清除日志")
                                LogUtil(context).clearLog()
                            },
                            enabled = true,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        )
                        MyFilledTonalButton(
                            text = "保存缓存日志",
                            onClick = {
                                LogUtil(context).log(logTag, "[DEBUG] 保存日志")
                                LogUtil(context).saveLog()
                            },
                            enabled = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 监控状态板块
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "监控状态",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    MonitorControlButton(
                        state = state,
                        onToggle = { viewModel.toggleMonitoring() }
                    )
                }
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