package com.jerryjeon.logjerry.table

import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Header(
    val number: ColumnInfo = ColumnInfo(ColumnType.Number, 50, true),
    val date: ColumnInfo = ColumnInfo(ColumnType.Date, 100, false),
    val time: ColumnInfo = ColumnInfo(ColumnType.Time, 100, true),
    val pid: ColumnInfo = ColumnInfo(ColumnType.Pid, 50, false),
    val tid: ColumnInfo = ColumnInfo(ColumnType.Tid, 50, false),
    val packageName: ColumnInfo = ColumnInfo(ColumnType.PackageName, 130, true),
    val priority: ColumnInfo = ColumnInfo(ColumnType.Priority, 40, true),
    val tag: ColumnInfo = ColumnInfo(ColumnType.Tag, 200, true),
    val log: ColumnInfo = ColumnInfo(ColumnType.Log, null, true),
) {

    val asColumnList: List<ColumnInfo> = listOf(number, date, time, pid, tid, packageName, priority, tag, log)

    operator fun get(columnType: ColumnType): ColumnInfo {
        return when (columnType) {
            ColumnType.Number -> number
            ColumnType.Date -> date
            ColumnType.Time -> time
            ColumnType.Pid -> pid
            ColumnType.Tid -> tid
            ColumnType.PackageName -> packageName
            ColumnType.Priority -> priority
            ColumnType.Tag -> tag
            ColumnType.Log -> log
        }
    }

    fun copyOf(columnType: ColumnType, columnInfo: ColumnInfo): Header {
        return when (columnType) {
            ColumnType.Number -> copy(number = columnInfo)
            ColumnType.Date -> copy(date = columnInfo)
            ColumnType.Time -> copy(time = columnInfo)
            ColumnType.Pid -> copy(pid = columnInfo)
            ColumnType.Tid -> copy(tid = columnInfo)
            ColumnType.PackageName -> copy(packageName = columnInfo)
            ColumnType.Priority -> copy(priority = columnInfo)
            ColumnType.Tag -> copy(tag = columnInfo)
            ColumnType.Log -> copy(log = columnInfo)
        }
    }

    companion object {
        val file = File(System.getProperty("java.io.tmpdir"), "LogJerryPreferencesHeader.json")
    }
}
