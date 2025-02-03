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
import com.huojieren.apppause.utils.LogUtil.Companion.logDebug

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val timeDesc = BuildConfig.TIME_DESC

    fun showFloatingWindow(
        remainingTime: Int,
        onTimeSelected: (Int) -> Unit,
        onExtendTime: (Int) -> Unit
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        @SuppressLint("InflateParams")// 忽略根视图可能为空警告
        val floatingView = inflater.inflate(R.layout.floating_window, null)

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

        windowManager.addView(floatingView, layoutParams)

        val timePicker = floatingView.findViewById<NumberPicker>(R.id.timePicker)
        val confirmButton = floatingView.findViewById<Button>(R.id.confirmButton)
        val remainingTimeTextView = floatingView.findViewById<TextView>(R.id.remainingTimeTextView)
        val extend5UnitsButton = floatingView.findViewById<Button>(R.id.extend5UnitsButton)
        val extend10UnitsButton = floatingView.findViewById<Button>(R.id.extend10UnitsButton)

        timePicker.minValue = 1
        timePicker.maxValue = 60
        timePicker.value = remainingTime

        remainingTimeTextView.text = buildString {
            append(context.getString(R.string.remaining_time, remainingTime))
            append(" $timeDesc")
        }
        extend5UnitsButton.text = buildString {
            append(context.getString(R.string.extend_time, 5))
            append(" $timeDesc")
        }
        extend10UnitsButton.text = buildString {
            append(context.getString(R.string.extend_time, 10))
            append(" $timeDesc")
        }

        confirmButton.setOnClickListener {
            val selectedTime = timePicker.value
            onTimeSelected(selectedTime)
            windowManager.removeView(floatingView)
        }

        extend5UnitsButton.setOnClickListener {
            onExtendTime(5) // 延长 5 秒/分钟
            windowManager.removeView(floatingView)
        }

        extend10UnitsButton.setOnClickListener {
            onExtendTime(10) // 延长 10 秒/分钟
            windowManager.removeView(floatingView)
        }
    }

    fun showTimeoutOverlay() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        @SuppressLint("InflateParams")// 忽略根视图可能为空警告
        val overlayView = inflater.inflate(R.layout.timeout_overlay, null)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        windowManager.addView(overlayView, layoutParams)

        val closeButton = overlayView.findViewById<Button>(R.id.closeButton)
        closeButton.setOnClickListener {
            // 回到桌面
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            logDebug("回到桌面")

            windowManager.removeView(overlayView)
        }
    }
}