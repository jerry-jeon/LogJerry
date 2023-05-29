package com.jerryjeon.logjerry.logview

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectorKey

data class InvestigationView(
    val refinedLogs: List<RefinedLog>,
    val allDetectionViews: Map<DetectorKey, List<Detection>>,
)
