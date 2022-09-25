package com.jerryjeon.logjerry.transformation

import com.jerryjeon.logjerry.detection.Detector
import com.jerryjeon.logjerry.log.refine.LogFilter

data class Transformation(
    val filters: List<LogFilter>,
    val detectors: List<Detector<*>>
)
