package com.jerryjeon.logjerry.detection

import Detection
import DetectionKey
import DetectionResult
import Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

class KeywordDetection(private val keyword: String) : Detection {
    override val key: DetectionKey = DetectionKey.Keyword
    override val detectedStyle: SpanStyle = SpanStyle(background = Color.Yellow)
    override fun detect(log: Log, logIndex: Int): DetectionResult? {
        if (keyword.isBlank()) return null
        val orKeywords = keyword.split("|")
            .filter { it.isNotBlank() }
        val indexRanges = mutableListOf<IntRange>()
        orKeywords.forEach {
            var startIndex = 0
            while (startIndex != -1) {
                startIndex = log.log.indexOf(it, startIndex)
                if (startIndex != -1) {
                    indexRanges.add(startIndex..startIndex + it.length)
                    startIndex += it.length
                }
            }
        }

        return if (indexRanges.isNotEmpty()) {
            DetectionResult(key, detectedStyle, indexRanges, log, logIndex)
        } else {
            null
        }
    }
}
