package com.jerryjeon.logjerry.logview

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.MarkDetection
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogContentView

class RefinedLog(
    val log: Log,
    val detections: Map<DetectorKey, List<Detection>>,
    val logContentViews: List<LogContentView>
) {
    val mark = detections[DetectorKey.Mark]?.firstOrNull() as? MarkDetection
    val marked = mark != null
}
