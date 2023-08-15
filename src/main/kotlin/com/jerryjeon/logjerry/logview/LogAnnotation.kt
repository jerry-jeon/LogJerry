package com.jerryjeon.logjerry.logview

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.jerryjeon.logjerry.detector.DataClassDetection
import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogContent
import com.jerryjeon.logjerry.log.LogContentView
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

object LogAnnotation {
    val json: Json = Json { prettyPrint = true }

    fun separateAnnotationStrings(log: Log, detectionResults: List<Detection>): List<LogContent> {
        val sortedDetections = detectionResults.sortedBy { it.range.first }
        val overlapRemovedDetections = removeOverlappingDetections(sortedDetections)
        val originalLog = log.log

        var lastEnded = 0
        val logContents = mutableListOf<LogContent>()
        overlapRemovedDetections.forEach {
            val newStart = it.range.first
            val newEnd = it.range.last
            // Assume that there are no overlapping areas.
            when (it) {
                is JsonDetection -> {
                    if (lastEnded != newStart) {
                        logContents.add(LogContent.Text(originalLog.substring(lastEnded, newStart)))
                    }
                    logContents.add(
                        LogContent.Json(
                            json.encodeToString(JsonObject.serializer(), it.json)
                        )
                    )
                    lastEnded = newEnd + 1
                }

                is DataClassDetection -> {
                    if (lastEnded != newStart) {
                        logContents.add(LogContent.Text(originalLog.substring(lastEnded, newStart)))
                    }
                    logContents.add(LogContent.DataClass(prettifyDataClass(it.map)))
                    lastEnded = newEnd + 1
                }
            }
        }
        if (lastEnded < originalLog.length) {
            logContents.add(LogContent.Text(originalLog.substring(lastEnded)))
        }
        return logContents
    }

    private fun removeOverlappingDetections(detections: List<Detection>): List<Detection> {
        // Sort the detections by start range
        val sortedDetections = detections.sortedBy { it.range.first }

        val result = mutableListOf<Detection>()
        var lastEnd = -1

        for (detection in sortedDetections) {
            // If this detection doesn't overlap with the previous one, add it to the result
            if (detection.range.first > lastEnd) {
                result.add(detection)
                lastEnd = detection.range.last
            } else if (detection is JsonDetection && result.last() is DataClassDetection) {
                // If current detection is of type JsonDetection and the last added is of type DataClassDetection, replace the last one
                result.removeAt(result.size - 1)
                result.add(detection)
                lastEnd = detection.range.last
            }
        }

        return result
    }

    fun annotate(log: Log, logContents: List<LogContent>, detectors: List<Detector<*>>): List<LogContentView> {
        val result = logContents.map { logContent ->
            when (logContent) {
                is LogContent.Json, is LogContent.DataClass -> {
                    val initial = AnnotatedString.Builder(logContent.text)
                    val newDetections = detectors.filter { !it.shownAsBlock }.flatMap { detection ->
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
                    LogContentView.Block(
                        if(logContent is LogContent.Json) "JSON" else "Kotlin Data Class",
                        builder.toAnnotatedString(),
                        JsonDetection.detectedStyle.background,
                        lineCount
                    )
                }
                is LogContent.Text -> {
                    val initial = AnnotatedString.Builder(logContent.text)
                    val newDetections = detectors.filter { !it.shownAsBlock }.flatMap { detection ->
                        detection.detect(logContent.text, log.index)
                    }

                    val builder = newDetections.fold(initial) { acc, next ->
                        acc.apply {
                            addStyle(
                                next.style,
                                next.range.first,
                                next.range.last
                            )
                        }
                    }
                    LogContentView.Simple(builder.toAnnotatedString())
                }
            }
        }

        return result
    }
}

private fun prettifyDataClass(parsedData: Map<String, Any?>, indent: String = ""): String {
    val builder = StringBuilder()

    builder.append("${parsedData["class"]}(\n")
    for ((key, value) in parsedData.filterKeys { it != "class" }) {
        builder.append("$indent    $key=")
        if (value is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            builder.append(prettifyDataClass(value as Map<String, Any?>, "$indent    "))
        } else {
            builder.append(value)
        }
        builder.append("\n")
    }
    builder.append("$indent)")

    return builder.toString()
}

