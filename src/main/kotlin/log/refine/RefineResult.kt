package log.refine

import DetectionKey
import DetectionResult
import Log

data class RefineResult(
    val originalLogs: List<Log>,
    val refined: List<RefinedLog>,
    val allDetectionResults: Map<DetectionKey, List<DetectionResult>>,
)

data class RefinedLog(
    val originalLog: Log,
    val refined: Log, // TODO I think that just taking the annotatedString looks simpler
    val detectionResults: Map<DetectionKey, List<DetectionResult>>
)