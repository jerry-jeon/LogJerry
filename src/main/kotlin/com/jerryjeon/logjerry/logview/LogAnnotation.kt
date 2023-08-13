package com.jerryjeon.logjerry.logview

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.detector.JsonDetector
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogContent
import com.jerryjeon.logjerry.log.LogContentView
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

object LogAnnotation {
    val json: Json = Json { prettyPrint = true }

    fun separateAnnotationStrings(log: Log, detectionResults: List<Detection>): List<LogContent> {
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

    fun annotate(log: Log, logContents: List<LogContent>, detectors: List<Detector<*>>): List<LogContentView> {
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
                    val lineCount = logContent.text.lines().size
                    LogContentView.Json(
                        builder.toAnnotatedString(),
                        JsonDetection.detectedStyle.background,
                        logContent.jsonDetection,
                        lineCount
                    )
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
                                next.range.last
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
