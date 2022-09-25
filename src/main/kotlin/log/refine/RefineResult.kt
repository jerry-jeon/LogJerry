package log.refine

import DetectionKey
import DetectionResult
import Log
import androidx.compose.ui.text.AnnotatedString

data class RefineResult(
    val originalLogs: List<Log>,
    val refined: List<RefinedLog>,
    val allDetectionResults: Map<DetectionKey, List<DetectionResult>>,
)

data class RefinedLog(
    val log: Log,
    val annotatedLog: AnnotatedString,
    val detectionResults: Map<DetectionKey, List<DetectionResult>>
)