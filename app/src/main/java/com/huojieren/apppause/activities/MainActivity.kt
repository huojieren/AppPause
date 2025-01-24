package com.huojieren.apppause.activities

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.huojieren.apppause.databinding.ActivityMainBinding
import com.huojieren.apppause.managers.AppMonitor
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化管理器
        permissionManager = PermissionManager(this)
        appMonitor = AppMonitor(this)
        overlayManager = OverlayManager(this)
        notificationManager = NotificationManager(this)

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    showToast(this, "通知权限已授予")
                } else {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_CODE_NOTIFICATION
                    )
                }
            } else {
                showToast(this, "通知权限已自动授予（Android 12 及以下）")
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
                startMonitoring()
                binding.startMonitoringButton.text = "停止监控"
                showToast(this, "监控已开始")
                isMonitoring = true
            } else {
                stopMonitoring()
                binding.startMonitoringButton.text = "开始监控"
                showToast(this, "监控已停止")
                isMonitoring = false
            }
        }
    }

    private fun startMonitoring() {
        // 检查权限
        if (!permissionManager.checkUsageStatsPermission()) {
            permissionManager.requestUsageStatsPermission(this)
            return
        }

        // 启动监控
        appMonitor.startMonitoring { packageName ->
            val remainingTime = appMonitor.getRemainingTime(packageName)
            if (remainingTime > 0) {
                notificationManager.showNotification("应用正在使用", remainingTime)
                overlayManager.showFloatingWindow(remainingTime) { selectedTime ->
                    appMonitor.setRemainingTime(packageName, selectedTime)
                    startTimer(selectedTime, packageName) // 启动计时器
                }
            } else {
                overlayManager.showFloatingWindow(0) { selectedTime ->
                    appMonitor.setRemainingTime(packageName, selectedTime)
                    startTimer(selectedTime, packageName) // 启动计时器
                }
            }
        }
    }


    private fun stopMonitoring() {
        appMonitor.stopMonitoring()
    }

    private fun startTimer(selectedTime: Int, packageName: String) {
        // 创建一个 Handler 用于延迟执行
        val handler = Handler(Looper.getMainLooper())
        var remainingTime = selectedTime // 剩余时间

        // 每秒输出剩余时间
        val logRunnable = object : Runnable {
            override fun run() {
                logDebug("剩余时间: " + remainingTime + "分钟")
                if (remainingTime > 0) {
                    remainingTime--
                    handler.postDelayed(this, 60000) // 每分钟执行一次
                }
            }
        }

        // 启动日志输出任务
        handler.post(logRunnable)

        // 倒计时结束后强制关闭应用
        val timerRunnable = Runnable {
            forceCloseApp(packageName)
            overlayManager.showTimeoutOverlay {
                // 用户点击关闭按钮后的逻辑
                showToast(this@MainActivity, "应用已关闭")
            }
        }

        // 将选定的时间转换为毫秒（秒为单位）
        val delayMillis = selectedTime * 60 * 1000L

        // 延迟执行倒计时结束任务
        handler.postDelayed(timerRunnable, delayMillis)

        // 显示计时器启动的提示
        showToast(this, "计时器已启动：$selectedTime 分钟")
    }

    private fun forceCloseApp(packageName: String) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent) // 返回桌面

        // 强制退出目标应用
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.killBackgroundProcesses(packageName)
        showToast(this, "已强制退出应用：$packageName")
    }

    companion object {
        private const val REQUEST_CODE_OVERLAY = 1001
        private const val REQUEST_CODE_NOTIFICATION = 1002
    }
}