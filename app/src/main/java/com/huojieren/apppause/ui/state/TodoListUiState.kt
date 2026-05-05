package com.huojieren.apppause.ui.state

import com.huojieren.apppause.data.local.entity.TodoEntity
import com.huojieren.apppause.data.local.entity.TodoGroupEntity

data class TodoListUiState(
    val todos: List<TodoEntity> = emptyList(),
    val groups: List<TodoGroupEntity> = emptyList(),
    val selectedGroupId: Long? = null,
    val isLoading: Boolean = false,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingTodo: TodoEntity? = null,
    val showAddGroupDialog: Boolean = false,
    val showEditGroupDialog: Boolean = false,
    val editingGroup: TodoGroupEntity? = null
)

data class TodoItemWithGroup(
    val todo: TodoEntity,
    val groupName: String?
)