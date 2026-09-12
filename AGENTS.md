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

## Git 工作流

- 通用 Git 安全操作遵循用户级 `$git-workflow` skill；暂存、提交、切换分支、合并、回滚、拉取和推送等写操作均需获得对应的明确授权，提交不代表推送。
- `dev` 是日常开发与集成分支；`master` 是稳定发布分支，仅在版本发布或大迭代获得明确指令时才把 `dev` 或发布分支合入 `master`。
- `feature/*`、`fix/*`、`refactor/*`、`docs/*` 从 `dev` 派生并合回 `dev`；`hotfix/*` 从 `master` 派生并将修复同步回 `dev`；Codex 创建的分支默认使用 `codex/` 前缀。
- 提交信息使用 `<type>(<scope>): <中文说明>`，其中 `type` 和可选的 `scope` 使用简短英文；提交前检查实际暂存差异并执行与风险相称的验证，不使用 `--no-verify`。
- 合并需要保留分支拓扑时使用 `git merge --no-ff <source>`；功能和修复分支合入 `dev`，合并后运行相关验证且不自动推送。
