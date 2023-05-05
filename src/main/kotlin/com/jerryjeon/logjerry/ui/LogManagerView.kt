@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.KeywordDetectionView
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogManager
import com.jerryjeon.logjerry.logview.LogSelection
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import kotlinx.coroutines.flow.StateFlow

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
    val keywordDetectionFocus by detectionManager.keywordDetectionFocus.collectAsState()
    val exceptionDetectionFocus by detectionManager.exceptionDetectionFocus.collectAsState()
    val jsonDetectionFocus by detectionManager.jsonDetectionFocus.collectAsState()
    val markDetectionFocus by detectionManager.markDetectionFocus.collectAsState()
    val activeDetectionFocus by detectionManager.activeDetectionFocusFlowState.collectAsState()

    val textFilters by filterManager.textFiltersFlow.collectAsState()
    val priorityFilters by filterManager.priorityFilterFlow.collectAsState()

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
                    ExceptionDetectionView(
                        Modifier.width(200.dp).wrapContentHeight(),
                        exceptionDetectionFocus,
                        { detectionManager.focusPreviousDetection(DetectorKey.Exception, it) },
                        { detectionManager.focusNextDetection(DetectorKey.Exception, it) },
                    )

                    Spacer(Modifier.width(8.dp))
                    Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                    Spacer(Modifier.width(8.dp))

                    JsonDetectionView(
                        Modifier.width(200.dp).wrapContentHeight(),
                        jsonDetectionFocus,
                        { detectionManager.focusPreviousDetection(DetectorKey.Json, it) },
                        { detectionManager.focusNextDetection(DetectorKey.Json, it) },
                    )

                    Spacer(Modifier.width(8.dp))
                    Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                    Spacer(Modifier.width(8.dp))

                    MarkDetectionView(
                        Modifier.width(200.dp).wrapContentHeight(),
                        markDetectionFocus,
                        { detectionManager.focusPreviousDetection(DetectorKey.Mark, it) },
                        { detectionManager.focusNextDetection(DetectorKey.Mark, it) },
                        { openNewTab(detectorManager.markedRowsFlow) }
                    )

                    Spacer(Modifier.width(8.dp))
                    Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                }
            }
        }
    }

    val filteredSize = investigationView.refinedLogs.size
    val totalSize = logManager.originalLogsFlow.value.size
    val filteredSizeText =
        (if (filteredSize != totalSize) "Filtered size : $filteredSize, " else "")
    Box(modifier = Modifier.fillMaxWidth()) {
        Text("${filteredSizeText}Total : $totalSize", modifier = Modifier.padding(8.dp))
        KeywordDetectionView(
            Modifier.align(Alignment.BottomEnd),
            keywordDetectionRequest,
            keywordDetectionFocus,
            detectorManager::findKeyword,
            detectorManager::setKeywordDetectionEnabled,
            { detectionManager.focusPreviousDetection(DetectorKey.Keyword, it) },
            { detectionManager.focusNextDetection(DetectorKey.Keyword, it) },
        )
    }
    Divider(color = Color.Black)

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

    LogsView(
        modifier = Modifier
            .onPreviewKeyEvent { keyEvent ->
                when {
                    keyEvent.key == Key.DirectionDown && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        selectedLog = selectedLog?.next()
                        true
                    }
                    keyEvent.key == Key.DirectionUp && keyEvent.type == KeyEventType.KeyDown -> {
                        if (investigationView.refinedLogs.isEmpty()) return@onPreviewKeyEvent false
                        selectedLog = selectedLog?.prev()
                        true
                    }
                    else -> {
                        false
                    }
                }
            },
        preferences = preferences,
        header = header,
        logs = investigationView.refinedLogs,
        detectionFocus = activeDetectionFocus,
        logSelection = selectedLog,
        collapseJsonDetection = logViewManager::collapseJsonDetection,
        expandJsonDetection = logViewManager::expandJsonDetection,
        toggleMark = detectorManager::toggleMark,
        selectLog = { selectedLog = LogSelection(it, investigationView.refinedLogs.indexOf(it)) }
    )
}
