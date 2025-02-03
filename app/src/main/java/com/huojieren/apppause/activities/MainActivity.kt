package com.huojieren.apppause.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.huojieren.apppause.databinding.ActivityMainBinding
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.AppPauseAccessibilityService
import com.huojieren.apppause.managers.NotificationManager
import com.huojieren.apppause.managers.OverlayManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    private lateinit var appMonitor: AppMonitor
    private lateinit var overlayManager: OverlayManager
    private lateinit var notificationManager: NotificationManager
    private var isMonitoring: Boolean = false
    private val TAG = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化管理器
        permissionManager = PermissionManager(this)
        appMonitor = AppMonitor(this)
        overlayManager = OverlayManager(this)
        notificationManager = NotificationManager(this)

        val sharedPreferences = getSharedPreferences("AppPause", Context.MODE_PRIVATE)

        // 无障碍权限按钮
        binding.accessibilityPermissionButton.setOnClickListener {
            if (permissionManager.checkAccessibilityPermission()) {
                showToast(this, "无障碍权限已授予")
            } else {
                permissionManager.requestAccessibilityPermission()
            }
        }

        // 悬浮窗权限按钮
        binding.overlayPermissionButton.setOnClickListener {
            if (permissionManager.checkOverlayPermission()) {
                showToast(this, "悬浮窗权限已授予")
            } else {
                permissionManager.requestOverlayPermission(this, REQUEST_CODE_OVERLAY)
            }
        }

        // 通知权限按钮
        binding.notificationPermissionButton.setOnClickListener {
            if (permissionManager.checkNotificationPermission()) {
                checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                showToast(this, "通知权限已授予")
            } else {
                permissionManager.requestNotificationPermission(this, REQUEST_CODE_NOTIFICATION)
            }
        }

        // 使用情况访问权限按钮
        binding.usageStatsPermissionButton.setOnClickListener {
            if (permissionManager.checkUsageStatsPermission()) {
                showToast(this, "使用情况访问权限已授予")
            } else {
                permissionManager.requestUsageStatsPermission(this)
            }
        }

        // 监控应用列表按钮
        binding.monitoredAppsButton.setOnClickListener {
            val intent = Intent(this, MonitoredAppsActivity::class.java)
            startActivity(intent)
        }

        // 开始/停止监控按钮
        binding.startMonitoringButton.setOnClickListener {
            // 判断是否正在监控
            if (!isMonitoring) {
                // 检查监控应用是否为空
                if (appMonitor.isEmptyMonitoredApps()) {
                    Log.d(TAG, "onCreate: 监控应用列表为空，开启监控失败")
                    showToast(this, "没有应用被监控，请先添加应用")
                } else {
                    // 检查权限是否全部获取
                    if (!permissionManager.checkOverlayPermission()
                        || !permissionManager.checkNotificationPermission()
                        || !permissionManager.checkUsageStatsPermission()
                    ) {
                        Log.d(TAG, "onCreate: 权限未获取，开启监控失败")
                        showToast(this, "请授予相关权限后再试")
                    } else {
                        binding.startMonitoringButton.text = "停止监控"
                        Log.d(TAG, "onCreate: 监控已开始")
                        showToast(this, "监控已开始")
                        isMonitoring = true
                        sharedPreferences.edit().putBoolean("isMonitoring", isMonitoring).apply()
                    }
                }
            } else {
                binding.startMonitoringButton.text = "开始监控"
                Log.d(TAG, "onCreate: 监控已停止")
                showToast(this, "监控已停止")
                isMonitoring = false
                sharedPreferences.edit().putBoolean("isMonitoring", isMonitoring).apply()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    // Android 12 及以下不需要申请 POST_NOTIFICATIONS 权限
    override fun onResume() {
        super.onResume()

        // 动态更新无障碍权限按钮状态
        if (permissionManager.checkAccessibilityPermission()) {
            binding.accessibilityPermissionButton.text = "无障碍权限已授予"
        } else {
            binding.accessibilityPermissionButton.text = "请求无障碍权限"
        }

        // 动态更新悬浮窗权限按钮状态
        if (permissionManager.checkOverlayPermission()) {
            binding.overlayPermissionButton.text = "悬浮窗权限已授予"
        } else {
            binding.overlayPermissionButton.text = "请求悬浮窗权限"
        }

        // 动态更新通知权限按钮状态
        if (permissionManager.checkNotificationPermission()) {
            binding.notificationPermissionButton.text = "通知权限已授予"
        } else {
            binding.notificationPermissionButton.text = "请求通知权限"
        }

        // 动态更新使用情况访问权限按钮状态
        if (permissionManager.checkUsageStatsPermission()) {
            binding.usageStatsPermissionButton.text = "使用情况访问权限已授予"
        } else {
            binding.usageStatsPermissionButton.text = "请求使用情况访问权限"
        }

        // 启动 AccessibilityService
        AppPauseAccessibilityService.start(this)
    }

    companion object {
        private const val REQUEST_CODE_OVERLAY = 1001
        private const val REQUEST_CODE_NOTIFICATION = 1002
    }
}