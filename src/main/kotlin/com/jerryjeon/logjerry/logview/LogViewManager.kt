package com.jerryjeon.logjerry.logview

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.jerryjeon.logjerry.detection.DetectionFinished
import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.detector.JsonDetector
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogContent
import com.jerryjeon.logjerry.log.LogContentView
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

// It's related how logs are going to shown
class LogViewManager(
    detectionFinishedFlow: Flow<DetectionFinished>
) {
    private val logViewScope = MainScope()
    val json = Json { prettyPrint = true }

    private val detectionExpandedFlow = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    val investigationViewFlow: StateFlow<InvestigationView> = combine(detectionFinishedFlow, detectionExpandedFlow) { investigation, expanded ->
        // Why should it be separated : make possible to change data of detectionResult
        // TODO don't want to repeat all annotate if just one log has changed. How can I achieve it
        val refinedLogs = investigation.detectionFinishedLogs.map {
            val logContents =
                separateAnnotationStrings(it.log, it.detections.values.flatten(), expanded)
            RefinedLog(it, annotate(it.log, logContents, investigation.detectors))
        }
        val allDetectionLogs = investigation.allDetections.mapValues { (_, v) ->
            v.map { DetectionView(it, false) }
        }
        InvestigationView(refinedLogs, allDetectionLogs)
    }.stateIn(logViewScope, SharingStarted.Lazily, InvestigationView(emptyList(), emptyMap()))

    init {
        logViewScope.launch {
            detectionFinishedFlow.collect { detectionFinished ->
                detectionExpandedFlow.value = detectionFinished.allDetections.values.flatten().associate {
                    it.id to (it is JsonDetection)
                }
            }
        }
    }

    private fun separateAnnotationStrings(log: Log, detectionResults: List<Detection>, detectionExpanded: Map<String, Boolean>): List<LogContent> {
        val sortedDetections = detectionResults.sortedBy { it.range.first }
        val originalLog = log.log

        var lastEnded = 0
        val logContents = mutableListOf<LogContent>()
        val jsonDetections = mutableListOf<JsonDetection>()
        sortedDetections.forEach {
            val newStart = it.range.first
            val newEnd = it.range.last
            // Assume that there are no overlapping areas.
            if (it is JsonDetection) {
                if (detectionExpanded[it.id] == true) {
                    if (lastEnded != newStart) {
                        logContents.add(LogContent.Text(originalLog.substring(lastEnded, newStart), jsonDetections.toList()))
                    }
                    logContents.add(
                        LogContent.ExpandedJson(
                            json.encodeToString(JsonObject.serializer(), it.json), it
                        )
                    )
                    jsonDetections.clear()
                    lastEnded = newEnd + 1
                } else {
                    jsonDetections.add(it.move(-lastEnded)) // expanded range is separated area, so range should be changed
                }
            }
        }
        if (lastEnded < originalLog.length) {
            logContents.add(LogContent.Text(originalLog.substring(lastEnded), jsonDetections.toList()))
        }
        return logContents
    }

    private fun annotate(log: Log, logContents: List<LogContent>, detectors: List<Detector<*>>): List<LogContentView> {
        val result = logContents.map { logContent ->
            when (logContent) {
                is LogContent.ExpandedJson -> {
                    val initial = AnnotatedString.Builder(logContent.text)
                    val newDetections = detectors.filter { it !is JsonDetector }.flatMap { detection ->
                        detection.detect(logContent.text, log.number)
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
                    LogContentView.Json(builder.toAnnotatedString(), Color.LightGray, logContent.jsonDetection)
                }
                is LogContent.Text -> {
                    val initial = AnnotatedString.Builder(logContent.text)
                    val newDetections = detectors.filter { it !is JsonDetector }.flatMap { detection ->
                        detection.detect(logContent.text, log.number)
                    }

                    val builder = (newDetections + logContent.jsonDetections).fold(initial) { acc, next ->
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

    fun collapseJsonDetection(detection: JsonDetection) {
        detectionExpandedFlow.value = detectionExpandedFlow.value + (detection.id to false)
    }

    fun expandJsonDetection(annotation: String) {
        detectionExpandedFlow.value = detectionExpandedFlow.value + (annotation to true)
    }
}
