package table

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
) {

    val asColumnList: List<ColumnInfo> = listOf(number, date, time, pid, tid, packageName, priority, tag, log)

    operator fun get(columnType: ColumnType): ColumnInfo {
        return when (columnType) {
            ColumnType.Number -> number
            ColumnType.Date -> date
            ColumnType.Time -> time
            ColumnType.PID -> pid
            ColumnType.TID -> tid
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
            ColumnType.PID -> copy(pid = columnInfo)
            ColumnType.TID -> copy(tid = columnInfo)
            ColumnType.PackageName -> copy(packageName = columnInfo)
            ColumnType.Priority -> copy(priority = columnInfo)
            ColumnType.Tag -> copy(tag = columnInfo)
            ColumnType.Log -> copy(log = columnInfo)
        }
    }

    companion object {
        val default = Header(
            number = ColumnInfo(ColumnType.Number, 100, true),
            date = ColumnInfo(ColumnType.Date, 100, false),
            time = ColumnInfo(ColumnType.Time, 110, true),
            pid = ColumnInfo(ColumnType.PID, 50, false),
            tid = ColumnInfo(ColumnType.TID, 50, false),
            packageName = ColumnInfo(ColumnType.PackageName, 130, true),
            priority = ColumnInfo(ColumnType.Priority, 70, true),
            tag = ColumnInfo(ColumnType.Tag, 200, true),
            log = ColumnInfo(ColumnType.Log, null, true),
        )
    }
}
