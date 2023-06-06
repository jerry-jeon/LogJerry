@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
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
import com.jerryjeon.logjerry.filter.PriorityFilter
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
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            val statusByKey by refineResult.statusByKey.collectAsState()
            Row(
                modifier = Modifier
                    .padding(12.dp)
                    .height(IntrinsicSize.Min)
            ) {
                FilterView(filterManager)
                Spacer(Modifier.width(8.dp))
                statusByKey[DetectorKey.Json]?.let {
                    JsonDetectionView(
                        modifier = Modifier.fillMaxHeight(),
                        detectionStatus = it,
                        moveToPreviousOccurrence = refineResult::selectPreviousDetection,
                        moveToNextOccurrence = refineResult::selectNextDetection,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val keywordDetectionRequest by detectorManager.keywordDetectionRequestFlow.collectAsState()
            KeywordDetectionView(
                keywordDetectionRequest = keywordDetectionRequest,
                detectionStatus = statusByKey[DetectorKey.Keyword],
                find = detectorManager::findKeyword,
                setFindEnabled = detectorManager::setKeywordDetectionEnabled,
                moveToPreviousOccurrence = refineResult::selectPreviousDetection,
                moveToNextOccurrence = refineResult::selectNextDetection,
            )
        }

        val textFilters by filterManager.textFiltersFlow.collectAsState()
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            textFilters.chunked(2).forEach {
                Row {
                    it.forEach { filter ->
                        AppliedTextFilter(filter, filterManager::removeTextFilter)
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))

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

@Composable
private fun FilterView(filterManager: FilterManager) {
    var showTextFilterPopup by remember { mutableStateOf(false) }
    var textFilterAnchor by remember { mutableStateOf(Offset.Zero) }
    var showLogLevelPopup by remember { mutableStateOf(false) }
    var logLevelAnchor by remember { mutableStateOf(Offset.Zero) }
    val priorityFilter by filterManager.priorityFilterFlow.collectAsState()

    OutlinedButton(
        onClick = {
            showTextFilterPopup = true
        },
        modifier = Modifier
            .height(48.dp)
            .onGloballyPositioned { coordinates ->
                textFilterAnchor = coordinates.positionInRoot()
            },
    ) {
        Text("Add Filter")
    }

    Spacer(Modifier.width(8.dp))

    OutlinedButton(
        onClick = {
            showLogLevelPopup = true
        },
        modifier = Modifier
            .height(48.dp)
            .onGloballyPositioned { coordinates ->
                logLevelAnchor = coordinates.positionInRoot()
            },
    ) {
        Text("Log Level | ${priorityFilter.priority.name}")
    }

    if (showTextFilterPopup) {
        Popup(
            onDismissRequest = { showTextFilterPopup = false },
            focusable = true,
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    val additionalMargin = 10
                    return IntOffset(textFilterAnchor.x.toInt(), (textFilterAnchor.y + anchorBounds.height + additionalMargin).toInt())
                }
            }
        ) {
            TextFilterView(
                filterManager::addTextFilter
            ) { showTextFilterPopup = false }
        }
    }

    PriorityFilterPopup(
        priorityFilter,
        logLevelAnchor,
        showLogLevelPopup,
        dismissPopup = { showLogLevelPopup = false },
        setPriorityFilter = filterManager::setPriorityFilter
    )
}

@Composable
private fun PriorityFilterPopup(
    priorityFilter: PriorityFilter,
    anchor: Offset,
    showPopup: Boolean,
    dismissPopup: () -> Unit,
    setPriorityFilter: (PriorityFilter) -> Unit
) {
    if (showPopup) {
        Popup(
            onDismissRequest = dismissPopup,
            focusable = true,
            popupPositionProvider = object : PopupPositionProvider {
                override fun calculatePosition(
                    anchorBounds: IntRect,
                    windowSize: IntSize,
                    layoutDirection: LayoutDirection,
                    popupContentSize: IntSize
                ): IntOffset {
                    return IntOffset(anchor.x.toInt(), (anchor.y + anchorBounds.height + 10).toInt())
                }
            }
        ) {
            PriorityFilterView(priorityFilter, setPriorityFilter)
        }
    }
}
