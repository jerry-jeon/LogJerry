package com.jerryjeon.logjerry.detection

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import java.util.UUID

class KeywordDetection(private val keyword: String) : Detection<DetectionResult> {
    override val key: DetectionKey = DetectionKey.Keyword
    override fun detect(logStr: String, logIndex: Int): List<DetectionResult> {
        if (keyword.isBlank()) return emptyList()
        val orKeywords = keyword.split("|")
            .filter { it.isNotBlank() }
        val indexRanges = mutableListOf<IntRange>()
        orKeywords.forEach {
            var startIndex = 0
            while (startIndex != -1) {
                startIndex = logStr.indexOf(it, startIndex, ignoreCase = true)
                if (startIndex != -1) {
                    indexRanges.add(startIndex..startIndex + it.length)
                    startIndex += it.length
                }
            }
        }

        return indexRanges.map { KeywordDetectionResult(it, logIndex) }
    }
}

class KeywordDetectionResult(override val range: IntRange, override val logIndex: Int) : DetectionResult {
    override val id: String = UUID.randomUUID().toString()
    override val key: DetectionKey = DetectionKey.Keyword
    override val style: SpanStyle
        get() = detectedStyle
    private val detectedStyle: SpanStyle = SpanStyle(background = Color.Yellow)
}
