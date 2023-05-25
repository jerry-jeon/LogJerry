package com.jerryjeon.logjerry.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.ColumnDivider
import com.jerryjeon.logjerry.HeaderDivider
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.detector.MarkDetection
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.mark.LogMark
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import kotlinx.coroutines.delay

@Composable
fun LogsView(
    modifier: Modifier = Modifier,
    preferences: Preferences,
    header: Header,
    logs: List<RefinedLog>,
    logSelection: LogSelection?,
    listState: LazyListState,
    markedRows: List<RefinedLog>,
    collapseJsonDetection: (JsonDetection) -> Unit,
    expandJsonDetection: (annotation: String) -> Unit,
    setMark: (logMark: LogMark) -> Unit,
    deleteMark: (logIndex: Int) -> Unit,
    selectLog: (RefinedLog) -> Unit,
) {
    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
            item { HeaderRow(header, divider) }
            item { HeaderDivider() }
            logs.forEach { refinedLog ->
                item {
                    Column {
                        val logRow: @Composable () -> Unit = {
                            LogRow(
                                refinedLog = refinedLog,
                                preferences = preferences,
                                header = header,
                                selected = refinedLog == logSelection?.refinedLog,
                                divider = divider,
                                collapseJsonDetection = collapseJsonDetection,
                                expandJsonDetection = expandJsonDetection,
                                setMark = setMark,
                                deleteMark = deleteMark,
                                selectLog = selectLog
                            )
                        }
                        val mark =
                            refinedLog.detectionFinishedLog.detections[DetectorKey.Mark]?.firstOrNull() as? MarkDetection
                        if (mark != null) {
                            Column(
                                modifier = Modifier.fillMaxWidth().wrapContentHeight().background(mark.color)
                                    .padding(start = 6.dp, top = 0.dp, end = 6.dp, bottom = 6.dp)
                            ) {
                                Text(
                                    text = mark.note,
                                    modifier = Modifier.wrapContentHeight().align(Alignment.CenterHorizontally)
                                        .padding(8.dp),
                                    color = Color.Black,
                                    style = MaterialTheme.typography.h5,
                                )
                                Box(modifier = Modifier.background(MaterialTheme.colors.background)) {
                                    logRow()
                                }
                            }
                        } else {
                            logRow()
                        }
                        Divider()
                    }
                }
            }
        }
        val adapter = rememberScrollbarAdapter(listState)

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd),
            adapter = adapter,
        )

        var isScrolling by remember { mutableStateOf(false) }
        LaunchedEffect(listState.firstVisibleItemScrollOffset) {
            isScrolling = true
            delay(1000)
            isScrolling = false
        }

        AnimatedVisibility(
            visible = isScrolling,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            val width = listState.layoutInfo.viewportSize.width
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(start = (width - 120).dp)
            ) {
                markedRows.forEach {
                    val y =
                        it.detectionFinishedLog.log.index.toFloat() / listState.layoutInfo.totalItemsCount.toFloat() * listState.layoutInfo.viewportSize.height
                    val mark = it.detectionFinishedLog.detections[DetectorKey.Mark]?.firstOrNull() as MarkDetection
                    Box (
                        modifier = Modifier.fillMaxWidth().height(30.dp)
                            .offset(y = y.toInt().dp)
                            .background(mark.color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = mark.note,
                            color = Color.Black,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // markedRow

    }
}
