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

## 5. Layout & Spacing Conventions

### Core Principle: Screen as Pure Component

**All spacing, padding, and FAB should be controlled by parent (AppPauseScreen), not individual Screens.**

- Screen is a pure UI component that only renders content
- Screen does NOT contain Scaffold, FAB, or internal padding
- Parent (AppPauseScreen) controls all layout decisions

### Specific Rules

| Rule | Example |
|------|---------|
| No internal Scaffold in Screen | ❌ `Scaffold { innerPadding -> ... }` |
| No FAB in Screen | ❌ `floatingActionButton = { ... }` |
| No internal padding | ❌ `Modifier.padding(16.dp)` |
| Pass modifier to content | ✅ `Column(modifier = modifier.fillMaxSize())` |
| FAB in parent | ✅ Scaffold in AppPauseScreen |
| Parent controls spacing | ✅ `modifier = Modifier.padding(16.dp)` |

### Example

```kotlin
// ❌ Wrong - Screen handles its own layout
@Composable
fun TodoListScreen(...) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = { ... }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) { ... }
    }
}

// ✅ Correct - Screen as pure component
@Composable
fun TodoListScreen(modifier: Modifier = Modifier, ...) {
    Column(modifier = modifier.fillMaxSize()) { ... }
}

// Parent controls layout
@Composable
fun AppPauseScreen(...) {
    Scaffold(
        floatingActionButton = { ... }  // FAB here
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            TodoListScreen(...)  // No padding inside
        }
    }
}
```

### Components: Same Principle

- Components should also accept `modifier` and not add internal padding
- Let parent control spacing through modifier
- Exception: Small self-contained components like simple icons, dividers

---

## Common Issues to Avoid

| Issue                                       | Solution                                          |
|---------------------------------------------|---------------------------------------------------|
| Screen receiving ViewModel directly         | Pass `uiState` + callbacks instead                |
| Missing Dark theme Preview                  | Add both annotations to the same Preview function |
| Screen has internal Scaffold/padding/FAB   | Move to parent, Screen as pure component         |
| Multiple unrelated Composables in one file  | Split into separate files                         |
| State class not following naming convention | Use `{feature}UiState` pattern                    |