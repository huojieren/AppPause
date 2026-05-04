package com.huojieren.apppause.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huojieren.apppause.data.local.entity.TodoEntity
import com.huojieren.apppause.data.local.entity.TodoGroupEntity
import com.huojieren.apppause.data.repository.TodoRepository
import com.huojieren.apppause.ui.state.TodoListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoRepository: TodoRepository
) : ViewModel() {

    private val _selectedGroupId = MutableStateFlow<Long?>(null)
    private val _showAddDialog = MutableStateFlow(false)
    private val _showEditDialog = MutableStateFlow(false)
    private val _editingTodo = MutableStateFlow<TodoEntity?>(null)

    val uiState: StateFlow<TodoListUiState> = combine(
        todoRepository.getAllTodos(),
        todoRepository.getAllGroups(),
        _selectedGroupId,
        _showAddDialog,
        _showEditDialog,
        _editingTodo
    ) { flows ->
        val todos = flows[0] as List<TodoEntity>
        val groups = flows[1] as List<TodoGroupEntity>
        val selectedGroupId = flows[2] as Long?
        val showAddDialog = flows[3] as Boolean
        val showEditDialog = flows[4] as Boolean
        val editingTodo = flows[5] as TodoEntity?

        val filteredTodos = if (selectedGroupId != null) {
            todos.filter { it.groupId == selectedGroupId }
        } else {
            todos
        }

        TodoListUiState(
            todos = filteredTodos,
            groups = groups,
            selectedGroupId = selectedGroupId,
            isLoading = false,
            showAddDialog = showAddDialog,
            showEditDialog = showEditDialog,
            editingTodo = editingTodo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoListUiState(isLoading = true)
    )

    fun selectGroup(groupId: Long?) {
        _selectedGroupId.value = groupId
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun showEditDialog(todo: TodoEntity) {
        _editingTodo.value = todo
        _showEditDialog.value = true
    }

    fun hideEditDialog() {
        _showEditDialog.value = false
        _editingTodo.value = null
    }

    fun addTodo(name: String, description: String, groupId: Long?) {
        viewModelScope.launch {
            val todo = TodoEntity(
                name = name,
                description = description,
                groupId = groupId
            )
            todoRepository.insertTodo(todo)
            hideAddDialog()
        }
    }

    fun updateTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepository.updateTodo(todo)
            hideEditDialog()
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepository.deleteTodo(todo)
        }
    }

    fun toggleTodoCompletion(todo: TodoEntity) {
        viewModelScope.launch {
            todoRepository.toggleTodoCompletion(todo.id, !todo.isCompleted)
        }
    }

    fun addGroup(name: String, color: String) {
        viewModelScope.launch {
            val group = TodoGroupEntity(
                name = name,
                color = color,
                isDefault = false
            )
            todoRepository.insertGroup(group)
        }
    }
}