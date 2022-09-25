package com.jerryjeon.logjerry.table

data class Header(
    val number: ColumnInfo,
    val date: ColumnInfo,
    val time: ColumnInfo,
    val pid: ColumnInfo,
    val tid: ColumnInfo,
    val packageName: ColumnInfo,
    val priority: ColumnInfo,
    val tag: ColumnInfo,
    val log: ColumnInfo,
    val detection: ColumnInfo,
) {

    val asColumnList: List<ColumnInfo> = listOf(number, date, time, pid, tid, packageName, priority, tag, log, detection)

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
            ColumnType.Detection -> detection
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
            ColumnType.Detection -> copy(detection = columnInfo)
        }
    }

    companion object {
        val default = Header(
            number = ColumnInfo(ColumnType.Number, 50, true),
            date = ColumnInfo(ColumnType.Date, 100, false),
            time = ColumnInfo(ColumnType.Time, 100, true),
            pid = ColumnInfo(ColumnType.Pid, 50, false),
            tid = ColumnInfo(ColumnType.Tid, 50, false),
            packageName = ColumnInfo(ColumnType.PackageName, 130, true),
            priority = ColumnInfo(ColumnType.Priority, 40, true),
            tag = ColumnInfo(ColumnType.Tag, 200, true),
            log = ColumnInfo(ColumnType.Log, null, true),
            detection = ColumnInfo(ColumnType.Detection, 50, true)
        )
    }
}
