package com.jerryjeon.logjerry.log

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectionFocus
import com.jerryjeon.logjerry.detector.DetectionKey
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.filter.FilterManager
import com.jerryjeon.logjerry.log.refine.DetectionFinishedLog
import com.jerryjeon.logjerry.log.refine.DetectionView
import com.jerryjeon.logjerry.log.refine.Investigation
import com.jerryjeon.logjerry.log.refine.InvestigationView
import com.jerryjeon.logjerry.log.refine.RefinedLog
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class LogManager(
    val originalLogs: List<Log>,
    filterManager: FilterManager,
    detectorManager: DetectorManager
) {
    private val logScope = MainScope()

    private val filteredLogsFlow = filterManager.filtersFlow.map { filters ->
        if (filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> filters.all { it.filter(log) } }
        }
    }

    private val investigationFlow: StateFlow<Investigation> = combine(filteredLogsFlow, detectorManager.detectorsFlow) { filteredLogs, detectors ->
        val detectionFinishedLogs = filteredLogs
            .mapIndexed { logIndex, log ->
                val detectionResults = detectors.map { it.detect(log.log, logIndex) }
                    .flatten()
                DetectionFinishedLog(log, detectionResults.groupBy { it.key })
            }

        val allDetectionResults = mutableMapOf<DetectionKey, List<Detection>>()
        detectionFinishedLogs.forEach {
            it.detections.forEach { (key, value) ->
                allDetectionResults[key] = (allDetectionResults[key] ?: emptyList()) + value
            }
        }
        Investigation(originalLogs, detectionFinishedLogs, allDetectionResults, detectors)
    }.stateIn(logScope, SharingStarted.Lazily, Investigation(emptyList(), emptyList(), emptyMap(), emptyList()))

    private val detectionExpanded = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    val investigationViewFlow: StateFlow<InvestigationView> = combine(investigationFlow, detectionExpanded) { investigation, expanded ->
        // Why should it be separated : make possible to change data of detectionResult
        // TODO don't want to repeat all annotate if just one log has changed. How can I achieve it
        val refinedLogs = investigation.detectionFinishedLogs.map {
            val logContents =
                separateAnnotationStrings(
                    it.log,
                    it.detections.values.flatten().map { detectionResult ->
                        DetectionView(detectionResult, expanded[detectionResult.id] ?: false)
                    }
                )
            RefinedLog(it, annotate(it.log, logContents, investigation.detectors))
        }
        val allDetectionLogs = investigation.allDetections.mapValues { (_, v) ->
            v.map { DetectionView(it, false) }
        }
        InvestigationView(refinedLogs, allDetectionLogs)
    }.stateIn(logScope, SharingStarted.Lazily, InvestigationView(emptyList(), emptyMap()))

    val keywordDetectionFocus = MutableStateFlow<DetectionFocus?>(null)
    val exceptionDetectionFocus = MutableStateFlow<DetectionFocus?>(null)
    val jsonDetectionFocus = MutableStateFlow<DetectionFocus?>(null)

    private val focuses = mapOf(
        DetectionKey.Keyword to keywordDetectionFocus,
        DetectionKey.Exception to exceptionDetectionFocus,
        DetectionKey.Json to jsonDetectionFocus,
    )

    init {
        logScope.launch {
            investigationFlow.collect { result ->
                val keywordDetections = result.allDetections[DetectionKey.Keyword] ?: emptyList()
                keywordDetectionFocus.value = keywordDetections.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Keyword, 0, null, keywordDetections)
                }

                val exceptionDetections = result.allDetections[DetectionKey.Exception] ?: emptyList()
                exceptionDetectionFocus.value = exceptionDetections.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Exception, 0, null, exceptionDetections)
                }

                val jsonDetections = result.allDetections[DetectionKey.Json] ?: emptyList()
                jsonDetectionFocus.value = jsonDetections.firstOrNull()?.let {
                    DetectionFocus(DetectionKey.Exception, 0, null, jsonDetections)
                }

                detectionExpanded.value = result.allDetections.values.flatten().associate {
                    it.id to (it is JsonDetection)
                }
            }
        }
    }

    val activeDetectionFocusFlowState =
        merge(keywordDetectionFocus, exceptionDetectionFocus, jsonDetectionFocus)
            .filter { it?.focusing != null }
            .stateIn(logScope, SharingStarted.Lazily, null)

    fun focusPreviousDetection(key: DetectionKey, focus: DetectionFocus) {
        val previousIndex = if (focus.currentIndex <= 0) {
            focus.allDetections.size - 1
        } else {
            focus.currentIndex - 1
        }

        focuses[key]?.value = focus.copy(currentIndex = previousIndex, focusing = focus.allDetections[previousIndex])
    }

    fun focusNextDetection(key: DetectionKey, focus: DetectionFocus) {
        val nextIndex = if (focus.currentIndex >= focus.allDetections.size - 1) {
            0
        } else {
            focus.currentIndex + 1
        }

        focuses[key]?.value = focus.copy(currentIndex = nextIndex, focusing = focus.allDetections[nextIndex])
    }

    val json = Json { prettyPrint = true }
    private fun separateAnnotationStrings(log: Log, detectionResults: List<DetectionView>): List<LogContent> {
        val sorted = detectionResults.sortedBy { it.detection.range.first }
        val originalLog = log.log

        var lastEnded = 0
        val logContents = mutableListOf<LogContent>()
        sorted.forEach {
            val newStart = it.detection.range.first
            val newEnd = it.detection.range.last
            // Assume that there are no overlapping areas.
            if (it.detection is JsonDetection && it.expanded) {
                if (lastEnded != newStart) {
                    logContents.add(LogContent.Simple(originalLog.substring(lastEnded, newStart)))
                }
                logContents.add(LogContent.Json(json.encodeToString(JsonObject.serializer(), it.detection.json), it.detection))
                lastEnded = newEnd + 1
            }
        }
        if (lastEnded < originalLog.length) {
            logContents.add(LogContent.Simple(originalLog.substring(lastEnded)))
        }
        return logContents
    }

    private fun annotate(log: Log, logContents: List<LogContent>, detectors: List<Detector<*>>): List<LogContentView> {
        val result = logContents.map {
            when (it) {
                is LogContent.Json -> {
                    val simple = it
                    val initial = AnnotatedString.Builder(simple.text)
                    val newDetections = detectors.flatMap { detection ->
                        detection.detect(simple.text, log.number)
                    }

                    val builder = newDetections.fold(initial) { acc, next ->
                        acc.apply {
                            addStyle(
                                SpanStyle(),
                                next.range.first,
                                next.range.last + 1
                            )
                        }
                    }
                    LogContentView.Json(builder.toAnnotatedString(), Color.LightGray, it.jdr)
                }
                is LogContent.Simple -> {
                    val simple = it
                    val initial = AnnotatedString.Builder(simple.text)
                    val newDetections = detectors.flatMap { detection ->
                        detection.detect(simple.text, log.number)
                    }

                    val builder = newDetections.fold(initial) { acc, next ->
                        acc.apply {
                            addStyle(
                                next.style,
                                next.range.first,
                                next.range.last + 1
                            )
                            if (next is JsonDetection) {
                                addStringAnnotation("Json", next.id, next.range.first, next.range.last + 1)
                            }
                        }
                    }
                    LogContentView.Simple(builder.toAnnotatedString())
                }
            }
        }

        return result
    }

    fun collapseJsonDetection(detection: Detection) {
        println("collapse: ${detection.id}")
        detectionExpanded.value = detectionExpanded.value + (detection.id to false)
    }

    fun expandJsonDetection(annotation: String) {
        println("expand: $annotation")
        detectionExpanded.value = detectionExpanded.value + (annotation to true)
    }
}
