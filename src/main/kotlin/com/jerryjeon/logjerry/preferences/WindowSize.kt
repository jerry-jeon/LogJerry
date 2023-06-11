package com.jerryjeon.logjerry.preferences

import kotlinx.serialization.Serializable

// null to maximize
@Serializable
data class WindowSize(
    val width: Int,
    val height: Int,
)
