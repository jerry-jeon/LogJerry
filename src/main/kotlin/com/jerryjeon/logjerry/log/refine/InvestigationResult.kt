package com.jerryjeon.logjerry.log.refine

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.jerryjeon.logjerry.detection.DetectionKey
import com.jerryjeon.logjerry.detection.DetectionResult
import com.jerryjeon.logjerry.detection.JsonDetectionResult
import com.jerryjeon.logjerry.log.Log

data class InvestigationResult(
    val originalLogs: List<Log>,
    val detectionFinishedLogs: List<DetectionFinishedLog>,
    val allDetectionResults: Map<DetectionKey, List<DetectionResult>>,
)

data class DetectionFinishedLog(
    val log: Log,
    val detectionResults: Map<DetectionKey, List<DetectionResult>>
)

data class InvestigationResultView(
    val refinedLogs: List<RefinedLog>,
    val allDetectionResults: Map<DetectionKey, List<DetectionResult>>,
)

data class RefinedLog(
    val detectionFinishedLog: DetectionFinishedLog,
    val logContents: List<LogContent>
)

// AnnotatedLog

// TODO move to other place
sealed class LogContent {

    class Simple(val str: AnnotatedString) : LogContent()

    class Json(
        val str: AnnotatedString,
        val background: Color?,
        val activated: Boolean,
        val jsonDetectionResult: JsonDetectionResult
    ) : LogContent()
}
