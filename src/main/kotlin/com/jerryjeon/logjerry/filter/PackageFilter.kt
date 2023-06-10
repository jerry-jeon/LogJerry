package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log

data class PackageFilter(
    val packageName: String?,
    val frequency: Int,
    val include: Boolean
)

data class PackageFilters(
    val filters: List<PackageFilter>
) : LogFilter {
    override fun filter(log: Log): Boolean {
        return filters.any {
            it.include && it.packageName == log.packageName
        }
    }
}
