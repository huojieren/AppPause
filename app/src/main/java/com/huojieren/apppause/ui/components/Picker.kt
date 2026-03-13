package com.huojieren.apppause.ui.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.huojieren.apppause.ui.state.PickerState
import com.huojieren.apppause.ui.state.rememberPickerState
import com.huojieren.apppause.ui.theme.AppTheme
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * 可滚动的选择器组件，用于从列表中选择一个项目
 *
 * @param modifier 用于修饰整个选择器组件的Modifier
 * @param items 可供选择的项目列表
 * @param state 选择器状态对象，用于跟踪当前选中的项目
 * @param startIndex 初始选中的项目索引
 * @param visibleItemsCount 可见项目的数量，必须为奇数以确保正确对齐
 * @param textModifier 用于修饰每个文本项目的Modifier
 * @param textStyle 文本的样式
 * @param dividerColor 分隔线的颜色
 */
@Composable
fun Picker(
    modifier: Modifier = Modifier,
    items: List<String>,
    state: PickerState = rememberPickerState(),
    startIndex: Int = 0,
    visibleItemsCount: Int = 3,
    textModifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    dividerColor: Color = LocalContentColor.current,
) {

    // 计算可见项目的中间位置
    val visibleItemsMiddle = visibleItemsCount / 2

    // 设置一个非常大的滚动范围以模拟无限滚动效果
    val listScrollCount = Integer.MAX_VALUE
    val listScrollMiddle = listScrollCount / 2

    // 计算列表的起始索引，确保初始选中项居中显示
    val listStartIndex =
        listScrollMiddle - listScrollMiddle % items.size - visibleItemsMiddle + startIndex

    // 获取指定索引处的项目，通过模运算实现循环效果
    fun getItem(index: Int) = items[index % items.size]

    // 创建并记住LazyColumn的状态，初始位置设置为计算出的起始索引
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = listStartIndex)

    // 创建并记住滚动行为，使项目能够自动对齐到中心位置
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // 跟踪项目高度的像素值和dp值
    val itemHeightPixels = remember { mutableStateOf(0) }
    val itemHeightDp = pixelsToDp(itemHeightPixels.value)

    // 创建渐变遮罩效果，使上下边缘的项目逐渐消失
    val fadingEdgeGradient = remember {
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.5f to Color.Black,
            1f to Color.Transparent
        )
    }

    // 监听列表状态变化，更新选中的项目
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { index -> getItem(index + visibleItemsMiddle) }
            .distinctUntilChanged()
            .collect { item -> state.selectedItem = item }
    }

    Box(modifier = modifier) {
        // 主要的可滚动列表组件
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp * visibleItemsCount)
                .fadingEdge(fadingEdgeGradient)
        ) {
            // 填充列表项，使用大数值模拟无限滚动
            items(listScrollCount) { index ->
                Text(
                    text = getItem(index),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = textStyle,
                    modifier = Modifier
                        .onSizeChanged { size -> itemHeightPixels.value = size.height }
                        .then(textModifier)
                )
            }
        }

        // 在选中项上方添加分隔线
        HorizontalDivider(
            modifier = Modifier.offset(y = itemHeightDp * visibleItemsMiddle),
            thickness = DividerDefaults.Thickness, color = dividerColor
        )

        // 在选中项下方添加分隔线
        HorizontalDivider(
            modifier = Modifier.offset(y = itemHeightDp * (visibleItemsMiddle + 1)),
            thickness = DividerDefaults.Thickness, color = dividerColor
        )

    }

}

private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@Composable
private fun pixelsToDp(pixels: Int) = with(LocalDensity.current) { pixels.toDp() }


@Preview
@Composable
fun PickerExample() {
    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {

                val values = remember { (1..99).map { it.toString() } }
                val valuesPickerState = rememberPickerState()
                val units = remember { listOf("seconds", "minutes", "hours") }
                val unitsPickerState = rememberPickerState()

                Text(text = "Example Picker", modifier = Modifier.padding(top = 16.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Picker(
                        modifier = Modifier.weight(0.3f),
                        items = values,
                        state = valuesPickerState,
                        textModifier = Modifier.padding(8.dp),
                        textStyle = TextStyle(fontSize = 32.sp)
                    )
                    Picker(
                        modifier = Modifier.weight(0.7f),
                        items = units,
                        state = unitsPickerState,
                        textModifier = Modifier.padding(8.dp),
                        textStyle = TextStyle(fontSize = 32.sp)
                    )
                }

                Text(
                    text = "Interval: ${valuesPickerState.selectedItem} ${unitsPickerState.selectedItem}",
                    modifier = Modifier.padding(vertical = 16.dp)
                )

            }
        }
    }
}