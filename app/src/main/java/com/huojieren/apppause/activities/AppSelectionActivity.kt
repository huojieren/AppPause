package com.huojieren.apppause.activities

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.promeg.pinyinhelper.Pinyin
import com.huojieren.apppause.adapters.AppListAdapter
import com.huojieren.apppause.databinding.ActivityAppSelectionBinding
import com.huojieren.apppause.models.AppInfo

class AppSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAppSelectionBinding
    private lateinit var appListAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 RecyclerView
        appListAdapter = AppListAdapter { packageName ->
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

        val pinyinComparator = Comparator { app1: ApplicationInfo, app2: ApplicationInfo ->
            val name1 = packageManager.getApplicationLabel(app1).toString()
            val name2 = packageManager.getApplicationLabel(app2).toString()
            Pinyin.toPinyin(name1, "").lowercase()
                .compareTo(Pinyin.toPinyin(name2, "").lowercase())
        }

        val sortedApps = installedApps
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .sortedWith(pinyinComparator)
            .map { app ->
                AppInfo(
                    packageManager.getApplicationLabel(app).toString(),
                    app.packageName
                )
            }

        appListAdapter.updateList(sortedApps)
    }


    // 返回选中的应用
    private fun returnSelectedApp(packageName: String) {
        val intent = Intent()
        intent.putExtra("packageName", packageName)
        setResult(RESULT_OK, intent)
        finish()
    }
}