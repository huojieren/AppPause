package com.huojieren.apppause.managers

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.NumberPicker
import com.huojieren.apppause.R

class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    fun showFloatingWindow(onTimeSelected: (Int) -> Unit) {
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

        timePicker.minValue = 1
        timePicker.maxValue = 60
        timePicker.value = 5

        confirmButton.setOnClickListener {
            val selectedTime = timePicker.value
            onTimeSelected(selectedTime)
            windowManager.removeView(floatingView)
        }
    }

    fun showTimeoutOverlay(onCloseClicked: () -> Unit) {
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
            onCloseClicked()
            windowManager.removeView(overlayView)
        }
    }
}