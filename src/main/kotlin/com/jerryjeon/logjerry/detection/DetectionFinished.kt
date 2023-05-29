package com.jerryjeon.logjerry.detection

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.Detector
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.log.Log

data class DetectionFinished(
    val detectors: List<Detector<*>>,
    val detectionsByLog: Map<Log, Map<DetectorKey, List<Detection>>>,
    val allDetections: Map<DetectorKey, List<Detection>>,
)
