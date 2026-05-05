package com.huojieren.apppause.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.data.local.entity.TodoEntity
import com.huojieren.apppause.data.local.entity.TodoGroupEntity
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.components.AddGroupDialog
import com.huojieren.apppause.ui.components.AddTodoDialog
import com.huojieren.apppause.ui.components.EditGroupDialog
import com.huojieren.apppause.ui.components.EditTodoDialog
import com.huojieren.apppause.ui.components.GroupFilterChips
import com.huojieren.apppause.ui.components.TodoEmptyState
import com.huojieren.apppause.ui.components.TodoListItem
import com.huojieren.apppause.ui.state.TodoListUiState
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun TodoListScreen(
    modifier: Modifier = Modifier,
    uiState: TodoListUiState,
    onSelectGroup: (Long?) -> Unit,
    onShowAddTodoDialog: () -> Unit,
    onHideAddTodoDialog: () -> Unit,
    onAddTodo: (name: String, description: String, groupId: Long?) -> Unit,
    onShowEditTodoDialog: (TodoEntity) -> Unit,
    onHideEditTodoDialog: () -> Unit,
    onUpdateTodo: (TodoEntity) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit,
    onToggleTodoCompletion: (TodoEntity) -> Unit,
    onShowAddGroupDialog: () -> Unit,
    onHideAddGroupDialog: () -> Unit,
    onAddGroup: (name: String, color: String) -> Unit,
    onShowEditGroupDialog: (TodoGroupEntity) -> Unit,
    onHideEditGroupDialog: () -> Unit,
    onUpdateGroup: (TodoGroupEntity) -> Unit,
    onDeleteGroup: (TodoGroupEntity) -> Unit
) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = onShowAddTodoDialog) {
                Icon(Icons.Default.Add, contentDescription = "添加待办")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            GroupFilterChips(
                groups = uiState.groups,
                selectedGroupId = uiState.selectedGroupId,
                onGroupSelected = onSelectGroup,
                onAddGroup = onShowAddGroupDialog,
                onEditGroup = onShowEditGroupDialog
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.todos.isEmpty()) {
                TodoEmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.todos, key = { it.id }) { todo ->
                        val group = uiState.groups.find { it.id == todo.groupId }
                        TodoListItem(
                            todo = todo,
                            groupName = group?.name,
                            groupColor = group?.color,
                            onToggleComplete = { onToggleTodoCompletion(todo) },
                            onDelete = { onDeleteTodo(todo) },
                            onClick = { onShowEditTodoDialog(todo) }
                        )
                    }
                }
            }
        }

        if (uiState.showAddDialog) {
            AddTodoDialog(
                groups = uiState.groups,
                onDismiss = onHideAddTodoDialog,
                onConfirm = onAddTodo
            )
        }

        if (uiState.showEditDialog && uiState.editingTodo != null) {
            EditTodoDialog(
                todo = uiState.editingTodo,
                groups = uiState.groups,
                onDismiss = onHideEditTodoDialog,
                onConfirm = onUpdateTodo
            )
        }

        if (uiState.showAddGroupDialog) {
            AddGroupDialog(
                onDismiss = onHideAddGroupDialog,
                onConfirm = onAddGroup
            )
        }

        if (uiState.showEditGroupDialog && uiState.editingGroup != null) {
            EditGroupDialog(
                group = uiState.editingGroup,
                onDismiss = onHideEditGroupDialog,
                onConfirm = onUpdateGroup,
                onDelete = onDeleteGroup
            )
        }
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun TodoListScreenPreview() {
    AppTheme {
        TodoListScreen(
            uiState = TodoListUiState(
                todos = listOf(
                    TodoEntity(
                        id = 1,
                        name = "学习 Kotlin",
                        description = "学习协程",
                        isCompleted = false,
                        groupId = 1
                    ),
                    TodoEntity(
                        id = 2,
                        name = "完成项目",
                        description = "App Pause",
                        isCompleted = true,
                        groupId = 1
                    )
                ),
                groups = listOf(
                    TodoGroupEntity(id = 1, name = "工作", color = "#2196F3", isDefault = true),
                    TodoGroupEntity(id = 2, name = "学习", color = "#FF9800", isDefault = true)
                )
            ),
            onSelectGroup = {},
            onShowAddTodoDialog = {},
            onHideAddTodoDialog = {},
            onAddTodo = { _, _, _ -> },
            onShowEditTodoDialog = {},
            onHideEditTodoDialog = {},
            onUpdateTodo = {},
            onDeleteTodo = {},
            onToggleTodoCompletion = {},
            onShowAddGroupDialog = {},
            onHideAddGroupDialog = {},
            onAddGroup = { _, _ -> },
            onShowEditGroupDialog = {},
            onHideEditGroupDialog = {},
            onUpdateGroup = {},
            onDeleteGroup = {}
        )
    }
}