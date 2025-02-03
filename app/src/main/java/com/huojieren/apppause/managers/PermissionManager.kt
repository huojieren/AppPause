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

class PermissionManager(private val context: Context) {

    // 检查无障碍权限是否已授权
    fun checkAccessibilityPermission(): Boolean {
        val accessibilityManager =
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val serviceName = context.packageName + "/.managers.AppPauseAccessibilityService"

        for (service in enabledServices) {
            if (service.id == serviceName) {
                return true
            }
        }
        return false
    }

    // 请求无障碍权限
    fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        context.startActivity(intent)
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
    }
}