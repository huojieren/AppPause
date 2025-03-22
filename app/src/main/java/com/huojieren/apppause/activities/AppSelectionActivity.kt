package com.huojieren.apppause.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.huojieren.apppause.ui.screens.AppSelectionScreen
import com.huojieren.apppause.ui.theme.AppTheme

class AppSelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppSelectionScreen(
                    onItemClick = { packageName ->  // 明确指定参数名称
                        returnSelectedApp(packageName)
                    }
                )
            }
        }
    }

    // 返回选中的应用
    private fun returnSelectedApp(packageName: String) {
        val intent = Intent()
        intent.putExtra("packageName", packageName)
        setResult(RESULT_OK, intent)
        finish()
    }
}