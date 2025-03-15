package com.huojieren.apppause.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.huojieren.apppause.managers.AppMonitor
import com.huojieren.apppause.managers.PermissionManager
import com.huojieren.apppause.ui.screens.MainScreen
import com.huojieren.apppause.ui.state.AppState
import com.huojieren.apppause.ui.theme.AppTheme

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class MainActivity : ComponentActivity() {

    private lateinit var appMonitor: AppMonitor
    private lateinit var appState: AppState
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化拼音库
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(this)))

        // 初始化权限管理器
        PermissionManager.init(applicationContext)

        // 初始化其他组件
        appMonitor = AppMonitor.getInstance(this)
        appState = AppState(PermissionManager.get(), appMonitor)

        appMonitor.addStateListener {
            appState.refresh()
        }

        setContent {
            AppTheme {
                MainScreen(this, appState, tag)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 每次回到界面刷新状态
        appState.refresh()
    }
}