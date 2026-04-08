package com.huojieren.apppause

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import com.huojieren.apppause.ui.AppPauseApp
import com.huojieren.apppause.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        
        setContent {
            val isDarkTheme = isSystemInDarkTheme()

            SideEffect {
                window.insetsController?.setSystemBarsAppearance(
                    if (isDarkTheme) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                )
            }
            
            AppTheme {
                AppPauseApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getSharedPreferences("app_status", MODE_PRIVATE)
            .edit().putBoolean("normal_exit", true).apply()
    }
}