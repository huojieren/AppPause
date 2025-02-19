// 模块级别的构建配置文件
plugins {
    alias(libs.plugins.android.application) // Android 应用插件
    alias(libs.plugins.kotlin.android) // Kotlin Android 插件
}

android {
    namespace = "com.huojieren.apppause" // 应用命名空间
    compileSdk = 35 // 编译 SDK 版本

    // 默认配置
    defaultConfig {
        applicationId = "com.huojieren.apppause" // 应用 ID
        minSdk = 26 // 最低支持的 SDK 版本
        targetSdk = 35 // 目标 SDK 版本
        versionCode = 1 // 版本代码
        versionName = "0.5.0" // 版本名称
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // 测试用例运行器
    }

    // 构建功能配置
    buildFeatures {
        buildConfig = true // 启用 BuildConfig 生成
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

    // ViewBinding 配置
    viewBinding {
        enable = true // 启用 ViewBinding
    }
}

// 依赖配置
dependencies {
    implementation(libs.androidx.core) // AndroidX Core 库
    implementation(libs.androidx.appcompat) // AndroidX AppCompat 库
    implementation(libs.material) // Material Design 库
    testImplementation(libs.junit) // JUnit 测试库
    androidTestImplementation(libs.androidx.junit) // AndroidX JUnit 测试库
    androidTestImplementation(libs.androidx.espresso.core) // Espresso 测试库
    implementation(libs.constraintlayout) // ConstraintLayout 库
}