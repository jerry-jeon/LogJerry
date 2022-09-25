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
        NumberCell(header.number, log)
        divider()
        DateCell(header.date, log)
        divider()
        TimeCell(header.time, log)
        divider()
        PidCell(header.pid, log)
        divider()
        TidCell(header.tid, log)
        divider()
        PackagerNameCell(header.packageName, log)
        divider()
        PriorityCell(header.priority, log)
        divider()
        TagCell(header.tag, log)
        divider()
        LogCell(header.log, log)
    }
}

@Composable
private fun RowScope.NumberCell(number: ColumnInfo, log: Log) {
    if (number.visible) {
        Text(text = log.number.toString(), modifier = applyWidth(number.width))
    }
}
@Composable
private fun RowScope.DateCell(date: ColumnInfo, log: Log) {
    if (date.visible) {
        Text(text = log.date, modifier = applyWidth(date.width))
    }
}

@Composable
private fun RowScope.TimeCell(time: ColumnInfo, log: Log) {
    if (time.visible) {
        Text(log.time, modifier = applyWidth(time.width))
    }
}

@Composable
private fun RowScope.PidCell(pid: ColumnInfo, log: Log) {
    if (pid.visible) {
        Text(log.pid.toString(), modifier = applyWidth(pid.width))
    }
}

@Composable
private fun RowScope.TidCell(tid: ColumnInfo, log: Log) {
    if (tid.visible) {
        Text(log.tid.toString(), modifier = applyWidth(tid.width))
    }
}

@Composable
private fun RowScope.PackagerNameCell(packageName: ColumnInfo, log: Log) {
    if (packageName.visible) {
        Text(log.packageName ?: "?", modifier = applyWidth(packageName.width))
    }
}

@Composable
private fun RowScope.PriorityCell(priority: ColumnInfo, log: Log) {
    if (priority.visible) {
        Text(log.priority, modifier = applyWidth(priority.width))
    }
}

@Composable
private fun RowScope.TagCell(tag: ColumnInfo, log: Log) {
    if (tag.visible) {
        Text(log.tag, modifier = applyWidth(tag.width))
    }
}

@Composable
private fun RowScope.LogCell(logHeader: ColumnInfo, log: Log) {
    if (logHeader.visible) {
        Text(log.log, modifier = applyWidth(logHeader.width))
    }
}

@Preview
@Composable
fun LogRowPreview() {
    MyTheme {
        LogRow(SampleData.log, Header.default) { Divider() }
    }
}
