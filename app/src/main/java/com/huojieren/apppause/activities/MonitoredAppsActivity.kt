package com.huojieren.apppause.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.huojieren.apppause.adapters.MonitoredAppsAdapter
import com.huojieren.apppause.databinding.ActivityMonitoredAppsBinding

class MonitoredAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonitoredAppsBinding
    private lateinit var monitoredAppsAdapter: MonitoredAppsAdapter
    private val monitoredApps = mutableListOf<String>() // 被监控的应用列表

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitoredAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 RecyclerView
        monitoredAppsAdapter = MonitoredAppsAdapter(monitoredApps) { packageName ->
            removeMonitoredApp(packageName)
        }
        binding.monitoredAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.monitoredAppsRecyclerView.adapter = monitoredAppsAdapter

        // 加载被监控的应用列表
        loadMonitoredApps()

        // 添加应用按钮点击事件
        binding.addAppButton.setOnClickListener {
            val intent = Intent(this, AppSelectionActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_APP)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_APP && resultCode == RESULT_OK) {
            val packageName = data?.getStringExtra("packageName")
            if (packageName != null) {
                addMonitoredApp(packageName)
            }
        }
    }

    // 加载被监控的应用列表
    private fun loadMonitoredApps() {
        val sharedPreferences = getSharedPreferences("AppPause", Context.MODE_PRIVATE)
        val apps = sharedPreferences.getStringSet("monitoredApps", mutableSetOf()) ?: mutableSetOf()
        monitoredApps.clear()
        monitoredApps.addAll(apps)
        monitoredAppsAdapter.notifyDataSetChanged()
    }

    // 添加被监控的应用
    private fun addMonitoredApp(packageName: String) {
        if (monitoredApps.contains(packageName)) {
            return
        }
        monitoredApps.add(packageName)
        saveMonitoredApps()
        monitoredAppsAdapter.notifyDataSetChanged()
    }

    // 删除被监控的应用
    private fun removeMonitoredApp(packageName: String) {
        monitoredApps.remove(packageName)
        saveMonitoredApps()
        monitoredAppsAdapter.notifyDataSetChanged()
    }

    // 保存被监控的应用列表
    private fun saveMonitoredApps() {
        val sharedPreferences = getSharedPreferences("AppPause", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("monitoredApps", monitoredApps.toSet())
        editor.apply()
    }

    companion object {
        private const val REQUEST_CODE_ADD_APP = 1002
    }
}