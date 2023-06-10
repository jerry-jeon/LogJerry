package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log

data class TagFilter(
    val tag: String?,
    val frequency: Int,
    val include: Boolean
) : LogFilter {
    override fun filter(log: Log): Boolean {
        return if (include) {
            log.tag == tag
        } else {
            log.tag != tag
        }
    }
}

data class TagFilters(
    val filters: List<TagFilter>
) : LogFilter {
    override fun filter(log: Log): Boolean {
        return filters.any { it.filter(log) }
    }
}
