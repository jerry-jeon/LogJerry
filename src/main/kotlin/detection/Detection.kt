import androidx.compose.ui.text.SpanStyle

// When we support custom detection, then key should be String
enum class DetectionKey {
    Keyword, Exception
}

interface Detection {
    val key: DetectionKey
    val detectedStyle: SpanStyle
    fun detect(log: Log, logIndex: Int): DetectionResult?
}

open class DetectionResult(
    val ranges: List<IntRange>, // Detected ranges
    val log: Log,
    val logIndex: Int
)

data class DetectionResultFocus(
    val key: DetectionKey,
    val currentIndex: Int,
    val focusing: DetectionResult?,
    val results: List<DetectionResult>,
) {
    val totalCount = results.size
    val currentIndexInView = currentIndex + 1
}
