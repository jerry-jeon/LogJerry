// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import parse.ParseResult
import java.awt.FileDialog
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.io.File

@Composable
@Preview
fun App(headerState: MutableState<Header>, sourceManager: SourceManager) {
    val parseStatus = sourceManager.parseStatusFlow.collectAsState(ParseStatus.NotStarted)
    // Flow would be better

    when (val status = parseStatus.value) {
        ParseStatus.NotStarted -> GettingStartedView()
        is ParseStatus.Proceeding -> Text("Proceeding.... ${status.percent}")
        is ParseStatus.Completed -> {
            Column {
                InvalidSentences(status.parseResult)
                FilterAndLogs(headerState, status.logManager)
            }
        }
    }
}

@Composable
private fun GettingStartedView() {
    Text("File - Load or Paste")
}

@Composable
private fun FilterAndLogs(headerState: MutableState<Header>, logManager: LogManager) {
    val refinedLogs by logManager.refinedLogs.collectAsState()
    val refinedLogsList = refinedLogs.refined
    val logs = logManager.originalLogs
    val fsState = logManager.findStatusFlow.collectAsState()
    Column {
        FindView(logManager, fsState.value)
        FilterView(logManager)
        val filteredSize = (if (refinedLogsList.size != logs.size) "Filtered size : ${refinedLogsList.size}, " else "")
        Text(filteredSize + "Total : ${logs.size}")
        LogsView(headerState.value, refinedLogsList)
    }
}

@Composable
private fun InvalidSentences(parseResult: ParseResult) {
    var showInvalidSentence by remember { mutableStateOf(true) }
    val invalidSentences = parseResult.invalidSentences
    if (showInvalidSentence) {
        Column(Modifier.fillMaxWidth().background(Color.LightGray).padding(8.dp)) {
            Row {
                IconButton(
                    onClick = { showInvalidSentence = false },
                    modifier = Modifier.size(16.dp).align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Close, "Close showing invalid sentences")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "There were ${invalidSentences.size} invalid sentences",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
            Spacer(Modifier.height(8.dp))
            invalidSentences.forEach { (index, s) ->
                Text("L#${index + 1} : $s")
            }
        }
    }
}

@Composable
private fun FindView(logManager: LogManager, findStatus: FindStatus) {
    when (findStatus) {
        is FindStatus.TurnedOn -> {
            Row {
                TextField(
                    value = findStatus.keyword,
                    onValueChange = {
                        logManager.find(it)
                    }
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = {
                    logManager.findEnabled(false)
                }) {
                    Icon(Icons.Default.Close, "Close find")
                }
            }
        }
        FindStatus.TurnedOff -> {}
    }
}

@Composable
private fun FilterView(logManager: LogManager) {
    val filters = logManager.filtersFlow.value
    // TODO take filters as flow
    // Provide only useful column types
    var columnType by remember { mutableStateOf(ColumnType.Log) }
    val items = listOf(ColumnType.Log, ColumnType.PackageName)
    var text by remember { mutableStateOf("Text") }
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.padding(8.dp)) {
        Row(modifier = Modifier.padding(8.dp)) {
            Box(modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(1.dp)).align(Alignment.CenterVertically)) {
                Row(modifier = Modifier.clickable(onClick = { expanded = true }).padding(8.dp)) {
                    Text(
                        columnType.name,
                        modifier = Modifier.width(80.dp).align(Alignment.CenterVertically),
                    )
                    Icon(Icons.Default.ArrowDropDown, "Column types")
                }
                DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                    items.forEach {
                        DropdownMenuItem(onClick = {
                            columnType = it
                            expanded = false
                        }) {
                            Text(text = it.name)
                        }
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            TextField(
                value = text,
                onValueChange = {
                    text = it
                }
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                logManager.addFilter(Filter(columnType, text))
                text = ""
            }) {
                Text("Add filter")
            }
        }

        Row {
            filters.forEach { filter ->

                Box(
                    Modifier
                        .height(50.dp)
                        .background(Color.LightGray, RoundedCornerShape(25.dp))
                ) {
                    Row(modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp)) {
                        if (filter.columnType.icon != null) {
                            Icon(
                                filter.columnType.icon,
                                contentDescription = "Remove a filter",
                                modifier = Modifier.size(ButtonDefaults.IconSize).align(Alignment.CenterVertically)
                            )
                        } else {
                            Text("${filter.columnType}", modifier = Modifier.align(Alignment.CenterVertically), style = TextStyle.Default.copy(fontSize = 12.sp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(filter.text, modifier = Modifier.align(Alignment.CenterVertically))
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .background(Color.Gray, CircleShape)
                                .clickable { logManager.removeFilter(filter) }
                                .padding(4.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove a filter",
                                modifier = Modifier.size(ButtonDefaults.IconSize).align(Alignment.Center)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
        }
    }
}

@Composable
private fun LogsView(header: Header, logs: List<Log>) {
    val divider: @Composable RowScope.() -> Unit = { ColumnDivider() }
    LazyColumn(Modifier.padding(10.dp)) {
        item { HeaderRow(header, divider) }
        item { HeaderDivider() }
        logs.forEach {
            item {
                Column {
                    LogRow(it, header, divider = divider)
                    Divider(color = Color(0xFFE5E7E9))
                }
            }
        }
    }
}

@Composable
fun ColumnDivider() {
    Box(Modifier.padding(horizontal = 5.dp)) {
        Divider(Modifier.fillMaxHeight().width(1.dp))
    }
}

@Composable
fun HeaderDivider() {
    Divider()
}

@Composable
fun MyTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}

fun main() = application {
    val sourceManager = SourceManager()
    val headerState = remember { mutableStateOf(Header.default) }
    Window(
        state = WindowState(width = 1600.dp, height = 800.dp),
        onCloseRequest = ::exitApplication,
        onPreviewKeyEvent = { keyEvent ->
            if (keyEvent.isMetaPressed && keyEvent.key == Key.V && keyEvent.type == KeyEventType.KeyUp) {
                Toolkit.getDefaultToolkit()
                    .systemClipboard
                    .getData(DataFlavor.stringFlavor)
                    .takeIf { it is String }
                    ?.let { sourceManager.changeSource(Source.Text(it.toString())) }
            }
            if (keyEvent.isMetaPressed && keyEvent.key == Key.F && keyEvent.type == KeyEventType.KeyUp) {
                sourceManager.findPressed()
            }
            false
        }
    ) {
        MyTheme {
            MenuBar {
                Menu("File") {
                    Item("Open file", shortcut = KeyShortcut(Key.L, ctrl = true)) {
                        sourceManager.openFileDialog()
                    }
                }
                Menu("Columns") {
                    headerState.value.asColumnList.forEach { columnInfo ->
                        columnCheckboxItem(columnInfo, headerState)
                    }
                }
            }
            App(headerState, sourceManager)
        }
    }
}

private fun SourceManager.openFileDialog() {
    val fileDialog = FileDialog(ComposeWindow())
    fileDialog.isVisible = true
    fileDialog.file?.let {
        val file = File(File(fileDialog.directory), it)
        changeSource(Source.File(file))
    }
}
