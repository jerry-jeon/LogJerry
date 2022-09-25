import androidx.compose.ui.text.SpanStyle

// When we support custom detection, then key should be String
enum class DetectionKey {
    Keyword, Exception, Json;
}

interface Detection {
    val key: DetectionKey
    val detectedStyle: SpanStyle
    fun detect(log: Log, logIndex: Int): DetectionResult?
}

open class DetectionResult(
    val key: DetectionKey,
    val style: SpanStyle, // TODO It would be better to move it to other place
    val ranges: List<IntRange>, // Detected ranges
    val log: Log,
    val logIndex: Int
)

data class DetectionFocus(
    val key: DetectionKey,
    val currentIndex: Int,
    val focusing: DetectionResult?,
    val results: List<DetectionResult>,
) {
    val totalCount = results.size
    val currentIndexInView = currentIndex + 1
}
