package com.huojieren.apppause

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.huojieren.apppause.ui.AppPauseApp
import com.huojieren.apppause.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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