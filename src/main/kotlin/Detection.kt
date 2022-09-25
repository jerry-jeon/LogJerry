import androidx.compose.ui.text.SpanStyle

interface Detection {
    val key: String
    val detectedStyle: SpanStyle
    fun detect(log: Log): List<IntRange> // Detected ranges
}

class DetectionResult(
    val detectionKey: String,
    val detectionIndex: Int,
    val logIndex: Int
) {
    val detectionIndexInView = detectionIndex + 1
}

class DetectionResultFocus(
    val focusingResult: DetectionResult,
    val detectionResults: List<DetectionResult>,
) {
    val totalCount = detectionResults.size
}
