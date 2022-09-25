package com.jerryjeon.logjerry.log

import com.jerryjeon.logjerry.detector.JsonDetection

sealed class LogContent {
    class Text(val text: String, val jsonDetections: List<JsonDetection>) : LogContent()
    class ExpandedJson(val text: String, val jsonDetection: JsonDetection) : LogContent()
}
