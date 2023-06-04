package com.jerryjeon.logjerry.log

import com.jerryjeon.logjerry.detection.DetectionFinished
import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.filter.FilterManager
import com.jerryjeon.logjerry.logview.LogAnnotation
import com.jerryjeon.logjerry.logview.RefineResult
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * The class that is created after parsing is completed.
 */
class ParseCompleted(
    val originalLogsFlow: StateFlow<List<Log>>,
    preferences: Preferences
) {
    private val refineScope = CoroutineScope(Dispatchers.Default)
    val filterManager = FilterManager()
    val detectorManager = DetectorManager(preferences)

    private val filteredLogsFlow = combine(originalLogsFlow, filterManager.filtersFlow) { originalLogs, filters ->
        if (filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> filters.all { it.filter(log) } }
        }
    }

    val detectionFinishedFlow = combine(filteredLogsFlow, detectorManager.detectorsFlow) { filteredLogs, detectors ->
        val allDetectionResults = mutableMapOf<DetectorKey, List<Detection>>()
        val detectionFinishedLogs = filteredLogs.associateWith { log ->
            val detections = detectors.associate { it.key to it.detect(log.log, log.index) }
            detections.forEach { (key, value) ->
                allDetectionResults[key] = (allDetectionResults[key] ?: emptyList()) + value
            }
            detections
        }

        DetectionFinished(detectors, detectionFinishedLogs, allDetectionResults)
    }.stateIn(
        refineScope,
        SharingStarted.Lazily,
        DetectionFinished(emptyList(), emptyMap(), emptyMap())
    )

    val refineResultFlow = combine(
        filteredLogsFlow,
        detectionFinishedFlow
    ) { filteredLogs, detectionFinished ->
        val allDetections = mutableMapOf<DetectorKey, List<Detection>>()
        var lastRefinedLog: RefinedLog? = null
        val refinedLogs = filteredLogs.map { log ->
            val detections = detectionFinished.detectionsByLog[log] ?: emptyMap()
            detections.forEach { (key, newValue) ->
                val list = allDetections.getOrPut(key) { emptyList() }
                allDetections[key] = list + newValue
            }

            // Why should it be separated : make possible to change data of detectionResult
            // TODO don't want to repeat all annotate if just one log has changed. How can I achieve it
            val logContents =
                LogAnnotation.separateAnnotationStrings(log, detections.values.flatten())
            val timeGap = lastRefinedLog?.log?.durationBetween(log)?.takeIf { it.toSeconds() >= 3 }
            RefinedLog(log, detections, LogAnnotation.annotate(log, logContents, detectionFinished.detectors), timeGap).also {
                lastRefinedLog = it
            }
        }
        RefineResult(refinedLogs, allDetections)
    }
        .stateIn(refineScope, SharingStarted.Lazily, RefineResult(emptyList(), emptyMap()))
}
