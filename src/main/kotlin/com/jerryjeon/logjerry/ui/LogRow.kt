@file:OptIn(ExperimentalComposeUiApi::class)

package com.jerryjeon.logjerry.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import com.jerryjeon.logjerry.detection.JsonDetectionResult
import com.jerryjeon.logjerry.log.Log
import com.jerryjeon.logjerry.log.refine.DetectionFinishedLog
import com.jerryjeon.logjerry.log.refine.LogContent
import com.jerryjeon.logjerry.log.refine.RefinedLog
import com.jerryjeon.logjerry.preferences.Preferences
import com.jerryjeon.logjerry.table.ColumnInfo
import com.jerryjeon.logjerry.table.ColumnType
import com.jerryjeon.logjerry.table.Header
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

val json = Json { prettyPrint = true }

@Composable
fun LogRow(
    refinedLog: RefinedLog,
    preferences: Preferences,
    header: Header,
    divider: @Composable RowScope.() -> Unit
) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Spacer(Modifier.width(8.dp))
        header.asColumnList.forEach { columnInfo ->
            if (columnInfo.visible) {
                CellByColumnType(preferences, columnInfo, refinedLog)
                if (columnInfo.columnType.showDivider) {
                    divider()
                }
            }
        }
        Spacer(Modifier.width(8.dp))
    }
}

@Composable
fun RowScope.CellByColumnType(preferences: Preferences, columnInfo: ColumnInfo, refinedLog: RefinedLog) {
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
        ColumnType.Log -> LogCell(preferences, columnInfo, refinedLog)
    }
}

@Composable
private fun RowScope.NumberCell(preferences: Preferences, number: ColumnInfo, log: Log) {
    Text(
        text = log.number.toString(),
        style = MaterialTheme.typography.body2.copy(
            fontSize = preferences.fontSize,
            color = preferences.colorByPriority.getValue(log.priority)
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
            color = preferences.colorByPriority.getValue(log.priority)
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
            color = preferences.colorByPriority.getValue(log.priority)
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
            color = preferences.colorByPriority.getValue(log.priority)
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
            color = preferences.colorByPriority.getValue(log.priority)
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
            color = preferences.colorByPriority.getValue(log.priority)
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
            color = preferences.colorByPriority.getValue(log.priority)
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
            color = preferences.colorByPriority.getValue(log.priority)
        ),
        modifier = this.cellDefaultModifier(tag.width)
    )
}

@Composable
private fun RowScope.LogCell(preferences: Preferences, logHeader: ColumnInfo, refinedLog: RefinedLog) {
    Box(modifier = this.cellDefaultModifier(logHeader.width)) {
        SelectionContainer {
            refinedLog.logContents.forEach { logContent ->
                Column {
                    AnnotatedLogView(preferences, logContent, refinedLog)
                }
            }
        }
    }
}

@Composable fun AnnotatedLogView(preferences: Preferences, logContent: LogContent, refinedLog: RefinedLog) {
    when (logContent) {
        is LogContent.Simple -> {
            Text(
                text = logContent.str,
                style = MaterialTheme.typography.body2.copy(
                    fontSize = preferences.fontSize,
                    color = preferences.colorByPriority.getValue(refinedLog.detectionFinishedLog.log.priority)
                ),
            )
        }
        is LogContent.Json -> {
            Text(
                text = logContent.str,
                style = MaterialTheme.typography.body2.copy(
                    fontSize = preferences.fontSize,
                    color = preferences.colorByPriority.getValue(refinedLog.detectionFinishedLog.log.priority)
                ),
                modifier = logContent.background?.let { Modifier.background(color = it) } ?: Modifier
            )
        }
    }
}

@Composable
private fun RowScope.DetectionCell(button: ColumnInfo, detectionFinishedLog: DetectionFinishedLog) {
    val showPrettyJsonDialog: MutableState<JsonObject?> = remember { mutableStateOf(null) }

    Column(modifier = this.cellDefaultModifier(button.width)) {
        detectionFinishedLog.detectionResults.values
            .flatten()
            .filterIsInstance<JsonDetectionResult>()
            .forEachIndexed { index, result ->
                TextButton(onClick = { showPrettyJsonDialog.value = result.json }) {
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
                    keyEvent.isMetaPressed && keyEvent.key == Key.W && keyEvent.type == KeyEventType.KeyDown -> {
                        showPrettyJsonDialogState.value = null
                        true
                    }
                    keyEvent.isMetaPressed && keyEvent.key == Key.C && keyEvent.type == KeyEventType.KeyDown -> {
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

private fun copyToClipboard(prettyJson: String) {
    val selection = StringSelection(prettyJson)
    Toolkit.getDefaultToolkit()
        .systemClipboard
        .setContents(selection, selection)
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
