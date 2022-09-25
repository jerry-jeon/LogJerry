package com.jerryjeon.logjerry.logview

import com.jerryjeon.logjerry.detector.Detection

// TODO The role is not clear... It should be refactored
data class DetectionView(
    val detection: Detection,
    val expanded: Boolean
)
