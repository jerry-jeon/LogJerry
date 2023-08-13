package com.jerryjeon.logjerry.log

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString

sealed class LogContentView {

    class Simple(val str: AnnotatedString) : LogContentView()

    class Block(
        val cation: String,
        val str: AnnotatedString,
        val background: Color?,
        val lineCount: Int
    ) : LogContentView()
}
