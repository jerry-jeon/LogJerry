package com.jerryjeon.logjerry.filter

import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.table.ColumnType

data class TextFilter(
    val columnType: ColumnType,
    val textFilterType: TextFilterType,
    val text: String
) : LogFilter {
    override fun filter(log: Log): Boolean {
        return when (textFilterType) {
            TextFilterType.Include -> {
                when (columnType) {
                    ColumnType.PackageName -> text in (log.packageName ?: "")
                    ColumnType.Tag -> if (log.tag == null) true else text in log.tag
                    ColumnType.Log -> text in log.log
                    else -> throw NotImplementedError("Not supported filter : $columnType")
                }
            }
            TextFilterType.Exclude -> {
                when (columnType) {
                    ColumnType.PackageName -> text !in (log.packageName ?: "")
                    ColumnType.Tag -> if (log.tag == null) true else text !in log.tag
                    ColumnType.Log -> text !in log.log
                    else -> throw NotImplementedError("Not supported filter : $columnType")
                }
            }
        }
    }
}

enum class TextFilterType {
    Include,
    Exclude
}
