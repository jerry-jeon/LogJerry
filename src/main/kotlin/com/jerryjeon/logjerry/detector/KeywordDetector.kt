package com.jerryjeon.logjerry.detector

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import java.util.UUID

class KeywordDetector(private val keyword: String) : Detector<Detection> {
    override val key: DetectorKey = DetectorKey.Keyword
    override val shownAsBlock: Boolean = false
    override fun detect(logStr: String, logIndex: Int): List<Detection> {
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

        return indexRanges.map { KeywordDetection(it, logIndex) }
    }
}

class KeywordDetection(override val range: IntRange, override val logIndex: Int) : Detection {
    override val id: String = UUID.randomUUID().toString()
    override val key: DetectorKey = DetectorKey.Keyword
    override val style: SpanStyle
        get() = detectedStyle
    private val detectedStyle: SpanStyle = SpanStyle(background = Color.Yellow)
}
