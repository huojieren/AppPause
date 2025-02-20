package com.huojieren.apppause.managers

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.checkSelfPermission
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.utils.LogUtil

class PermissionManager(private val context: Context) {

    private val tag = "PermissionManager"

    // 检查无障碍权限是否已授权
    fun checkAccessibilityPermission(): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        // 动态生成 serviceName，确保与 service.id 格式一致
        val serviceName = if (BuildConfig.DEBUG) {
            "${context.packageName}/com.huojieren.apppause.managers.AppPauseAccessibilityService"
        } else {
            "${context.packageName}/.managers.AppPauseAccessibilityService"
        }

        for (service in enabledServices) {
            LogUtil(context).d(tag, "checkAccessibilityPermission: service.id= ${service.id}")
            LogUtil(context).d(tag, "checkAccessibilityPermission: serviceName= $serviceName")
            if (service.id == serviceName) {
                return true
            }
        }
        return false
    }

    // 请求无障碍权限
    fun requestAccessibilityPermission(activity: AppCompatActivity) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
        waitForPermissionAndReturn(activity) { checkAccessibilityPermission() }
    }

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
        val checkRunnable = object : Runnable {
            override fun run() {
                if (permissionCheck()) {
                    LogUtil(context).d(tag, "run: 检测到获取权限，返回应用")
                    val intent =
                        activity.packageManager.getLaunchIntentForPackage(activity.packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    activity.startActivity(intent)
                } else {
                    handler.postDelayed(this, 100) // 100ms 后再次检查
                }
            }
        }
        handler.post(checkRunnable) // 启动定时检查
    }
}