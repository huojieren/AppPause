# AGENTS.md

## 构建命令

```bash
./gradlew assembleDebug  # 构建 Debug APK
./gradlew detektCheck    # 检查 Kotlin 代码风格
./gradlew lintDebug      # 执行 Android 静态分析
./gradlew dependencies   # 查看依赖树
```

## 技术栈

- **Kotlin**：2.2.10，使用 Compose 编译器插件
- **AGP**：8.11.1
- **JDK**：17 及以上（构建使用 Java 21）
- **依赖注入**：Hilt + Dagger
- **状态管理**：ViewModel + StateFlow
- **存储**：DataStore Preferences

## 架构

- 单模块 Android 应用
- `app/src/main/java/com/huojieren/apppause/`：
    - `managers/`：业务逻辑（计时、监控、悬浮窗、权限）
    - `ui/`：Compose 页面、组件、ViewModel 和主题
    - `data/`：模型、仓库和 DataStore
    - `monitor/`：AccessibilityService 和前台应用检测
    - `service/`：MonitorService 实现
    - `di/`：Hilt 模块

## 关键文件

- 应用入口：`MainActivity.kt`
- 无障碍配置：`app/src/main/res/xml/accessibility_config.xml`
- 主题：`ui/theme/`（Color.kt、Theme.kt、Type.kt）
- 权限定义：`data/Permissions.kt`

## 依赖管理

版本目录位于 `gradle/libs.versions.toml`。

## 测试

当前只有一个单元测试：`app/src/test/java/com/huojieren/apppause/ExampleUnitTest.kt`。

## 代码规范

- UI Compose 规范：参见 `.agents/skills/ui-convention/SKILL.md`。
- 涉及 Git 状态检查、分支、提交、合并、回滚、推送或历史修改时，使用机器级
  `$git-workflow` skill。
