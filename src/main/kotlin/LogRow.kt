import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LogRow(
    log: Log,
    header: Header
) {
    Row {
        DateCell(header.date, log)
        Spacer(Modifier.padding(5.dp))
        TimeCell(header.time, log)
        Spacer(Modifier.padding(5.dp))
        PidCell(header.pid, log)
        Spacer(Modifier.padding(5.dp))
        TidCell(header.tid, log)
        Spacer(Modifier.padding(5.dp))
        PackagerNameCell(header.packageName, log)
        Spacer(Modifier.padding(5.dp))
        PriorityCell(header.priority, log)
        Spacer(Modifier.padding(5.dp))
        TagCell(header.tag, log)
        Spacer(Modifier.padding(5.dp))
        LogCell(header.log, log)
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
        LogRow(SampleData.log, Header.default)
    }
}
