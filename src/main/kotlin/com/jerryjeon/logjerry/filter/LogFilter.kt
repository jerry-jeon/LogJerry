package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log

interface LogFilter {
    fun filter(log: Log): Boolean
}
