package com.jerryjeon.logjerry.log

import com.jerryjeon.logjerry.detection.Detections
import com.jerryjeon.logjerry.detector.Detectors
import com.jerryjeon.logjerry.filter.Filters
import com.jerryjeon.logjerry.logview.LogViewManager
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.ui.focus.LogFocus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

/**
 * The class that is created after parsing is completed.
 */
class ParseCompleted(
    val originalLogsFlow: StateFlow<List<Log>>,
    preferences: Preferences
) {
    val filters = Filters()
    val detectors = Detectors(preferences)

    private val filteredLogsFlow = combine(originalLogsFlow, filters.filtersFlow) { originalLogs, filters ->
        if (filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> filters.all { it.filter(log) } }
        }
    }

    val detections = Detections(filteredLogsFlow, detectors.detectorsFlow)

    val logViewManager = LogViewManager(detections.detectionFinishedFlow, preferences)

    val currentFocus = MutableStateFlow<LogFocus?>(null)
}
