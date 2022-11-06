package com.jerryjeon.logjerry.detection

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectionFocus
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

    val keywordDetectionFocus = MutableStateFlow<DetectionFocus?>(null)
    val exceptionDetectionFocus = MutableStateFlow<DetectionFocus?>(null)
    val jsonDetectionFocus = MutableStateFlow<DetectionFocus?>(null)
    val markDetectionFocus = MutableStateFlow<DetectionFocus?>(null)

    private val focuses = mapOf(
        DetectorKey.Keyword to keywordDetectionFocus,
        DetectorKey.Exception to exceptionDetectionFocus,
        DetectorKey.Json to jsonDetectionFocus,
        DetectorKey.Mark to markDetectionFocus,
    )

    val activeDetectionFocusFlowState =
        merge(keywordDetectionFocus, exceptionDetectionFocus, jsonDetectionFocus, markDetectionFocus)
            .filter { it?.focusing != null }
            .stateIn(detectionScope, SharingStarted.Lazily, null)

    init {
        detectionScope.launch {
            detectionFinishedFlow.collect { result ->
                val keywordDetections = result.allDetections[DetectorKey.Keyword] ?: emptyList()
                keywordDetectionFocus.value = keywordDetections.firstOrNull()?.let {
                    DetectionFocus(DetectorKey.Keyword, 0, null, keywordDetections)
                }

                val exceptionDetections = result.allDetections[DetectorKey.Exception] ?: emptyList()
                exceptionDetectionFocus.value = exceptionDetections.firstOrNull()?.let {
                    DetectionFocus(DetectorKey.Exception, 0, null, exceptionDetections)
                }

                val jsonDetections = result.allDetections[DetectorKey.Json] ?: emptyList()
                jsonDetectionFocus.value = jsonDetections.firstOrNull()?.let {
                    DetectionFocus(DetectorKey.Exception, 0, null, jsonDetections)
                }

                val markDetections = result.allDetections[DetectorKey.Mark] ?: emptyList()
                markDetectionFocus.value = markDetections.firstOrNull()?.let {
                    DetectionFocus(DetectorKey.Exception, 0, null, markDetections)
                }
            }
        }
    }

    fun focusPreviousDetection(key: DetectorKey, focus: DetectionFocus) {
        val previousIndex = if (focus.currentIndex <= 0) {
            focus.allDetections.size - 1
        } else {
            focus.currentIndex - 1
        }

        focuses[key]?.value = focus.copy(currentIndex = previousIndex, focusing = focus.allDetections[previousIndex])
    }

    fun focusNextDetection(key: DetectorKey, focus: DetectionFocus) {
        val nextIndex = if (focus.currentIndex >= focus.allDetections.size - 1) {
            0
        } else {
            focus.currentIndex + 1
        }

        focuses[key]?.value = focus.copy(currentIndex = nextIndex, focusing = focus.allDetections[nextIndex])
    }
}
