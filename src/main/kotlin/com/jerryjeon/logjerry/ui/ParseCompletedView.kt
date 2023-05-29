@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.ParseCompleted
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.focus.DetectionFocus
import com.jerryjeon.logjerry.ui.focus.KeyboardFocus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ParseCompletedView(
    preferences: Preferences,
    header: Header,
    parseCompleted: ParseCompleted,
    openNewTab: (StateFlow<List<Log>>) -> Unit,
    InvalidSentences: @Composable () -> Unit
) {
    val filters = parseCompleted.filters
    val detectors = parseCompleted.detectors
    val keywordDetectionRequest by detectors.keywordDetectionRequestFlow.collectAsState()
    val detections = parseCompleted.detections
    val logViewManager = parseCompleted.logViewManager

    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .focusRequester(focusRequester),
    ) {
        InvalidSentences()
        FilterView(
            filters,
            preferences,
            detections,
            openNewTab,
            detectors,
            keywordDetectionRequest,
        )

/*
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
*/

        LaunchedEffect(Unit) {
            parseCompleted.currentFocus.collectLatest {
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

        val investigationView by logViewManager.investigationViewFlow.collectAsState()
        val markedRows = investigationView.refinedLogs.filter { it.marked } // TODO omg performance would be bad
        // val markedRows by detectorManager.markedRowsFlow.collectAsState()

        LogsView(
            preferences = preferences,
            investigationView = investigationView,
            parseCompleted = parseCompleted,
            detectors = detectors,
            header = header,
            logs = investigationView.refinedLogs,
            listState = listState,
            markedRows = markedRows,
            setMark = detectors::setMark,
            deleteMark = detectors::deleteMark,
        )

        LaunchedEffect(parseCompleted) {
            focusRequester.requestFocus()
        }
    }
}
