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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.NotificationManager
import com.huojieren.apppause.managers.OverlayManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.ui.components.MonitorControlButton
import com.huojieren.apppause.ui.components.MyFilledTonalButton
import com.huojieren.apppause.ui.state.MonitorState
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.utils.LogUtil
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : AppCompatActivity() {

    private lateinit var permissionManager: PermissionManager
    private lateinit var appMonitor: AppMonitor
    private lateinit var overlayManager: OverlayManager
    private lateinit var notificationManager: NotificationManager
    private val tag = "MainActivity"
    private val monitorState by lazy {
        MonitorState(AppMonitor.getInstance(this), PermissionManager(this))
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(this)))

        // 初始化管理器
        permissionManager = PermissionManager(this)
        appMonitor = AppMonitor.getInstance(this)
        overlayManager = OverlayManager(this)
        notificationManager = NotificationManager(this)

        setContent {
            AppTheme {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 悬浮窗权限按钮
                    MyFilledTonalButton(
                        text = if (permissionManager.checkOverlayPermission()) "悬浮窗权限已授予" else "请求悬浮窗权限",
                        onClick = { requestPermission("overlay") },
                        enabled = !permissionManager.checkOverlayPermission()
                    )
                    // 通知权限按钮
                    MyFilledTonalButton(
                        text = if (permissionManager.checkNotificationPermission()) "通知权限已授予" else "请求通知权限",
                        onClick = { requestPermission("notification") },
                        enabled = !permissionManager.checkNotificationPermission()
                    )
                    // 使用情况权限按钮
                    MyFilledTonalButton(
                        text = if (permissionManager.checkUsageStatsPermission()) "使用情况权限已授予" else "请求使用情况权限",
                        onClick = { requestPermission("usageStats") },
                        enabled = !permissionManager.checkUsageStatsPermission()
                    )
                    // 监控应用列表按钮
                    MyFilledTonalButton(
                        text = "监控应用列表",
                        onClick = {
                            val intent =
                                Intent(this@MainActivity, MonitoredAppsActivity::class.java)
                            startActivity(intent)
                        },
                        enabled = true
                    )
                    // 日志按钮
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    // 开始监控按钮
                    MonitorControlButton(
                        state = monitorState,
                        onStartMonitor = {
                            appMonitor.startMonitoring()
                            showToast(this@MainActivity, "监控已开始")
                        },
                        onStopMonitor = {
                            appMonitor.stopMonitoring()
                            showToast(this@MainActivity, "监控已停止")
                        }
                    )
                    Text(
                        text = getString(R.string.version_text, BuildConfig.VERSION_NAME)
                    )
                }
            }
        }
    }

    // Android 12 及以下不需要申请 POST_NOTIFICATIONS 权限
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission(requestCode: String) {
        when (requestCode) {
            "overlay" -> {
                permissionManager.requestOverlayPermission(this, 1001)
            }

            "notification" -> {
                permissionManager.requestNotificationPermission(this, 1002)
            }

            "usageStats" -> {
                permissionManager.requestUsageStatsPermission(this)
            }
        }
    }
}