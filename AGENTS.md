# AGENTS.md

## Build Commands

```bash
./gradlew assembleDebug    # Debug APK build
./gradlew detektCheck    # Kotlin code style check
./gradlew lintDebug     # Android static analysis
./gradlew dependencies # View dependency tree
```

## Tech Stack

- **Kotlin**: 2.2.10 with Compose compiler plugin
- **AGP**: 8.11.1
- **JDK**: 17+ (build uses Java 21)
- **DI**: Hilt + Dagger
- **State**: ViewModel + StateFlow
- **Storage**: DataStore Preferences

## Architecture

- Single-module Android app
- `app/src/main/java/com/huojieren/apppause/`:
    - `managers/` - Business logic (Timer, Monitor, Overlay, Permission)
    - `ui/` - Compose screens, components, viewmodels, theme
    - `data/` - Models, repositories, DataStore
    - `monitor/` - AccessibilityService, foreground app detection
    - `service/` - MonitorService implementation
    - `di/` - Hilt modules

## Key Files

- Entry point: `MainActivity.kt`
- Accessibility config: `app/src/main/res/xml/accessibility_config.xml`
- Theme: `ui/theme/` (Color.kt, Theme.kt, Type.kt)
- Permissions: `data/Permissions.kt`

## Dependencies

Version catalog in `gradle/libs.versions.toml`

## Testing

Single unit test at `app/src/test/java/com/huojieren/apppause/ExampleUnitTest.kt`