package com.huojieren.apppause.managers

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.huojieren.apppause.ui.FloatingWindowLifecycleOwner

class OverlayManager(
    private val context: Context,
) {
    private val tag = "OverlayManager"
    private var lifecycleOwner: FloatingWindowLifecycleOwner? = null
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null

    fun showOverlay(content: @Composable () -> Unit) {
        if (composeView != null) return

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            TYPE_APPLICATION_OVERLAY,
            FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // 创建并设置生命周期
        lifecycleOwner = FloatingWindowLifecycleOwner().also {
            it.initialize()
        }

        composeView = ComposeView(context).apply {
            setContent { content() }
        }

        // 设置 LifecycleOwner 到 ComposeView
        composeView?.let { view ->
            lifecycleOwner?.attachToComposeView(view)
        }
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(composeView, layoutParams)
    }

    fun removeOverlay() {
        composeView?.let {
            windowManager?.removeView(it)
            composeView = null
        }

        // 清理生命周期
        lifecycleOwner?.destroy()
        lifecycleOwner = null
    }

    /**
     * 显示时间选择悬浮窗
     */
    /*fun showFloatingWindow(
        onDisMiss: () -> Unit,
        onTimeSelected: (Int) -> Unit,
        onExtendTime: (Int) -> Unit,
    ) {
        // 如果已有悬浮窗显示，先移除
        hideCurrentOverlay()

        val composeView = ComposeView(context).apply {
            setContent {
                AppTheme {
                    TimerScreen(
                        // FIXME: 2025/10/18 21:13 viewModel没有默认构造方法，导致preview渲染错误
                        onExtend5Clicked = {
                            val currentTime = 5 * 60 // 5分钟
                            onExtendTime(currentTime)
                            hideCurrentOverlay()
                        },
                        onExtend10Clicked = {
                            val currentTime = 10 * 60 // 10分钟
                            onExtendTime(currentTime)
                            hideCurrentOverlay()
                        },
                        onCancelButtonClicked = {
                            onDisMiss()
                            hideCurrentOverlay()
                        },
                        onConfirmButtonClicked = { time ->
                            onTimeSelected(time)
                            hideCurrentOverlay()
                        }
                    )
                }
            }
        }

        currentComposeView = composeView

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            TYPE_APPLICATION_OVERLAY,
            FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        lifecycleOwner = FloatingWindowLifecycleOwner().also {
            it.initialize()
            it.attachToComposeView(composeView)
        }

        try {
            windowManager.addView(composeView, layoutParams)
        } catch (e: Exception) {
            LogRepository(context).log(tag, "[ERROR] 添加悬浮窗失败: ${e.message}")
        }
    }*/

    /**
     * 显示超时覆盖层
     */
    /* fun showTimeoutOverlay(
         appInfo: AppInfo
     ) {
         // 如果已有悬浮窗显示，先移除
         hideCurrentOverlay()

         // 创建 ComposeView 容器
         val composeView = ComposeView(context).apply {
             setContent {
                 AppTheme {
                     TimeOutScreen(
                         appInfo = appInfo,
                         onReturnToHomeScreenClicked = {
                             hideCurrentOverlay()
                         }
                     )
                 }
             }
         }

         currentComposeView = composeView

         // 窗口参数配置
         val layoutParams = WindowManager.LayoutParams(
             WindowManager.LayoutParams.MATCH_PARENT,
             WindowManager.LayoutParams.MATCH_PARENT,
             TYPE_APPLICATION_OVERLAY,
             FLAG_LAYOUT_NO_LIMITS,
             PixelFormat.TRANSLUCENT
         )

        lifecycleOwner = FloatingWindowLifecycleOwner().also {
            it.attachToComposeView(composeView)
             it.onCreate()
         }

         // 添加视图到窗口
         try {
             windowManager.addView(composeView, layoutParams)
         } catch (e: Exception) {
             LogRepository(context).log(tag, "[ERROR] 添加超时覆盖层失败: ${e.message}")
         }
     }*/

    /**
     * 隐藏当前显示的悬浮窗
     */
    /* fun hideCurrentOverlay() {
         currentComposeView?.let { view ->
             try {
                 if (view.isAttachedToWindow) {
                     windowManager.removeView(view)
                 }
             } catch (e: Exception) {
                 LogRepository(context).log(tag, "[ERROR] 移除悬浮窗失败: ${e.message}")
             }
         }

        lifecycleOwner?.destroy()
        lifecycleOwner = null
         currentComposeView = null
     }*/

    /**
     * 安全移除视图，避免重复移除导致的异常
     */
    /*    @SuppressLint("NewApi")
        private fun removeViewSafely(view: View) {
            try {
                if (view.isAttachedToWindow) {
                    windowManager.removeView(view)
                }
            } catch (e: Exception) {
                LogRepository(context).log(tag, "[ERROR] 移除悬浮窗失败: ${e.message}")
            }
        }*/
}