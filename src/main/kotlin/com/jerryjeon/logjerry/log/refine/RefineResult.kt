package com.jerryjeon.logjerry.log.refine

import androidx.compose.ui.text.AnnotatedString
import com.jerryjeon.logjerry.detection.DetectionKey
import com.jerryjeon.logjerry.detection.DetectionResult
import com.jerryjeon.logjerry.log.Log

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

// JsonDetectionResult annotation affects other DetectionResults, so it seems
// annotate(AnnotatedString, List) is needed

// If detectionResult is activated...?
//
