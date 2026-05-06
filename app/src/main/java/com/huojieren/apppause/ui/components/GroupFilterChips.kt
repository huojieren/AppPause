package com.huojieren.apppause.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
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
    var expandedGroupMenuId by remember { mutableStateOf<Long?>(null) }

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
                            onClick = { expandedGroupMenuId = group.id },
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
                    expanded = expandedGroupMenuId == group.id,
                    onDismissRequest = { expandedGroupMenuId = null },
                    modifier = Modifier.widthIn(min = 128.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    CompositionLocalProvider(
                        LocalMinimumInteractiveComponentSize provides Dp.Unspecified
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "编辑分组",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                expandedGroupMenuId = null
                                onEditGroup(group)
                            },
                            modifier = Modifier.height(40.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        )
                    }
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
