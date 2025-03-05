package com.huojieren.apppause.managers

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import com.huojieren.apppause.utils.LogUtil

class PermissionManager(private val context: Context) {

    private val tag = "PermissionManager"

    // 检查悬浮窗权限是否已授权
    fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    // 请求悬浮窗权限
    fun requestOverlayPermission(activity: AppCompatActivity, requestCode: Int) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
        activity.startActivityForResult(intent, requestCode)
        waitForPermissionAndReturn(activity) { checkOverlayPermission() }
    }

    // 检查通知权限是否已授权
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    // Android 12 及以下不需要申请 POST_NOTIFICATIONS 权限
    fun checkNotificationPermission(): Boolean {
        return checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    // 请求通知权限
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotificationPermission(activity: AppCompatActivity, requestCode: Int) {
        activity.requestPermissions(
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            requestCode
        )
    }

    // 检查后台服务权限是否已授权
    fun checkUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // 请求后台服务权限
    fun requestUsageStatsPermission(activity: AppCompatActivity) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        activity.startActivity(intent)
        waitForPermissionAndReturn(activity) { checkUsageStatsPermission() }
    }

    // 启动后台检查，当获取权限时自动返回应用
    private fun waitForPermissionAndReturn(
        activity: AppCompatActivity,
        permissionCheck: () -> Boolean
    ) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val maxAttempts = 600 // 最大检查次数（100ms × 600 = 1分钟）
        var currentAttempt = 0

        val checkRunnable = object : Runnable {
            override fun run() {
                currentAttempt++
                if (permissionCheck()) {
                    LogUtil(context).log(tag, "[DEBUG] 检测到获取权限，返回应用")
                    val intent =
                        activity.packageManager.getLaunchIntentForPackage(activity.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    activity.startActivity(intent)
                } else {
                    if (currentAttempt < maxAttempts) {
                        handler.postDelayed(this, 100)
                    } else {
                        LogUtil(context).log(tag, "[DEBUG] 权限检测超时，需手动跳转")
                    }
                }
            }
        }
        handler.post(checkRunnable)
    }
}