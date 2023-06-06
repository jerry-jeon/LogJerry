package com.jerryjeon.logjerry.log

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import com.jerryjeon.logjerry.detector.JsonDetection

sealed class LogContentView {

    class Simple(val str: AnnotatedString) : LogContentView()

    class Json(
        val str: AnnotatedString,
        val background: Color?,
        val jsonDetection: JsonDetection,
        val lineCount: Int
    ) : LogContentView()
}
