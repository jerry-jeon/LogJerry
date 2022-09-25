package com.jerryjeon.logjerry.log.refine

import Log
import com.jerryjeon.logjerry.table.ColumnType

data class TextFilter(
    val columnType: ColumnType,
    val text: String
) : LogFilter {
    override fun filter(log: Log): Boolean {
        return when (columnType) {
            ColumnType.PackageName -> text in (log.packageName ?: "")
            ColumnType.Tag -> text in log.tag
            ColumnType.Log -> text in log.log
            else -> throw NotImplementedError("Not supported filter : $columnType")
        }
    }
}
