package com.jerryjeon.logjerry.detection

import androidx.compose.ui.text.SpanStyle

// When we support custom detection, then key should be String
enum class DetectionKey {
    Keyword, Exception, Json;
}

interface Detection<T : DetectionResult> {
    val key: DetectionKey
    fun detect(logStr: String, logIndex: Int): List<T>
}

interface DetectionResult {
    val key: DetectionKey
    val range: IntRange
    val logIndex: Int
    val style: SpanStyle
}

data class DetectionFocus(
    val key: DetectionKey,
    val currentIndex: Int,
    val focusing: DetectionResult?,
    val results: List<DetectionResult>,
) {
    val totalCount = results.size
    val currentIndexInView = currentIndex + 1
}
