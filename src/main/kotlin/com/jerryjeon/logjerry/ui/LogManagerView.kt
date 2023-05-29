@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.KeywordDetectionView
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogManager
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.focus.DetectionFocus
import com.jerryjeon.logjerry.ui.focus.KeyboardFocus
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// TODO Consider intuitive name
@Composable
fun LogManagerView(
    preferences: Preferences,
    header: Header,
    logManager: LogManager,
    openNewTab: (StateFlow<List<Log>>) -> Unit,
    InvalidSentences: @Composable () -> Unit
) {
    val filterManager = logManager.filterManager

    val logViewManager = logManager.logViewManager
    val investigationView by logViewManager.investigationViewFlow.collectAsState()

    val detectorManager = logManager.detectorManager
    val keywordDetectionRequest by detectorManager.keywordDetectionRequestFlow.collectAsState()

    // TODO Find way to abstract these
    val detectionManager = logManager.detectionManager
    val selections by detectionManager.selections.collectAsState()
    val keywordDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Keyword)
    val exceptionDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Exception)
    val jsonDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Json)
    val markDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Mark)

    val textFilters by filterManager.textFiltersFlow.collectAsState()
    val priorityFilters by filterManager.priorityFilterFlow.collectAsState()

    val filteredSize = investigationView.refinedLogs.size
    val totalSize = logManager.originalLogsFlow.value.size
    val filteredSizeText =
        (if (filteredSize != totalSize) "Filtered size : $filteredSize, " else "")

    val markedRows = investigationView.refinedLogs.filter { it.marked } // TODO omg performance would be bad
    // val markedRows by detectorManager.markedRowsFlow.collectAsState()

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

    val listState = rememberLazyListState()

    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .onPreviewKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.DirectionDown && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        val currentLog = selectedLog
                        val nextLog = currentLog?.next() ?: LogSelection(investigationView.refinedLogs.first(), 0)
                        selectedLog = nextLog
                        logManager.currentFocus.value = KeyboardFocus(nextLog.index)
                        true
                    }

                    keyEvent.key == Key.DirectionUp && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        selectedLog = selectedLog?.prev()
                        logManager.currentFocus.value = selectedLog?.index?.let { KeyboardFocus(it) }
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
            .focusRequester(focusRequester),
    ) {

        InvalidSentences()
        Row(modifier = Modifier.padding(16.dp)) {
            TextFilterView(textFilters, filterManager::addTextFilter, filterManager::removeTextFilter)
            Spacer(Modifier.width(16.dp))
            PriorityFilterView(priorityFilters, filterManager::setPriorityFilter)
            Spacer(Modifier.width(16.dp))
            Box(modifier = Modifier.weight(0.5f).border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))) {
                Column {
                    Text("Auto-detection", modifier = Modifier.padding(8.dp))
                    Divider()
                    Row {
                        if (preferences.showExceptionDetection) {
                            ExceptionDetectionView(
                                Modifier.width(200.dp).wrapContentHeight(),
                                exceptionDetectionSelection,
                                { detectionManager.selectPreviousDetection(DetectorKey.Exception, it) },
                                { detectionManager.selectNextDetection(DetectorKey.Exception, it) },
                            )

                            Spacer(Modifier.width(8.dp))
                            Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                            Spacer(Modifier.width(8.dp))
                        }

                        JsonDetectionView(
                            Modifier.width(200.dp).wrapContentHeight(),
                            jsonDetectionSelection,
                            { detectionManager.selectPreviousDetection(DetectorKey.Json, it) },
                            { detectionManager.selectNextDetection(DetectorKey.Json, it) },
                        )

                        Spacer(Modifier.width(8.dp))
                        Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                        Spacer(Modifier.width(8.dp))

                        MarkDetectionView(
                            Modifier.width(200.dp).wrapContentHeight(),
                            markDetectionSelection,
                            { detectionManager.selectPreviousDetection(DetectorKey.Mark, it) },
                            { detectionManager.selectNextDetection(DetectorKey.Mark, it) },
                            { openNewTab(detectorManager.markedRowsFlow) }
                        )

                        Spacer(Modifier.width(8.dp))
                        Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            Text("${filteredSizeText}Total : $totalSize", modifier = Modifier.padding(8.dp))
            KeywordDetectionView(
                Modifier.align(Alignment.BottomEnd),
                keywordDetectionRequest,
                keywordDetectionSelection,
                detectorManager::findKeyword,
                detectorManager::setKeywordDetectionEnabled,
                { detectionManager.selectPreviousDetection(DetectorKey.Keyword, it) },
                { detectionManager.selectNextDetection(DetectorKey.Keyword, it) },
            )
        }
        Divider(color = Color.Black)

        LaunchedEffect(selections?.active) {
            // TODO Reposition would happen again, which is unnecessary
            val index = investigationView.refinedLogs.indexOfFirst { refinedLog ->
                refinedLog.detectionFinishedLog.log.index == selections?.active?.selected?.logIndex
            }
            if (index != -1) {
                selectedLog = LogSelection(investigationView.refinedLogs[index], index)
                logManager.currentFocus.value = DetectionFocus(index)
            }
        }

        LaunchedEffect(Unit) {
            logManager.currentFocus.collectLatest {
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

        LogsView(
            preferences = preferences,
            header = header,
            logs = investigationView.refinedLogs,
            logSelection = selectedLog,
            listState = listState,
            markedRows = markedRows,
            collapseJsonDetection = logViewManager::collapseJsonDetection,
            expandJsonDetection = logViewManager::expandJsonDetection,
            setMark = detectorManager::setMark,
            deleteMark = detectorManager::deleteMark,
            selectLog = { selectedLog = LogSelection(it, investigationView.refinedLogs.indexOf(it)) }
        )

        MarkDialog(showMarkDialog, detectorManager::setMark)

        LaunchedEffect(logManager) {
            focusRequester.requestFocus()
        }
    }
}
