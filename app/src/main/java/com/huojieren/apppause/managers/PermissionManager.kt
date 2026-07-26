package com.huojieren.apppause.managers

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.huojieren.apppause.data.Permissions
import com.huojieren.apppause.data.logging.AppLog.logger
import com.huojieren.apppause.service.AppPauseAccessibilityService

class PermissionManager(
    private val context: Context,
) {
    /**
     * 检查权限是否已授权
     */
    fun refreshPermission(key: Permissions): Boolean {
        val permission = when (key) {
            // 检查悬浮窗权限是否已授权
            Permissions.Overlay -> Settings.canDrawOverlays(context)

            // 检查通知权限是否已授权
            Permissions.Notification -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) ==
                            PackageManager.PERMISSION_GRANTED
                } else true
            }

            // 检查使用统计权限是否已授权
            Permissions.UsageStats -> {
                val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appOps.unsafeCheckOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        context.packageName
                    )
                } else {
                    @Suppress("DEPRECATION")
                    appOps.checkOpNoThrow(
                        AppOpsManager.OPSTR_GET_USAGE_STATS,
                        Process.myUid(),
                        context.packageName
                    )
                }
                mode == AppOpsManager.MODE_ALLOWED
            }

            // 检查无障碍服务权限是否已授权
            Permissions.Accessibility -> AppPauseAccessibilityService.isInitialized()

            // 检查电池优化 exemption 权限是否已授权
            Permissions.BatteryOptimization -> {
                val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        logger("PermissionManager", "refreshPermission: $key, $permission")
        return permission
    }

    /**
     * 请求权限
     */
    fun requestPermission(key: Permissions) {
        when (key) {
            // 请求悬浮窗权限
            Permissions.Overlay -> {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:${context.packageName}".toUri()
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

            // 请求通知权限
            Permissions.Notification -> {
                // 通知权限需要在Activity中请求，这里只提供跳转设置页面的方法
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }

            // 请求使用统计权限
            Permissions.UsageStats -> {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

            // 请求无障碍服务权限
            Permissions.Accessibility -> {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

            Permissions.BatteryOptimization -> {
                val intent1 = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent1)
                val intent2 = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent2)
            }
        }
    }
}
