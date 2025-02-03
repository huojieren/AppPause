package com.huojieren.apppause.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.databinding.ActivityMainBinding
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.AppPauseAccessibilityService
import com.huojieren.apppause.managers.NotificationManager
import com.huojieren.apppause.managers.OverlayManager
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.utils.LogUtil.Companion.logDebug
import com.huojieren.apppause.utils.ToastUtil.Companion.showToast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var permissionManager: PermissionManager
    private lateinit var appMonitor: AppMonitor
    private lateinit var overlayManager: OverlayManager
    private lateinit var notificationManager: NotificationManager
    private var isMonitoring = false
    private val timeUnit = BuildConfig.TIME_UNIT
    private val timeDesc = BuildConfig.TIME_DESC

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
            if (!isMonitoring) {
                // 检查权限
                if (!permissionManager.checkOverlayPermission()
                    || !permissionManager.checkNotificationPermission()
                    || !permissionManager.checkUsageStatsPermission()
                ) {
                    logDebug("权限未获取")
                    showToast(this, "请授予相关权限后再试")
                } else {
                    startMonitoring()
                    binding.startMonitoringButton.text = "停止监控"
                    showToast(this, "监控已开始")
                    isMonitoring = true
                }
            } else {
                stopMonitoring()
                binding.startMonitoringButton.text = "开始监控"
                showToast(this, "监控已停止")
                isMonitoring = false
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

    private fun startMonitoring() { // 启动监控
        appMonitor.startMonitoring { packageName ->
            logDebug("应用正在使用: $packageName")
            val remainingTime = appMonitor.getRemainingTime(packageName)
            if (remainingTime > 0) {
                logDebug("剩余时间: $remainingTime $timeDesc")
                notificationManager.showNotification("应用正在使用", remainingTime)
                overlayManager.showFloatingWindow(remainingTime, { selectedTime ->
                    // 用户选择时间后的回调
                    appMonitor.setRemainingTime(packageName, selectedTime)
                    startTimer(selectedTime) // 启动计时器
                }, { extendTime ->
                    // 用户点击“延长使用时间”按钮后的回调
                    val newRemainingTime = remainingTime + extendTime
                    appMonitor.setRemainingTime(packageName, newRemainingTime)
                    showToast(this, "已延长使用时间: $extendTime $timeDesc")
                })
            } else {
                logDebug("应用无剩余时间")
                overlayManager.showFloatingWindow(0, { selectedTime ->
                    // 用户选择时间后的回调
                    appMonitor.setRemainingTime(packageName, selectedTime)
                    startTimer(selectedTime) // 启动计时器
                    showToast(this, "计时器已启动：$selectedTime $timeDesc")
                }, { extendTime ->
                    // 用户点击“延长使用时间”按钮后的回调
                    val newRemainingTime = extendTime // 如果剩余时间为 0，直接设置为延长时间
                    appMonitor.setRemainingTime(packageName, newRemainingTime)
                    startTimer(newRemainingTime) // 启动计时器
                    showToast(this, "已延长使用时间: $extendTime $timeDesc")
                })
            }
        }
    }

    private fun stopMonitoring() {
        logDebug("停止监控")
        appMonitor.stopMonitoring()
    }

    private fun startTimer(selectedTime: Int) {
        val handler = Handler(Looper.getMainLooper())
        var remainingTime = selectedTime // 剩余时间

        // 每秒/分钟输出剩余时间
        val logRunnable = object : Runnable {
            override fun run() {
                logDebug("剩余时间: $remainingTime $timeDesc")
                if (remainingTime > 0) {
                    remainingTime--
                    handler.postDelayed(this, timeUnit) // 每秒/分钟执行一次
                }
            }
        }

        // 启动日志输出任务
        handler.post(logRunnable)

        // 倒计时结束后返回桌面并显示全屏悬浮窗
        val timerRunnable = Runnable {
            logDebug("倒计时结束")
            overlayManager.showTimeoutOverlay()
        }

        // 将选定的时间转换为毫秒
        val delayMillis = selectedTime * 60 * 1000L

        // 延迟执行倒计时结束任务
        handler.postDelayed(timerRunnable, delayMillis)
    }

    companion object {
        private const val REQUEST_CODE_OVERLAY = 1001
        private const val REQUEST_CODE_NOTIFICATION = 1002
    }
}