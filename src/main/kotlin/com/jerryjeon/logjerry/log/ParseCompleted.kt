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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.yield

/**
 * The class that is created after parsing is completed.
 */
class ParseCompleted(
    val originalLogsFlow: StateFlow<List<Log>>,
    preferences: Preferences,
) {
    private val refineScope = CoroutineScope(Dispatchers.Default)
    val filterManager = FilterManager(originalLogsFlow)
    val detectorManager = DetectorManager(preferences)

    private val filteredLogsFlow =
        combine(
            originalLogsFlow,
            filterManager.filtersFlow,
            ::Pair
        ).mapLatest { (originalLogs, filters) ->
            if (filters.isEmpty()) {
                originalLogs
            } else {
                originalLogs
                    .filter { log ->
                        filters.all {
                            yield()
                            it.filter(log)
                        }
                    }
            }
        }

    val detectionFinishedFlow =
        combine(
            filteredLogsFlow,
            detectorManager.detectorsFlow,
            ::Pair
        ).mapLatest { (filteredLogs, detectors) ->
            val allDetectionResults = mutableMapOf<DetectorKey, List<Detection>>()
            val detectionFinishedLogs = filteredLogs.associateWith { log ->
                yield()
                val detections = detectors.associate { it.key to it.detect(log.log, log.index) }
                detections.forEach { (key, value) ->
                    yield()
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
        detectionFinishedFlow,
        ::Pair,
    ).mapLatest { (filteredLogs, detectionFinished) ->
        val allDetections = mutableMapOf<DetectorKey, List<Detection>>()
        var lastRefinedLog: RefinedLog? = null
        val refinedLogs = filteredLogs.map { log ->
            yield()
            val detections = detectionFinished.detectionsByLog[log] ?: emptyMap()
            detections.forEach { (key, newValue) ->
                yield()
                val list = allDetections.getOrPut(key) { emptyList() }
                allDetections[key] = list + newValue
            }

            // Why should it be separated : make possible to change data of detectionResult
            // TODO don't want to repeat all annotate if just one log has changed. How can I achieve it
            val logContents =
                LogAnnotation.separateAnnotationStrings(log, detections.values.flatten())
            val timeGap = lastRefinedLog?.log?.durationBetween(log)?.takeIf { it.toSeconds() >= 3 }
            RefinedLog(
                log,
                detections,
                LogAnnotation.annotate(log, logContents, detectionFinished.detectors),
                timeGap
            )
                .also {
                    lastRefinedLog = it
                }
        }
        RefineResult(refinedLogs, allDetections)
    }.stateIn(refineScope, SharingStarted.Lazily, RefineResult(emptyList(), emptyMap()))

    val dateSet = originalLogsFlow.map { logs ->
        logs.map { it.date }.toSet()
    }
        .stateIn(refineScope, SharingStarted.Lazily, emptySet())

    val singleDate = dateSet.map { it.singleOrNull() }
        .stateIn(refineScope, SharingStarted.Lazily, null)

    val optimizedHeader = combine(
        preferences.headerFlow,
        filterManager.tagFiltersFlow,
        filterManager.packageFiltersFlow,
        dateSet,
    ) { header, tagFilters, packageFilters, dateSet ->
        header.copy(
            tag = header.tag.copy(visible = tagFilters.filters.filter { it.include }.size != 1),
            packageName = header.packageName.copy(visible = packageFilters.filters.filter { it.include }.size != 1),
            date = header.date.copy(visible = dateSet.size > 1)
        )
    }
        .stateIn(refineScope, SharingStarted.Lazily, preferences.headerFlow.value)
}
