package com.jerryjeon.logjerry.log.refine

import com.jerryjeon.logjerry.detection.Detection
import com.jerryjeon.logjerry.detection.DetectionKey
import com.jerryjeon.logjerry.detection.DetectionResult
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogContentView

data class InvestigationResult(
    val originalLogs: List<Log>,
    val detectionFinishedLogs: List<DetectionFinishedLog>,
    val allDetectionResults: Map<DetectionKey, List<DetectionResult>>,
    val detections: List<Detection<*>>
)

data class DetectionFinishedLog(
    val log: Log,
    val detectionResults: Map<DetectionKey, List<DetectionResult>>
)

// TODO The role is not clear... It should be refactored
data class DetectionResultView(
    val detectionResult: DetectionResult,
    val expanded: Boolean
)

data class InvestigationResultView(
    val refinedLogs: List<RefinedLog>,
    val allDetectionResults: Map<DetectionKey, List<DetectionResultView>>,
)

class RefinedLog(
    val detectionFinishedLog: DetectionFinishedLog,
    val logContentViews: List<LogContentView>
)

// AnnotatedLog
