package com.jerryjeon.logjerry.log

import com.jerryjeon.logjerry.detection.DetectionManager
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.filter.FilterManager
import com.jerryjeon.logjerry.logview.LogAnnotation
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.ui.focus.LogFocus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

/**
 * The class that is created after parsing is completed.
 */
class ParseCompleted(
    val originalLogsFlow: StateFlow<List<Log>>,
    preferences: Preferences
) {
    private val scope = CoroutineScope(Dispatchers.Default)
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

    val detectionManager = DetectionManager(filteredLogsFlow, detectorManager.detectorsFlow)

    val currentFocus = MutableStateFlow<LogFocus?>(null)

    val refinedLogs =
        combine(
            filteredLogsFlow,
            detectionManager.detectionFinishedFlow
        ) { filteredLogs, detectionFinished ->
            filteredLogs.map { log ->
                val detections = detectionFinished.detectionsByLog[log] ?: emptyMap()

                // Why should it be separated : make possible to change data of detectionResult
                // TODO don't want to repeat all annotate if just one log has changed. How can I achieve it
                val logContents =
                    LogAnnotation.separateAnnotationStrings(log, detections.values.flatten())
                RefinedLog(log, detections, LogAnnotation.annotate(log, logContents, detectionFinished.detectors))
            }
        }
            .stateIn(scope, SharingStarted.Lazily, emptyList())

    init {
        // TODO remove detectorsFlow
    }
}
