package com.jerryjeon.logjerry.detector

data class DetectionFocus(
    val key: DetectorKey,
    val currentIndex: Int,
    val focusing: Detection?,
    val allDetections: List<Detection>,
) {
    val totalCount = allDetections.size
    val currentIndexInView = currentIndex + 1
}
