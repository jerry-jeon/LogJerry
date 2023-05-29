package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jerryjeon.logjerry.detection.DetectionManager
import com.jerryjeon.logjerry.detector.DetectionSelections
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.detector.KeywordDetectionView
import com.jerryjeon.logjerry.filter.FilterManager
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FilterAndDetectionView(
    preferences: Preferences,
    filterManager: FilterManager,
    detectorManager: DetectorManager,
    detectionManager: DetectionManager,
    openNewTab: (StateFlow<List<Log>>) -> Unit,
) {
    val selections by detectionManager.selections.collectAsState()
    val keywordDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Keyword)
    val keywordDetectionRequest by detectorManager.keywordDetectionRequestFlow.collectAsState()
    Column {
        Row(modifier = Modifier.padding(16.dp)) {
            FilterView(filterManager)
            DetectionView(preferences, detectorManager, detectionManager, selections, openNewTab)
        }

        Box(modifier = Modifier.fillMaxWidth()) {
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
    }
}
@Composable
fun FilterView(
    filterManager: FilterManager,
) {
    val textFilters by filterManager.textFiltersFlow.collectAsState()
    val priorityFilters by filterManager.priorityFilterFlow.collectAsState()

    TextFilterView(textFilters, filterManager::addTextFilter, filterManager::removeTextFilter)
    Spacer(Modifier.width(16.dp))
    PriorityFilterView(priorityFilters, filterManager::setPriorityFilter)
    Spacer(Modifier.width(16.dp))
}

@Composable
fun RowScope.DetectionView(
    preferences: Preferences,
    detectorManager: DetectorManager,
    detectionManager: DetectionManager,
    selections: DetectionSelections?,
    openNewTab: (StateFlow<List<Log>>) -> Unit
) {
    val exceptionDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Exception)
    val jsonDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Json)
    val markDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Mark)

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
