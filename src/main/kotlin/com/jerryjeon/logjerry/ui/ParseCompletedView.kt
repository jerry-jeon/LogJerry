@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.KeywordDetectionView
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
    val filterManager = parseCompleted.filterManager
    val detectorManager = parseCompleted.detectorManager
    val focusRequester = remember { FocusRequester() }
    val refineResult by parseCompleted.refineResultFlow.collectAsState()
    Column(
        modifier = Modifier
            .focusRequester(focusRequester),
    ) {
        InvalidSentences()
        Column {
            val keywordDetectionRequest by detectorManager.keywordDetectionRequestFlow.collectAsState()
            val statusByKey by refineResult.statusByKey.collectAsState()
            Row(modifier = Modifier.padding(16.dp)) {
                FilterView(filterManager)
                DetectionView(
                    preferences,
                    detectorManager,
                    statusByKey,
                    openNewTab,
                    refineResult::selectPreviousDetection,
                    refineResult::selectNextDetection,
                )
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                KeywordDetectionView(
                    Modifier.align(Alignment.BottomEnd),
                    keywordDetectionRequest,
                    statusByKey[DetectorKey.Keyword],
                    detectorManager::findKeyword,
                    detectorManager::setKeywordDetectionEnabled,
                    refineResult::selectPreviousDetection,
                    refineResult::selectNextDetection,
                )
            }
        }
        val listState = rememberLazyListState()
        LaunchedEffect(Unit) {
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
            changeFocus = { refineResult.currentFocus.value = it }
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}
