plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.huojieren.apppause"
    compileSdk = 35

    // 配置默认配置
    defaultConfig {
        applicationId = "com.huojieren.apppause"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"// 测试用例运行器
    }

    // 配置构建功能
    buildFeatures {
        buildConfig = true
    }

    // 配置构建类型
    buildTypes {
        debug {
            isMinifyEnabled = false// 是否混淆
            buildConfigField("long", "TIME_UNIT", "1000L")
            buildConfigField("String", "TIME_DESC", "\"秒\"")
        }
        release {
            isMinifyEnabled = false// 是否混淆
            proguardFiles(// 混淆文件
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("long", "TIME_UNIT", "60000L")
            buildConfigField("String", "TIME_DESC", "\"分钟\"")
        }
    }

    // 配置编译选项
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // 配置 Kotlin 编译选项
    kotlinOptions {
        jvmTarget = "11"
    }

    // 配置 ViewBinding
    viewBinding {
        enable = true
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.constraintlayout)
}