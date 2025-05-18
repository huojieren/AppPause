package com.huojieren.apppause.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict
import com.huojieren.apppause.ui.screens.MainScreen
import com.huojieren.apppause.ui.theme.AppTheme
import com.huojieren.apppause.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化拼音库
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(this)))

        setContent {
            AppTheme {
                val viewModel: MainViewModel by viewModels()
                MainScreen(this, viewModel)
            }
        }
    }
}