package com.huojieren.apppause

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.huojieren.apppause.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private var startTime: Long = 0
    private var selectedTime: Int = 5 // 默认使用时长

    // 用于请求悬浮窗权限的合约
    private val requestOverlayPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Settings.canDrawOverlays(this)) {
                showToast("悬浮窗权限已授予")
            } else {
                showToast("悬浮窗权限未授予")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 Handler
        handler = Handler(Looper.getMainLooper())

        // 加载用户选择的时间
        loadSelectedTime()

        // 初始化 WindowManager
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // 悬浮窗权限按钮
        binding.overlayPermissionButton.setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                showToast("悬浮窗权限已授予")
            } else {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                requestOverlayPermission.launch(intent)
            }
        }

        // 通知权限按钮
        binding.notificationPermissionButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    showToast("通知权限已授予")
                } else {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        REQUEST_CODE_NOTIFICATION
                    )
                }
            } else {
                showToast("通知权限已自动授予（Android 12 及以下）")
            }
        }

        // 使用情况访问权限按钮
        binding.usageStatsPermissionButton.setOnClickListener {
            checkUsageStatsPermission()
        }

        // 显示悬浮窗按钮（测试用）
        binding.showFloatingWindowButton.setOnClickListener {
            showFloatingWindow()
        }

        // 开始监控应用使用时长
        binding.startMonitoringButton.setOnClickListener {
            startMonitoring()
        }
    }

    // 检查使用情况访问权限
    private fun checkUsageStatsPermission() {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        if (mode == AppOpsManager.MODE_ALLOWED) {
            showToast("使用情况访问权限已授予")
        } else {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }
    }

    // 显示 Toast 提示
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_NOTIFICATION = 1001
    }

    // 显示悬浮窗
    private fun showFloatingWindow() {
        if (!Settings.canDrawOverlays(this)) {
            showToast("请先授予悬浮窗权限")
            return
        }

        // 初始化悬浮窗视图
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_window, null)

        // 设置悬浮窗参数
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.CENTER
        layoutParams.x = 0
        layoutParams.y = 0

        // 添加悬浮窗
        windowManager.addView(floatingView, layoutParams)

        // 初始化时间选择器和确认按钮
        val timePicker = floatingView.findViewById<NumberPicker>(R.id.timePicker)
        val confirmButton = floatingView.findViewById<Button>(R.id.confirmButton)

        // 设置时间选择器范围
        timePicker.minValue = 1  // 最小值
        timePicker.maxValue = 60 // 最大值
        timePicker.value = 5     // 默认值

        // 确认按钮点击事件
        confirmButton.setOnClickListener {
            val selectedTime = timePicker.value
            saveSelectedTime(selectedTime)
            showToast("已设置使用时长：$selectedTime 分钟")
            windowManager.removeView(floatingView) // 关闭悬浮窗
        }
    }

    // 加载用户选择的时间
    private fun loadSelectedTime() {
        val sharedPreferences = getSharedPreferences("AppPause", Context.MODE_PRIVATE)
        selectedTime = sharedPreferences.getInt("selectedTime", 5)
    }

    // 保存用户选择的时间
    private fun saveSelectedTime(time: Int) {
        val sharedPreferences = getSharedPreferences("AppPause", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("selectedTime", time)
        editor.apply()
    }

    // 开始监控应用使用时长
    private fun startMonitoring() {
        if (!checkUsageStatsPermissionGranted()) {
            showToast("请先授予使用情况访问权限")
            return
        }

        // 获取当前时间
        startTime = System.currentTimeMillis()

        // 启动计时器
        runnable = object : Runnable {
            override fun run() {
                val usageStatsManager =
                    getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                val endTime = System.currentTimeMillis()
                val usageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    startTime,
                    endTime
                )

                for (usageStat in usageStats) {
                    if (usageStat.packageName == "com.example.targetapp") { // 替换为目标应用的包名
                        val usedTime =
                            TimeUnit.MILLISECONDS.toMinutes(endTime - usageStat.lastTimeUsed)
                        if (usedTime >= selectedTime) {
                            showToast("使用时间已到")
                            handler.removeCallbacks(this)
                            return
                        }
                    }
                }

                // 每隔1秒检查一次
                handler.postDelayed(this, 1000)
            }
        }

        handler.post(runnable)
    }

    // 检查使用情况访问权限是否已授予
    private fun checkUsageStatsPermissionGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

}