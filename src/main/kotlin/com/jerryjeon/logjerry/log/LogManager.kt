package com.jerryjeon.logjerry.log

import com.jerryjeon.logjerry.detection.DetectionManager
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.filter.FilterManager
import com.jerryjeon.logjerry.logview.LogViewManager
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map

class LogManager(
    val originalLogs: List<Log>,
    preferences: Preferences
) {
    val filterManager = FilterManager()
    val detectorManager = DetectorManager()

    private val filteredLogsFlow = filterManager.filtersFlow.map { filters ->
        if (filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> filters.all { it.filter(log) } }
        }
    }

    val detectionManager = DetectionManager(filteredLogsFlow, detectorManager.detectorsFlow.debounce(250))

    val logViewManager = LogViewManager(detectionManager.detectionFinishedFlow, preferences)
}
