package detection

import Detection
import Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

class KeywordDetection(private val keyword: String) : Detection {
    override val key: String = "keyword"
    override val detectedStyle: SpanStyle = SpanStyle(background = Color.Yellow)
    override fun detect(log: Log): List<IntRange> {
        if (keyword.isBlank()) return emptyList()
        var startIndex = 0
        val indexRanges = mutableListOf<IntRange>()
        while (startIndex != -1) {
            startIndex = log.originalLog.indexOf(keyword, startIndex)
            if (startIndex != -1) {
                indexRanges.add(startIndex..startIndex + keyword.length)
                startIndex += keyword.length
            }
        }

        return indexRanges
    }
}
