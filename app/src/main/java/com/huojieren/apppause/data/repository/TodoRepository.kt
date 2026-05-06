package com.huojieren.apppause.data.repository

import com.huojieren.apppause.data.local.dao.TodoDao
import com.huojieren.apppause.data.local.dao.TodoGroupDao
import com.huojieren.apppause.data.local.entity.TodoEntity
import com.huojieren.apppause.data.local.entity.TodoGroupEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao,
    private val todoGroupDao: TodoGroupDao
) {
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()

    fun getTodosByGroup(groupId: Long): Flow<List<TodoEntity>> = todoDao.getTodosByGroup(groupId)

    fun getActiveTodos(): Flow<List<TodoEntity>> = todoDao.getActiveTodos()

    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)

    suspend fun insertTodo(todo: TodoEntity): Long = todoDao.insertTodo(todo)

    suspend fun updateTodo(todo: TodoEntity) = todoDao.updateTodo(todo)

    suspend fun deleteTodo(todo: TodoEntity) = todoDao.deleteTodo(todo)

    suspend fun toggleTodoCompletion(id: Long, isCompleted: Boolean) {
        todoDao.updateTodoCompletion(id, isCompleted)
    }

    fun searchTodos(query: String): Flow<List<TodoEntity>> = todoDao.searchTodos(query)

    // ===== TodoGroup 操作 =====
    fun getAllGroups(): Flow<List<TodoGroupEntity>> = todoGroupDao.getAllGroups()

    suspend fun getGroupById(id: Long): TodoGroupEntity? = todoGroupDao.getGroupById(id)

    suspend fun insertGroup(group: TodoGroupEntity): Long = todoGroupDao.insertGroup(group)

    suspend fun updateGroup(group: TodoGroupEntity) = todoGroupDao.updateGroup(group)

    suspend fun deleteGroup(group: TodoGroupEntity) = todoGroupDao.deleteGroup(group)

    suspend fun getGroupCount(): Int = todoGroupDao.getGroupCount()
}