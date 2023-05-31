@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.flow.StateFlow

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
    val refineResult by parseCompleted.refineResultFlow.collectAsState()
    Column(
        modifier = Modifier
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

        LogsView(refineResult, parseCompleted, preferences, detectorManager, header, filterManager::hide)
    }
}

