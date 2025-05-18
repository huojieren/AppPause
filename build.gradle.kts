// 项目级别的构建配置文件，用于配置所有子项目/模块共享的选项
buildscript {
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.51")
    }
}
plugins {
    alias(libs.plugins.android.application) apply false // Android 应用插件
    alias(libs.plugins.kotlin.android) apply false // Kotlin Android 插件
    alias(libs.plugins.compose.compiler) apply false // Compose 编译器插件
    alias(libs.plugins.jetbrains.kotlin.kapt) apply false // Kotlin 编译时注解插件
}