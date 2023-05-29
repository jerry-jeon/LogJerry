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
import com.jerryjeon.logjerry.detection.Detections
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.Detectors
import com.jerryjeon.logjerry.detector.KeywordDetectionRequest
import com.jerryjeon.logjerry.detector.KeywordDetectionView
import com.jerryjeon.logjerry.filter.Filters
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.flow.StateFlow

@Composable
fun FilterView(
    filters: Filters,
    preferences: Preferences,
    detections: Detections,
    openNewTab: (StateFlow<List<Log>>) -> Unit,
    detectors: Detectors,
    keywordDetectionRequest: KeywordDetectionRequest,
) {
    val textFilters by filters.textFiltersFlow.collectAsState()
    val priorityFilters by filters.priorityFilterFlow.collectAsState()

    val selections by detections.selections.collectAsState()
    val keywordDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Keyword)
    val exceptionDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Exception)
    val jsonDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Json)
    val markDetectionSelection = selections?.selectionByKey?.get(DetectorKey.Mark)

    Column {
        Row(modifier = Modifier.padding(16.dp)) {
            TextFilterView(textFilters, filters::addTextFilter, filters::removeTextFilter)
            Spacer(Modifier.width(16.dp))
            PriorityFilterView(priorityFilters, filters::setPriorityFilter)
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
                                { detections.selectPreviousDetection(DetectorKey.Exception, it) },
                                { detections.selectNextDetection(DetectorKey.Exception, it) },
                            )

                            Spacer(Modifier.width(8.dp))
                            Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                            Spacer(Modifier.width(8.dp))
                        }

                        JsonDetectionView(
                            Modifier.width(200.dp).wrapContentHeight(),
                            jsonDetectionSelection,
                            { detections.selectPreviousDetection(DetectorKey.Json, it) },
                            { detections.selectNextDetection(DetectorKey.Json, it) },
                        )

                        Spacer(Modifier.width(8.dp))
                        Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                        Spacer(Modifier.width(8.dp))

                        MarkDetectionView(
                            Modifier.width(200.dp).wrapContentHeight(),
                            markDetectionSelection,
                            { detections.selectPreviousDetection(DetectorKey.Mark, it) },
                            { detections.selectNextDetection(DetectorKey.Mark, it) },
                            { openNewTab(detectors.markedRowsFlow) }
                        )

                        Spacer(Modifier.width(8.dp))
                        Divider(Modifier.width(1.dp).height(70.dp).align(Alignment.CenterVertically))
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            KeywordDetectionView(
                Modifier.align(Alignment.BottomEnd),
                keywordDetectionRequest,
                keywordDetectionSelection,
                detectors::findKeyword,
                detectors::setKeywordDetectionEnabled,
                { detections.selectPreviousDetection(DetectorKey.Keyword, it) },
                { detections.selectNextDetection(DetectorKey.Keyword, it) },
            )
        }
    }
}
