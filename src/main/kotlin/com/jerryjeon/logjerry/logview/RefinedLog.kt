package com.jerryjeon.logjerry.logview

import com.jerryjeon.logjerry.detection.DetectionFinishedLog
import com.jerryjeon.logjerry.log.LogContentView

class RefinedLog(
    val detectionFinishedLog: DetectionFinishedLog,
    val logContentViews: List<LogContentView>
)
