package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log

data class TagFilter(
    val tag: String?,
    val frequency: Int,
    val include: Boolean
)

data class TagFilters(
    val filters: List<TagFilter>
) : LogFilter {
    override fun filter(log: Log): Boolean {
        return filters.any {
            it.include && it.tag == log.tag
        }
    }
}
