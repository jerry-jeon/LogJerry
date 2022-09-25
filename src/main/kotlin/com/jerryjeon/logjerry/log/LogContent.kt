package com.jerryjeon.logjerry.log

import com.jerryjeon.logjerry.detection.JsonDetectionResult

sealed class LogContent {
    class Simple(val text: String) : LogContent()
    class Json(val text: String, val jdr: JsonDetectionResult) : LogContent()
}
