plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.dependency.analysis)
    id("dagger.hilt.android.plugin")
    kotlin("plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.huojieren.apppause"
    //noinspection GradleDependency
    compileSdk = 35

    defaultConfig {
        applicationId = "com.huojieren.apppause"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 35
        versionCode = 2
        versionName = "0.7.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
        freeCompilerArgs += "-Xjvm-default=all-compatibility"
    }

    kapt {
        correctErrorTypes = true
        showProcessorStats = true
    }
}

dependencies {
    // ===== Compose =====
    implementation(platform(libs.androidx.compose.bom)) // Compose BOM
    implementation(libs.androidx.ui) // UI 基础
    implementation(libs.androidx.ui.graphics) // 图形
    implementation(libs.androidx.ui.text) // 文本
    implementation(libs.androidx.ui.tooling.preview) // 预览
    implementation(libs.androidx.ui.unit) // 单位支持
    implementation(libs.androidx.animation) // 动画
    implementation(libs.androidx.foundation) // 基础组件
    implementation(libs.androidx.foundation.layout) // 布局
    implementation(libs.androidx.material3) // Material3
    implementation(libs.androidx.material.icons.core) // Material Icons
    implementation(libs.androidx.activity.compose) // Activity Compose 集成
    implementation(libs.androidx.lifecycle.viewmodel.compose) // ViewModel Compose 集成
    implementation(libs.androidx.runtime) // Runtime
    implementation(libs.androidx.runtime.annotation) // Runtime 注解
    implementation(libs.androidx.runtime.livedata) // LiveData 集成
    debugImplementation(libs.androidx.ui.tooling) // 调试工具
    debugRuntimeOnly(libs.androidx.ui.test.manifest) // 测试 Manifest
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // https://mvnrepository.com/artifact/com.google.accompanist/accompanist-drawablepainter
    runtimeOnly(libs.accompanist.drawablepainter)
    runtimeOnly(libs.accompanist.systemuicontroller)

    // ===== Lifecycle =====
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)

    // ===== Navigation =====
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.navigation.compose)

    // ===== DataStore =====
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.kotlinx.serialization.json)

    // ===== AndroidX Core & 基础 =====
    implementation(libs.androidx.activity)
    implementation(libs.androidx.core)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.savedstate)

    // ===== 依赖注入 (DI) =====
    implementation(libs.dagger)
    kapt(libs.dagger.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.core)
    kapt(libs.hilt.compiler)
    implementation(libs.javax.inject)
    implementation(libs.androidx.hilt.navigation.compose)

    // ===== 协程 =====
    implementation(libs.kotlinx.coroutines.core)

    // ===== Material Design =====
    implementation(libs.material)

    // ===== Timber =====
    implementation(libs.timber)

    // ===== Pinyin4j =====
    implementation(libs.pinyin4j)

    // ===== 测试 =====
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.monitor)
}
