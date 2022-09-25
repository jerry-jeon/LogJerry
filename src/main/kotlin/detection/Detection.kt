import androidx.compose.ui.text.SpanStyle

// When we support custom detection, then key should be String
enum class DetectionKey {
    Keyword, Exception
}

interface Detection {
    val key: DetectionKey
    val detectedStyle: SpanStyle
    fun detect(log: Log): DetectionResult?
}

open class DetectionResult(
    val ranges: List<IntRange> // Detected ranges
)

class IndexedDetectionResult(
    val detectionKey: DetectionKey,
    val detectionResult: DetectionResult,
    val detectionIndex: Int,
    val logIndex: Int
) {
    val detectionIndexInView = detectionIndex + 1
} // IndexedDectionResult

class DetectionResultFocus(
    val focusingResult: IndexedDetectionResult,
    val detectionResults: List<IndexedDetectionResult>,
) {
    val totalCount = detectionResults.size
}
