package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.Priority

data class HiddenFilter(val hiddenLogIndices: Set<Int>) : LogFilter {
    override fun filter(log: Log): Boolean {
        return log.index !in hiddenLogIndices
    }
}
