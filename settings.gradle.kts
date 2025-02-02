// 插件管理配置
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral() // Maven 中央仓库
        gradlePluginPortal() // Gradle 插件门户
    }
}

// 依赖解析管理配置
dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    // 忽略实验性功能警告
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // 禁止模块级别覆盖仓库配置
    @Suppress("UnstableApiUsage")
    // 忽略实验性功能警告
    repositories {
        google() // Google Maven 仓库
        mavenCentral() // Maven 中央仓库
    }
}

// 项目名称和模块配置
rootProject.name = "AppPause" // 项目名称
include(":app") // 包含 app 模块