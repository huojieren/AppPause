package com.huojieren.apppause.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme

private val colorOptions = listOf(
    "#2196F3", "#4CAF50", "#FF9800", "#E91E63",
    "#9C27B0", "#00BCD4", "#795548", "#607D8B"
)

@Composable
fun AddGroupDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加分组") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分组名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("选择颜色", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorOptions.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parseColor(color))
                                .clickable { selectedColor = color }
                                .then(
                                    if (selectedColor == color) {
                                        Modifier.background(
                                            Color.White.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                    } else Modifier
                                )
                        ) {
                            if (selectedColor == color) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, selectedColor) },
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

fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color.Gray
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun AddGroupDialogPreview() {
    AppTheme {
        AddGroupDialog(
            onDismiss = {},
            onConfirm = { _, _ -> }
        )
    }
}