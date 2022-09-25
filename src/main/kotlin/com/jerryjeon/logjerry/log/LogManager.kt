package com.jerryjeon.logjerry.log

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.jerryjeon.logjerry.detection.DetectionManager
import com.jerryjeon.logjerry.detection.DetectionView
import com.jerryjeon.logjerry.detection.InvestigationView
import com.jerryjeon.logjerry.detection.RefinedLog
import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.detector.DetectorManager
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.filter.FilterManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class LogManager(
    val originalLogs: List<Log>,
) {
    val filterManager = FilterManager()
    val detectorManager = DetectorManager()
    private val logScope = MainScope()

    private val filteredLogsFlow = filterManager.filtersFlow.map { filters ->
        if (filters.isEmpty()) {
            originalLogs
        } else {
            originalLogs
                .filter { log -> filters.all { it.filter(log) } }
        }
    }

    val detectionManager = DetectionManager(filteredLogsFlow, detectorManager.detectorsFlow)

    private val detectionExpanded = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    val investigationViewFlow: StateFlow<InvestigationView> = combine(detectionManager.detectionFinishedFlow, detectionExpanded) { investigation, expanded ->
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

    init {
        logScope.launch {
            detectionManager.detectionFinishedFlow.map { detectionFinished ->
                detectionExpanded.value = detectionFinished.allDetections.values.flatten().associate {
                    it.id to (it is JsonDetection)
                }
            }
        }
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
