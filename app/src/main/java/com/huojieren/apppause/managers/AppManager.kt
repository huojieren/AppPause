package com.huojieren.apppause.managers

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
import com.huojieren.apppause.data.models.AppLetterGroup
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType

class AppManager(
    private val context: Context
) {
    private val pm = context.packageManager

    // 最大缓存条目数
    private val maxEntries = (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt().coerceAtLeast(4)

    // 内存缓存：key = packageName，value = ImageBitmap
    private val cache = LruCache<String, Painter>(maxEntries)

    companion object {
        private val pinyinFormat = HanyuPinyinOutputFormat().apply {
            caseType = HanyuPinyinCaseType.UPPERCASE
            toneType = HanyuPinyinToneType.WITHOUT_TONE
            vCharType = HanyuPinyinVCharType.WITH_V
        }

        fun getFirstLetter(name: String): String {
            if (name.isEmpty()) return "#"
            val firstChar = name.first()
            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstChar, pinyinFormat)
            return if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                pinyinArray[0].first().uppercaseChar().toString()
            } else {
                val upper = firstChar.uppercaseChar()
                if (upper in 'A'..'Z') upper.toString() else "#"
            }
        }
    }

    /**
     * 获取已安装的应用列表（按拼音字母分组）
     */
    fun loadInstalledAppsGrouped(): List<AppLetterGroup<AppInfo>> {
        val apps = loadInstalledApps()
        return apps.groupBy { getFirstLetter(it.name) }
            .entries
            .sortedBy { it.key }
            .map { AppLetterGroup(it.key, it.value) }
    }

    /**
     * 获取已安装的应用列表
     */
    fun loadInstalledApps(): List<AppInfo> {
        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            // 排除系统应用
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
            .filter { !it.packageName.startsWith("com.huojieren.apppause") }
            .sortedWith(pinyinComparator())
            .map {
                AppInfo(
                    name = pm.getApplicationLabel(it).toString(),
                    packageName = it.packageName,
                )
            }
    }

    private fun pinyinComparator() = Comparator<ApplicationInfo> { a, b ->
        val nameA = pm.getApplicationLabel(a).toString()
        val nameB = pm.getApplicationLabel(b).toString()
        getPinyin(nameA).compareTo(getPinyin(nameB))
    }

    private fun getPinyin(name: String): String {
        val sb = StringBuilder()
        for (char in name) {
            val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, pinyinFormat)
            if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                sb.append(pinyinArray[0])
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }

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
            logger("AppManager", "Failed to get app info: ${e.message}", Log.ERROR)
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
                logger("AppManager", "Failed to load icon: ${e.message}", Log.ERROR)
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