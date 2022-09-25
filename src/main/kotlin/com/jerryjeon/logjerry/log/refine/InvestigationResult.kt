package com.jerryjeon.logjerry.log.refine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.jerryjeon.logjerry.detection.Detection
import com.jerryjeon.logjerry.detection.DetectionKey
import com.jerryjeon.logjerry.detection.DetectionResult
import com.jerryjeon.logjerry.detection.JsonDetectionResult
import com.jerryjeon.logjerry.log.Log

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

// TODO move to other place
sealed class LogContentView {

    class Simple(val str: AnnotatedString) : LogContentView()

    class Json(
        val str: AnnotatedString,
        val background: Color?,
        val jsonDetectionResult: JsonDetectionResult
    ) : LogContentView()
}
