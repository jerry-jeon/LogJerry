import androidx.compose.ui.text.SpanStyle

data class RefinedLogs(
    val originalLogs: List<Log>,
    val refined: List<Log>,
    val detectionResults: Map<String, List<DetectionResult>>,
)

class DetectionResult(
    val detectionKey: String,
    val detectionIndex: Int,
    val logIndex: Int
) {
    val detectionIndexInView = detectionIndex + 1
}

interface Detection {
    val key: String
    val detectedStyle: SpanStyle
    fun detect(log: Log): List<IntRange> // Detected ranges
}

class FindResult(
    val focusingResult: DetectionResult,
    val detectionResults: List<DetectionResult>,
) {
    val totalCount = detectionResults.size
}
