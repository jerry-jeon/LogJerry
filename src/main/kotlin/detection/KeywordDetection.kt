package detection

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
        var startIndex = 0
        val indexRanges = mutableListOf<IntRange>()
        while (startIndex != -1) {
            startIndex = log.originalLog.indexOf(keyword, startIndex)
            if (startIndex != -1) {
                indexRanges.add(startIndex..startIndex + keyword.length)
                startIndex += keyword.length
            }
        }

        return if (indexRanges.isNotEmpty()) {
            DetectionResult(indexRanges, log, logIndex)
        } else {
            null
        }
    }
}
