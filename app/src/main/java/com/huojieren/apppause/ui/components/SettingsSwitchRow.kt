package com.huojieren.apppause.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huojieren.apppause.ui.DarkComponentPreview
import com.huojieren.apppause.ui.LightComponentPreview
import com.huojieren.apppause.ui.theme.AppTheme

@Composable
fun SettingsSwitchRow(
    modifier: Modifier = Modifier,
    title: String,
    infoText: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    infoInitiallyExpanded: Boolean = false,
) {
    var showInfo by remember { mutableStateOf(infoInitiallyExpanded) }
    var anchorWidth by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .onGloballyPositioned { coordinates ->
                anchorWidth = coordinates.size.width
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (infoText != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "说明",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f),
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { showInfo = true }
                        .padding(7.dp)
                )
            }
            Switch(
                modifier = Modifier
                    .width(48.dp)
                    .height(36.dp)
                    .scale(0.86f),
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
        if (infoText != null) {
            DropdownMenu(
                expanded = showInfo,
                onDismissRequest = { showInfo = false },
                modifier = if (anchorWidth > 0) {
                    Modifier.width(with(density) { anchorWidth.toDp() })
                } else {
                    Modifier
                }
            ) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@LightComponentPreview
@DarkComponentPreview
@Composable
fun SettingsSwitchRowPreview() {
    AppTheme {
        SettingsSwitchRow(
            title = "设置项名称",
            infoText = "这是设置项的说明文字",
            checked = true,
            onCheckedChange = {}
        )
    }
}

@Preview(
    name = "Light Theme Expanded",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO,
    heightDp = 160
)
@Preview(
    name = "Dark Theme Expanded",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    heightDp = 160
)
@Composable
fun SettingsSwitchRowExpandedPreview() {
    AppTheme {
        SettingsSwitchRow(
            title = "所有应用共享额度",
            infoText = "开启后所有应用一起计算使用时长，关闭后每个应用单独计时",
            checked = true,
            onCheckedChange = {},
            infoInitiallyExpanded = true
        )
    }
}
