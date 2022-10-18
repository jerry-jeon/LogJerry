@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Expand
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import com.jerryjeon.logjerry.detection.DetectionFinishedLog
import com.jerryjeon.logjerry.detector.JsonDetection
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.LogContentView
import com.jerryjeon.logjerry.logview.RefinedLog
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.ColumnInfo
import com.jerryjeon.logjerry.table.ColumnType
import com.jerryjeon.logjerry.table.Header
import com.jerryjeon.logjerry.util.copyToClipboard
import com.jerryjeon.logjerry.util.isCtrlOrMetaPressed
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

val json = Json { prettyPrint = true }

@Composable
fun LogRow(
    refinedLog: RefinedLog,
    preferences: Preferences,
    header: Header,
    collapseJsonDetection: (JsonDetection) -> Unit,
    expandJsonDetection: (annotation: String) -> Unit,
    divider: @Composable RowScope.() -> Unit
) {
    Row(Modifier) {
        Spacer(Modifier.width(8.dp))
        header.asColumnList.forEach { columnInfo ->
            if (columnInfo.visible) {
                CellByColumnType(preferences, columnInfo, refinedLog, collapseJsonDetection, expandJsonDetection)
                if (columnInfo.columnType.showDivider) {
                    divider()
                }
            }
        }
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
fun RowScope.CellByColumnType(
    preferences: Preferences,
    columnInfo: ColumnInfo,
    refinedLog: RefinedLog,
    collapseJsonDetection: (JsonDetection) -> Unit,
    expandJsonDetection: (annotation: String) -> Unit,
) {
    val log = refinedLog.detectionFinishedLog.log
    when (columnInfo.columnType) {
        ColumnType.Number -> NumberCell(preferences, columnInfo, log)
        ColumnType.Date -> DateCell(preferences, columnInfo, log)
        ColumnType.Time -> TimeCell(preferences, columnInfo, log)
        ColumnType.Pid -> PidCell(preferences, columnInfo, log)
        ColumnType.Tid -> TidCell(preferences, columnInfo, log)
        ColumnType.PackageName -> PackagerNameCell(preferences, columnInfo, log)
        ColumnType.Priority -> PriorityCell(preferences, columnInfo, log)
        ColumnType.Tag -> TagCell(preferences, columnInfo, log)
        ColumnType.Detection -> DetectionCell(columnInfo, refinedLog.detectionFinishedLog)
        ColumnType.Log -> LogCell(preferences, columnInfo, refinedLog, collapseJsonDetection, expandJsonDetection)
    }
}

@Composable
private fun RowScope.NumberCell(preferences: Preferences, number: ColumnInfo, log: Log) {
    Text(
        text = log.number.toString(),
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority().getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(number.width)
    )
}

@Composable
private fun RowScope.DateCell(preferences: Preferences, date: ColumnInfo, log: Log) {
    Text(
        text = log.date,
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority().getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(date.width)
    )
}

@Composable
private fun RowScope.TimeCell(preferences: Preferences, time: ColumnInfo, log: Log) {
    Text(
        text = log.time,
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority().getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(time.width)
    )
}

@Composable
private fun RowScope.PidCell(preferences: Preferences, pid: ColumnInfo, log: Log) {
    Text(
        text = log.pid.toString(),
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority().getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(pid.width)
    )
}

@Composable
private fun RowScope.TidCell(preferences: Preferences, tid: ColumnInfo, log: Log) {
    Text(
        text = log.tid.toString(),
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority().getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(tid.width)
    )
}

@Composable
private fun RowScope.PackagerNameCell(preferences: Preferences, packageName: ColumnInfo, log: Log) {
    Text(
        text = log.packageName ?: "?",
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority().getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(packageName.width)
    )
}

@Composable
private fun RowScope.PriorityCell(preferences: Preferences, priority: ColumnInfo, log: Log) {
    Text(
        text = log.priority.text,
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority().getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(priority.width)
    )
}

@Composable
private fun RowScope.TagCell(preferences: Preferences, tag: ColumnInfo, log: Log) {
    Text(
        text = log.tag,
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority().getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(tag.width)
    )
}

@Composable
private fun RowScope.LogCell(
    preferences: Preferences,
    logHeader: ColumnInfo,
    refinedLog: RefinedLog,
    collapseJsonDetection: (JsonDetection) -> Unit,
    expandJsonDetection: (annotation: String) -> Unit,
) {
    Box(modifier = this.cellDefaultModifier(logHeader.width)) {
        Column {
            refinedLog.logContentViews.forEach { logContent ->
                AnnotatedLogView(preferences, logContent, refinedLog, collapseJsonDetection, expandJsonDetection)
            }
        }
    }
}

@Composable fun AnnotatedLogView(
    preferences: Preferences,
    logContentView: LogContentView,
    refinedLog: RefinedLog,
    collapseJsonDetection: (JsonDetection) -> Unit,
    expandJsonDetection: (annotation: String) -> Unit,
) {
    when (logContentView) {
        is LogContentView.Simple -> {
            ClickableText(
                text = logContentView.str,
                style = MaterialTheme.typography.body2.copy(
                    fontSize = preferences.fontSize,
                    color = preferences.colorByPriority().getValue(refinedLog.detectionFinishedLog.log.priority)
                ),
                onClick = { offset ->
                    logContentView.str.getStringAnnotations(tag = "Json", start = offset, end = offset)
                        .firstOrNull()?.let {
                            println("click :${it.item}")
                            expandJsonDetection(it.item)
                        }
                }
            )
        }
        is LogContentView.Json -> {
            val modifier = logContentView.background?.let { Modifier.background(color = it) } ?: Modifier
            Box(modifier = modifier.padding(4.dp)) {
                Text(
                    text = logContentView.str,
                    style = MaterialTheme.typography.body2.copy(
                        fontSize = preferences.fontSize,
                        color = preferences.colorByPriority().getValue(refinedLog.detectionFinishedLog.log.priority)
                    ),
                    modifier = Modifier.padding(end = 32.dp)
                )
                Row(modifier = Modifier.align(Alignment.TopEnd)) {
                    IconButton(
                        onClick = { copyToClipboard(logContentView.str.toString()) },
                        modifier = Modifier.size(16.dp),
                    ) {
                        Icon(Icons.Default.ContentCopy, "Copy the json")
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(
                        onClick = { collapseJsonDetection(logContentView.jsonDetection) },
                        modifier = Modifier.size(16.dp),
                    ) {
                        Icon(Icons.Default.Expand, "Collapse the json")
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DetectionCell(button: ColumnInfo, detectionFinishedLog: DetectionFinishedLog) {
    val showPrettyJsonDialog: MutableState<JsonObject?> = remember { mutableStateOf(null) }

    Column(modifier = this.cellDefaultModifier(button.width)) {
        detectionFinishedLog.detections.values
            .flatten()
            .filterIsInstance<JsonDetection>()
            .forEachIndexed { index, jsonDetection ->
                TextButton(onClick = { showPrettyJsonDialog.value = jsonDetection.json }) {
                    Row {
                        Text("{ }")
                        Text("${index + 1}", fontSize = 9.sp)
                    }
                }
            }
    }

    JsonPrettyDialog(showPrettyJsonDialog)
}

@Composable
private fun JsonPrettyDialog(
    showPrettyJsonDialogState: MutableState<JsonObject?>,
) {
    showPrettyJsonDialogState.value?.let { jsonObject ->
        val prettyJson = json.encodeToString(JsonObject.serializer(), jsonObject)
        Dialog(
            onCloseRequest = { showPrettyJsonDialogState.value = null },
            title = "Pretty Json",
            state = DialogState(width = 800.dp, height = 600.dp),
            onPreviewKeyEvent = { keyEvent ->
                when {
                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown -> {
                        showPrettyJsonDialogState.value = null
                        true
                    }
                    keyEvent.isCtrlOrMetaPressed && keyEvent.key == Key.C && keyEvent.type == KeyEventType.KeyDown -> {
                        copyToClipboard(prettyJson)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                SelectionContainer(Modifier.fillMaxSize()) {
                    BasicTextField(
                        value = prettyJson,
                        onValueChange = {},
                        modifier = Modifier.fillMaxSize(),
                        readOnly = true,
                    )
                }
                Button(onClick = { copyToClipboard(prettyJson) }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Text("Copy all (Cmd + C)")
                }
            }
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
        .padding(horizontal = 4.dp, vertical = 8.dp)
}
