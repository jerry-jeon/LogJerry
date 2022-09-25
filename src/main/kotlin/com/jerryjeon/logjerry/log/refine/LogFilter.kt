package com.jerryjeon.logjerry.log.refine

import com.jerryjeon.logjerry.log.Log

interface LogFilter {
    fun filter(log: Log): Boolean
}

