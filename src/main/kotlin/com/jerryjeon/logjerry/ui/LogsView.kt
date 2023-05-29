@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.ColumnDivider
import com.jerryjeon.logjerry.HeaderDivider
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.Detectors
import com.jerryjeon.logjerry.detector.MarkDetection
import com.jerryjeon.logjerry.log.ParseCompleted
import com.jerryjeon.logjerry.logview.InvestigationView
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.mark.LogMark
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.focus.KeyboardFocus
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LogsView(
    modifier: Modifier = Modifier,
    investigationView: InvestigationView,
    parseCompleted: ParseCompleted,
    detectors: Detectors,
    preferences: Preferences,
    header: Header,
    logs: List<RefinedLog>,
    listState: LazyListState,
    markedRows: List<RefinedLog>,
    setMark: (logMark: LogMark) -> Unit,
    deleteMark: (logIndex: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val showMarkDialog = remember { mutableStateOf<RefinedLog?>(null) }
    // TODO move to other class
    var selectedLog by remember { mutableStateOf<LogSelection?>(null) }

    fun LogSelection.next(): LogSelection {
        val nextIndex = (this.index + 1).coerceAtMost(investigationView.refinedLogs.lastIndex)
        val nextLog = investigationView.refinedLogs[nextIndex]
        return LogSelection(nextLog, nextIndex)
    }

    fun LogSelection.prev(): LogSelection {
        val nextIndex = (this.index - 1).coerceAtLeast(0)
        val nextLog = investigationView.refinedLogs[nextIndex]
        return LogSelection(nextLog, nextIndex)
    }

    MarkDialog(showMarkDialog, detectors::setMark)

    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    Column(
        modifier = modifier.fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.DirectionDown && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        val currentLog = selectedLog
                        val nextLog = currentLog?.next() ?: LogSelection(investigationView.refinedLogs.first(), 0)
                        selectedLog = nextLog
                        parseCompleted.currentFocus.value = KeyboardFocus(nextLog.index)
                        true
                    }

                    keyEvent.key == Key.DirectionUp && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        selectedLog = selectedLog?.prev()
                        parseCompleted.currentFocus.value = selectedLog?.index?.let { KeyboardFocus(it) }
                        true
                    }

                    keyEvent.key == Key.MoveEnd && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        scope.launch {
                            val lastIndex = investigationView.refinedLogs.lastIndex
                            listState.scrollToItem(lastIndex)
                        }
                        true
                    }

                    keyEvent.key == Key.MoveHome && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        scope.launch {
                            listState.scrollToItem(0)
                        }
                        true
                    }

                    keyEvent.key == Key.PageDown && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        scope.launch {
                            listState.scrollBy(listState.layoutInfo.viewportSize.height.toFloat())
                        }
                        true
                    }

                    keyEvent.key == Key.PageUp && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        scope.launch {
                            listState.scrollBy(-listState.layoutInfo.viewportSize.height.toFloat())
                        }
                        true
                    }

                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.M && keyEvent.type == KeyEventType.KeyDown -> {
                        showMarkDialog.value = selectedLog?.refinedLog
                        true
                    }

                    else -> {
                        false
                    }
                }
            }

    ) {
        // TODO hmm..
        val filteredSize = investigationView.refinedLogs.size
        val totalSize = parseCompleted.originalLogsFlow.value.size
        val filteredSizeText =
            (if (filteredSize != totalSize) "Filtered size : $filteredSize, " else "")
        Text("${filteredSizeText}Total : $totalSize", modifier = Modifier.padding(8.dp))

        Divider(color = Color.Black)

        Box {
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
                                    selected = refinedLog == selectedLog?.refinedLog,
                                    divider = divider,
                                    setMark = setMark,
                                    deleteMark = deleteMark,
                                    selectLog = {
                                        selectedLog = LogSelection(it, investigationView.refinedLogs.indexOf(it))
                                    }
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

            // TODO why this should be annotated
            this@Column.AnimatedVisibility(
                visible = isScrolling,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                val width = listState.layoutInfo.viewportSize.width
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = (width - 120).dp)
                ) {
                    markedRows.forEach {
                        val y =
                            it.detectionFinishedLog.log.index.toFloat() / listState.layoutInfo.totalItemsCount.toFloat() * listState.layoutInfo.viewportSize.height
                        Box(
                            modifier = Modifier.fillMaxWidth().height(30.dp)
                                .offset(y = y.toInt().dp)
                                .background(it.mark!!.color),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = it.mark.note,
                                color = Color.Black,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
