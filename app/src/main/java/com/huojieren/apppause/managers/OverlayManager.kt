package com.huojieren.apppause.managers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import androidx.compose.ui.platform.ComposeView
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.ui.components.FloatingWindow
import com.huojieren.apppause.ui.components.TimeoutOverlay
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.utils.LogUtil

/**
 * 悬浮窗管理类，负责：
 * 1. 显示时间选择悬浮窗
 * 2. 显示超时锁定覆盖层
 * 3. 管理悬浮窗生命周期
 */
class OverlayManager(private val context: Context) {
    // region 成员变量
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val tag = "OverlayManager"
    private var lifecycleOwner: MyComposeViewLifecycleOwner? = null
    // endregion

    fun showFloatingWindow(
        onDisMiss: () -> Unit,
        onTimeSelected: (Int) -> Unit,
        onExtendTime: (Int) -> Unit,
        appName: String
    ) {
        val composeView = ComposeView(context).apply {
            setContent {
                AppTheme {
                    FloatingWindow(
                        appName = appName,
                        timeUnitDesc = BuildConfig.TIME_DESC,
                        onConfirm = { time ->
                            onTimeSelected(time)
                            removeViewSafely(this)
                        },
                        onCancel = {
                            onDisMiss()
                            removeViewSafely(this)
                        },
                        onExtend = { units ->
                            onExtendTime(units)
                            removeViewSafely(this)
                        }
                    )
                }
            }
        }

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        lifecycleOwner = MyComposeViewLifecycleOwner().also {
            it.attachToDecorView(composeView)
            it.onCreate()
        }

        windowManager.addView(composeView, layoutParams)
    }

    fun showTimeoutOverlay(appName: String) {
        // 创建 ComposeView 容器
        val composeView = ComposeView(context).apply {
            setContent {
                AppTheme { // 使用项目主题
                    TimeoutOverlay(
                        appName = appName,
                        onCloseRequest = {
                            // 返回桌面操作
                            Intent(Intent.ACTION_MAIN).apply {
                                addCategory(Intent.CATEGORY_HOME)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(this)
                            }
                            // TODO: 修改removeViewSafely函数为传递composeView
                            // 移除视图
                            windowManager.removeViewImmediate(this)
                            lifecycleOwner?.onDestroy()
                            lifecycleOwner = null
                        }
                    )
                }
            }
        }

        // 窗口参数配置
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        lifecycleOwner = MyComposeViewLifecycleOwner().also {
            it.attachToDecorView(composeView)
            it.onCreate()
        }

        // 添加视图到窗口
        windowManager.addView(composeView, layoutParams)
    }
    // endregion

    // region 工具方法
    /**
     * 安全移除视图，避免重复移除导致的异常
     */
    @SuppressLint("NewApi")
    private fun removeViewSafely(view: android.view.View) {
        try {
            if (view.isAttachedToWindow) {
                windowManager.removeView(view)
            }
        } catch (e: Exception) {
            LogUtil(context).log(tag, "[ERROR] 移除悬浮窗失败: ${e.message}")
        }
    }
}