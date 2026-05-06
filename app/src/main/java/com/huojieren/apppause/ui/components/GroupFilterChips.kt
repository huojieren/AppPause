package com.huojieren.apppause.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
fun GroupFilterChips(
    groups: List<TodoGroupEntity>,
    selectedGroupId: Long?,
    modifier: Modifier = Modifier,
    onGroupSelected: (Long?) -> Unit,
    onAddGroup: () -> Unit,
    onEditGroup: (TodoGroupEntity) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CompactFilterChip(
                selected = selectedGroupId == null,
                onClick = { onGroupSelected(null) },
                label = { Text("全部") }
            )
        }
        items(groups) { group ->
            Box {
                CompactFilterChip(
                    selected = selectedGroupId == group.id,
                    onClick = { onGroupSelected(group.id) },
                    label = { Text(group.name) },
                    leadingIcon = if (selectedGroupId == group.id) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = parseColor(group.color),
                                        shape = CircleShape
                                    )
                            )
                        }
                    } else null,
                    trailingIcon = {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "分组菜单",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("编辑分组") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = {
                            showMenu = false
                            onEditGroup(group)
                        }
                    )
                }
            }
        }
        item {
            CompactFilterChip(
                selected = false,
                onClick = onAddGroup,
                label = { Text("+ 分组") }
            )
        }
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun GroupFilterChipsPreview() {
    AppTheme {
        GroupFilterChips(
            groups = listOf(
                TodoGroupEntity(id = 1, name = "工作", color = "#2196F3", isDefault = true),
                TodoGroupEntity(id = 2, name = "生活", color = "#4CAF50", isDefault = true),
                TodoGroupEntity(id = 3, name = "学习", color = "#FF9800", isDefault = true)
            ),
            selectedGroupId = 1,
            onGroupSelected = {},
            onAddGroup = {},
            onEditGroup = {}
        )
    }
}
