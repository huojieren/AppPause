package com.huojieren.apppause.activities

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
import com.huojieren.apppause.databinding.ActivityMainBinding
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.NotificationManager
import com.huojieren.apppause.managers.OverlayManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.ui.components.PermissionButtons
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.utils.LogUtil
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    private lateinit var appMonitor: AppMonitor
    private lateinit var overlayManager: OverlayManager
    private lateinit var notificationManager: NotificationManager
    private val tag = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(this)))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化管理器
        permissionManager = PermissionManager(this)
        appMonitor = AppMonitor.getInstance(this)
        overlayManager = OverlayManager(this)
        notificationManager = NotificationManager(this)

        // 初始化 ComposeView
        binding.composeContainer.apply {
            setContent {
                AppTheme {
                    PermissionButtons(
                        overlayPermissionGranted = permissionManager.checkOverlayPermission(),
                        notificationPermissionGranted = permissionManager.checkNotificationPermission(),
                        usageStatsPermissionGranted = permissionManager.checkUsageStatsPermission(),
                        onRequestOverlay = { requestPermission("overlay") },
                        onRequestNotification = { requestPermission("notification") },
                        onRequestUsageStats = { requestPermission("usageStats") },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }

        // 显示版本号
        binding.versionTextView.text = getString(R.string.version_text, BuildConfig.VERSION_NAME)

        binding.clearLogButton.setOnClickListener {
            LogUtil(this).log(tag, "[DEBUG] 清除日志")
            LogUtil(this).clearLog()
        }

        binding.saveLogButton.setOnClickListener {
            LogUtil(this).log(tag, "[DEBUG] 保存日志")
            LogUtil(this).saveLog()
        }

        // 通过使用情况访问权限开始监控
        binding.startWithUsageStatsManagerButton.setOnClickListener {
            if (binding.startWithUsageStatsManagerButton.isClickable) {
                // 判断是否正在监控
                if (!appMonitor.isMonitoring) {
                    // 检查权限是否全部获取
                    if (!permissionManager.checkOverlayPermission()
                        || !permissionManager.checkNotificationPermission()
                        || !permissionManager.checkUsageStatsPermission()
                    ) {
                        LogUtil(this).log(tag, "[DEBUG] 检查权限失败")
                        showToast(this, "请授予相关权限后再试")
                    } else {
                        // 检查监控应用是否为空
                        if (appMonitor.isEmptyMonitoredApps()) {
                            LogUtil(this).log(tag, "[DEBUG] 检查应用失败")
                            showToast(this, "没有应用被监控，请先添加应用")
                        } else {
                            binding.startWithUsageStatsManagerButton.text =
                                getString(R.string.stop_monitor)
                            appMonitor.startMonitoring()
                            LogUtil(this).log(tag, "[STATE] 开始监控")
                            showToast(this, "监控已开始")
                        }
                    }
                } else {
                    binding.startWithUsageStatsManagerButton.text =
                        getString(R.string.start_monitor)
                    appMonitor.stopMonitoring()
                    LogUtil(this).log(tag, "[STATE] 停止监控")
                    showToast(this, "监控已停止")
                }
            }
        }
    }

    // 保持原有权限请求方法不变
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