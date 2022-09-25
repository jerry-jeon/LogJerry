package com.jerryjeon.logjerry.log.refine

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectionKey
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogContentView

data class Investigation(
    val originalLogs: List<Log>,
    val detectionFinishedLogs: List<DetectionFinishedLog>,
    val allDetections: Map<DetectionKey, List<Detection>>,
    val detectors: List<Detector<*>>
)

data class DetectionFinishedLog(
    val log: Log,
    val detections: Map<DetectionKey, List<Detection>>
)

// TODO The role is not clear... It should be refactored
data class DetectionView(
    val detection: Detection,
    val expanded: Boolean
)

data class InvestigationView(
    val refinedLogs: List<RefinedLog>,
    val allDetectionViews: Map<DetectionKey, List<DetectionView>>,
)

class RefinedLog(
    val detectionFinishedLog: DetectionFinishedLog,
    val logContentViews: List<LogContentView>
)

// AnnotatedLog
