package com.huojieren.apppause.activities

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.huojieren.apppause.adapters.AppListAdapter
import com.huojieren.apppause.databinding.ActivityAppSelectionBinding

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var appListAdapter: AppListAdapter
    private val appList = mutableListOf<AppInfo>() // 应用列表

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 RecyclerView
        appListAdapter = AppListAdapter(appList) { packageName ->
            returnSelectedApp(packageName)
        }
        binding.appListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.appListRecyclerView.adapter = appListAdapter

        // 加载应用列表
        loadInstalledApps()
    }

    // 加载已安装的应用列表
    private fun loadInstalledApps() {
        val packageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        for (app in installedApps) {
            if (app.flags and ApplicationInfo.FLAG_SYSTEM == 0) { // 过滤系统应用
                val appName = packageManager.getApplicationLabel(app).toString()
                appList.add(AppInfo(appName, app.packageName))
            }
        }
        appListAdapter.notifyDataSetChanged()
    }

    // 返回选中的应用
    private fun returnSelectedApp(packageName: String) {
        val intent = Intent()
        intent.putExtra("packageName", packageName)
        setResult(RESULT_OK, intent)
        finish()
    }

    // 应用信息数据类
    data class AppInfo(val name: String, val packageName: String)
}