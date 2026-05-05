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
import com.huojieren.apppause.data.local.entity.TodoGroupEntity
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun AddTodoDialog(
    groups: List<TodoGroupEntity>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String, groupId: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加待办") },
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
                onClick = { onConfirm(name, description, selectedGroupId) },
                enabled = name.isNotBlank()
            ) {
                Text("添加")
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
fun AddTodoDialogPreview() {
    AppTheme {
        AddTodoDialog(
            groups = listOf(
                TodoGroupEntity(id = 1, name = "工作", color = "#2196F3", isDefault = true),
                TodoGroupEntity(id = 2, name = "生活", color = "#4CAF50", isDefault = true)
            ),
            onDismiss = {},
            onConfirm = { _, _, _ -> }
        )
    }
}