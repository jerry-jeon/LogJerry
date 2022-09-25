package log.refine

import table.ColumnType

data class Filter(
    val columnType: ColumnType,
    val text: String
)
