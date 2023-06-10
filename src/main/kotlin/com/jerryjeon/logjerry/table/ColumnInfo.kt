package com.jerryjeon.logjerry.table

import kotlinx.serialization.Serializable

@Serializable
data class ColumnInfo(
    val columnType: ColumnType,
    val width: Int?, // null means new weight 1f
    val visible: Boolean
)
