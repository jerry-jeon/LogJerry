package com.jerryjeon.logjerry.detection

import com.jerryjeon.logjerry.detector.*
import com.jerryjeon.logjerry.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DetectionManager(
    logsFlow: Flow<List<Log>>,
    detectorsFlow: Flow<List<Detector<*>>>
) {
    private val detectionScope = CoroutineScope(Dispatchers.Default)

    val detectionFinishedFlow: StateFlow<DetectionFinished> =
        combine(logsFlow, detectorsFlow) { filteredLogs, detectors ->
            val detectionFinishedLogs = filteredLogs
                .map { log ->
                    val detectionResults = detectors.map { it.detect(log.log, log.index) }
                        .flatten()
                    DetectionFinishedLog(log, detectionResults.groupBy { it.key })
                }

            val allDetectionResults = mutableMapOf<DetectorKey, List<Detection>>()
            detectionFinishedLogs.forEach {
                it.detections.forEach { (key, value) ->
                    allDetectionResults[key] = (allDetectionResults[key] ?: emptyList()) + value
                }
            }
            DetectionFinished(detectionFinishedLogs, allDetectionResults, detectors)
        }.stateIn(detectionScope, SharingStarted.Lazily, DetectionFinished(emptyList(), emptyMap(), emptyList()))

    val selections = MutableStateFlow<DetectionSelections?>(null)

    init {
        detectionScope.launch {
            detectionFinishedFlow.collect { result ->
                val keywordDetections = result.allDetections[DetectorKey.Keyword] ?: emptyList()
                val keywordSelection = DetectorKey.Keyword to keywordDetections.firstOrNull()?.let {
                    DetectionSelection(DetectorKey.Keyword, 0, null, keywordDetections)
                }

                val exceptionDetections = result.allDetections[DetectorKey.Exception] ?: emptyList()
                val exceptionSelection = DetectorKey.Exception to exceptionDetections.firstOrNull()?.let {
                    DetectionSelection(DetectorKey.Exception, 0, null, exceptionDetections)
                }

                val jsonDetections = result.allDetections[DetectorKey.Json] ?: emptyList()
                val jsonSelection = DetectorKey.Json to jsonDetections.firstOrNull()?.let {
                    DetectionSelection(DetectorKey.Exception, 0, null, jsonDetections)
                }

                val markDetections = result.allDetections[DetectorKey.Mark] ?: emptyList()
                val markSelection = DetectorKey.Mark to markDetections.firstOrNull()?.let {
                    DetectionSelection(DetectorKey.Exception, 0, null, markDetections)
                }
                selections.value = selections.value?.copy(
                    selectionByKey = listOf(keywordSelection, exceptionSelection, jsonSelection, markSelection).toMap()
                )
            }
        }
    }

    fun selectPreviousDetection(key: DetectorKey, selection: DetectionSelection) {
        val previousIndex = if (selection.currentIndex <= 0) {
            selection.allDetections.size - 1
        } else {
            selection.currentIndex - 1
        }

        val newSelection =
            selection.copy(currentIndex = previousIndex, selected = selection.allDetections[previousIndex])
        selections.value = selections.value?.updatedSelections(key, newSelection)
    }

    fun selectNextDetection(key: DetectorKey, selection: DetectionSelection) {
        val nextIndex = if (selection.currentIndex >= selection.allDetections.size - 1) {
            0
        } else {
            selection.currentIndex + 1
        }

        val newSelection = selection.copy(currentIndex = nextIndex, selected = selection.allDetections[nextIndex])
        selections.value = selections.value?.updatedSelections(key, newSelection)
    }
}
