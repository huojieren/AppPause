package com.huojieren.apppause.managers

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import com.huojieren.apppause.R
import com.huojieren.apppause.utils.LogUtil.Companion.logDebug

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun showFloatingWindow(
        remainingTime: Int,
        onTimeSelected: (Int) -> Unit,
        onExtendTime: (Int) -> Unit
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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
        val extend5MinutesButton = floatingView.findViewById<Button>(R.id.extend5MinutesButton)
        val extend10MinutesButton = floatingView.findViewById<Button>(R.id.extend10MinutesButton)

        timePicker.minValue = 1
        timePicker.maxValue = 60
        timePicker.value = remainingTime

        remainingTimeTextView.text = "剩余时间: $remainingTime 分钟"

        confirmButton.setOnClickListener {
            val selectedTime = timePicker.value
            onTimeSelected(selectedTime)
            windowManager.removeView(floatingView)
        }

        extend5MinutesButton.setOnClickListener {
            onExtendTime(5) // 延长 5 分钟
            windowManager.removeView(floatingView)
        }

        extend10MinutesButton.setOnClickListener {
            onExtendTime(10) // 延长 10 分钟
            windowManager.removeView(floatingView)
        }
    }

    fun showTimeoutOverlay() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
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