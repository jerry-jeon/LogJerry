package com.jerryjeon.logjerry.detector

import androidx.compose.ui.text.SpanStyle

interface Detector<T : Detection> {
    val key: DetectorKey
    fun detect(logStr: String, logIndex: Int): List<T>
}

interface Detection {
    val id: String
    val key: DetectorKey
    val range: IntRange
    val logIndex: Int
    val style: SpanStyle
}
