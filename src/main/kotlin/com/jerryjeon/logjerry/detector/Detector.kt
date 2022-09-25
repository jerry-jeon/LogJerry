package com.jerryjeon.logjerry.detector

import androidx.compose.ui.text.SpanStyle

// When we support custom detection, then key should be String
enum class DetectionKey {
    Keyword, Exception, Json;
}

interface Detector<T : Detection> {
    val key: DetectionKey
    fun detect(logStr: String, logIndex: Int): List<T>
}

interface Detection {
    val id: String
    val key: DetectionKey
    val range: IntRange
    val logIndex: Int
    val style: SpanStyle
}

data class DetectionFocus(
    val key: DetectionKey,
    val currentIndex: Int,
    val focusing: Detection?,
    val allDetections: List<Detection>,
) {
    val totalCount = allDetections.size
    val currentIndexInView = currentIndex + 1
}
