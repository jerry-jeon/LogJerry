@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.KeywordDetectionView
import com.jerryjeon.logjerry.filter.FilterManager
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
) {
    val filterManager = parseCompleted.filterManager
    val detectorManager = parseCompleted.detectorManager
    val refineResult by parseCompleted.refineResultFlow.collectAsState()
    Column(
        modifier = Modifier
    ) {
        Row {
            TextFilterPopup(filterManager)
        }

        val textFilters by filterManager.textFiltersFlow.collectAsState()
        Column {
            textFilters.chunked(2).forEach {
                Row {
                    it.forEach { filter ->
                        AppliedTextFilter(filter, filterManager::removeTextFilter)
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }

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

        LogsView(
            refineResult = refineResult,
            parseCompleted = parseCompleted,
            preferences = preferences,
            detectorManager = detectorManager,
            header = header,
            hide = filterManager::hide,
            moveToPreviousMark = { refineResult.selectPreviousDetection(DetectorKey.Mark) },
            moveToNextMark = { refineResult.selectNextDetection(DetectorKey.Mark) }
        )
    }
}

private fun TextFilterPopup(filterManager: FilterManager) {
    var showPopup by remember { mutableStateOf(false) }
    var anchor by remember { mutableStateOf(Offset.Zero) }
    OutlinedButton(
        onClick = {
            showPopup = true
        },
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                anchor = coordinates.positionInRoot()
            },
    ) {
        Text("Add Filter")
    }

    if (showPopup) {
        Popup(
            onDismissRequest = { showPopup = false },
            focusable = true,
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    return IntOffset(anchor.x.toInt(), (anchor.y + anchorBounds.height).toInt())
                }
            }
        ) {
            TextFilterView(
                filterManager::addTextFilter
            ) { showPopup = false }
        }
    }
}
