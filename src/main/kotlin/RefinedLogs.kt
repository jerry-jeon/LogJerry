import androidx.compose.ui.text.SpanStyle

data class RefinedLogs(
    val originalLogs: List<Log>,
    val refined: List<Log>,
    val detectionResults: Map<String, List<DetectionResult>>
)

class DetectionResult(
    val detectionKey: String,
    val index: Int
)

interface Detection {
    val key: String
    val detectedStyle: SpanStyle
    fun detect(log: Log): List<IntRange> // Detected ranges
}
