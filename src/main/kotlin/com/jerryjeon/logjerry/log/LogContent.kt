package com.jerryjeon.logjerry.log

import com.jerryjeon.logjerry.detector.JsonDetection

sealed class LogContent {
    class Simple(val text: String) : LogContent()
    class Json(val text: String, val jdr: JsonDetection) : LogContent()
}
