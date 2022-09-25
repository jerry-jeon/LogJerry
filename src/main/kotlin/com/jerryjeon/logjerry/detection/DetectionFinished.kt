package com.jerryjeon.logjerry.detection

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.log.Log

data class DetectionFinished(
    val detectionFinishedLogs: List<DetectionFinishedLog>,
    val allDetections: Map<DetectorKey, List<Detection>>,
    val detectors: List<Detector<*>>
)

data class DetectionFinishedLog(
    val log: Log,
    val detections: Map<DetectorKey, List<Detection>>
)
