package com.jerryjeon.logjerry.log.refine

import Log
import Priority

data class PriorityFilter(val priority: Priority) : LogFilter {
    override fun filter(log: Log): Boolean {
        return priority.level <= log.priority.level
    }
}