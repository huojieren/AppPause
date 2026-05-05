---
name: ui-convention
description: UI Compose coding conventions for this project
---

# UI Compose Conventions

## 1. Screen Conventions

- **One file per Screen**: `MainScreen.kt`, `SettingsScreen.kt`, `TodoListScreen.kt`, etc.
- Screen Composables accept only `uiState` and callback functions, never ViewModel directly
- Screen contains only business logic composition, no reusable UI components
- **Exception**: Closely related Composables may coexist (e.g., `TimeSelectionScreen` +
  `TimeOutScreen` in `TimerScreen.kt`), but prefer splitting

## 2. Component Extraction Conventions

- **One reusable component per file**: `TodoListItem.kt`, `GroupFilterChips.kt`,
  `AddTodoDialog.kt`, etc.
- Components go in `ui/components/` directory
- Naming: `{feature name} + {type}`, e.g., `TodoListItem`, `GroupFilterChips`

## 3. Preview Conventions (Key Update)

- **Every Screen must have one Preview function** with both `@LightComponentPreview` +
  `@DarkComponentPreview` annotations
- **Every Component must have one Preview function** with both `@LightComponentPreview` +
  `@DarkComponentPreview` annotations
- **Never use single-theme Preview** (e.g., only `@LightComponentPreview`)
- **Correct format**:
  ```kotlin
  @LightComponentPreview
  @DarkComponentPreview
  @Composable
  fun SettingsScreenPreview() { ... }
  ```
- Use mock data in Preview to verify UI rendering

## 4. ViewModel and State Relationship

- ViewModel handles business logic and data flow (StateFlow)
- Screen Composables receive only **State object + callback functions**
- AppPauseScreen (parent) is responsible for creating/obtaining ViewModel, collecting State, passing to child Screens
- State naming: `{feature name}UiState`, e.g., `TodoListUiState`, `AppStatusUiState`

---

## Reference: Preview Annotations

```kotlin
// ui/annotations.kt
@Preview(name = "Light Theme", uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class LightComponentPreview

@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class DarkComponentPreview
```

## Common Issues to Avoid

| Issue                                       | Solution                                          |
|---------------------------------------------|---------------------------------------------------|
| Screen receiving ViewModel directly         | Pass `uiState` + callbacks instead                |
| Missing Dark theme Preview                  | Add both annotations to the same Preview function |
| Multiple unrelated Composables in one file  | Split into separate files                         |
| State class not following naming convention | Use `{feature}UiState` pattern                    |