package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.Priority

data class PriorityFilter(val priority: Priority) : LogFilter {
    override fun filter(log: Log): Boolean {
        return priority.level <= log.priority.level
    }
}
