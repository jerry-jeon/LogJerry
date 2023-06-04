package com.jerryjeon.logjerry.logview

import com.jerryjeon.logjerry.detector.Detection
import com.jerryjeon.logjerry.detector.DetectorKey
import com.jerryjeon.logjerry.detector.MarkDetection
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogContentView
import java.time.Duration

class RefinedLog(
    val log: Log,
    val detections: Map<DetectorKey, List<Detection>>,
    val logContentViews: List<LogContentView>,
    val timeGap: Duration?
) {
    val mark = detections[DetectorKey.Mark]?.firstOrNull() as? MarkDetection
    val marked = mark != null

    fun durationBetween(other: RefinedLog): Duration? {
        return this.log.durationBetween(other.log)
    }
}

fun Duration.toHumanReadable(): String {
    val hours: Long = toHours()
    val minutes: Long = toMinutes() % 60
    val seconds: Long = seconds % 60

    return when {
        hours > 0 -> {
            "${hours}h ${minutes}m ${seconds}s"
        }
        minutes > 0 -> {
            "${minutes}m ${seconds}s"
        }
        else -> {
            "${toMillis()}ms"
        }
    }
}
