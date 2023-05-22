package com.jerryjeon.logjerry.detector

data class DetectionSelection(
    val key: DetectorKey,
    val currentIndex: Int,
    val selected: Detection?,
    val allDetections: List<Detection>,
) {
    val totalCount = allDetections.size
    val currentIndexInView = currentIndex + 1
}
