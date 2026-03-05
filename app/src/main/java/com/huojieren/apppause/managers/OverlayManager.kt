package com.huojieren.apppause.managers

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
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

        val statusBarHeight = getStatusBarHeight()
        val screenHeight = getScreenHeight()

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            screenHeight + statusBarHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
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

    private fun getStatusBarHeight(): Int {
        val resources = context.resources
        return resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android")
        )
    }

    private fun getScreenHeight(): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels
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
}