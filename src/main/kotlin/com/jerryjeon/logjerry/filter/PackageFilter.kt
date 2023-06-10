package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log

data class PackageFilter(
    val packageName: String?,
    val frequency: Int,
    val include: Boolean
) : LogFilter {
    override fun filter(log: Log): Boolean {
        return if (include) {
            log.packageName == packageName
        } else {
            log.packageName != packageName
        }
    }
}

data class PackageFilters(
    val filters: List<PackageFilter>
) : LogFilter {
    override fun filter(log: Log): Boolean {
        return filters.any { it.filter(log) }
    }
}
