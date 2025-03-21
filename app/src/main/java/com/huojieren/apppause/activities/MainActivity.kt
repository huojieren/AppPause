package com.huojieren.apppause.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
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
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.ui.components.MonitorControlButton
import com.huojieren.apppause.ui.components.MyFilledTonalButton
import com.huojieren.apppause.ui.components.PermissionButton
import com.huojieren.apppause.ui.state.AppState
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.utils.LogUtil

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : AppCompatActivity() {

    private lateinit var appMonitor: AppMonitor
    private lateinit var appState: AppState
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化拼音库
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(this)))

        // 初始化权限管理器
        PermissionManager.init(applicationContext)

        // 初始化其他组件
        appMonitor = AppMonitor.getInstance(this)
        appState = AppState(PermissionManager.get(), appMonitor)

        appMonitor.addStateListener {
            appState.refresh()
        }

        setContent {
            AppTheme {
                MainScreen(appState)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到界面刷新状态
        appState.refresh()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainScreen(appState: AppState) {
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
                                Intent(this@MainActivity, MonitoredAppsActivity::class.java)
                            startActivity(intent)
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
                                LogUtil(this@MainActivity).log(
                                    this@MainActivity.tag,
                                    "[DEBUG] 清除日志"
                                )
                                LogUtil(this@MainActivity).clearLog()
                            },
                            enabled = true,
                            modifier = Modifier.weight(1f)
                        )
                        MyFilledTonalButton(
                            text = "保存日志",
                            onClick = {
                                LogUtil(this@MainActivity).log(
                                    this@MainActivity.tag,
                                    "[DEBUG] 保存日志"
                                )
                                LogUtil(this@MainActivity).saveLog()
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
                    text = getString(R.string.version_text, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}