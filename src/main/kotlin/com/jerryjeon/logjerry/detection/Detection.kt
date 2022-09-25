import androidx.compose.ui.text.SpanStyle

// When we support custom detection, then key should be String
enum class DetectionKey {
    Keyword, Exception, Json;
}

interface Detection<T : DetectionResult> {
    val key: DetectionKey
    val detectedStyle: SpanStyle
    fun detect(logStr: String, logIndex: Int): List<T>
}

open class DetectionResult(
    val key: DetectionKey,
    val style: SpanStyle, // TODO It would be better to move it to other place
    val range: IntRange,
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
