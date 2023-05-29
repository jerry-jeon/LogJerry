package com.jerryjeon.logjerry.logview

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
import com.jerryjeon.logjerry.preferences.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

// It's related how logs are going to shown
class LogViewManager(
    detectionFinishedFlow: Flow<DetectionFinished>,
    preferences: Preferences
) {
    private val logViewScope = CoroutineScope(Dispatchers.Default)
    val json = Json { prettyPrint = true }

    val investigationViewFlow: StateFlow<InvestigationView> = detectionFinishedFlow.map { investigation ->
        // Why should it be separated : make possible to change data of detectionResult
        // TODO don't want to repeat all annotate if just one log has changed. How can I achieve it
        val refinedLogs = investigation.detectionFinishedLogs.map {
            val logContents =
                separateAnnotationStrings(it.log, it.detections.values.flatten())
            RefinedLog(it, annotate(it.log, logContents, investigation.detectors))
        }
        InvestigationView(refinedLogs, investigation.allDetections)
    }.stateIn(logViewScope, SharingStarted.Lazily, InvestigationView(emptyList(), emptyMap()))

    private fun separateAnnotationStrings(log: Log, detectionResults: List<Detection>): List<LogContent> {
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
                        detection.detect(logContent.text, log.index)
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
                    LogContentView.Json(builder.toAnnotatedString(), JsonDetection.detectedStyle.background, logContent.jsonDetection)
                }
                is LogContent.Text -> {
                    val initial = AnnotatedString.Builder(logContent.text)
                    val newDetections = detectors.filter { it !is JsonDetector }.flatMap { detection ->
                        detection.detect(logContent.text, log.index)
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
}
