package com.jerryjeon.logjerry.table

data class ColumnInfo(
    val columnType: ColumnType,
    val width: Int?, // null means new weight 1f
    val visible: Boolean
)
