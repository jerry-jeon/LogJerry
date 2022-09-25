import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import detection.JsonDetectionResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import log.refine.RefinedLog
import table.ColumnInfo
import table.ColumnType
import table.Header

@Composable
fun LogRow(
    refinedLog: RefinedLog,
    header: Header,
    divider: @Composable RowScope.() -> Unit
) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        header.asColumnList.forEach { columnInfo ->
            if (columnInfo.visible) {
                CellByColumnType(columnInfo, refinedLog)
                divider()
            }
        }
    }
}

@Composable fun RowScope.CellByColumnType(columnInfo: ColumnInfo, refinedLog: RefinedLog) {
    val log = refinedLog.originalLog
    when (columnInfo.columnType) {
        ColumnType.Number -> NumberCell(columnInfo, log)
        ColumnType.Date -> DateCell(columnInfo, log)
        ColumnType.Time -> TimeCell(columnInfo, log)
        ColumnType.PID -> PidCell(columnInfo, log)
        ColumnType.TID -> TidCell(columnInfo, log)
        ColumnType.PackageName -> PackagerNameCell(columnInfo, log)
        ColumnType.Priority -> PriorityCell(columnInfo, log)
        ColumnType.Tag -> TagCell(columnInfo, log)
        ColumnType.B -> ButtonCell(columnInfo, refinedLog)
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
private fun RowScope.ButtonCell(logHeader: ColumnInfo, refinedLog: RefinedLog) {
    var showDialog by remember { mutableStateOf(false) }

    if(refinedLog.detectionResults[DetectionKey.Json] != null) {
        TextButton(onClick = { showDialog = true }) {
            Text("{ }")
        }
    }

    if(showDialog) {
        Dialog(
            onCloseRequest = { showDialog = false },
            title = "Pretty Json",
            state = DialogState(width = 800.dp, height = 600.dp)
        ) {
            val json = Json { prettyPrint = true }
            Text(json.encodeToString(JsonObject.serializer(), (refinedLog.detectionResults.get(DetectionKey.Json)!!.get(0) as JsonDetectionResult).jsonList.get(0)))
        }
    }
}

@Composable
private fun RowScope.LogCell(logHeader: ColumnInfo, log: Log) {
    // TODO make if configurable
    val style = if (log.priority == Priority.Error) TextStyle.Default.copy(color = Color.Red) else TextStyle.Default
    Text(log.log, modifier = this.cellDefaultModifier(logHeader.width), style = style)
}

/*
@Preview
@Composable
fun LogRowPreview() {
    MyTheme {
        LogRow(SampleData.log, Header.default) { Divider() }
    }
}
*/
fun RowScope.cellDefaultModifier(width: Int?, modifier: Modifier = Modifier): Modifier {
    return applyWidth(width, modifier)
        .padding(4.dp)
}
