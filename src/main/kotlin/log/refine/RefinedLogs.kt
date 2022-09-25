package log.refine

import DetectionKey
import DetectionResult
import Log

data class RefinedLogs(
    val originalLogs: List<Log>,
    val refined: List<Log>,
    val detectionResults: Map<DetectionKey, List<DetectionResult>>,
)
