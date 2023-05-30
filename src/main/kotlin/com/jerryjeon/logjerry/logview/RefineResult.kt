package com.jerryjeon.logjerry.logview

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectionStatus
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.ui.focus.DetectionFocus
import com.jerryjeon.logjerry.ui.focus.LogFocus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class RefineResult(
    val refinedLogs: List<RefinedLog>,
    val allDetections: Map<DetectorKey, List<Detection>>
) {
    val markedRows = refinedLogs.filter { it.marked }

    val currentFocus = MutableStateFlow<LogFocus?>(null)

    val statusByKey = MutableStateFlow(
        allDetections
            .mapValues { (key, values) ->
                if (values.isEmpty()) {
                    null
                } else {
                    DetectionStatus(key, 0, null, values)
                }
            }
    )

    fun selectPreviousDetection(status: DetectionStatus) {
        val previousIndex = if (status.currentIndex <= 0) {
            status.allDetections.size - 1
        } else {
            status.currentIndex - 1
        }

        val newStatus = status.copy(currentIndex = previousIndex, selected = status.allDetections[previousIndex])
        updateDetectionStatus(newStatus)
    }

    fun selectNextDetection(selection: DetectionStatus) {
        val nextIndex = if (selection.currentIndex >= selection.allDetections.size - 1) {
            0
        } else {
            selection.currentIndex + 1
        }
        val newSelection = selection.copy(currentIndex = nextIndex, selected = selection.allDetections[nextIndex])
        updateDetectionStatus(newSelection)
    }

    private fun updateDetectionStatus(newStatus: DetectionStatus) {
        statusByKey.update { it + (newStatus.key to newStatus) }

        val indexInRefinedLogs = refinedLogs.indexOfFirst { refinedLog ->
            refinedLog.log.index == newStatus.selected?.logIndex
        }
        if (indexInRefinedLogs != -1) {
            currentFocus.value = DetectionFocus(indexInRefinedLogs)
        }
    }
}
