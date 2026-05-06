package com.huojieren.apppause.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.data.local.entity.TodoEntity
import com.huojieren.apppause.data.local.entity.TodoGroupEntity
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun EditTodoDialog(
    todo: TodoEntity,
    groups: List<TodoGroupEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TodoEntity) -> Unit
) {
    var name by remember { mutableStateOf(todo.name) }
    var description by remember { mutableStateOf(todo.description) }
    var selectedGroupId by remember { mutableStateOf(todo.groupId) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑待办") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(
                            text = groups.find { it.id == selectedGroupId }?.name
                                ?: "选择分组（可选）"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("无") },
                            onClick = {
                                selectedGroupId = null
                                expanded = false
                            }
                        )
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroupId = group.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        todo.copy(
                            name = name,
                            description = description,
                            groupId = selectedGroupId
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun EditTodoDialogPreview() {
    AppTheme {
        EditTodoDialog(
            todo = TodoEntity(
                id = 1,
                name = "学习 Kotlin",
                description = "学习协程",
                isCompleted = false,
                groupId = 1
            ),
            groups = listOf(
                TodoGroupEntity(id = 1, name = "工作", color = "#2196F3", isDefault = true),
                TodoGroupEntity(id = 2, name = "生活", color = "#4CAF50", isDefault = true)
            ),
            onDismiss = {},
            onConfirm = {}
        )
    }
}