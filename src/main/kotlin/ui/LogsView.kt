import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import log.refine.RefinedLog
import table.Header

@Composable
fun LogsView(header: Header, logs: List<RefinedLog>, detectionFocus: DetectionFocus?) {
    val listState = rememberLazyListState()

    LaunchedEffect(detectionFocus) {
        detectionFocus?.focusing?.let {
            listState.scrollToItem(it.logIndex)
        }
    }

    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    LazyColumn(modifier = Modifier.simpleVerticalScrollbar(listState), state = listState) {
        item { HeaderRow(header, divider) }
        item { HeaderDivider() }
        logs.forEach {
            item {
                Column {
                    LogRow(it, header, divider = divider)
                    Divider(color = Color(0xFFE5E7E9))
                }
            }
        }
    }
}

// https://stackoverflow.com/questions/66341823/jetpack-compose-scrollbars
@Composable
fun Modifier.simpleVerticalScrollbar(
    state: LazyListState,
    width: Dp = 8.dp,
    scrollbarHeight: Dp = 24.dp,
    scrollbarColor: Color = Color.LightGray
): Modifier {

    return drawWithContent {
        drawContent()
        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        if (firstVisibleElementIndex != null) {
            val totalDistance = this.size.height - scrollbarHeight.toPx()
            val ratio = firstVisibleElementIndex.toFloat() / state.layoutInfo.totalItemsCount.toFloat()
            val scrollbarOffsetY = totalDistance * ratio

            drawRect(
                color = scrollbarColor,
                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                size = Size(width.toPx(), scrollbarHeight.toPx())
            )
        }
    }
}
