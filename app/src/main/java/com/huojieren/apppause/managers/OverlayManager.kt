package com.huojieren.apppause.managers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import com.huojieren.apppause.BuildConfig
import com.huojieren.apppause.R
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
    private val timeDesc = BuildConfig.TIME_DESC // 时间单位描述（分钟/秒）
    private val tag = "OverlayManager"
    // endregion

    fun showFloatingWindow(
        onDisMiss: () -> Unit,
        onTimeSelected: (Int) -> Unit,
        onExtendTime: (Int) -> Unit,
        appName: String
    ) {
        // region 窗口参数配置
        // 初始化视图
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams")
        val floatingView = inflater.inflate(R.layout.floating_window, null)
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, // 不获取焦点防止影响底层应用
            android.graphics.PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
            x = 0
            y = 0
        }
        // 添加视图到窗口
        windowManager.addView(floatingView, layoutParams)
        // endregion

        // region 初始化控件
        val timePicker = floatingView.findViewById<NumberPicker>(R.id.timePicker).apply {
            minValue = 1   // 最小选择时间单位
            maxValue = 60  // 最大选择时间单位
            value = 1      // 默认选中值
        }

        val confirmButton = floatingView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = floatingView.findViewById<Button>(R.id.cancelButton)
        val extend5UnitsButton = floatingView.findViewById<Button>(R.id.extend5UnitsButton)
        val extend10UnitsButton = floatingView.findViewById<Button>(R.id.extend10UnitsButton)
        val appNameTextView = floatingView.findViewById<TextView>(R.id.appNameTextView)
        // endregion

        // region 配置按钮文本
        extend5UnitsButton.text = context.getString(
            R.string.extend_time_with_unit,
            5,
            timeDesc
        )
        extend10UnitsButton.text = context.getString(
            R.string.extend_time_with_unit,
            10,
            timeDesc
        )
        // endregion

        // region 按钮事件处理
        confirmButton.setOnClickListener {
            onTimeSelected(timePicker.value)
            removeViewSafely(floatingView)
        }

        cancelButton.setOnClickListener {
            removeViewSafely(floatingView)
            onDisMiss()
        }

        extend5UnitsButton.setOnClickListener {
            onExtendTime(5)
            removeViewSafely(floatingView)
        }

        extend10UnitsButton.setOnClickListener {
            onExtendTime(10)
            removeViewSafely(floatingView)
        }

        appNameTextView.text = context.getString(
            R.string.set_time_for_appName,
            appName
        )
        // endregion
    }

    fun showTimeoutOverlay(appName: String) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        @SuppressLint("InflateParams")
        val overlayView = inflater.inflate(R.layout.timeout_overlay, null)

        // 全屏覆盖层参数
        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, layoutParams)

        overlayView.findViewById<TextView>(R.id.appTimeOutTextView).text = context.getString(
            R.string.app_time_out,
            appName
        )

        // 关闭按钮处理
        overlayView.findViewById<Button>(R.id.closeButton).setOnClickListener {
            // 返回桌面操作
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(this)
            }
            LogUtil(context).log(tag, "[STATE] 触发返回桌面操作")
            removeViewSafely(overlayView)
        }
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