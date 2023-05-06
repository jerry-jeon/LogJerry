package com.jerryjeon.logjerry.detection

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectionSelection
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.detector.DetectorKey
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

    val detectionFinishedFlow: StateFlow<DetectionFinished> = combine(logsFlow, detectorsFlow) { filteredLogs, detectors ->
        val detectionFinishedLogs = filteredLogs
            .mapIndexed { logIndex, log ->
                val detectionResults = detectors.map { it.detect(log.log, logIndex) }
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

    val keywordDetectionSelection = MutableStateFlow<DetectionSelection?>(null)
    val exceptionDetectionSelection = MutableStateFlow<DetectionSelection?>(null)
    val jsonDetectionSelection = MutableStateFlow<DetectionSelection?>(null)
    val markDetectionSelection = MutableStateFlow<DetectionSelection?>(null)

    private val selections = mapOf(
        DetectorKey.Keyword to keywordDetectionSelection,
        DetectorKey.Exception to exceptionDetectionSelection,
        DetectorKey.Json to jsonDetectionSelection,
        DetectorKey.Mark to markDetectionSelection,
    )

    val activeDetectionSelectionFlowState =
        merge(keywordDetectionSelection, exceptionDetectionSelection, jsonDetectionSelection, markDetectionSelection)
            .filter { it?.selected != null }
            .stateIn(detectionScope, SharingStarted.Lazily, null)

    init {
        detectionScope.launch {
            detectionFinishedFlow.collect { result ->
                val keywordDetections = result.allDetections[DetectorKey.Keyword] ?: emptyList()
                keywordDetectionSelection.value = keywordDetections.firstOrNull()?.let {
                    DetectionSelection(DetectorKey.Keyword, 0, null, keywordDetections)
                }

                val exceptionDetections = result.allDetections[DetectorKey.Exception] ?: emptyList()
                exceptionDetectionSelection.value = exceptionDetections.firstOrNull()?.let {
                    DetectionSelection(DetectorKey.Exception, 0, null, exceptionDetections)
                }

                val jsonDetections = result.allDetections[DetectorKey.Json] ?: emptyList()
                jsonDetectionSelection.value = jsonDetections.firstOrNull()?.let {
                    DetectionSelection(DetectorKey.Exception, 0, null, jsonDetections)
                }

                val markDetections = result.allDetections[DetectorKey.Mark] ?: emptyList()
                markDetectionSelection.value = markDetections.firstOrNull()?.let {
                    DetectionSelection(DetectorKey.Exception, 0, null, markDetections)
                }
            }
        }
    }

    fun selectPreviousDetection(key: DetectorKey, selection: DetectionSelection) {
        val previousIndex = if (selection.currentIndex <= 0) {
            selection.allDetections.size - 1
        } else {
            selection.currentIndex - 1
        }

        selections[key]?.value = selection.copy(currentIndex = previousIndex, selected = selection.allDetections[previousIndex])
    }

    fun selectNextDetection(key: DetectorKey, selection: DetectionSelection) {
        val nextIndex = if (selection.currentIndex >= selection.allDetections.size - 1) {
            0
        } else {
            selection.currentIndex + 1
        }

        selections[key]?.value = selection.copy(currentIndex = nextIndex, selected = selection.allDetections[nextIndex])
    }
}
