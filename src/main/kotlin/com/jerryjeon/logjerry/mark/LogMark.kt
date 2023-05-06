package com.jerryjeon.logjerry.mark

import androidx.compose.ui.graphics.Color
import com.jerryjeon.logjerry.log.Log

data class LogMark(
    val log: Log,
    val note: String,
    val color: Color
)