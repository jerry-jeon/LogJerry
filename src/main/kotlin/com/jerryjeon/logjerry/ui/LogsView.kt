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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jerryjeon.logjerry.ColumnDivider
import com.jerryjeon.logjerry.HeaderDivider
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.log.ParseCompleted
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.logview.RefineResult
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.mark.LogMark
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.focus.DetectionFocus
import com.jerryjeon.logjerry.ui.focus.KeyboardFocus
import com.jerryjeon.logjerry.ui.focus.LogFocus
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LogsView(
    refineResult: RefineResult,
    parseCompleted: ParseCompleted,
    preferences: Preferences,
    detectorManager: DetectorManager,
    header: Header,
    focusRequester: FocusRequester,
    hide: (logIndex: Int) -> Unit
) {
    val listState = rememberLazyListState()
    LaunchedEffect(refineResult) {
        refineResult.currentFocus.collectLatest {
            if (it == null) return@collectLatest
            val headerCount = 2
            val exactPosition = it.index + headerCount

            when (it) {
                is DetectionFocus -> {
                    listState.scrollToItem(exactPosition)
                }

                is KeyboardFocus -> {
                    // TODO Seems like inefficient... :(
                    if (exactPosition < listState.firstVisibleItemIndex) {
                        listState.scrollToItem(exactPosition)
                    } else {
                        val viewportHeight = listState.layoutInfo.viewportSize.height
                        val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        if (exactPosition > lastVisibleItemIndex) {
                            listState.scrollToItem(exactPosition, scrollOffset = -viewportHeight + 200)
                        }
                    }
                }
            }
        }
    }

    val filteredSize = refineResult.refinedLogs.size
    val totalSize = parseCompleted.originalLogsFlow.value.size
    val filteredSizeText =
        (if (filteredSize != totalSize) "Filtered size : $filteredSize, " else "")
    Text("${filteredSizeText}Total : $totalSize", modifier = Modifier.padding(8.dp))

    LogsView(
        preferences = preferences,
        refinedLogs = refineResult.refinedLogs,
        detectorManager = detectorManager,
        header = header,
        listState = listState,
        markedRows = refineResult.markedRows,
        setMark = detectorManager::setMark,
        deleteMark = detectorManager::deleteMark,
        hide = hide,
        changeFocus = { refineResult.currentFocus.value = it }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun LogsView(
    modifier: Modifier = Modifier,
    refinedLogs: List<RefinedLog>,
    detectorManager: DetectorManager,
    preferences: Preferences,
    header: Header,
    listState: LazyListState,
    markedRows: List<RefinedLog>,
    setMark: (logMark: LogMark) -> Unit,
    deleteMark: (logIndex: Int) -> Unit,
    hide: (logIndex: Int) -> Unit,
    changeFocus: (LogFocus?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val showMarkDialog = remember { mutableStateOf<RefinedLog?>(null) }
    // TODO move to other class
    var selectedLog by remember { mutableStateOf<LogSelection?>(null) }

    fun LogSelection.next(): LogSelection {
        val nextIndex = (this.index + 1).coerceAtMost(refinedLogs.lastIndex)
        val nextLog = refinedLogs[nextIndex]
        return LogSelection(nextLog, nextIndex)
    }

    fun LogSelection.prev(): LogSelection {
        val nextIndex = (this.index - 1).coerceAtLeast(0)
        val nextLog = refinedLogs[nextIndex]
        return LogSelection(nextLog, nextIndex)
    }

    MarkDialog(showMarkDialog, detectorManager::setMark)

    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    Column(
        modifier = modifier.fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.DirectionDown && keyEvent.type == KeyEventType.KeyDown -> {
                        if (refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        val currentLog = selectedLog
                        val nextLog = currentLog?.next() ?: LogSelection(refinedLogs.first(), 0)
                        selectedLog = nextLog
                        changeFocus(KeyboardFocus(nextLog.index))
                        true
                    }

                    keyEvent.key == Key.DirectionUp && keyEvent.type == KeyEventType.KeyDown -> {
                        if (refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        selectedLog = selectedLog?.prev()
                        changeFocus(selectedLog?.index?.let { KeyboardFocus(it) })
                        true
                    }

                    keyEvent.key == Key.MoveEnd && keyEvent.type == KeyEventType.KeyDown -> {
                        if (refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        scope.launch {
                            val lastIndex = refinedLogs.lastIndex
                            listState.scrollToItem(lastIndex)
                        }
                        true
                    }

                    keyEvent.key == Key.MoveHome && keyEvent.type == KeyEventType.KeyDown -> {
                        if (refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        scope.launch {
                            listState.scrollToItem(0)
                        }
                        true
                    }

                    keyEvent.key == Key.PageDown && keyEvent.type == KeyEventType.KeyDown -> {
                        if (refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        scope.launch {
                            listState.scrollBy(listState.layoutInfo.viewportSize.height.toFloat())
                        }
                        true
                    }

                    keyEvent.key == Key.PageUp && keyEvent.type == KeyEventType.KeyDown -> {
                        if (refinedLogs.isEmpty()) return@onPreviewKeyEvent false
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
        Box {
            LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                item { HeaderRow(header, divider) }
                item { HeaderDivider() }
                refinedLogs.forEach { refinedLog ->
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
                                    hide = hide,
                                    selectLog = {
                                        selectedLog = LogSelection(it, refinedLogs.indexOf(it))
                                    }
                                )
                            }
                            if (refinedLog.mark != null) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                                        .background(refinedLog.mark.color)
                                        .padding(start = 6.dp, top = 0.dp, end = 6.dp, bottom = 6.dp)
                                ) {
                                    Text(
                                        text = refinedLog.mark.note,
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
                            it.log.index.toFloat() / listState.layoutInfo.totalItemsCount.toFloat() * listState.layoutInfo.viewportSize.height
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
