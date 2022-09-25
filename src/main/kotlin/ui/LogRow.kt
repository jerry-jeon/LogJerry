@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import detection.JsonDetectionResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import log.refine.RefinedLog
import table.ColumnInfo
import table.ColumnType
import table.Header

val json = Json { prettyPrint = true }

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
                if (columnInfo.columnType.showDivider) {
                    divider()
                }
            }
        }
    }
}

@Composable
fun RowScope.CellByColumnType(columnInfo: ColumnInfo, refinedLog: RefinedLog) {
    val log = refinedLog.log
    when (columnInfo.columnType) {
        ColumnType.Number -> NumberCell(columnInfo, log)
        ColumnType.Date -> DateCell(columnInfo, log)
        ColumnType.Time -> TimeCell(columnInfo, log)
        ColumnType.Pid -> PidCell(columnInfo, log)
        ColumnType.Tid -> TidCell(columnInfo, log)
        ColumnType.PackageName -> PackagerNameCell(columnInfo, log)
        ColumnType.Priority -> PriorityCell(columnInfo, log)
        ColumnType.Tag -> TagCell(columnInfo, log)
        ColumnType.Detection -> DetectionCell(columnInfo, refinedLog)
        ColumnType.Log -> LogCell(columnInfo, refinedLog)
    }
}

@Composable
private fun RowScope.NumberCell(number: ColumnInfo, log: Log) {
    Text(text = log.number.toString(), style = MaterialTheme.typography.body2, modifier = this.cellDefaultModifier(number.width))
}

@Composable
private fun RowScope.DateCell(date: ColumnInfo, log: Log) {
    Text(text = log.date, style = MaterialTheme.typography.body2, modifier = this.cellDefaultModifier(date.width))
}

@Composable
private fun RowScope.TimeCell(time: ColumnInfo, log: Log) {
    Text(log.time, style = MaterialTheme.typography.body2, modifier = this.cellDefaultModifier(time.width))
}

@Composable
private fun RowScope.PidCell(pid: ColumnInfo, log: Log) {
    Text(log.pid.toString(), style = MaterialTheme.typography.body2, modifier = this.cellDefaultModifier(pid.width))
}

@Composable
private fun RowScope.TidCell(tid: ColumnInfo, log: Log) {
    Text(log.tid.toString(), style = MaterialTheme.typography.body2, modifier = this.cellDefaultModifier(tid.width))
}

@Composable
private fun RowScope.PackagerNameCell(packageName: ColumnInfo, log: Log) {
    Text(log.packageName ?: "?", style = MaterialTheme.typography.body2, modifier = this.cellDefaultModifier(packageName.width))
}

@Composable
private fun RowScope.PriorityCell(priority: ColumnInfo, log: Log) {
    Text(log.priority.text, style = MaterialTheme.typography.body2, modifier = this.cellDefaultModifier(priority.width))
}

@Composable
private fun RowScope.TagCell(tag: ColumnInfo, log: Log) {
    Text(log.tag, style = MaterialTheme.typography.body2, modifier = this.cellDefaultModifier(tag.width))
}

@Composable
private fun RowScope.DetectionCell(button: ColumnInfo, refinedLog: RefinedLog) {
    var showPrettyJsonDialog: Pair<Boolean, JsonObject>? by remember { mutableStateOf(null) }

    Column(modifier = this.cellDefaultModifier(button.width)) {
        refinedLog.detectionResults.values.flatten().forEach { result ->
            when (result) {
                is JsonDetectionResult -> {
                    result.jsonList.forEachIndexed { index, jsonObject ->
                        TextButton(onClick = { showPrettyJsonDialog = true to jsonObject }) {
                            Row {
                                Text("{ }")
                                Text("${index + 1}", fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    showPrettyJsonDialog?.let { (_, jsonObject) ->
        Dialog(
            onCloseRequest = { showPrettyJsonDialog = null },
            title = "Pretty Json",
            state = DialogState(width = 800.dp, height = 600.dp),
            onPreviewKeyEvent = { keyEvent ->
                if (keyEvent.isMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown) {
                    showPrettyJsonDialog = null
                }
                false
            }
        ) {
            SelectionContainer {
                Text(json.encodeToString(JsonObject.serializer(), jsonObject), modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun RowScope.LogCell(logHeader: ColumnInfo, refinedLog: RefinedLog) {
    val log = refinedLog.log
    Box(modifier = this.cellDefaultModifier(logHeader.width)) {
        SelectionContainer {
            // TODO make if configurable
            val style =
                if (log.priority == Priority.Error) TextStyle.Default.copy(color = Color.Red) else TextStyle.Default
            Text(refinedLog.annotatedLog, modifier = Modifier, style = style)
        }
    }
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
