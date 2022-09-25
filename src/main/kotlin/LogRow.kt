import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LogRow(
    log: Log,
    header: Header,
    divider: @Composable RowScope.() -> Unit
) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        header.asColumnList.forEach { columnInfo ->
            if (columnInfo.visible) {
                CellByColumnType(columnInfo, log)
                divider()
            }
        }
    }
}

@Composable fun RowScope.CellByColumnType(columnInfo: ColumnInfo, log: Log) {
    when (columnInfo.columnType) {
        ColumnType.Number -> NumberCell(columnInfo, log)
        ColumnType.Date -> DateCell(columnInfo, log)
        ColumnType.Time -> TimeCell(columnInfo, log)
        ColumnType.PID -> PidCell(columnInfo, log)
        ColumnType.TID -> TidCell(columnInfo, log)
        ColumnType.PackageName -> PackagerNameCell(columnInfo, log)
        ColumnType.Priority -> PriorityCell(columnInfo, log)
        ColumnType.Tag -> TagCell(columnInfo, log)
        ColumnType.Log -> LogCell(columnInfo, log)
    }
}
@Composable
private fun RowScope.NumberCell(number: ColumnInfo, log: Log) {
    Text(text = log.number.toString(), modifier = applyWidth(number.width))
}
@Composable
private fun RowScope.DateCell(date: ColumnInfo, log: Log) {
    Text(text = log.date, modifier = applyWidth(date.width))
}

@Composable
private fun RowScope.TimeCell(time: ColumnInfo, log: Log) {
    Text(log.time, modifier = applyWidth(time.width))
}

@Composable
private fun RowScope.PidCell(pid: ColumnInfo, log: Log) {
    Text(log.pid.toString(), modifier = applyWidth(pid.width))
}

@Composable
private fun RowScope.TidCell(tid: ColumnInfo, log: Log) {
    Text(log.tid.toString(), modifier = applyWidth(tid.width))
}

@Composable
private fun RowScope.PackagerNameCell(packageName: ColumnInfo, log: Log) {
    Text(log.packageName ?: "?", modifier = applyWidth(packageName.width))
}

@Composable
private fun RowScope.PriorityCell(priority: ColumnInfo, log: Log) {
    Text(log.priority, modifier = applyWidth(priority.width))
}

@Composable
private fun RowScope.TagCell(tag: ColumnInfo, log: Log) {
    Text(log.tag, modifier = applyWidth(tag.width))
}

@Composable
private fun RowScope.LogCell(logHeader: ColumnInfo, log: Log) {
    Text(log.log, modifier = applyWidth(logHeader.width))
}

@Preview
@Composable
fun LogRowPreview() {
    MyTheme {
        LogRow(SampleData.log, Header.default) { Divider() }
    }
}
