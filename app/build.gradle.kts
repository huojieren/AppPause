// 模块级别的构建配置文件
plugins {
    alias(libs.plugins.android.application) // Android 应用插件
    alias(libs.plugins.kotlin.android) // Kotlin Android 插件
    alias(libs.plugins.compose.compiler) // Compose 编译器插件
    alias(libs.plugins.jetbrains.kotlin.kapt) //  Hilt 编译器插件
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.huojieren.apppause" // 应用命名空间
    compileSdk = 35 // 编译 SDK 版本

    // 默认配置
    defaultConfig {
        applicationId = "com.huojieren.apppause" // 应用 ID
        minSdk = 26 // 最低支持的 SDK 版本
        targetSdk = 35 // 目标 SDK 版本
        versionCode = 2 // 版本代码
        versionName = "0.7.0" // 版本名称
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // 测试用例运行器
    }

    // 构建功能配置
    buildFeatures {
        buildConfig = true // 启用 BuildConfig
        viewBinding = true // 启用 ViewBinding
        compose = true // 启用 Compose
    }

    // 构建类型配置
    buildTypes {
        debug {
            applicationIdSuffix = ".debug" // 应用 ID 后缀
            isMinifyEnabled = false // 是否启用代码混淆
            buildConfigField("long", "TIME_UNIT", "1000L") // 时间单位（秒）
            buildConfigField("String", "TIME_DESC", "\"秒\"") // 时间描述
            buildConfigField("String", "APP_NAME", "\"AppPause Debug\"") // 应用名称
            resValue("string", "app_name", "AppPause Debug") // 资源值
        }
        release {
            isMinifyEnabled = false // 是否启用代码混淆
            proguardFiles( // 混淆文件
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("long", "TIME_UNIT", "60000L") // 时间单位（分钟）
            buildConfigField("String", "TIME_DESC", "\"分钟\"") // 时间描述
            buildConfigField("String", "APP_NAME", "\"AppPause\"") // 应用名称
            resValue("string", "app_name", "AppPause") // 资源值
            signingConfig = signingConfigs.getByName("debug") // 使用 debug 签名配置
        }
    }

    // 编译选项
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // 源代码兼容性
        targetCompatibility = JavaVersion.VERSION_11 // 目标代码兼容性
    }

    // Kotlin 编译选项
    kotlinOptions {
        jvmTarget = "11" // JVM 目标版本
    }
}

// 依赖配置
dependencies {
    implementation(libs.androidx.core) // AndroidX Core 库
    implementation(libs.androidx.appcompat) // AndroidX AppCompat 库
    implementation(libs.material)
    implementation(libs.androidx.work.runtime.ktx) // Material Design 库
    testImplementation(libs.junit) // JUnit 测试库
    androidTestImplementation(libs.androidx.junit) // AndroidX JUnit 测试库
    androidTestImplementation(libs.androidx.espresso.core) // Espresso 测试库
    implementation(libs.constraintlayout) // ConstraintLayout 库
    implementation(libs.tinypinyin) // TinyPinyin 库
    implementation(libs.tinypinyin.lexicons.android.cncity)

    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)


    implementation(libs.androidx.material3) // Material Design 3

    // Android Studio Preview support
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)

    // UI Tests
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Optional - Included automatically by material, only add when you need
    // the icons but not the material library (e.g. when using Material3 or a
    // custom design system based on Foundation)
    implementation(libs.androidx.material.icons.core)
    // Optional - Add full set of material icons
    implementation(libs.androidx.material.icons.extended)
    // Optional - Add window size utils
    implementation(libs.androidx.adaptive)

    // Optional - Integration with activities
    implementation(libs.androidx.activity.compose)
    // Optional - Integration with ViewModels
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Optional - Integration with LiveData
    implementation(libs.androidx.runtime.livedata)
    // Optional - Integration with RxJava
    implementation(libs.androidx.runtime.rxjava2)

    // Compose基础库
    implementation(libs.androidx.ui)
    implementation(libs.material3)
    implementation(libs.ui.tooling.preview)

    // 现有项目的兼容支持
    implementation(libs.runtime.livedata)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.numberpicker)

    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}