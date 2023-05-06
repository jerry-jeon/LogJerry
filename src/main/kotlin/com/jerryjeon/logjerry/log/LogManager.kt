package com.jerryjeon.logjerry.log

import com.jerryjeon.logjerry.detection.DetectionManager
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.filter.FilterManager
import com.jerryjeon.logjerry.logview.LogViewManager
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.ui.focus.LogFocus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class LogManager(
    val originalLogsFlow: StateFlow<List<Log>>,
    preferences: Preferences
) {
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

    val logViewManager = LogViewManager(detectionManager.detectionFinishedFlow, preferences)

    val currentFocus = MutableStateFlow<LogFocus?>(null)

}
