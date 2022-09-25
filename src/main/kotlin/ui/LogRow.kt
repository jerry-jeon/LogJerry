import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import log.SampleData
import table.ColumnInfo
import table.ColumnType
import table.Header

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
    Text(text = log.number.toString(), modifier = this.cellDefaultModifier(number.width))
}
@Composable
private fun RowScope.DateCell(date: ColumnInfo, log: Log) {
    Text(text = log.date, modifier = this.cellDefaultModifier(date.width))
}

@Composable
private fun RowScope.TimeCell(time: ColumnInfo, log: Log) {
    Text(log.time, modifier = this.cellDefaultModifier(time.width))
}

@Composable
private fun RowScope.PidCell(pid: ColumnInfo, log: Log) {
    Text(log.pid.toString(), modifier = this.cellDefaultModifier(pid.width))
}

@Composable
private fun RowScope.TidCell(tid: ColumnInfo, log: Log) {
    Text(log.tid.toString(), modifier = this.cellDefaultModifier(tid.width))
}

@Composable
private fun RowScope.PackagerNameCell(packageName: ColumnInfo, log: Log) {
    Text(log.packageName ?: "?", modifier = this.cellDefaultModifier(packageName.width))
}

@Composable
private fun RowScope.PriorityCell(priority: ColumnInfo, log: Log) {
    Text(log.priority.text, modifier = this.cellDefaultModifier(priority.width))
}

@Composable
private fun RowScope.TagCell(tag: ColumnInfo, log: Log) {
    Text(log.tag, modifier = this.cellDefaultModifier(tag.width))
}

@Composable
private fun RowScope.LogCell(logHeader: ColumnInfo, log: Log) {
    // TODO make if configurable
    val style = if (log.priority == Priority.Error) TextStyle.Default.copy(color = Color.Red) else TextStyle.Default
    Text(log.log, modifier = this.cellDefaultModifier(logHeader.width), style = style)
}

@Preview
@Composable
fun LogRowPreview() {
    MyTheme {
        LogRow(SampleData.log, Header.default) { Divider() }
    }
}
fun RowScope.cellDefaultModifier(width: Int?, modifier: Modifier = Modifier): Modifier {
    return applyWidth(width, modifier)
        .padding(4.dp)
}
