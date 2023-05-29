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
import com.jerryjeon.logjerry.log.LogManager
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.ui.focus.DetectionFocus
import com.jerryjeon.logjerry.ui.focus.KeyboardFocus
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest

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
    val detectorManager = logManager.detectorManager
    val keywordDetectionRequest by detectorManager.keywordDetectionRequestFlow.collectAsState()
    val detectionManager = logManager.detectionManager
    val logViewManager = logManager.logViewManager

    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .focusRequester(focusRequester),
    ) {
        InvalidSentences()
        FilterView(
            filterManager,
            preferences,
            detectionManager,
            openNewTab,
            detectorManager,
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

        val investigationView by logViewManager.investigationViewFlow.collectAsState()
        val markedRows = investigationView.refinedLogs.filter { it.marked } // TODO omg performance would be bad
        // val markedRows by detectorManager.markedRowsFlow.collectAsState()

        LogsView(
            preferences = preferences,
            investigationView = investigationView,
            logManager = logManager,
            detectorManager = detectorManager,
            header = header,
            logs = investigationView.refinedLogs,
            listState = listState,
            markedRows = markedRows,
            setMark = detectorManager::setMark,
            deleteMark = detectorManager::deleteMark,
        )

        LaunchedEffect(logManager) {
            focusRequester.requestFocus()
        }
    }
}
