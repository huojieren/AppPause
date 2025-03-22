package com.huojieren.apppause.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.edit
import com.huojieren.apppause.ui.screens.MonitoredAppsScreen
import com.huojieren.apppause.ui.theme.AppTheme

class MonitoredAppsActivity : ComponentActivity() {
    private val monitoredApps = mutableStateListOf<String>() // 被监控的应用列表

    companion object {
        private const val REQUEST_CODE_ADD_APP = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始加载数据
        loadInitialData()

        setContent {
            AppTheme {
                MonitoredAppsScreen(
                    monitoredApps = monitoredApps,
                    toAppSelectionClick = ::navigateToAppSelection,
                    onRemoveAppClick = ::removeMonitoredApp
                )
            }
        }
    }

    // 初始化加载数据
    private fun loadInitialData() {
        val sharedPrefs = getSharedPreferences("AppPause", MODE_PRIVATE)
        val apps = sharedPrefs.getStringSet("monitoredApps", emptySet()) ?: emptySet()
        monitoredApps.clear()
        monitoredApps.addAll(apps)
    }

    // 导航到应用选择页面
    private fun navigateToAppSelection() {
        startActivityForResult(
            Intent(this, AppSelectionActivity::class.java),
            REQUEST_CODE_ADD_APP
        )
    }

    // 处理从应用选择页面返回的结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_APP && resultCode == RESULT_OK) {
            data?.getStringExtra("packageName")?.let(::addMonitoredApp)
        }
    }

    // 添加被监控的应用
    private fun addMonitoredApp(packageName: String) {
        if (monitoredApps.contains(packageName)) return
        monitoredApps.add(packageName)
        saveMonitoredApps()
    }

    // 删除被监控的应用
    private fun removeMonitoredApp(packageName: String) {
        if (monitoredApps.remove(packageName)) {
            saveMonitoredApps()
        }
    }

    // 保存被监控的应用列表
    private fun saveMonitoredApps() {
        getSharedPreferences("AppPause", MODE_PRIVATE).edit {
            putStringSet("monitoredApps", monitoredApps.toSet())
        }
    }
}