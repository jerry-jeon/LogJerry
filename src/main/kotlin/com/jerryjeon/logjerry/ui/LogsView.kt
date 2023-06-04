@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.ColumnDivider
import com.jerryjeon.logjerry.HeaderDivider
import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.log.ParseCompleted
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.logview.MarkInfo
import com.jerryjeon.logjerry.logview.RefineResult
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.mark.LogMark
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.focus.DetectionFocus
import com.jerryjeon.logjerry.ui.focus.KeyboardFocus
import com.jerryjeon.logjerry.ui.focus.LogFocus
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun LogsView(
    refineResult: RefineResult,
    parseCompleted: ParseCompleted,
    preferences: Preferences,
    detectorManager: DetectorManager,
    header: Header,
    hide: (logIndex: Int) -> Unit,
    moveToPreviousMark: () -> Unit,
    moveToNextMark: () -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(refineResult.currentFocus) {
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

    Box {
        LogsView(
            modifier = Modifier.fillMaxWidth(),
            endPadding = 120.dp,
            preferences = preferences,
            refinedLogs = refineResult.refinedLogs,
            detectorManager = detectorManager,
            header = header,
            listState = listState,
            markInfos = refineResult.markInfos,
            setMark = detectorManager::setMark,
            deleteMark = detectorManager::deleteMark,
            hide = hide,
            changeFocus = { refineResult.currentFocus.value = it },
            moveToPreviousMark = moveToPreviousMark,
            moveToNextMark = moveToNextMark,
            selectDetection = refineResult::selectDetection,
        )
    }
}

@Composable
fun LogsView(
    modifier: Modifier = Modifier,
    endPadding: Dp,
    refinedLogs: List<RefinedLog>,
    detectorManager: DetectorManager,
    preferences: Preferences,
    header: Header,
    listState: LazyListState,
    markInfos: List<MarkInfo>,
    setMark: (logMark: LogMark) -> Unit,
    deleteMark: (logIndex: Int) -> Unit,
    hide: (logIndex: Int) -> Unit,
    changeFocus: (LogFocus?) -> Unit,
    moveToPreviousMark: () -> Unit,
    moveToNextMark: () -> Unit,
    selectDetection: (Detection) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
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
        modifier = modifier
            .fillMaxSize()
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

                    keyEvent.key == Key.Backspace && keyEvent.type == KeyEventType.KeyDown -> {
                        selectedLog?.refinedLog?.log?.index?.let(hide)
                        true
                    }

                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.LeftBracket && keyEvent.type == KeyEventType.KeyDown -> {
                        moveToPreviousMark()
                        true
                    }

                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.RightBracket && keyEvent.type == KeyEventType.KeyDown -> {
                        moveToNextMark()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
            .focusRequester(focusRequester)
            .focusable()
    ) {
        Box {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(end = endPadding), state = listState) {
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
                        }
                    }
                }
            }
            val adapter = rememberScrollbarAdapter(listState)

            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd),
                adapter = adapter,
            )

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .padding(end = LocalScrollbarStyle.current.thickness)
                    .align(Alignment.TopEnd)
            ) {
                MarkView(markInfos, listState.layoutInfo.viewportSize.height, refinedLogs.size, selectDetection)
            }
        }
    }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun MarkView(
    markInfos: List<MarkInfo>,
    viewportHeight: Int,
    refinedLogsSize: Int,
    selectDetection: (Detection) -> Unit,
) {
    val minHeight = 40
    val minRatio = minHeight.toFloat() / viewportHeight.toFloat()

    Column(modifier = Modifier.fillMaxSize()) {
        markInfos.forEach {
            when (it) {
                is MarkInfo.Marked -> {
                    val mark = it.markedLog.mark!!
                    Box(
                        modifier = Modifier.fillMaxWidth().height(60.dp).background(mark.color)
                            .clickable { selectDetection(mark) },
                    ) {
                        Text(
                            mark.note,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center,
                            color = Color.Black,
                        )
                    }
                }

                is MarkInfo.StatBetweenMarks -> {
                    val ratio = it.logCount.toFloat() / refinedLogsSize.toFloat() / markInfos.size
                    val baseModifier = if (ratio < minRatio) {
                        Modifier.height(minHeight.dp)
                    } else {
                        Modifier.weight(ratio)
                    }
                    Box(
                        modifier = baseModifier.fillMaxWidth()
                    ) {
                        DashedDivider(
                            modifier = Modifier.fillMaxHeight().align(Alignment.Center),
                            thickness = 2.dp,
                            color = Color(0x22888888)
                        )
                        Text(
                            text = "${it.logCount} logs, ${it.duration}",
                            modifier = Modifier.padding(vertical = 12.dp).align(Alignment.Center).background(MaterialTheme.colors.background),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }

    /* TODO it seems not useful..
    var isScrolling by remember { mutableStateOf(false) }
    LaunchedEffect(listState.firstVisibleItemScrollOffset) {
        isScrolling = true
        delay(1000)
        isScrolling = false
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isScrolling,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isScrolling,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
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
     */
}

@Composable
fun DashedDivider(
    thickness: Dp,
    color: Color = MaterialTheme.colors.onSurface,
    phase: Float = 10f,
    intervals: FloatArray = floatArrayOf(20f, 25f),
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val dividerHeight = thickness.toPx()
        drawRoundRect(
            color = color,
            style = Stroke(
                width = dividerHeight,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = intervals,
                    phase = phase
                )
            )
        )
    }
}
