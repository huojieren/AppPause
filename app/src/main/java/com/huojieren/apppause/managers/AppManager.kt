package com.huojieren.apppause.managers

//import com.github.promeg.pinyinhelper.Pinyin
//import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.collection.LruCache
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap
import com.huojieren.apppause.R
import com.huojieren.apppause.data.models.AppInfo
import com.huojieren.apppause.data.models.AppInfoUi
import com.huojieren.apppause.data.repository.LogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class AppManager(
    private val context: Context,
    private val logRepository: LogRepository
) {
    private val pm = context.packageManager

    // 最大缓存条目数
    private val maxEntries = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt().coerceAtLeast(4)

    // 内存缓存：key = packageName，value = ImageBitmap
    private val cache = LruCache<String, Painter>(maxEntries)

// TODO: 拼音排序
    /*    init {
            Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(context)))
        }*/

    /**
     * 获取已安装的应用列表
     */
    fun loadInstalledApps(): List<AppInfo> {
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            // 排除系统应用
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .filter { !it.packageName.startsWith("com.huojieren.apppause") }
//            .sortedWith(pinyinComparator())
            .map {
                AppInfo(
                    name = pm.getApplicationLabel(it).toString(),
                    packageName = it.packageName,
                )
            }
    }

    /*    private fun pinyinComparator() = Comparator<ApplicationInfo> { a, b ->
            val nameA = pm.getApplicationLabel(a).toString()
            val nameB = pm.getApplicationLabel(b).toString()
            Pinyin.toPinyin(nameA, "").compareTo(Pinyin.toPinyin(nameB, ""))
        }*/

    /**
     * 根据包名获取应用信息
     */
    fun getAppInfo(packageName: String): AppInfo {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val name = pm.getApplicationLabel(appInfo).toString()
            AppInfo(
                name = name,
                packageName = packageName,
            )
        } catch (e: Exception) {
            logRepository.log("AppManager", "[ERROR] 获取应用信息失败: ${e.message}")
            // 返回默认的 AppInfo
            AppInfo(
                name = packageName,
                packageName = packageName
            )
        }
    }

    /**
     * 异步转换 AppInfoList 为 AppInfoUiLIst
     */
    suspend fun toUiList(appList: List<AppInfo>): List<AppInfoUi> = coroutineScope {
        appList.map { app ->
            async {
                val icon = loadIcon(app.packageName)
                AppInfoUi(app.name, app.packageName, icon)
            }
        }.awaitAll()
    }

    /**
     * 异步加载图标：先查缓存，若无则从系统加载
     */
    suspend fun loadIcon(packageName: String): Painter {
        getCachedIcon(packageName)?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val drawable = pm.getApplicationIcon(packageName)
                val bitmap = drawable.toBitmap()
                val imageBitmap = bitmap.asImageBitmap()
                val painter = BitmapPainter(imageBitmap)
                cache.put(packageName, painter)
                painter
            } catch (e: Exception) {
                logRepository.log("AppManager", "[ERROR] 加载图标失败: ${e.message}", Log.ERROR)
                getPlaceholderIcon()
            }
        }
    }

    /**
     * 从缓存中查找图标
     */
    fun getCachedIcon(packageName: String): Painter? = cache[packageName]


    fun getPlaceholderIcon(): Painter {
        return BitmapPainter(
            AppCompatResources.getDrawable(context, R.drawable.ic_launcher_foreground)!!
                .toBitmap().asImageBitmap()
        )
    }
}